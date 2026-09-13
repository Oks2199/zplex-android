package zechs.zplex.ui.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomePlaybackRulesTest {

    @Test
    fun `continue watching requires actual progress`() {
        assertFalse(HomePlaybackRules.isInProgress(0L, 1_000L))
        assertFalse(HomePlaybackRules.isInProgress(100L, 0L))
        assertTrue(HomePlaybackRules.isInProgress(100L, 1_000L))
        assertFalse(HomePlaybackRules.isInProgress(901L, 1_000L))
    }

    @Test
    fun `Drive history requires indexed media or a matching download`() {
        assertTrue(
            HomePlaybackRules.hasPlayableSource(
                savedFileId = "drive-file",
                savedAsOffline = false,
                savedOfflineFileExists = false,
                indexedOnDrive = true,
                matchingOfflineDownloadExists = false
            )
        )
        assertFalse(
            HomePlaybackRules.hasPlayableSource(
                savedFileId = "drive-file",
                savedAsOffline = false,
                savedOfflineFileExists = false,
                indexedOnDrive = false,
                matchingOfflineDownloadExists = false
            )
        )
        assertTrue(
            HomePlaybackRules.hasPlayableSource(
                savedFileId = "removed-drive-file",
                savedAsOffline = false,
                savedOfflineFileExists = false,
                indexedOnDrive = false,
                matchingOfflineDownloadExists = true
            )
        )
    }

    @Test
    fun `offline history requires an existing download`() {
        assertFalse(
            HomePlaybackRules.hasPlayableSource(
                savedFileId = "missing-file",
                savedAsOffline = true,
                savedOfflineFileExists = false,
                indexedOnDrive = true,
                matchingOfflineDownloadExists = false
            )
        )
        assertTrue(
            HomePlaybackRules.hasPlayableSource(
                savedFileId = "downloaded-file",
                savedAsOffline = true,
                savedOfflineFileExists = true,
                indexedOnDrive = false,
                matchingOfflineDownloadExists = true
            )
        )
    }

    @Test
    fun `legacy history can use either known source`() {
        assertTrue(
            HomePlaybackRules.hasPlayableSource(
                savedFileId = null,
                savedAsOffline = false,
                savedOfflineFileExists = false,
                indexedOnDrive = true,
                matchingOfflineDownloadExists = false
            )
        )
        assertTrue(
            HomePlaybackRules.hasPlayableSource(
                savedFileId = null,
                savedAsOffline = false,
                savedOfflineFileExists = false,
                indexedOnDrive = false,
                matchingOfflineDownloadExists = true
            )
        )
    }
}
