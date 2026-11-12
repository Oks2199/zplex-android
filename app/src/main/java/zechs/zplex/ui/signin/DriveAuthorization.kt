package zechs.zplex.ui.signin

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

internal class DriveAuthorization private constructor(
    val state: String,
    val codeVerifier: String
) {
    val codeChallenge: String = base64Url(
        MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray(Charsets.US_ASCII))
    )

    companion object {
        private val random = SecureRandom()

        fun create() = DriveAuthorization(state = randomValue(), codeVerifier = randomValue())

        private fun randomValue(): String =
            base64Url(ByteArray(32).also(random::nextBytes))

        private fun base64Url(value: ByteArray): String =
            Base64.getUrlEncoder().withoutPadding().encodeToString(value)
    }
}
