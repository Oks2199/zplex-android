package zechs.zplex.utils

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import zechs.zplex.data.model.drive.AuthorizationResponse
import zechs.zplex.data.model.drive.DRIVE_APPDATA_SCOPE
import zechs.zplex.data.model.drive.DriveClient
import zechs.zplex.data.model.drive.DriveProfileAccess
import zechs.zplex.data.model.drive.TokenResponse
import zechs.zplex.data.model.drive.hasScope
import javax.inject.Inject


class SessionManager @Inject constructor(
    context: Context,
    private val gson: Gson
) {

    private val sessionStore = context.dataStore

    suspend fun saveDriveSession(client: DriveClient, authorization: AuthorizationResponse) {
        sessionStore.edit { settings ->
            settings[stringPreferencesKey(DRIVE_CLIENT)] = gson.toJson(client)
            settings[stringPreferencesKey(REFRESH_TOKEN)] = authorization.refreshToken
            settings[stringPreferencesKey(ACCESS_TOKEN)] =
                gson.toJson(authorization.toTokenResponse().withExpiryTimestamp())
        }
    }

    suspend fun fetchClient(): DriveClient? {
        val dataStoreKey = stringPreferencesKey(DRIVE_CLIENT)
        val preferences = sessionStore.data.first()
        val value = preferences[dataStoreKey]
        val client: DriveClient? = value?.let {
            val type = object : TypeToken<DriveClient?>() {}.type
            gson.fromJson(value, type)
        }
        return client
    }

    suspend fun saveAccessToken(data: TokenResponse, refreshToken: String): TokenResponse? {
        val dataStoreKey = stringPreferencesKey(ACCESS_TOKEN)
        var saved: TokenResponse? = null
        sessionStore.edit { settings ->
            if (settings[stringPreferencesKey(REFRESH_TOKEN)] != refreshToken) return@edit
            val previous = settings[dataStoreKey]?.let { gson.fromJson(it, TokenResponse::class.java) }
            saved = data.copy(scope = data.scope ?: previous?.scope).withExpiryTimestamp()
            settings[dataStoreKey] = gson.toJson(saved)
        }
        return saved
    }

    private fun TokenResponse.withExpiryTimestamp(): TokenResponse =
        copy(expiresIn = System.currentTimeMillis() / 1000 + expiresIn)

    suspend fun fetchAccessToken(): TokenResponse? {
        val dataStoreKey = stringPreferencesKey(ACCESS_TOKEN)
        val preferences = sessionStore.data.first()
        val value = preferences[dataStoreKey]
        val login: TokenResponse? = value?.let {
            val type = object : TypeToken<TokenResponse?>() {}.type
            gson.fromJson(value, type)
        }
        return login
    }

    suspend fun fetchRefreshToken(): String? {
        val dataStoreKey = stringPreferencesKey(REFRESH_TOKEN)
        val preferences = sessionStore.data.first()
        val value = preferences[dataStoreKey]
        return value
    }

    fun isLoggedIn(): Flow<Boolean> {
        return sessionStore.data.map { preferences ->
            val value = preferences[stringPreferencesKey(ACCESS_TOKEN)]
            return@map value != null
        }
    }

    val driveProfileAccess: Flow<DriveProfileAccess> = sessionStore.data.map { settings ->
        val token = settings[stringPreferencesKey(ACCESS_TOKEN)]
            ?.let { gson.fromJson(it, TokenResponse::class.java) }
        when {
            token == null -> DriveProfileAccess.SIGNED_OUT
            token.hasScope(DRIVE_APPDATA_SCOPE) -> DriveProfileAccess.GRANTED
            else -> DriveProfileAccess.NEEDS_PERMISSION
        }
    }.distinctUntilChanged()

    suspend fun saveMovieFolder(movieFolderId: String) {
        val dataStoreKey = stringPreferencesKey(MOVIE_FOLDER)
        sessionStore.edit { settings ->
            settings[dataStoreKey] = movieFolderId
        }
        Log.d(TAG, "saveMovieFolder: $movieFolderId")
    }

    suspend fun fetchMovieFolder(): String? {
        val dataStoreKey = stringPreferencesKey(MOVIE_FOLDER)
        val preferences = sessionStore.data.first()
        val value = preferences[dataStoreKey]
        Log.d(TAG, "fetchMovieFolder: $value")
        return value
    }

    suspend fun saveShowsFolder(showsFolderId: String) {
        val dataStoreKey = stringPreferencesKey(SHOWS_FOLDER)
        sessionStore.edit { settings ->
            settings[dataStoreKey] = showsFolderId
        }
        Log.d(TAG, "saveMovieFolder: $showsFolderId")
    }

    suspend fun fetchShowsFolder(): String? {
        val dataStoreKey = stringPreferencesKey(SHOWS_FOLDER)
        val preferences = sessionStore.data.first()
        val value = preferences[dataStoreKey]
        Log.d(TAG, "fetchShowsFolder: $value")
        return value
    }

    fun fetchMovieFolderFlow(): Flow<String?> {
        return sessionStore.data.map { preferences ->
            preferences[stringPreferencesKey(MOVIE_FOLDER)]
        }
    }

    fun fetchShowsFolderFlow(): Flow<String?> {
        return sessionStore.data.map { preferences ->
            preferences[stringPreferencesKey(SHOWS_FOLDER)]
        }
    }

    suspend fun resetDataStore() {
        sessionStore.edit { it.clear() }
    }

    companion object {
        private val Context.dataStore by preferencesDataStore(
            "DRIVE_SESSION"
        )
        const val TAG = "SessionManager"
        const val DRIVE_CLIENT = "DRIVE_CLIENT"
        const val ACCESS_TOKEN = "ACCESS_TOKEN"
        const val REFRESH_TOKEN = "REFRESH_TOKEN"
        const val MOVIE_FOLDER = "MOVIE_FOLDER"
        const val SHOWS_FOLDER = "SHOWS_FOLDER"
    }

}
