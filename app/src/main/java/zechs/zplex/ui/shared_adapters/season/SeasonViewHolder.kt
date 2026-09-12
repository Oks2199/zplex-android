package zechs.zplex.ui.shared_adapters.season

import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Precision
import zechs.zplex.R
import zechs.zplex.data.model.PosterSize
import zechs.zplex.data.model.tmdb.entities.Season
import zechs.zplex.databinding.ItemDetailedMediaBinding
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.util.Converter
import java.util.Locale

class SeasonViewHolder(
    private val showName: String,
    private val itemBinding: ItemDetailedMediaBinding,
    val seasonsAdapter: SeasonsAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {
    fun bind(season: Season) {
        itemBinding.apply {
            val seasonPosterUrl = if (season.poster_path == null) {
                R.drawable.no_poster
            } else {
                "${TMDB_IMAGE_PREFIX}/${PosterSize.w342}${season.poster_path}"
            }

            ivPoster.load(seasonPosterUrl) {
                precision(Precision.EXACT)
                placeholder(R.drawable.no_poster)
            }

            val seasonNumber = season.season_number
            val seasonName = root.context.getString(R.string.season_number, seasonNumber)
            tvTitle.text = season.name

            var premiered = root.context.getString(R.string.season_of_show, seasonName, showName)
            var yearSeason = ""

            val formattedDate = season.air_date?.let { date ->
                yearSeason += "${date.take(4)} | "
                Converter.parseDate(
                    date,
                    dstPattern = "d MMMM yyyy",
                    locale = Locale.FRENCH
                )
            }

            val episodeCount = season.episode_count
            yearSeason += root.resources.getQuantityString(
                R.plurals.episode_count,
                episodeCount,
                episodeCount
            )
            tvYear.text = yearSeason

            formattedDate?.let {
                premiered += " ${root.context.getString(R.string.premiered_on, formattedDate)}"
            }
            val seasonPlot = if (season.overview.toString() == "") {
                premiered
            } else season.overview
            tvPlot.text = seasonPlot
            root.setOnClickListener {
                seasonsAdapter.seasonOnClick.invoke(season)
            }
        }
    }
}
