package zechs.zplex.ui.shared_adapters.banner

import android.graphics.Rect
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Precision
import zechs.zplex.R
import zechs.zplex.data.model.BackdropSize
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.PosterSize
import zechs.zplex.databinding.ItemWideBannerBinding
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.ext.isTelevision

class BannerViewHolder(
    private val itemBinding: ItemWideBannerBinding,
    val bannerAdapter: BannerAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(item: BannerItem) {
        val media = item.media
        val television = itemBinding.root.context.isTelevision
        val placeholder = if (television) R.drawable.tv_banner_background else R.drawable.no_poster
        val image = when {
            television && !media.backdrop_path.isNullOrBlank() ->
                "$TMDB_IMAGE_PREFIX/${BackdropSize.w1280}${media.backdrop_path}"
            !media.poster_path.isNullOrBlank() ->
                "$TMDB_IMAGE_PREFIX/${PosterSize.w780}${media.poster_path}"
            else -> placeholder
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

            ivMainBanner.load(image) {
                precision(Precision.EXACT)
                placeholder(placeholder)
                error(placeholder)
            }

            val openDetails = {
                bannerAdapter.bannerOnClick.invoke(item)
            }
            if (!television) {
                root.setOnClickListener { openDetails() }
                materialCardView.setOnClickListener { openDetails() }
            } else {
                btnDetails.setOnFocusChangeListener { button, focused ->
                    if (focused) button.post {
                        if (button.hasFocus()) {
                            root.requestRectangleOnScreen(Rect(0, 0, root.width, root.height), true)
                        }
                    }
                }
            }
            btnDetails.setOnClickListener { openDetails() }
        }
    }
}
