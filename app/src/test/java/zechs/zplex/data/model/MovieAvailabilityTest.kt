package zechs.zplex.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import zechs.zplex.data.model.tmdb.availability.CountryReleaseDates
import zechs.zplex.data.model.tmdb.availability.CountryWatchProviders
import zechs.zplex.data.model.tmdb.availability.MovieReleaseDate
import zechs.zplex.data.model.tmdb.availability.MovieReleaseDatesResponse
import zechs.zplex.data.model.tmdb.availability.MovieWatchProvidersResponse
import zechs.zplex.data.model.tmdb.availability.WatchProvider
import java.time.LocalDate

class MovieAvailabilityTest {

    private val today = LocalDate.of(2026, 9, 13)

    @Test
    fun `recent french theatrical release is detected`() {
        val availability = MovieAvailabilityMapper.map(
            releaseDates = releaseDates("2026-09-09", type = 3),
            watchProviders = null,
            today = today
        )

        assertEquals(LocalDate.of(2026, 9, 9), availability.recentTheatricalDate)
    }

    @Test
    fun `future and old theatrical releases are not marked as in theatres`() {
        val future = MovieAvailabilityMapper.map(
            releaseDates("2026-09-14", type = 3),
            null,
            today
        )
        val old = MovieAvailabilityMapper.map(
            releaseDates("2026-08-13", type = 3),
            null,
            today
        )

        assertNull(future.recentTheatricalDate)
        assertNull(old.recentTheatricalDate)
    }

    @Test
    fun `french streaming providers are merged deduplicated and sorted`() {
        val netflix = provider(8, "Netflix", priority = 2)
        val prime = provider(119, "Prime Video", priority = 1)
        val providers = MovieWatchProvidersResponse(
            id = 1,
            results = mapOf(
                "FR" to CountryWatchProviders(
                    flatrate = listOf(netflix),
                    ads = listOf(netflix),
                    free = listOf(prime)
                )
            )
        )

        val availability = MovieAvailabilityMapper.map(null, providers, today)

        assertEquals(listOf("Prime Video", "Netflix"), availability.streamingProviders.map { it.providerName })
    }

    @Test
    fun `drive file always has priority over external availability`() {
        val availability = MovieAvailabilityMapper.map(
            releaseDates("2026-09-09", type = 3),
            MovieWatchProvidersResponse(
                id = 1,
                results = mapOf(
                    "FR" to CountryWatchProviders(flatrate = listOf(provider(8, "Netflix")))
                )
            ),
            today
        )

        assertEquals(
            MoviePrimaryAction.Playable,
            MoviePrimaryActionResolver.resolve(
                hasFile = true,
                availability = availability
            )
        )
    }

    @Test
    fun `single subscription service is named on the primary action`() {
        val availability = MovieAvailability(
            recentTheatricalDate = null,
            streamingProviders = listOf(provider(8, "Netflix")),
            rentProviders = emptyList(),
            buyProviders = emptyList(),
            tmdbLink = null
        )

        assertEquals(
            MoviePrimaryAction.OnProvider("Netflix"),
            MoviePrimaryActionResolver.resolve(
                hasFile = false,
                availability = availability
            )
        )
    }

    private fun releaseDates(date: String, type: Int) = MovieReleaseDatesResponse(
        id = 1,
        results = listOf(
            CountryReleaseDates(
                countryCode = "FR",
                releaseDates = listOf(MovieReleaseDate(releaseDate = date, type = type))
            )
        )
    )

    private fun provider(id: Int, name: String, priority: Int = 0) = WatchProvider(
        providerId = id,
        providerName = name,
        displayPriority = priority
    )
}
