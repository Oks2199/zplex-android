package zechs.zplex.ui.shared_adapters.banner

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Precision
import zechs.zplex.R
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.PosterSize
import zechs.zplex.databinding.ItemWideBannerBinding
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX

class BannerViewHolder(
    private val itemBinding: ItemWideBannerBinding,
    val bannerAdapter: BannerAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(item: BannerItem) {
        val media = item.media
        val mediaPosterUrl = if (media.poster_path == null) {
            R.drawable.no_poster
        } else {
            "${TMDB_IMAGE_PREFIX}/${PosterSize.w780}${media.poster_path}"
        }

        itemBinding.apply {
            tvTitle.text = media.title ?: media.name
            tvAvailability.text = item.availabilityLabel
            tvAvailability.isVisible = !item.availabilityLabel.isNullOrBlank()
            val releaseYear = (media.release_date ?: media.first_air_date)
                ?.take(4)
                ?.takeIf { year -> year.all { character -> character.isDigit() } }
            val mediaType = when (media.media_type) {
                MediaType.movie -> root.context.getString(R.string.movie_label)
                MediaType.tv -> root.context.getString(R.string.series_label)
                else -> null
            }
            tvMediaMeta.text = listOfNotNull(mediaType, releaseYear).joinToString(" • ")
            tvMediaMeta.isVisible = tvMediaMeta.text.isNotBlank()

            val rating = media.vote_average?.div(2)?.takeIf { it > 0.0 }
            ratingContainer.isVisible = rating != null
            tvRatingText.text = rating?.let {
                root.context.getString(R.string.rating_out_of_five, it)
            }

            ivMainBanner.load(mediaPosterUrl) {
                precision(Precision.EXACT)
                placeholder(R.drawable.no_poster)
                error(R.drawable.no_poster)
            }

            val openDetails = {
                bannerAdapter.bannerOnClick.invoke(item)
            }
            root.setOnClickListener { openDetails() }
            materialCardView.setOnClickListener { openDetails() }
            btnDetails.setOnClickListener { openDetails() }
        }
    }
}
