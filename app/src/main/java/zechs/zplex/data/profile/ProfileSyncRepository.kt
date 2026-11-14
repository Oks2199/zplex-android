package zechs.zplex.data.profile

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import zechs.zplex.data.model.drive.DRIVE_APPDATA_SCOPE
import zechs.zplex.data.model.drive.hasScope
import zechs.zplex.data.repository.DriveRepository
import zechs.zplex.utils.SessionManager
import zechs.zplex.utils.ext.isTelevision
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class ProfileSyncStatus {
    IDLE, SIGNED_OUT, NEEDS_PERMISSION, SYNCING, WAITING_FOR_PHONE, SYNCED, PROFILE_UNAVAILABLE, RETRY
}

@Singleton
class ProfileSyncRepository @Inject internal constructor(
    private val api: ProfileDriveApi,
    private val local: ProfileLocalStore,
    private val sessions: SessionManager,
    private val drive: DriveRepository,
    moshi: Moshi,
    @ApplicationContext private val context: Context
) {
    private val mutex = Mutex()
    private val documentAdapter = moshi.adapter(ProfileDocument::class.java)
    private val json = "application/json; charset=UTF-8".toMediaType()
    private val cache = mutableMapOf<String, Pair<String, ProfileDocument>>()
    private val _status = MutableStateFlow(ProfileSyncStatus.IDLE)
    val status = _status.asStateFlow()

    internal suspend fun accountId(accessToken: String): String =
        api.account("Bearer $accessToken").user.permissionId

    suspend fun sync(): ProfileSyncStatus = mutex.withLock {
        val result = try {
            synchronize()
        } catch (cancelled: CancellationException) {
            _status.value = ProfileSyncStatus.IDLE
            throw cancelled
        } catch (error: HttpException) {
            when (error.code()) {
                401 -> ProfileSyncStatus.NEEDS_PERMISSION
                403 -> if (error.response()?.errorBody()?.use { it.string() }
                        ?.contains("insufficientPermissions") == true
                ) ProfileSyncStatus.NEEDS_PERMISSION else ProfileSyncStatus.RETRY
                else -> ProfileSyncStatus.RETRY
            }
        } catch (_: Exception) {
            ProfileSyncStatus.RETRY
        }
        _status.value = result
        result
    }

    private suspend fun synchronize(): ProfileSyncStatus {
        val client = sessions.fetchClient() ?: return ProfileSyncStatus.SIGNED_OUT
        val savedToken = sessions.fetchAccessToken() ?: return ProfileSyncStatus.SIGNED_OUT
        if (!savedToken.hasScope(DRIVE_APPDATA_SCOPE)) return ProfileSyncStatus.NEEDS_PERMISSION
        val session = sessions.fetchRefreshToken() ?: return ProfileSyncStatus.NEEDS_PERMISSION
        val access = drive.fetchAccessToken(client).data ?: return ProfileSyncStatus.RETRY
        if (!access.hasScope(DRIVE_APPDATA_SCOPE)) return ProfileSyncStatus.NEEDS_PERMISSION
        _status.value = ProfileSyncStatus.SYNCING
        val account = accountId(access.accessToken)
        val token = "Bearer ${access.accessToken}"

        val saved = local.readState()
        if (saved != null && saved.accountId != account) return ProfileSyncStatus.PROFILE_UNAVAILABLE

        val files = listFiles(token)
        val root = files.singleOrNull { it.name == ROOT_NAME }
        if (root == null) {
            if (files.isNotEmpty() || saved?.published == true) return ProfileSyncStatus.PROFILE_UNAVAILABLE
            if (saved == null && context.isTelevision) return ProfileSyncStatus.WAITING_FOR_PHONE
            val prepared = local.prepareSeed(
                saved ?: ProfileState(account, UUID.randomUUID().toString(), generateId(token))
            )
            checkSession(session)
            val id = prepared.profileId
            upload(token, ROOT_NAME, id, prepared.document(), existing = false)
            local.save(prepared.copy(fileId = id, published = true, applied = prepared.own))
            return ProfileSyncStatus.SYNCED
        }
        if (saved != null && saved.profileId != root.id) return ProfileSyncStatus.PROFILE_UNAVAILABLE

        val remote = files.associateWith { read(token, it, root.id) }
        val state = saved ?: ProfileState(account, UUID.randomUUID().toString(), root.id)
        checkSession(session)
        val merged = local.reconcile(state, remote.values.flatMap { it.changes }, firstImport = saved == null)
        val ownFile = remote.entries.singleOrNull { it.value.deviceId == merged.deviceId }
        if (ownFile?.value?.changes == merged.own || (ownFile == null && merged.own.isEmpty())) {
            local.save(merged.copy(fileId = ownFile?.key?.id ?: merged.fileId, published = true))
            return ProfileSyncStatus.SYNCED
        }

        val id = ownFile?.key?.id ?: merged.fileId ?: generateId(token)
        val name = ownFile?.key?.name ?: "$FILE_PREFIX-${merged.deviceId}.json"
        val prepared = merged.copy(fileId = id, published = true)
        local.save(prepared)
        checkSession(session)
        upload(token, name, id, merged.document(), existing = ownFile != null)
        return ProfileSyncStatus.SYNCED
    }

    private suspend fun checkSession(refreshToken: String) {
        if (sessions.fetchRefreshToken() != refreshToken) throw IOException("Drive session changed")
    }

    private suspend fun listFiles(token: String): List<ProfileDriveFile> = buildList {
        var page: String? = null
        do {
            val response = api.files(token, "name contains '$FILE_PREFIX'", page)
            addAll(response.files.filter { it.name == ROOT_NAME || it.name.startsWith("$FILE_PREFIX-") })
            page = response.nextPageToken
        } while (page != null)
    }

    private suspend fun read(token: String, file: ProfileDriveFile, profileId: String): ProfileDocument {
        cache[file.id]?.takeIf { it.first == file.version }?.let { return it.second }
        val doc = api.read(token, file.id)
        val validName = if (file.name == ROOT_NAME) file.id == profileId
        else file.name == "$FILE_PREFIX-${doc.deviceId}.json"
        if (!validName || doc.version != 1 || doc.profileId != profileId ||
            doc.changes.any { it.deviceId != doc.deviceId || it.revision < 1 ||
                it.key.tmdbId <= 0 || (it.value != null && it.key != it.value.key) }
        ) throw IOException("Invalid Drive profile document")
        cache[file.id] = file.version to doc
        return doc
    }

    private suspend fun generateId(token: String): String =
        api.generateId(token).ids.singleOrNull() ?: throw IOException("Missing Drive profile file ID")

    private fun ProfileState.document() = ProfileDocument(profileId, deviceId, own)

    private suspend fun upload(
        token: String,
        name: String,
        id: String,
        document: ProfileDocument,
        existing: Boolean
    ) {
        val media = documentAdapter.toJson(document).toRequestBody(json)
        val uploaded = if (existing) {
            api.update(token, id, media)
        } else {
            val metadata = JSONObject()
                .put("id", id)
                .put("name", name)
                .put("mimeType", "application/json")
                .put("parents", JSONArray().put("appDataFolder"))
            val multipart = MultipartBody.Builder().setType("multipart/related".toMediaType())
                .addPart(metadata.toString().toRequestBody(json))
                .addPart(media)
                .build()
            try {
                api.create(token, multipart)
            } catch (error: HttpException) {
                if (error.code() != 409) throw error
                val prior = api.read(token, id)
                if (prior.profileId != document.profileId || prior.deviceId != document.deviceId) throw error
                api.update(token, id, media)
            }
        }
        cache[id] = uploaded.version to document
    }

    private companion object {
        const val FILE_PREFIX = "movynex-profile-v1"
        const val ROOT_NAME = "$FILE_PREFIX.json"
    }
}
