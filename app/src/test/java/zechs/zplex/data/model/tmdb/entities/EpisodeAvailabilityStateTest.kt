package zechs.zplex.data.model.tmdb.entities

import org.junit.Assert.assertEquals
import org.junit.Test

class EpisodeAvailabilityStateTest {

    @Test
    fun `downloaded episode has priority over Drive state`() {
        val episode = episode(fileId = "local/path.mkv", offline = true)

        assertEquals(EpisodeAvailabilityState.DOWNLOADED, episode.availabilityState)
    }

    @Test
    fun `Drive episode is playable`() {
        val episode = episode(fileId = "drive-file-id", offline = false)

        assertEquals(EpisodeAvailabilityState.ON_DRIVE, episode.availabilityState)
    }

    @Test
    fun `episode without file remains information only`() {
        val episode = episode(fileId = null, offline = false)

        assertEquals(EpisodeAvailabilityState.INFORMATION_ONLY, episode.availabilityState)
    }

    private fun episode(fileId: String?, offline: Boolean) = Episode(
        id = 1,
        name = "Épisode",
        overview = null,
        episode_number = 1,
        season_number = 1,
        still_path = null,
        guest_stars = null,
        episode_type = null,
        fileId = fileId,
        fileSize = null,
        offline = offline
    )
}
