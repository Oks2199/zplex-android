package zechs.zplex.ui.signin

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

internal class DriveLoopbackCallback private constructor(
    private val server: ServerSocket
) : Closeable {
    val port: Int get() = server.localPort

    sealed interface Reply {
        class Code(val value: String) : Reply
        data object Denied : Reply
        data object Failed : Reply
        data object TimedOut : Reply
    }

    suspend fun await(state: String, timeoutMillis: Long = TimeUnit.MINUTES.toMillis(5)): Reply {
        val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis)
        while (System.nanoTime() < deadline) {
            currentCoroutineContext().ensureActive()
            val socket = try {
                server.accept()
            } catch (_: SocketTimeoutException) {
                continue
            }

            socket.use {
                val reply = try {
                    readRequest(socket, state, deadline)
                } catch (_: IOException) {
                    null
                }
                try {
                    respond(socket, reply != null)
                } catch (_: IOException) {
                    // A browser can close its tab before reading the confirmation page.
                }
                if (reply != null) return reply
            }
        }
        return Reply.TimedOut
    }

    private fun readRequest(socket: Socket, state: String, deadline: Long): Reply? {
        socket.soTimeout = 750
        val requestDeadline = minOf(deadline, System.nanoTime() + TimeUnit.SECONDS.toNanos(2))
        val input = socket.getInputStream().buffered()
        val request = input.readHttpLine(requestDeadline)?.split(' ', limit = 3) ?: return null
        if (request.size != 3 || request[0] != "GET" || request[2] !in httpVersions) return null

        var host: String? = null
        repeat(32) {
            val header = input.readHttpLine(requestDeadline) ?: return null
            if (header.isEmpty()) {
                if (host != "$HOST:$port") return null
                return parseReply(request[1], state)
            }
            if (header.substringBefore(':').equals("Host", ignoreCase = true)) {
                if (host != null) return null
                host = header.substringAfter(':').trim()
            }
        }
        return null
    }

    private fun InputStream.readHttpLine(deadline: Long): String? {
        val line = StringBuilder()
        repeat(4096) {
            if (System.nanoTime() >= deadline) return null
            when (val byte = read()) {
                -1 -> return null
                10 -> return line.toString().removeSuffix("\r")
                else -> line.append(byte.toChar())
            }
        }
        return null
    }

    private fun parseReply(target: String, state: String): Reply? {
        val uri = try {
            URI(target)
        } catch (_: URISyntaxException) {
            return null
        }
        if (uri.isAbsolute || uri.rawAuthority != null || uri.rawPath != "/" || uri.fragment != null) {
            return null
        }
        val query = uri.rawQuery ?: return null
        val params = mutableMapOf<String, String>()
        for (param in query.split('&')) {
            val name = URLDecoder.decode(param.substringBefore('='), "UTF-8")
            if (name !in responseParameters) continue
            val value = URLDecoder.decode(param.substringAfter('=', ""), "UTF-8")
            if (params.put(name, value) != null) return null
        }
        if (params["state"] != state) return null
        val code = params["code"]?.takeIf(String::isNotBlank)
        val error = params["error"]?.takeIf(String::isNotBlank)
        return when {
            code != null && error == null -> Reply.Code(code)
            code == null && error == "access_denied" -> Reply.Denied
            code == null && error != null -> Reply.Failed
            else -> null
        }
    }

    private fun respond(socket: Socket, accepted: Boolean) {
        val status = if (accepted) "200 OK" else "400 Bad Request"
        val message = if (accepted) {
            "Tu peux revenir dans Movynex pour terminer la connexion."
        } else {
            "Relance la connexion depuis Movynex."
        }
        val body = """
            <!doctype html>
            <html lang="fr">
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Movynex</title>
            <body style="margin:2em;font:1.25rem system-ui,sans-serif">
            <h1>Movynex</h1><p>$message</p>
            </body></html>
        """.trimIndent().toByteArray(Charsets.UTF_8)
        val headers = "HTTP/1.1 $status\r\n" +
            "Content-Type: text/html; charset=utf-8\r\n" +
            "Content-Length: ${body.size}\r\n" +
            "Cache-Control: no-store\r\n" +
            "Referrer-Policy: no-referrer\r\n" +
            "Connection: close\r\n\r\n"
        socket.getOutputStream().apply {
            write(headers.toByteArray(Charsets.US_ASCII))
            write(body)
            flush()
        }
    }

    override fun close() = server.close()

    companion object {
        const val HOST = "127.0.0.1"
        const val PORT = 53682
        const val REDIRECT_URI = "http://$HOST:$PORT/"
        private val httpVersions = setOf("HTTP/1.0", "HTTP/1.1")
        private val responseParameters = setOf("state", "code", "error")

        fun open(port: Int = PORT): DriveLoopbackCallback {
            val server = ServerSocket()
            try {
                server.reuseAddress = true
                server.bind(InetSocketAddress(HOST, port))
                server.soTimeout = 250
                return DriveLoopbackCallback(server)
            } catch (e: IOException) {
                server.close()
                throw e
            }
        }
    }
}
