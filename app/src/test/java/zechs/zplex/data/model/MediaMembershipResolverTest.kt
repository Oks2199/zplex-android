package zechs.zplex.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaMembershipResolverTest {

    @Test
    fun `an unsaved media belongs to neither collection`() {
        assertEquals(
            MediaMembership.NONE,
            MediaMembershipResolver.resolve(isSaved = false, fileId = null)
        )
    }

    @Test
    fun `a saved media without a Drive file belongs to the watchlist`() {
        assertEquals(
            MediaMembership.WATCHLIST,
            MediaMembershipResolver.resolve(isSaved = true, fileId = null)
        )
    }

    @Test
    fun `adding a Drive file moves a watchlist media to the library`() {
        assertEquals(
            MediaMembership.LIBRARY,
            MediaMembershipResolver.resolve(isSaved = true, fileId = "drive-file-id")
        )
    }

    @Test
    fun `an offline movie belongs to the library`() {
        assertEquals(
            MediaMembership.LIBRARY,
            MediaMembershipResolver.resolve(
                isSaved = false,
                fileId = null,
                isOffline = true
            )
        )
    }

    @Test
    fun `remote cleanup never removes a watchlist row`() {
        assertFalse(MediaMembershipResolver.isMissingFromRemote(null, emptySet()))
    }

    @Test
    fun `remote cleanup removes only a missing Drive row`() {
        assertTrue(
            MediaMembershipResolver.isMissingFromRemote(
                fileId = "missing-drive-file",
                remoteIds = setOf("existing-drive-file")
            )
        )
        assertFalse(
            MediaMembershipResolver.isMissingFromRemote(
                fileId = "existing-drive-file",
                remoteIds = setOf("existing-drive-file")
            )
        )
    }
}
