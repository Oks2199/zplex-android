package zechs.zplex.ui.player

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import fi.iki.elonen.NanoHTTPD
import java.io.EOFException
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.UUID

/**
 * Exposes one Storage Access Framework document on the loopback interface.
 *
 * libmpv cannot reliably consume file descriptors backed by Google Drive's
 * document provider. HTTP gives libmpv its normal seekable input while all
 * Drive access remains read-only and handled by Android.
 */
class DocumentMediaServer(
    private val contentResolver: ContentResolver,
    private val documentUri: Uri
) : NanoHTTPD(LOOPBACK_ADDRESS, 0) {

    private val route = "/media/${UUID.randomUUID()}"
    private val contentLength = queryContentLength()
    private val contentType = contentResolver.getType(documentUri) ?: DEFAULT_MIME_TYPE

    fun startAndGetUrl(): String {
        require(contentLength > 0) { "Unable to determine the media file size" }
        start(SOCKET_READ_TIMEOUT, false)
        return "http://$LOOPBACK_ADDRESS:$listeningPort$route"
    }

    override fun serve(session: IHTTPSession): Response {
        if (session.uri != route) {
            return newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                MIME_PLAINTEXT,
                "Not found"
            )
        }

        return try {
            val requestedRange = session.headers["range"]
            val byteRange = requestedRange?.let {
                HttpByteRange.parse(it, contentLength)
                    ?: return rangeNotSatisfiable()
            }
            val start = byteRange?.start ?: 0L
            val end = byteRange?.endInclusive ?: (contentLength - 1)
            val responseLength = end - start + 1
            val inputStream = contentResolver.openInputStream(documentUri)
                ?: throw FileNotFoundException("Unable to open the media file")

            try {
                inputStream.skipFully(start)
            } catch (exception: Exception) {
                inputStream.close()
                throw exception
            }

            newFixedLengthResponse(
                if (byteRange == null) Response.Status.OK else Response.Status.PARTIAL_CONTENT,
                contentType,
                inputStream,
                responseLength
            ).apply {
                addHeader("Accept-Ranges", "bytes")
                if (byteRange != null) {
                    addHeader("Content-Range", "bytes $start-$end/$contentLength")
                }
            }
        } catch (exception: Exception) {
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                exception.message ?: "Unable to stream the media file"
            )
        }
    }

    private fun rangeNotSatisfiable(): Response =
        newFixedLengthResponse(
            Response.Status.RANGE_NOT_SATISFIABLE,
            MIME_PLAINTEXT,
            "Requested range not satisfiable"
        ).apply {
            addHeader("Content-Range", "bytes */$contentLength")
        }

    private fun queryContentLength(): Long {
        contentResolver.query(
            documentUri,
            arrayOf(OpenableColumns.SIZE),
            null,
            null,
            null
        )?.use { cursor ->
            val sizeColumn = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeColumn >= 0 && cursor.moveToFirst() && !cursor.isNull(sizeColumn)) {
                return cursor.getLong(sizeColumn)
            }
        }

        return contentResolver.openAssetFileDescriptor(documentUri, "r")
            ?.use { it.length }
            ?: -1L
    }

    companion object {
        private const val LOOPBACK_ADDRESS = "127.0.0.1"
        private const val DEFAULT_MIME_TYPE = "application/octet-stream"
    }
}

data class HttpByteRange(val start: Long, val endInclusive: Long) {
    companion object {
        private val pattern = Regex("^bytes=(\\d*)-(\\d*)$")

        fun parse(header: String, contentLength: Long): HttpByteRange? {
            if (contentLength <= 0) return null
            val match = pattern.matchEntire(header.trim()) ?: return null
            val startText = match.groupValues[1]
            val endText = match.groupValues[2]
            if (startText.isEmpty() && endText.isEmpty()) return null

            if (startText.isEmpty()) {
                val suffixLength = endText.toLongOrNull()?.takeIf { it > 0 } ?: return null
                val start = (contentLength - suffixLength).coerceAtLeast(0)
                return HttpByteRange(start, contentLength - 1)
            }

            val start = startText.toLongOrNull() ?: return null
            if (start >= contentLength) return null
            val end = if (endText.isEmpty()) {
                contentLength - 1
            } else {
                endText.toLongOrNull()?.coerceAtMost(contentLength - 1) ?: return null
            }
            if (end < start) return null
            return HttpByteRange(start, end)
        }
    }
}

private fun InputStream.skipFully(byteCount: Long) {
    var remaining = byteCount
    while (remaining > 0) {
        val skipped = skip(remaining)
        if (skipped > 0) {
            remaining -= skipped
        } else if (read() == -1) {
            throw EOFException("Unexpected end of media file")
        } else {
            remaining--
        }
    }
}
