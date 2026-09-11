package zechs.zplex.ui.player

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HttpByteRangeTest {

    @Test
    fun `parses open ended range`() {
        assertEquals(HttpByteRange(100, 999), HttpByteRange.parse("bytes=100-", 1000))
    }

    @Test
    fun `parses bounded range`() {
        assertEquals(HttpByteRange(100, 199), HttpByteRange.parse("bytes=100-199", 1000))
    }

    @Test
    fun `parses suffix range`() {
        assertEquals(HttpByteRange(900, 999), HttpByteRange.parse("bytes=-100", 1000))
    }

    @Test
    fun `rejects range outside file`() {
        assertNull(HttpByteRange.parse("bytes=1000-", 1000))
    }

    @Test
    fun `clamps end to file size`() {
        assertEquals(HttpByteRange(900, 999), HttpByteRange.parse("bytes=900-2000", 1000))
    }
}
