package zechs.zplex.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import zechs.zplex.data.model.entities.WatchedMovie
import zechs.zplex.data.model.entities.finishedMovieIds

class WatchProgressTest {
    @Test
    fun completionUsesTheSameThresholdAsContinueWatching() {
        assertFalse(WatchProgress.isFinished(900, 1_000))
        assertTrue(WatchProgress.isFinished(901, 1_000))
        assertFalse(WatchProgress.isFinished(1_000, 0))
        assertEquals(0, WatchProgress.percentage(1_000, 0))
        assertEquals(100, WatchProgress.percentage(1_100, 1_000))
    }

    @Test
    fun latestProgressWinsWhenHistoryHasDuplicateFilms() {
        val completed = WatchedMovie(1, "Film", "movie", null, 1_000, 1_000, 1)
        val replay = completed.copy(watchedDuration = 200, createdAt = 2)
        val otherFilm = completed.copy(tmdbId = 2)
        assertEquals(setOf(2), listOf(replay, otherFilm, completed).finishedMovieIds())
    }
}
