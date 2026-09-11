package zechs.zplex.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import zechs.zplex.R
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.repository.DocumentTreeRepository
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.util.Locale
import javax.inject.Inject
import kotlin.math.ln
import kotlin.math.pow
import kotlin.random.Random

class DownloadWorkerFactory @Inject constructor(
    private val documentTreeRepository: DocumentTreeRepository
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker =
        DownloadWorker(appContext, workerParameters, documentTreeRepository)
}


@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val documentTreeRepository: DocumentTreeRepository
) : CoroutineWorker(context, workerParams) {

    private val notificationManager = applicationContext.getSystemService(
        Context.NOTIFICATION_SERVICE
    ) as NotificationManager

    companion object {
        const val DOWNLOADS_FOLDER_NAME = "movynex-downloads"
        const val TAG = "FileDownloadWorker"
        const val FILE_PATH = "filePath"
        const val NOTIFICATION_ID = "notificationId"
        const val FILE_ID = "fileId"
        const val FILE_TITLE = "fileTitle"
        const val TMDB_ID = "tmdbId"
        const val OFFLINE_DOWNLOADS_GROUP = "OFFLINE_DOWNLOADS_GROUP"
        const val MEDIA_TYPE = "mediaType"
        const val SEASON_NUMBER = "seasonNumber"
        const val EPISODE_NUMBER = "episodeNumber"
        const val CHANNEL_ID = "download_channel"
        const val CHANNEL_NAME = "Downloads"

        fun getDownloadsFolderPath(context: Context): File {
            return File(context.filesDir, DOWNLOADS_FOLDER_NAME)
        }
    }

    override suspend fun doWork(): Result {
        val fileId = inputData.getString(FILE_ID)
            ?: return fail("Download fileId is required.")

        val title = inputData.getString(FILE_TITLE)
            ?: return fail("Download title is required.")

        val tmdbId = inputData.getInt(TMDB_ID, 0)
            .takeIf { it != 0 }
            ?: return fail("TMDB ID is required.")

        val mediaType = inputData.getString(MEDIA_TYPE)
            ?: return fail("Media type is required.")

        val seasonNumber: Int?
        val episodeNumber: Int?
        when (mediaType) {
            MediaType.tv.name -> {
                seasonNumber = inputData.getInt(SEASON_NUMBER, 0)
                    .takeIf { it != 0 }
                    ?: return fail("Season number is required.")

                episodeNumber = inputData.getInt(EPISODE_NUMBER, 0)
                    .takeIf { it != 0 }
                    ?: return fail("Episode number is required.")
            }

            MediaType.movie.name -> {
                seasonNumber = null
                episodeNumber = null
            }

            else -> {
                return fail("Unsupported media type: $mediaType")
            }
        }

        ensureDownloadsFolder()
        createNotificationChannel()
        val notificationId = Random.nextInt(1, Int.MAX_VALUE)

        val file = try {
            downloadFile(title = title, fileId = fileId, notificationId = notificationId)
        } catch (e: Exception) {
            return fail("Download failed: ${e.message ?: "Unknown error"}")
        }

        return if (file != null) {
            val outputData = when (mediaType) {
                MediaType.tv.name -> workDataOf(
                    FILE_TITLE to title,
                    MEDIA_TYPE to mediaType,
                    TMDB_ID to tmdbId,
                    SEASON_NUMBER to seasonNumber,
                    EPISODE_NUMBER to episodeNumber,
                    NOTIFICATION_ID to notificationId,
                    FILE_PATH to file.path
                )

                MediaType.movie.name -> workDataOf(
                    FILE_TITLE to title,
                    MEDIA_TYPE to mediaType,
                    TMDB_ID to tmdbId,
                    NOTIFICATION_ID to notificationId,
                    FILE_PATH to file.path
                )

                else -> null
            }
            outputData?.let { Result.success(it) } ?: fail("Unexpected error: outputData is null")
        } else {
            fail("Download returned null file for fileId=$fileId, title=$title")
        }
    }

    private suspend fun downloadFile(
        title: String,
        fileId: String,
        notificationId: Int,
    ): File? {
        try {
            if (getFilePath(fileId).exists()) {
                Log.d(TAG, "File already downloaded...")
                return getFilePath(fileId)
            }

            return withContext(Dispatchers.IO) {
                documentTreeRepository.openInputStream(fileId).use { inputStream ->
                    writeDownload(
                        inputStream = inputStream,
                        fileId = fileId,
                        notificationId = notificationId,
                        title = title
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Something went wrong!", e)
            showDownloadErrorNotification(notificationId, "Download Failed", title)
            return null
        }
    }

    private fun showDownloadErrorNotification(
        notificationId: Int,
        title: String,
        content: String
    ) {
        if (!hasNotificationPermission()) {
            Log.d(TAG, "Notification permission denied.")
            return
        }
        Log.d(TAG, content)
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setOngoing(false)
            .setGroup(OFFLINE_DOWNLOADS_GROUP)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun writeDownload(
        inputStream: InputStream,
        fileId: String,
        notificationId: Int,
        title: String
    ): File {
        val file = File.createTempFile("drive_file_", ".tmp", getDownloadsFolderPath(context))
        val handler = Handler(Looper.getMainLooper())
        var isRunning = true
        var progressBytes = 0L
        var previousBytes = 0L
        val totalBytes = runCatching {
            context.contentResolver.openAssetFileDescriptor(android.net.Uri.parse(fileId), "r")
                ?.use { it.length }
        }.getOrNull()?.takeIf { it > 0 } ?: -1L
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val cancelIntent = Intent(applicationContext, DownloadActionReceiver::class.java)
            .apply {
                action = DownloadActionReceiver.CANCEL_ACTION
                putExtra(DownloadActionReceiver.DOWNLOAD_NAME, title)
                putExtra(DownloadActionReceiver.NOTIFICATION_ID, notificationId)
                putExtra(DownloadActionReceiver.DOWNLOAD_ID, fileId)
            }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val updateRunnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    val bytesChangedInInterval = progressBytes - previousBytes
                    val progress = if (totalBytes > 0) {
                        ((progressBytes * 100) / totalBytes).toInt()
                    } else 0
                    val remainingBytes = totalBytes - progressBytes
                    val remainingTimeInSeconds = if (totalBytes > 0 && bytesChangedInInterval > 0) {
                        (remainingBytes / bytesChangedInInterval).toInt()
                    } else -1

                    val remainingTimeFormatted = if (remainingTimeInSeconds >= 0) {
                        val minutes = remainingTimeInSeconds / 60
                        val seconds = remainingTimeInSeconds % 60
                        "${minutes}m ${seconds}s"
                    } else "Calculating..."

                    showProgressNotification(
                        notificationId = notificationId,
                        cancelIntent = cancelPendingIntent,
                        title = title,
                        downloaded = humanReadableSize(progressBytes),
                        total = if (totalBytes > 0) humanReadableSize(totalBytes) else "Unknown",
                        remainingTime = remainingTimeFormatted,
                        speed = humanReadableSpeed(bytesChangedInInterval),
                        progress = progress
                    )
                    previousBytes = progressBytes
                    handler.postDelayed(this, 1000L)
                }
            }
        }

        try {
            file.outputStream().use { outputStream ->
                handler.post(updateRunnable)

                var bytes = inputStream.read(buffer)
                while (bytes >= 0) {
                    outputStream.write(buffer, 0, bytes)
                    progressBytes += bytes
                    bytes = inputStream.read(buffer)
                    if (isStopped) {
                        throw DownloadCancelled()
                    }
                }
                isRunning = false
                handler.removeCallbacks(updateRunnable)
            }

            // Rename temp file to final file
            val destinationFile = getFilePath(fileId)
            check(file.renameTo(destinationFile)) { "Unable to finish the download" }

            Log.d(TAG, "Write download completed (fileId=$fileId)")
            return destinationFile
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: ${e.message}")
            if (file.exists()) {
                Log.d(TAG, "Delete temp file: ${file.absolutePath}")
                file.delete()
            }
            throw e
        } finally {
            isRunning = false
            handler.removeCallbacks(updateRunnable)
        }
    }

    private fun showProgressNotification(
        notificationId: Int,
        cancelIntent: PendingIntent,
        title: String,
        downloaded: String,
        total: String,
        remainingTime: String,
        speed: String,
        progress: Int,
    ) {
        val notificationView = RemoteViews(
            applicationContext.packageName,
            R.layout.notification_download
        ).apply {
            setTextViewText(R.id.title, title)
            setTextViewText(R.id.progress_text, "Progress: $progress%")
            setTextViewText(R.id.downloaded_total, "Downloaded: $downloaded / $total")
            setTextViewText(R.id.remaining_time, "Remaining Time: $remainingTime")
            setProgressBar(R.id.progress_bar, 100, progress, false)
            setTextViewText(R.id.speed_text, "Speed: $speed")
            setOnClickPendingIntent(R.id.cancel_btn, cancelIntent)
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setCustomContentView(notificationView)
            .setCustomBigContentView(notificationView)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setGroup(OFFLINE_DOWNLOADS_GROUP)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            /* id = */ CHANNEL_ID,
            /* name = */ CHANNEL_NAME,
            /* importance = */ NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun getFilePath(fileUri: String): File {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(fileUri.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(getDownloadsFolderPath(context), "$digest.media")
    }

    private fun ensureDownloadsFolder() {
        val folderPath = File(context.filesDir, DOWNLOADS_FOLDER_NAME)
        if (!folderPath.exists()) {
            Log.d(TAG, "Created ${folderPath.absolutePath}")
            folderPath.mkdir()
        }
    }

    private fun getDownloadsFolderPathAsString(): String {
        return File(context.filesDir, DOWNLOADS_FOLDER_NAME).absolutePath
    }

    private fun humanReadableSpeed(bytes: Long): String {
        return humanReadableSize(bytes) + "/s"
    }

    private fun humanReadableSize(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB", "TB", "PB", "EB")
        val exp = (ln(bytes.toDouble()) / ln(1024.0)).toInt()
        val size = bytes / 1024.0.pow(exp.toDouble())
        return String.format(Locale.ENGLISH, "%.1f %s", size, units[exp - 1])
    }

    private fun fail(message: String): Result {
        Log.e(TAG, message)
        return Result.failure()
    }
}

class DownloadCancelled : Exception()
