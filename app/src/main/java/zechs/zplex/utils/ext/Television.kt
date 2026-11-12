package zechs.zplex.utils.ext

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils
import androidx.core.view.allViews
import androidx.core.view.children
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationBarItemView
import com.google.android.material.shape.Shapeable
import zechs.zplex.R

val Context.isTelevision: Boolean
    get() = resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK ==
        Configuration.UI_MODE_TYPE_TELEVISION

val Context.televisionGridColumns: Int
    get() = ((resources.configuration.screenWidthDp - 180) / 148).coerceIn(3, 8)

interface TelevisionFocusNavigation {
    fun moveTelevisionFocusUp(focused: View): Boolean
}

fun View.enableTelevisionAppBarFocus() {
    allViews.filterIsInstance<ViewGroup>()
        .filter { it is AppBarLayout || it is Toolbar }
        .forEach { bar ->
            bar.touchscreenBlocksFocus = false
            bar.isKeyboardNavigationCluster = false
        }
}

fun Toolbar.focusTelevisionBackButton() {
    val description = navigationContentDescription ?: return
    children.filterIsInstance<ImageButton>()
        .firstOrNull { it.contentDescription == description }
        ?.requestFocus()
}

fun RecyclerView.isInFirstTelevisionRow(focused: View, firstRowCount: Int): Boolean {
    val position = findContainingViewHolder(focused)?.adapterPosition ?: RecyclerView.NO_POSITION
    return position in 0 until firstRowCount
}

fun View.showTelevisionFocus(excluded: View? = null) {
    val stroke = resources.getDimensionPixelSize(R.dimen.tv_focus_stroke)
    val fallbackRadius = resources.getDimension(R.dimen.tv_focus_radius)
    val focusColor = context.getColor(R.color.tv_focus)
    val focusOnLight = context.getColor(R.color.tv_focus_on_light)
    val outline = GradientDrawable().apply { setColor(Color.TRANSPARENT) }
    val surface = Outline()
    val bounds = Rect()
    fun positionOutline(view: View) {
        val buttonTint = (view as? MaterialButton)?.backgroundTintList
            ?.getColorForState(view.drawableState, Color.TRANSPARENT)
        val color = if (buttonTint != null && Color.alpha(buttonTint) == 255 &&
            ColorUtils.calculateContrast(focusColor, buttonTint) < 3.0
        ) focusOnLight else focusColor
        outline.setStroke(stroke, color)
        surface.setEmpty()
        if (view is Shapeable) view.background?.getOutline(surface)
        if (surface.getRect(bounds) && !bounds.isEmpty) {
            outline.cornerRadius = (surface.radius - stroke / 2f).coerceAtLeast(0f)
            outline.bounds = bounds
        } else {
            outline.cornerRadius = (fallbackRadius - stroke / 2f).coerceAtLeast(0f)
            outline.setBounds(0, 0, view.width, view.height)
        }
    }
    val resize = View.OnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
        positionOutline(view)
    }
    fun show(view: View) {
        if (view === excluded) return
        view.defaultFocusHighlightEnabled = false
        if (view is NavigationBarItemView) return
        positionOutline(view)
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
    val position = findContainingViewHolder(focused)?.adapterPosition
        ?.takeUnless { it == RecyclerView.NO_POSITION } ?: return null
    val inner = generateSequence(focused.parent as? View) { it.parent as? View }
        .takeWhile { it != this }
        .filterIsInstance<RecyclerView>()
        .firstOrNull()
    val innerPosition = inner?.findContainingViewHolder(focused)?.adapterPosition
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
