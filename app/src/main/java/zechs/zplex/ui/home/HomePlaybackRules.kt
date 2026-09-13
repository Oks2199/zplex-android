package zechs.zplex.ui.home

internal object HomePlaybackRules {

    fun isInProgress(watchedDuration: Long, totalDuration: Long): Boolean {
        if (watchedDuration <= 0L || totalDuration <= 0L) return false
        return watchedDuration.toDouble() / totalDuration <= 0.90
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
