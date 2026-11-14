package zechs.zplex.ui.signin

import android.app.Application
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.Lazy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import zechs.zplex.BuildConfig
import zechs.zplex.R
import zechs.zplex.data.model.drive.DRIVE_APPDATA_SCOPE
import zechs.zplex.data.model.drive.DRIVE_READ_SCOPE
import zechs.zplex.data.model.drive.DriveClient
import zechs.zplex.data.model.drive.hasScope
import zechs.zplex.data.profile.ProfileSyncRepository
import zechs.zplex.data.repository.DriveRepository
import zechs.zplex.service.ProfileSyncScheduler
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.utils.SessionManager
import zechs.zplex.utils.state.Resource
import java.io.IOException
import retrofit2.HttpException
import javax.inject.Inject

internal sealed interface DriveSignInState {
    data object Ready : DriveSignInState
    data object Preparing : DriveSignInState
    class OpenBrowser(val url: Uri) : DriveSignInState
    data object WaitingForGoogle : DriveSignInState
    data object ExchangingCode : DriveSignInState
    data object Connected : DriveSignInState
    class Error(val message: String) : DriveSignInState
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    app: Application,
    savedState: SavedStateHandle,
    private val driveRepository: Lazy<DriveRepository>,
    private val sessionManager: SessionManager,
    private val profiles: ProfileSyncRepository,
    private val profileScheduler: ProfileSyncScheduler
) : BaseAndroidViewModel(app) {

    private val _state = MutableStateFlow<DriveSignInState>(DriveSignInState.Ready)
    internal val state = _state.asStateFlow()
    val isLoggedIn = sessionManager.isLoggedIn()
    val hasBuiltInClient = BuildConfig.DRIVE_CLIENT_ID.isNotEmpty()
    val authorizeProfile = savedState.get<Boolean>("authorizeProfile") == true

    private var connection: Job? = null

    fun connect(enteredId: String, enteredSecret: String) {
        if (connection?.isCompleted == false) return
        _state.value = DriveSignInState.Preparing
        connection = viewModelScope.launch {
            try {
                if (!authorizeProfile && isLoggedIn.first()) {
                    _state.value = DriveSignInState.Connected
                    return@launch
                }
                val storedClient = if (authorizeProfile) sessionManager.fetchClient() else null
                val clientId = storedClient?.clientId
                    ?: if (hasBuiltInClient) BuildConfig.DRIVE_CLIENT_ID else enteredId.trim()
                val clientSecret = storedClient?.clientSecret
                    ?: if (hasBuiltInClient) BuildConfig.DRIVE_CLIENT_SECRET else enteredSecret.trim()
                if (clientId.isEmpty() || clientSecret.isEmpty()) {
                    _state.value = error(R.string.drive_sign_in_missing_client)
                    return@launch
                }
                val client = DriveClient(
                    clientId, clientSecret, DriveLoopbackCallback.REDIRECT_URI,
                    listOf(DRIVE_READ_SCOPE, DRIVE_APPDATA_SCOPE)
                )
                val previousAccount = if (storedClient != null) {
                    val token = driveRepository.get().fetchAccessToken(storedClient).data
                    if (token == null) {
                        _state.value = error(R.string.drive_sign_in_exchange_failed)
                        return@launch
                    }
                    profiles.accountId(token.accessToken)
                } else null
                val authorization = DriveAuthorization.create()
                val reply = withContext(Dispatchers.IO) {
                    DriveLoopbackCallback.open().use { callback ->
                        withContext(Dispatchers.Main) {
                            _state.value = DriveSignInState.OpenBrowser(
                                client.authUrl(authorization.state, authorization.codeChallenge)
                            )
                        }
                        callback.await(authorization.state)
                    }
                }

                _state.value = when (reply) {
                    is DriveLoopbackCallback.Reply.Code -> {
                        _state.value = DriveSignInState.ExchangingCode
                        val response = driveRepository.get().fetchRefreshToken(
                            client, reply.value, authorization.codeVerifier
                        ) { token ->
                            when {
                                authorizeProfile && !token.toTokenResponse().hasScope(DRIVE_APPDATA_SCOPE) ->
                                    R.string.profile_sync_permission
                                previousAccount != null && profiles.accountId(token.accessToken) != previousAccount ->
                                    R.string.profile_sync_same_account
                                else -> null
                            }
                        }
                        if (response is Resource.Success) {
                            profileScheduler.request()
                            DriveSignInState.Connected
                        } else {
                            DriveSignInState.Error(response.message
                                ?: context.getString(R.string.drive_sign_in_exchange_failed))
                        }
                    }
                    DriveLoopbackCallback.Reply.Denied ->
                        error(R.string.drive_sign_in_denied)
                    DriveLoopbackCallback.Reply.Failed ->
                        error(R.string.drive_sign_in_exchange_failed)
                    DriveLoopbackCallback.Reply.TimedOut ->
                        error(R.string.drive_sign_in_timed_out)
                }
            } catch (_: IOException) {
                _state.value = error(R.string.drive_sign_in_local_error)
            } catch (_: HttpException) {
                _state.value = error(R.string.drive_sign_in_exchange_failed)
            } finally {
                when (_state.value) {
                    is DriveSignInState.Error, DriveSignInState.Connected -> Unit
                    else -> _state.value = DriveSignInState.Ready
                }
            }
        }
    }

    internal fun openBrowser(launch: (Uri) -> Unit) {
        val pending = _state.value as? DriveSignInState.OpenBrowser ?: return
        _state.value = DriveSignInState.WaitingForGoogle
        launch(pending.url)
    }

    fun browserUnavailable() {
        _state.value = error(R.string.drive_sign_in_no_browser)
        connection?.cancel()
    }

    fun cancel() {
        connection?.cancel()
    }

    private fun error(@StringRes message: Int) = DriveSignInState.Error(context.getString(message))
}
