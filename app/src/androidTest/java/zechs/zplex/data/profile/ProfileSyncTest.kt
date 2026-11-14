package zechs.zplex.data.profile

import android.content.Context
import android.content.res.Configuration
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import zechs.zplex.data.local.WatchlistDatabase
import zechs.zplex.data.model.drive.AuthorizationResponse
import zechs.zplex.data.model.drive.DRIVE_APPDATA_SCOPE
import zechs.zplex.data.model.drive.DRIVE_READ_SCOPE
import zechs.zplex.data.model.drive.DriveClient
import zechs.zplex.data.model.entities.Movie
import zechs.zplex.data.model.entities.WatchedMovie
import zechs.zplex.data.remote.DriveApi
import zechs.zplex.data.repository.DriveRepository
import zechs.zplex.utils.SessionManager
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ProfileSyncTest {
    @Test
    fun phoneSeedsThenBothDevicesShareChangesWithoutOverwritingLibrary() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val sessions = SessionManager(context, Gson())
        val scopes = listOf(DRIVE_READ_SCOPE, DRIVE_APPDATA_SCOPE)
        sessions.saveDriveSession(
            DriveClient("fixture-client", "fixture-secret", "http://127.0.0.1:53682/", scopes),
            AuthorizationResponse("fixture-access", 3600, "fixture-refresh", "Bearer", scopes.joinToString(" "))
        )
        val drive = DriveRepository(
            Retrofit.Builder().baseUrl("https://example.test/")
                .addConverterFactory(MoshiConverterFactory.create(moshi)).build().create(DriveApi::class.java),
            { error("The test's access token is still valid") },
            sessions,
            context
        )
        val remote = FakeDrive(moshi)
        val phone = Room.inMemoryDatabaseBuilder(context, WatchlistDatabase::class.java).build()
        val tv = Room.inMemoryDatabaseBuilder(context, WatchlistDatabase::class.java).build()
        try {
            val phoneSync = repository(phone, remote, sessions, drive, moshi, context, false)
            val tvSync = repository(tv, remote, sessions, drive, moshi, context, true)
            val phoneMovies = phone.getMovieDao()
            val tvMovies = tv.getMovieDao()
            val phoneHistory = phone.getWatchedMovieDao()
            val tvHistory = tv.getWatchedMovieDao()
            phoneMovies.upsertMovie(movie(11))
            phoneHistory.upsertWatchedMovie(watched(23, 1_380_000, "shared-source"))
            tvMovies.upsertMovie(movie(22))
            tvMovies.upsertMovie(movie(33, "library-source"))
            tvHistory.upsertWatchedMovie(watched(44, 800_000, "tv-source"))

            assertEquals(ProfileSyncStatus.WAITING_FOR_PHONE, tvSync.sync())
            assertEquals(0, remote.documents.size)
            assertNotNull(tvMovies.getMovieById(22))

            assertEquals(ProfileSyncStatus.SYNCED, phoneSync.sync())
            assertEquals(ProfileSyncStatus.SYNCED, tvSync.sync())
            assertEquals(movie(11), tvMovies.getMovieById(11))
            assertNull(tvMovies.getMovieById(22))
            assertEquals("library-source", tvMovies.getMovieById(33)?.fileId)
            assertEquals(1_380_000L, tvHistory.getWatchedMovie(23)?.watchedDuration)
            assertNull(tvHistory.getWatchedMovie(44))

            tvMovies.upsertMovie(movie(55))
            tvHistory.upsertWatchedMovie(watched(23, 1_980_000, "shared-source"))
            phoneMovies.deleteWatchlistMovie(11)
            remote.failNextWrite = true
            assertEquals(ProfileSyncStatus.RETRY, tvSync.sync())
            assertEquals(ProfileSyncStatus.SYNCED, phoneSync.sync())
            val restartedTv = repository(tv, remote, sessions, drive, moshi, context, true)
            assertEquals(ProfileSyncStatus.SYNCED, restartedTv.sync())
            assertEquals(ProfileSyncStatus.SYNCED, phoneSync.sync())
            assertEquals(movie(55), phoneMovies.getMovieById(55))
            assertNull(tvMovies.getMovieById(11))
            assertEquals(1_980_000L, phoneHistory.getWatchedMovie(23)?.watchedDuration)
            assertEquals(2, remote.documents.size)
        } finally {
            phone.close()
            tv.close()
            sessions.resetDataStore()
        }
    }

    private fun repository(
        database: WatchlistDatabase,
        remote: ProfileDriveApi,
        sessions: SessionManager,
        drive: DriveRepository,
        moshi: Moshi,
        context: Context,
        television: Boolean
    ): ProfileSyncRepository {
        val mode = if (television) Configuration.UI_MODE_TYPE_TELEVISION else Configuration.UI_MODE_TYPE_NORMAL
        val configuration = Configuration(context.resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_TYPE_MASK.inv()) or mode
        }
        return ProfileSyncRepository(
            remote, ProfileLocalStore(database, moshi), sessions, drive, moshi,
            context.createConfigurationContext(configuration)
        )
    }

    private fun movie(id: Int, fileId: String? = null) = Movie(id, "Film $id", "movie", null, null, fileId)

    private fun watched(id: Int, duration: Long, fileId: String) =
        WatchedMovie(id, "Film $id", "movie", null, duration, 6_000_000, duration, fileId = fileId)

    private class FakeDrive(moshi: Moshi) : ProfileDriveApi {
        private val adapter = moshi.adapter(ProfileDocument::class.java)
        private val files = mutableMapOf<String, ProfileDriveFile>()
        val documents = mutableMapOf<String, ProfileDocument>()
        var failNextWrite = false
        private var nextId = 0

        override suspend fun account(token: String) = ProfileDriveAbout(ProfileDriveUser("fixture-account"))

        override suspend fun files(token: String, query: String, pageToken: String?) =
            ProfileDriveFiles(files.values.toList())

        override suspend fun generateId(token: String) = ProfileDriveIds(listOf("fixture-file-${++nextId}"))

        override suspend fun read(token: String, id: String): ProfileDocument = documents.getValue(id)

        override suspend fun create(token: String, body: RequestBody): ProfileDriveFile {
            val parts = (body as MultipartBody).parts
            val metadata = JSONObject(parts.first().body.text())
            assertEquals("appDataFolder", metadata.getJSONArray("parents").getString(0))
            return write(metadata.getString("id"), metadata.getString("name"), parts.last().body)
        }

        override suspend fun update(token: String, id: String, body: RequestBody): ProfileDriveFile =
            write(id, files.getValue(id).name, body)

        private fun write(id: String, name: String, body: RequestBody): ProfileDriveFile {
            if (failNextWrite) {
                failNextWrite = false
                throw IOException("Simulated interrupted upload")
            }
            documents[id] = checkNotNull(adapter.fromJson(body.text()))
            val version = (files[id]?.version?.toInt() ?: 0) + 1
            return ProfileDriveFile(id, name, version.toString()).also { files[id] = it }
        }

        private fun RequestBody.text(): String = Buffer().also(::writeTo).readUtf8()
    }
}
