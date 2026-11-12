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
import android.widget.ImageView
import androidx.annotation.DimenRes
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils
import androidx.core.view.allViews
import androidx.core.view.children
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationBarItemView
import com.google.android.material.shape.Shapeable
import com.google.android.material.textfield.TextInputEditText
import zechs.zplex.R
import zechs.zplex.utils.view.TelevisionToolbarBackground

val Context.isTelevision: Boolean
    get() = resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK ==
        Configuration.UI_MODE_TYPE_TELEVISION

val Context.televisionGridColumns: Int
    get() = ((resources.configuration.screenWidthDp - 180) / 148).coerceIn(3, 8)

fun Context.televisionCarousel(@DimenRes itemWidth: Int): LinearLayoutManager {
    val widthPx = resources.getDimensionPixelSize(itemWidth)
    return object : LinearLayoutManager(this, HORIZONTAL, false) {
        override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
            if (lp == null) return super.checkLayoutParams(lp)
            lp.width = widthPx
            return true
        }
    }
}

interface TelevisionFocusNavigation {
    fun moveTelevisionFocusUp(focused: View): Boolean
}

fun View.enableTelevisionAppBarFocus() {
    allViews.forEach { view ->
        if (view is AppBarLayout || view is Toolbar) {
            val bar = view as ViewGroup
            bar.touchscreenBlocksFocus = false
            bar.isKeyboardNavigationCluster = false
        }
        view.styleTelevisionToolbarControl()
    }
}

private fun View.styleTelevisionToolbarControl(): Boolean {
    val iconControl = when (parent) {
        is Toolbar -> this is ImageButton
        is ActionMenuView -> this is ImageView || (this is ActionMenuItemView && !hasText())
        else -> false
    }
    if (!iconControl) return false
    if (background !is TelevisionToolbarBackground) {
        background = TelevisionToolbarBackground(context)
        defaultFocusHighlightEnabled = false
    }
    return true
}

fun Toolbar.focusTelevisionBackButton(): Boolean {
    val description = navigationContentDescription ?: return false
    return children.filterIsInstance<ImageButton>()
        .firstOrNull { it.contentDescription == description }
        ?.requestFocus() == true
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
    var outlinedView: View? = null
    fun clear() {
        outlinedView?.removeOnLayoutChangeListener(resize)
        outlinedView?.overlay?.remove(outline)
        outlinedView = null
    }
    fun show(view: View) {
        if (view === excluded || !view.hasWindowFocus()) return
        view.defaultFocusHighlightEnabled = false
        if (view is NavigationBarItemView) return
        if (view is TextInputEditText) return
        if (view.styleTelevisionToolbarControl()) return
        if (view is ViewGroup && !view.isClickable && !view.isLongClickable) return
        positionOutline(view)
        view.overlay.add(outline)
        view.addOnLayoutChangeListener(resize)
        outlinedView = view
    }
    findFocus()?.let(::show)
    viewTreeObserver.addOnGlobalFocusChangeListener { _, current ->
        clear()
        current?.let(::show)
    }
    viewTreeObserver.addOnWindowFocusChangeListener { focused ->
        clear()
        if (focused) findFocus()?.let(::show)
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
