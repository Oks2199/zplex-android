package zechs.zplex.data.model

sealed interface MoviePrimaryAction {
    data object Playable : MoviePrimaryAction
    data object InTheatres : MoviePrimaryAction
    data class OnProvider(val providerName: String) : MoviePrimaryAction
    data object OnStreaming : MoviePrimaryAction
    data object Rent : MoviePrimaryAction
    data object Buy : MoviePrimaryAction
    data object Unavailable : MoviePrimaryAction
}

object MoviePrimaryActionResolver {
    fun resolve(hasFile: Boolean, availability: MovieAvailability?): MoviePrimaryAction = when {
        hasFile -> MoviePrimaryAction.Playable
        availability?.recentTheatricalDate != null -> MoviePrimaryAction.InTheatres
        availability?.streamingProviders?.size == 1 -> {
            MoviePrimaryAction.OnProvider(availability.streamingProviders.first().providerName)
        }
        availability?.streamingProviders?.isNotEmpty() == true -> MoviePrimaryAction.OnStreaming
        availability?.rentProviders?.isNotEmpty() == true -> MoviePrimaryAction.Rent
        availability?.buyProviders?.isNotEmpty() == true -> MoviePrimaryAction.Buy
        else -> MoviePrimaryAction.Unavailable
    }
}
