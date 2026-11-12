package zechs.zplex.ui.settings.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import zechs.zplex.R
import zechs.zplex.utils.ext.fitTelevisionWindow
import zechs.zplex.utils.ext.isTelevision

class LoadingDialog(
    context: Context,
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_loading)
    }

    override fun onStart() {
        super.onStart()
        if (context.isTelevision) {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            fitTelevisionWindow(340)
        }
    }

}
