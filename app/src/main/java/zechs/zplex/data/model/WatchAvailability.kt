package zechs.zplex.data.model

import zechs.zplex.data.model.tmdb.availability.MovieWatchProvidersResponse
import zechs.zplex.data.model.tmdb.availability.WatchProvider
import zechs.zplex.utils.Constants.TMDB_REGION

data class WatchAvailability(
    val streamingProviders: List<WatchProvider>,
    val rentProviders: List<WatchProvider>,
    val buyProviders: List<WatchProvider>,
    val tmdbLink: String?
) {
    val hasInformation: Boolean
        get() = streamingProviders.isNotEmpty() ||
            rentProviders.isNotEmpty() ||
            buyProviders.isNotEmpty()
}

object WatchAvailabilityMapper {

    fun map(watchProviders: MovieWatchProvidersResponse?): WatchAvailability {
        val frenchProviders = watchProviders?.results?.get(TMDB_REGION)
        val streaming = (
            frenchProviders?.flatrate.orEmpty() +
                frenchProviders?.free.orEmpty() +
                frenchProviders?.ads.orEmpty()
            ).normalized()

        return WatchAvailability(
            streamingProviders = streaming,
            rentProviders = frenchProviders?.rent.orEmpty().normalized(),
            buyProviders = frenchProviders?.buy.orEmpty().normalized(),
            tmdbLink = frenchProviders?.link
        )
    }

    private fun List<WatchProvider>.normalized(): List<WatchProvider> =
        distinctBy { it.providerId }.sortedBy { it.displayPriority }
}
