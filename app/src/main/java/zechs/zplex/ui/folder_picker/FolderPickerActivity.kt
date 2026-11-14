package zechs.zplex.ui.folder_picker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import zechs.zplex.R
import zechs.zplex.databinding.ActivityFolderPickerBinding
import zechs.zplex.utils.ext.isTelevision
import zechs.zplex.utils.ext.showTelevisionFocus

@AndroidEntryPoint
class FolderPickerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_TYPE = "type"
        const val TAG = "FolderPickerActivity"
    }

    private lateinit var binding: ActivityFolderPickerBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.folderPickerNavHostFragment
        ) as NavHostFragment
        navController = navHostFragment.navController
        if (isTelevision) binding.root.showTelevisionFocus()
        ViewCompat.setOnApplyWindowInsetsListener(binding.linearLayout) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            val horizontal = if (isTelevision) resources.getDimensionPixelSize(R.dimen.tv_safe_horizontal) else 0
            val vertical = if (isTelevision) resources.getDimensionPixelSize(R.dimen.tv_safe_vertical) else 0
            view.updatePadding(
                left = maxOf(bars.left, horizontal),
                top = maxOf(bars.top, vertical),
                right = maxOf(bars.right, horizontal),
                bottom = maxOf(bars.bottom, vertical),
            )
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}
