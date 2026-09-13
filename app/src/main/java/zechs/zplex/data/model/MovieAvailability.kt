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

        val frenchProviders = watchProviders?.results?.get(TMDB_REGION)
        val streaming = (
            frenchProviders?.flatrate.orEmpty() +
                frenchProviders?.free.orEmpty() +
                frenchProviders?.ads.orEmpty()
            ).normalized()

        return MovieAvailability(
            recentTheatricalDate = recentTheatricalDate,
            streamingProviders = streaming,
            rentProviders = frenchProviders?.rent.orEmpty().normalized(),
            buyProviders = frenchProviders?.buy.orEmpty().normalized(),
            tmdbLink = frenchProviders?.link
        )
    }

    private fun parseTmdbDate(value: String): LocalDate? = try {
        LocalDate.parse(value.take(10))
    } catch (_: DateTimeParseException) {
        null
    }

    private fun List<WatchProvider>.normalized(): List<WatchProvider> =
        distinctBy { it.providerId }.sortedBy { it.displayPriority }
}
