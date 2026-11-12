package zechs.zplex.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.Interpolator
import android.view.animation.TranslateAnimation
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.ThisApp
import zechs.zplex.databinding.ActivityMainBinding
import zechs.zplex.service.CacheCleanupWorker
import zechs.zplex.service.RemoteLibraryIndexingService
import zechs.zplex.utils.Constants.CACHE_TTL_IN_DAYS
import zechs.zplex.utils.MaterialMotionInterpolator
import zechs.zplex.utils.ext.TelevisionFocusNavigation
import zechs.zplex.utils.ext.enableTelevisionAppBarFocus
import zechs.zplex.utils.ext.isTelevision
import zechs.zplex.utils.ext.showTelevisionFocus
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var navHost: NavHostFragment

    @Inject
    lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        val television = isTelevision
        if (television) binding.root.showTelevisionFocus()

        navHost = supportFragmentManager.findFragmentById(
            R.id.mainNavHostFragment
        ) as NavHostFragment
        if (television) {
            navHost.childFragmentManager.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentViewCreated(
                        fm: FragmentManager, fragment: Fragment, view: View, savedInstanceState: Bundle?
                    ) {
                        view.enableTelevisionAppBarFocus()
                    }
                },
                true
            )
            navHost.view?.enableTelevisionAppBarFocus()
        }
        navController = navHost.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showNavigation = when (destination.id) {
                R.id.fragmentMedia, R.id.castsFragment,
                R.id.episodesListFragment, R.id.fragmentList,
                R.id.fragmentCollection, R.id.watchFragment,
                R.id.bigImageFragment, R.id.signInFragment,
                R.id.settingsFragment, R.id.seasonsBottomSheetFragment,
                R.id.searchFragment -> false
                else -> true
            }
            if (television) {
                binding.bottomNavigationView.isVisible = showNavigation
            } else {
                if (showNavigation) showSlideUp(binding.bottomNavigationView)
                else hideSlideDown(binding.bottomNavigationView)
                ViewCompat.setOnApplyWindowInsetsListener(binding.zplexFrame) { view, insets ->
                    val bars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
                    )
                    view.updatePadding(
                        left = bars.left,
                        top = bars.top,
                        right = bars.right,
                        bottom = if (showNavigation) 0 else bars.bottom,
                    )
                    WindowInsetsCompat.CONSUMED
                }
            }
        }
        val app = application as ThisApp
        if (!app.indexServiceExecuted) {
            startService(Intent(this, RemoteLibraryIndexingService::class.java))
            app.indexServiceExecuted = true
        }

        if (!hasNotificationPermission()) {
            requestNotificationPermission()
        }

        scheduleCacheCleanup()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DPAD_UP &&
            isTelevision && ::navHost.isInitialized
        ) {
            val destination = navHost.childFragmentManager.primaryNavigationFragment
            val focused = currentFocus
            if (focused != null && destination is TelevisionFocusNavigation &&
                destination.moveTelevisionFocusUp(focused)
            ) return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun scheduleCacheCleanup() {
        val cleanupWorkRequest = PeriodicWorkRequestBuilder<CacheCleanupWorker>(
            CACHE_TTL_IN_DAYS, TimeUnit.DAYS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "CacheCleanupJob",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupWorkRequest
        )
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
        }
    }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Log.i(TAG, "Notification permission was ${if (isGranted) "granted" else "denied"}")
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkNotificationPermission() {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i(TAG, "Notification granted!")
            }

            shouldShowRequestPermissionRationale(permission) -> {
                Log.i(TAG, "Notification permission denied permanently")
            }

            else -> {
                requestNotificationPermission.launch(permission)
            }
        }
    }

    private fun slideAnimation(
        view: View,
        fromXDelta: Float, toXDelta: Float,
        fromYDelta: Float, toYDelta: Float,
        show: Boolean
    ) = lifecycleScope.launch {
        if (view.animation?.hasStarted() == true && view.animation?.hasEnded() == false) {
            return@launch
        }
        if (show && view.isVisible) {
            return@launch
        }
        if (!show && view.isGone) {
            return@launch
        }

        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        val animationListener = object : AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                view.setLayerType(View.LAYER_TYPE_NONE, null)
                view.isGone = !show
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        }

        val interpolator = if (show) {
            MaterialMotionInterpolator.getEmphasizedDecelerateInterpolator()
        } else {
            MaterialMotionInterpolator.getEmphasizedAccelerateInterpolator()
        }
        createTranslateAnimation(
            fromXDelta,
            toXDelta,
            fromYDelta,
            toYDelta,
            interpolator,
            animationListener
        ).also { view.startAnimation(it) }
    }

    private fun createTranslateAnimation(
        fromXDelta: Float, toXDelta: Float,
        fromYDelta: Float, toYDelta: Float,
        interpolator: Interpolator,
        animationListener: AnimationListener? = null
    ): TranslateAnimation {
        return TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta).apply {
            this.interpolator = interpolator
            duration = 250L
            animationListener?.let { setAnimationListener(it) }
        }
    }

    private fun showSlideUp(view: View) {
        slideAnimation(view, 0f, 0f, view.height.toFloat(), 0f, true)
    }

    private fun hideSlideDown(view: View) {
        slideAnimation(view, 0f, 0f, 0f, view.height.toFloat(), false)
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    companion object {
        const val TAG = "MainActivity"
    }

}
