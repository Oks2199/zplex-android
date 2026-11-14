package zechs.zplex.ui.signin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.Socket
import java.security.MessageDigest
import java.util.Base64

class DriveLoopbackCallbackTest {

    @Test
    fun `rejects other browser requests before accepting the matching callback`() = runBlocking {
        DriveLoopbackCallback.open(0).use { callback ->
            val result = async(Dispatchers.IO) { callback.await("expected-state", 4_000) }

            assertTrue(request(callback.port, "/favicon.ico").startsWith("HTTP/1.1 400"))
            assertTrue(request(callback.port, "/?state=wrong&code=ignored").startsWith("HTTP/1.1 400"))
            assertTrue(request(callback.port, "/?state=expected-state&code=a&code=b")
                .startsWith("HTTP/1.1 400"))

            val response = request(callback.port, "/?state=expected-state&code=demo%2Fcode")
            assertTrue(response.startsWith("HTTP/1.1 200"))
            assertTrue(response.contains("Cache-Control: no-store"))
            assertEquals("demo/code", (result.await() as DriveLoopbackCallback.Reply.Code).value)
        }
    }

    @Test
    fun `accepts denial only for the active attempt`() = runBlocking {
        DriveLoopbackCallback.open(0).use { callback ->
            val result = async(Dispatchers.IO) { callback.await("expected-state", 4_000) }
            assertTrue(request(callback.port, "/?state=wrong&error=access_denied").startsWith("HTTP/1.1 400"))
            assertTrue(request(callback.port, "/?state=expected-state&error=access_denied").startsWith("HTTP/1.1 200"))
            assertEquals(DriveLoopbackCallback.Reply.Denied, result.await())
        }
    }

    @Test
    fun `uses independent state and an S256 challenge derived from the verifier`() {
        val attempt = DriveAuthorization.create()
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(attempt.codeVerifier.toByteArray(Charsets.US_ASCII))

        assertEquals(43, attempt.codeVerifier.length)
        assertNotEquals(attempt.state, attempt.codeVerifier)
        assertEquals(Base64.getUrlEncoder().withoutPadding().encodeToString(digest), attempt.codeChallenge)
    }

    private fun request(port: Int, target: String): String =
        Socket(DriveLoopbackCallback.HOST, port).use { socket ->
            socket.soTimeout = 2_000
            val request = "GET $target HTTP/1.1\r\nHost: ${DriveLoopbackCallback.HOST}:$port\r\n\r\n"
            socket.getOutputStream().write(request.toByteArray(Charsets.US_ASCII))
            socket.getInputStream().bufferedReader().readText()
        }
}
