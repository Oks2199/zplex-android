package zechs.zplex.utils.ext

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.RecyclerView
import zechs.zplex.R

val Context.isTelevision: Boolean
    get() = resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK ==
        Configuration.UI_MODE_TYPE_TELEVISION

val Context.televisionGridColumns: Int
    get() = ((resources.configuration.screenWidthDp - 180) / 148).coerceIn(3, 8)

fun View.showTelevisionFocus() {
    val stroke = resources.getDimensionPixelSize(R.dimen.tv_focus_stroke)
    val outline = GradientDrawable().apply {
        setColor(Color.TRANSPARENT)
        setStroke(stroke, context.getColor(R.color.tv_focus))
        cornerRadius = resources.getDimension(R.dimen.tv_focus_radius)
    }
    val resize = View.OnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
        outline.setBounds(0, 0, view.width, view.height)
    }
    fun show(view: View) {
        outline.setBounds(0, 0, view.width, view.height)
        view.overlay.add(outline)
        view.addOnLayoutChangeListener(resize)
    }
    findFocus()?.let(::show)
    viewTreeObserver.addOnGlobalFocusChangeListener { previous, current ->
        previous?.removeOnLayoutChangeListener(resize)
        previous?.overlay?.remove(outline)
        current?.let(::show)
    }
}

data class TvListFocus(val position: Int, val innerPosition: Int?, val viewId: Int)

fun RecyclerView.rememberTelevisionFocus(): TvListFocus? {
    if (!context.isTelevision) return null
    val focused = findFocus() ?: return null
    val position = findContainingViewHolder(focused)?.absoluteAdapterPosition
        ?.takeUnless { it == RecyclerView.NO_POSITION } ?: return null
    val inner = generateSequence(focused.parent as? View) { it.parent as? View }
        .takeWhile { it != this }
        .filterIsInstance<RecyclerView>()
        .firstOrNull()
    val innerPosition = inner?.findContainingViewHolder(focused)?.absoluteAdapterPosition
        ?.takeUnless { it == RecyclerView.NO_POSITION }
    return TvListFocus(position, innerPosition, focused.id)
}

fun RecyclerView.restoreTelevisionFocus(focus: TvListFocus) {
    withItemView(focus.position) { item ->
        val row = item.findViewById<RecyclerView>(R.id.rvList)
        if (focus.innerPosition != null && row != null) {
            row.withItemView(focus.innerPosition) { it.requestSavedFocus(focus.viewId) }
        } else {
            item.requestSavedFocus(focus.viewId)
        }
    }
}

private fun RecyclerView.withItemView(position: Int, action: (View) -> Unit) {
    if (position !in 0 until (adapter?.itemCount ?: 0)) return
    val item = findViewHolderForAdapterPosition(position)?.itemView
    if (item != null && item.isLaidOut) {
        action(item)
    } else {
        doOnNextLayout { findViewHolderForAdapterPosition(position)?.itemView?.let(action) }
        scrollToPosition(position)
    }
}

private fun View.requestSavedFocus(viewId: Int) {
    val target = viewId.takeUnless { it == View.NO_ID }?.let { findViewById<View>(it) }
    if (target?.requestFocus() != true) requestFocus()
}
