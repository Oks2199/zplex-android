package zechs.zplex.ui.shared_adapters.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import zechs.zplex.data.model.MediaType
import zechs.zplex.data.model.tmdb.entities.Media
import zechs.zplex.databinding.ItemMediaBinding

class MediaAdapter(
    val rating: Boolean = false,
    val mediaOnLongClick: ((Media) -> Unit)? = null,
    val mediaOnClick: (Media) -> Unit
) : ListAdapter<Media, MediaViewHolder>(MediaItemDiffCallback()) {

    private var watchedMovieIds = emptySet<Int>()

    fun setWatchedMovies(ids: Set<Int>) {
        if (watchedMovieIds == ids) return
        val previous = watchedMovieIds
        watchedMovieIds = ids
        currentList.forEachIndexed { position, media ->
            if (media.media_type == MediaType.movie && (media.id in previous) != (media.id in ids)) {
                notifyItemChanged(position, WatchedBadgeChanged)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ) = MediaViewHolder(
        itemBinding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        ),
        mediaAdapter = this
    )

    override fun onBindViewHolder(
        holder: MediaViewHolder, position: Int
    ) {
        val media = getItem(position)
        holder.bind(media, isWatched(media))
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty() && payloads.all { it === WatchedBadgeChanged }) {
            holder.bindWatched(isWatched(getItem(position)))
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    private fun isWatched(media: Media): Boolean =
        media.media_type == MediaType.movie && media.id in watchedMovieIds

    private object WatchedBadgeChanged

}
