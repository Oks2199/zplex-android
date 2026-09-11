package zechs.zplex.data.model.drive

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DriveFileTest {

    @Test
    fun `Android document directory is recognized as a folder`() {
        assertTrue(file("Series", "vnd.android.document/directory").isFolder)
    }

    @Test
    fun `video extension is recognized when provider returns generic mime type`() {
        assertTrue(file("Movie (2026) [123].mkv", "application/octet-stream").isVideoFile)
    }

    @Test
    fun `ordinary document is not recognized as video`() {
        assertFalse(file("notes.txt", "text/plain").isVideoFile)
    }

    private fun file(name: String, mimeType: String) = DriveFile(
        id = "content://provider/document/1",
        name = name,
        size = null,
        mimeType = mimeType,
        iconLink = null,
        modifiedTime = null,
        shortcutDetails = ShortcutDetails()
    )
}
