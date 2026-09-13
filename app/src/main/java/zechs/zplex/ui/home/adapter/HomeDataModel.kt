package zechs.zplex.ui.home.adapter

import androidx.annotation.Keep
import zechs.zplex.ui.home.adapter.watched.WatchedDataModel
import zechs.zplex.ui.shared_adapters.banner.BannerItem

typealias tmdbMedia = zechs.zplex.data.model.tmdb.entities.Media

sealed class HomeDataModel {

    enum class Section {
        CONTINUE_WATCHING,
        DRIVE,
        WATCHLIST,
        TRENDING,
        THEATRES,
        STREAMING
    }

    @Keep
    data class Header(
        val section: Section,
        val heading: String
    ) : HomeDataModel()

    @Keep
    data class Attribution(
        val text: String
    ) : HomeDataModel()

    @Keep
    data class DiscoveryTitle(
        val text: String
    ) : HomeDataModel()

    @Keep
    data class Media(
        val section: Section,
        val media: List<tmdbMedia>
    ) : HomeDataModel()

    @Keep
    data class Banner(
        val banners: List<BannerItem>
    ) : HomeDataModel()

    @Keep
    data class Watched(
        val section: Section = Section.CONTINUE_WATCHING,
        val watched: List<WatchedDataModel>
    ) : HomeDataModel()

}
