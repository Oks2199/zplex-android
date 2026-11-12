package zechs.zplex.utils.ext

import android.app.Dialog
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import zechs.zplex.R

fun Dialog.fitTelevisionWindow(maximumWidthDp: Int, height: Int = ViewGroup.LayoutParams.WRAP_CONTENT) {
    val resources = context.resources
    val safeInset = resources.getDimensionPixelSize(R.dimen.tv_safe_horizontal)
    val density = resources.displayMetrics.density
    val available = (resources.configuration.screenWidthDp * density).toInt() - safeInset * 2
    val maximum = (maximumWidthDp * density).toInt()
    window?.setLayout(minOf(available.coerceAtLeast(1), maximum), height)
}

fun AlertDialog.Builder.showWithTelevisionFocus(): AlertDialog {
    val dialog = show()
    if (!dialog.context.isTelevision) return dialog
    dialog.fitTelevisionWindow(620)

    val options = dialog.listView
    if (options != null) {
        options.setSelector(R.drawable.tv_dialog_selection)
        options.post {
            if (options.requestFocus() && options.count > 0) {
                val checked = options.checkedItemPosition
                options.setSelection(if (checked == AdapterView.INVALID_POSITION) 0 else checked)
            }
        }
    } else {
        val preferred = listOf(
            DialogInterface.BUTTON_NEGATIVE,
            DialogInterface.BUTTON_POSITIVE,
            DialogInterface.BUTTON_NEUTRAL,
        )
            .mapNotNull(dialog::getButton)
            .firstOrNull { it.visibility == View.VISIBLE && it.isEnabled }
        preferred?.post { preferred.requestFocus() }
    }
    return dialog
}
