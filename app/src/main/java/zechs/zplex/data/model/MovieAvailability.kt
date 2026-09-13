package zechs.zplex.data.model

import zechs.zplex.data.model.tmdb.availability.MovieReleaseDatesResponse
import zechs.zplex.data.model.tmdb.availability.MovieWatchProvidersResponse
import zechs.zplex.data.model.tmdb.availability.WatchProvider
import zechs.zplex.utils.Constants.TMDB_REGION
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class MovieAvailability(
    val recentTheatricalDate: LocalDate?,
    val streamingProviders: List<WatchProvider>,
    val rentProviders: List<WatchProvider>,
    val buyProviders: List<WatchProvider>,
    val tmdbLink: String?
) {
    val watchAvailability: WatchAvailability
        get() = WatchAvailability(
            streamingProviders = streamingProviders,
            rentProviders = rentProviders,
            buyProviders = buyProviders,
            tmdbLink = tmdbLink
        )

    val hasInformation: Boolean
        get() = recentTheatricalDate != null ||
            streamingProviders.isNotEmpty() ||
            rentProviders.isNotEmpty() ||
            buyProviders.isNotEmpty()
}

object MovieAvailabilityMapper {

    private const val RECENT_THEATRICAL_DAYS = 30L
    private val THEATRICAL_TYPES = setOf(2, 3)

    fun map(
        releaseDates: MovieReleaseDatesResponse?,
        watchProviders: MovieWatchProvidersResponse?,
        today: LocalDate = LocalDate.now()
    ): MovieAvailability {
        val frenchDates = releaseDates?.results
            ?.firstOrNull { it.countryCode == TMDB_REGION }
            ?.releaseDates
            .orEmpty()

        val recentTheatricalDate = frenchDates
            .asSequence()
            .filter { it.type in THEATRICAL_TYPES }
            .mapNotNull { parseTmdbDate(it.releaseDate) }
            .filter { !it.isAfter(today) && !it.isBefore(today.minusDays(RECENT_THEATRICAL_DAYS)) }
            .maxOrNull()

        val watchAvailability = WatchAvailabilityMapper.map(watchProviders)

        return MovieAvailability(
            recentTheatricalDate = recentTheatricalDate,
            streamingProviders = watchAvailability.streamingProviders,
            rentProviders = watchAvailability.rentProviders,
            buyProviders = watchAvailability.buyProviders,
            tmdbLink = watchAvailability.tmdbLink
        )
    }

    private fun parseTmdbDate(value: String): LocalDate? = try {
        LocalDate.parse(value.take(10))
    } catch (_: DateTimeParseException) {
        null
    }
}
