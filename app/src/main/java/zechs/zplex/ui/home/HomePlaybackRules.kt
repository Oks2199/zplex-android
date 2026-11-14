package zechs.zplex.ui.home

import zechs.zplex.data.model.WatchProgress

internal object HomePlaybackRules {

    fun isInProgress(watchedDuration: Long, totalDuration: Long): Boolean {
        if (watchedDuration <= 0L || totalDuration <= 0L) return false
        return !WatchProgress.isFinished(watchedDuration, totalDuration)
    }

    fun hasPlayableSource(
        savedFileId: String?,
        savedAsOffline: Boolean,
        savedOfflineFileExists: Boolean,
        indexedOnDrive: Boolean,
        matchingOfflineDownloadExists: Boolean
    ): Boolean = when {
        savedAsOffline -> savedOfflineFileExists || matchingOfflineDownloadExists
        !savedFileId.isNullOrBlank() -> indexedOnDrive || matchingOfflineDownloadExists
        else -> indexedOnDrive || matchingOfflineDownloadExists
    }
}
