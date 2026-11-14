package zechs.zplex.ui.episodes.adapter

import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Precision
import zechs.zplex.R
import zechs.zplex.data.model.StillSize
import zechs.zplex.data.model.tmdb.entities.Episode
import zechs.zplex.data.model.tmdb.entities.EpisodeAvailabilityState
import zechs.zplex.databinding.ItemEpisodeBinding
import zechs.zplex.ui.shared_adapters.showWatchedBadge
import zechs.zplex.utils.Constants.TMDB_IMAGE_PREFIX
import zechs.zplex.utils.ext.ifNullOrEmpty

class EpisodeViewHolder(
    private val itemBinding: ItemEpisodeBinding,
    val episodesAdapter: EpisodesAdapter
) : RecyclerView.ViewHolder(itemBinding.root) {
    fun bind(episode: Episode) {
        val count = itemBinding.root.context.getString(R.string.episode_number, episode.episode_number)
        val title = episode.name?.ifNullOrEmpty {
            itemBinding.root.context.getString(R.string.no_title)
        }
        itemBinding.apply {
            tvTitle.text = title
            tvEpisodeCount.text = count

            val episodeThumb = episode.still_path?.takeIf { it.isNotBlank() }?.let {
                "$TMDB_IMAGE_PREFIX/${StillSize.original}$it"
            }
            ivThumb.load(episodeThumb ?: R.drawable.no_thumb) {
                precision(Precision.EXACT)
                placeholder(R.drawable.no_thumb)
            }
            root.showWatchedBadge(ivThumb, watchedBadge, watched = episode.progress >= 100)
            watchProgress.isVisible = episode.progress in 1..99
            watchProgress.setProgressCompat(episode.progress, false)
            offlineBadge.apply {
                isGone = false
                when (episode.availabilityState) {
                    EpisodeAvailabilityState.DOWNLOADED -> {
                        text = context.getString(R.string.downloaded)
                        setBackgroundColor(ContextCompat.getColor(context, R.color.colorSuccessContainer))
                        setTextColor(ContextCompat.getColor(context, R.color.colorOnSuccessContainer))
                    }

                    EpisodeAvailabilityState.ON_DRIVE -> {
                        text = context.getString(R.string.on_drive)
                        setBackgroundColor(ContextCompat.getColor(context, R.color.colorDriveContainer))
                        setTextColor(ContextCompat.getColor(context, R.color.colorOnDriveContainer))
                    }

                    EpisodeAvailabilityState.INFORMATION_ONLY -> {
                        text = context.getString(R.string.not_on_drive)
                        setBackgroundColor(ContextCompat.getColor(context, R.color.colorUnavailableContainer))
                        setTextColor(ContextCompat.getColor(context, R.color.colorOnUnavailableContainer))
                    }
                }
            }
            finaleBadge.isInvisible = !episode.isSeasonFinale

            root.setOnClickListener {
                episodesAdapter.episodeOnClick.invoke(episode)
            }
            root.setOnLongClickListener() {
                episodesAdapter.episodeOnLongPress.invoke(episode)
                return@setOnLongClickListener true
            }
        }
    }
}
