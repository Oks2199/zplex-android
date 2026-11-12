package zechs.zplex.ui.shared_adapters.media

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R
import zechs.zplex.utils.ext.isTelevision
import zechs.zplex.utils.ext.televisionGridColumns

fun Context.mediaGridLayoutManager(): GridLayoutManager {
    if (isTelevision) return GridLayoutManager(this, televisionGridColumns)

    val columns = resources.getInteger(R.integer.grid_span_count)
    val widthRatio = when (columns) {
        3 -> 0.30
        4 -> 0.22
        5 -> 0.18
        6 -> 0.15
        7 -> 0.13
        else -> 1.0 / columns
    }
    return object : GridLayoutManager(this, columns) {
        override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
            if (lp == null) return super.checkLayoutParams(lp)
            lp.width = (widthRatio * width).toInt()
            return true
        }
    }
}
