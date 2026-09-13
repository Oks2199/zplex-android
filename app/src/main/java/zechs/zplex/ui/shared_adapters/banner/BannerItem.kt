package zechs.zplex.ui.shared_adapters.banner

import zechs.zplex.data.model.tmdb.entities.Media

data class BannerItem(
    val media: Media,
    val availabilityLabel: String? = null
)
