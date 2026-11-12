package zechs.zplex.ui.signin

import android.app.Application
import android.net.Uri
import androidx.annotation.StringRes
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
import zechs.zplex.data.model.drive.DriveClient
import zechs.zplex.data.repository.DriveRepository
import zechs.zplex.ui.BaseAndroidViewModel
import zechs.zplex.utils.SessionManager
import zechs.zplex.utils.state.Resource
import java.io.IOException
import javax.inject.Inject

internal sealed interface DriveSignInState {
    data object Ready : DriveSignInState
    data object Preparing : DriveSignInState
    class OpenBrowser(val url: Uri) : DriveSignInState
    data object WaitingForGoogle : DriveSignInState
    data object ExchangingCode : DriveSignInState
    data object Connected : DriveSignInState
    class Error(@StringRes val message: Int) : DriveSignInState
}

@HiltViewModel
class SignInViewModel @Inject constructor(
    app: Application,
    private val driveRepository: Lazy<DriveRepository>,
    private val sessionManager: SessionManager
) : BaseAndroidViewModel(app) {

    private val _state = MutableStateFlow<DriveSignInState>(DriveSignInState.Ready)
    internal val state = _state.asStateFlow()
    val isLoggedIn = sessionManager.isLoggedIn()
    val hasBuiltInClient = BuildConfig.DRIVE_CLIENT_ID.isNotEmpty()

    private var connection: Job? = null

    fun connect(enteredId: String, enteredSecret: String) {
        if (connection?.isCompleted == false) return
        val clientId = if (hasBuiltInClient) BuildConfig.DRIVE_CLIENT_ID else enteredId.trim()
        val clientSecret = if (hasBuiltInClient) BuildConfig.DRIVE_CLIENT_SECRET else enteredSecret.trim()
        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            _state.value = DriveSignInState.Error(R.string.drive_sign_in_missing_client)
            return
        }

        val client = DriveClient(
            clientId = clientId,
            clientSecret = clientSecret,
            redirectUri = DriveLoopbackCallback.REDIRECT_URI,
            scopes = listOf(context.getString(R.string.drive_scope))
        )
        _state.value = DriveSignInState.Preparing
        connection = viewModelScope.launch {
            try {
                if (isLoggedIn.first()) {
                    _state.value = DriveSignInState.Connected
                    return@launch
                }
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
                        when (driveRepository.get().fetchRefreshToken(client, reply.value, authorization.codeVerifier)) {
                            is Resource.Success -> DriveSignInState.Connected
                            else -> DriveSignInState.Error(R.string.drive_sign_in_exchange_failed)
                        }
                    }
                    DriveLoopbackCallback.Reply.Denied ->
                        DriveSignInState.Error(R.string.drive_sign_in_denied)
                    DriveLoopbackCallback.Reply.Failed ->
                        DriveSignInState.Error(R.string.drive_sign_in_exchange_failed)
                    DriveLoopbackCallback.Reply.TimedOut ->
                        DriveSignInState.Error(R.string.drive_sign_in_timed_out)
                }
            } catch (_: IOException) {
                _state.value = DriveSignInState.Error(R.string.drive_sign_in_local_error)
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
        _state.value = DriveSignInState.Error(R.string.drive_sign_in_no_browser)
        connection?.cancel()
    }

    fun cancel() {
        connection?.cancel()
    }
}
