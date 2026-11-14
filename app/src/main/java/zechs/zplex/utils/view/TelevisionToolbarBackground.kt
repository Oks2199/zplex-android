package zechs.zplex.utils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import zechs.zplex.R

internal class TelevisionToolbarBackground(context: Context) : Drawable() {
    private val diameter = context.resources.getDimension(R.dimen.tv_toolbar_indicator_size)
    private val strokeWidth = context.resources.getDimension(R.dimen.tv_focus_stroke)
    private val focusColor = context.getColor(R.color.tv_focus)
    private val focusSurface = context.getColor(R.color.tv_panel)
    private val pressedSurface = context.getColor(R.color.tv_pressed)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var drawableAlpha = 255

    override fun isStateful() = true

    override fun onStateChange(state: IntArray): Boolean {
        invalidateSelf()
        return true
    }

    override fun draw(canvas: Canvas) {
        if (android.R.attr.state_enabled !in state) return
        val focused = android.R.attr.state_focused in state &&
            android.R.attr.state_window_focused in state
        val pressed = android.R.attr.state_pressed in state
        if (!focused && !pressed) return

        val radius = minOf(diameter, bounds.width().toFloat(), bounds.height().toFloat()) / 2
        val x = bounds.exactCenterX()
        val y = bounds.exactCenterY()
        paint.style = Paint.Style.FILL
        paint.color = if (pressed) pressedSurface else focusSurface
        paint.alpha = drawableAlpha
        canvas.drawCircle(x, y, radius, paint)
        if (focused) {
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeWidth
            paint.color = focusColor
            paint.alpha = drawableAlpha
            canvas.drawCircle(x, y, (radius - strokeWidth / 2).coerceAtLeast(0f), paint)
        }
    }

    override fun setAlpha(alpha: Int) {
        drawableAlpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Drawable")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
