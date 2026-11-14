package zechs.zplex.data.model.drive

import android.net.Uri
import androidx.annotation.Keep
import androidx.core.net.toUri
import zechs.zplex.utils.Constants.GOOGLE_ACCOUNTS_URL
import java.io.Serializable

@Keep
data class DriveClient(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val scopes: List<String>
) : Serializable {

    fun authUrl(state: String, codeChallenge: String): Uri =
        "${GOOGLE_ACCOUNTS_URL}/o/oauth2/v2/auth".toUri().buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("access_type", "offline")
            .appendQueryParameter("prompt", "consent")
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("scope", scopes.joinToString(" "))
            .appendQueryParameter("state", state)
            .appendQueryParameter("code_challenge", codeChallenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .build()
}
