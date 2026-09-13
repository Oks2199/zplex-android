package zechs.zplex.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import zechs.zplex.data.model.tmdb.availability.CountryWatchProviders
import zechs.zplex.data.model.tmdb.availability.MovieWatchProvidersResponse
import zechs.zplex.data.model.tmdb.availability.WatchProvider

class WatchAvailabilityTest {

    @Test
    fun `french providers are mapped by category`() {
        val response = MovieWatchProvidersResponse(
            id = 10,
            results = mapOf(
                "FR" to CountryWatchProviders(
                    flatrate = listOf(provider(8, "Netflix", 2)),
                    free = listOf(provider(119, "Prime Video", 1)),
                    rent = listOf(provider(2, "Apple TV")),
                    buy = listOf(provider(3, "Canal VOD"))
                )
            )
        )

        val availability = WatchAvailabilityMapper.map(response)

        assertEquals(
            listOf("Prime Video", "Netflix"),
            availability.streamingProviders.map { it.providerName }
        )
        assertEquals(listOf("Apple TV"), availability.rentProviders.map { it.providerName })
        assertEquals(listOf("Canal VOD"), availability.buyProviders.map { it.providerName })
        assertTrue(availability.hasInformation)
    }

    @Test
    fun `providers outside France are ignored`() {
        val response = MovieWatchProvidersResponse(
            id = 10,
            results = mapOf(
                "US" to CountryWatchProviders(
                    flatrate = listOf(provider(8, "Netflix"))
                )
            )
        )

        val availability = WatchAvailabilityMapper.map(response)

        assertFalse(availability.hasInformation)
    }

    @Test
    fun `streaming providers are deduplicated`() {
        val netflix = provider(8, "Netflix")
        val response = MovieWatchProvidersResponse(
            id = 10,
            results = mapOf(
                "FR" to CountryWatchProviders(
                    flatrate = listOf(netflix),
                    ads = listOf(netflix)
                )
            )
        )

        val availability = WatchAvailabilityMapper.map(response)

        assertEquals(1, availability.streamingProviders.size)
    }

    private fun provider(id: Int, name: String, priority: Int = 0) = WatchProvider(
        providerId = id,
        providerName = name,
        displayPriority = priority
    )
}
