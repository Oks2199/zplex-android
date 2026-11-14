package zechs.zplex.ui.shared_adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import zechs.zplex.R

internal fun View.showWatchedBadge(artwork: ImageView, badge: TextView, watched: Boolean) {
    badge.isVisible = watched
    artwork.foreground = if (watched) context.getDrawable(R.color.watched_artwork_scrim) else null
    stateDescription = if (watched) context.getString(R.string.watched_badge) else null
}
