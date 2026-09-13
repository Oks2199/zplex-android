package zechs.zplex.ui.episodes.adapter

import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.size.Precision
import com.google.android.material.progressindicator.LinearProgressIndicator
import zechs.zplex.R
import zechs.zplex.data.model.StillSize
import zechs.zplex.data.model.tmdb.entities.Episode
import zechs.zplex.data.model.tmdb.entities.EpisodeAvailabilityState
import zechs.zplex.databinding.ItemEpisodeBinding
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
            val watchProgressTAG = "watchProgressTAG"

            tvTitle.text = title
            tvEpisodeCount.text = count

            if (!episode.still_path.isNullOrEmpty()) {
                val episodeThumb = "${TMDB_IMAGE_PREFIX}/${StillSize.original}${episode.still_path}"
                ivThumb.load(episodeThumb) {
                    precision(Precision.EXACT)
                    placeholder(R.drawable.no_thumb)
                }
            }

            if (episode.progress == 0) {
                watchProgress.isGone = true
            } else {
                watchProgress.isGone = false
                if (watchProgress.tag == null) {
                    animateProgress(watchProgress, episode.progress)
                    watchProgress.tag = watchProgressTAG
                } else {
                    watchProgress.progress = episode.progress
                }
            }
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

    private fun animateProgress(watchProgress: LinearProgressIndicator, targetProgress: Int) {
        val animator = ValueAnimator.ofInt(0, targetProgress)
        animator.duration = 500
        animator.interpolator = DecelerateInterpolator()

        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            watchProgress.progress = animatedValue
        }

        animator.start()
    }
}
