package zechs.zplex.service

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import zechs.zplex.data.local.WatchlistDatabase
import zechs.zplex.data.model.drive.DriveProfileAccess
import zechs.zplex.utils.SessionManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ProfileSyncScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val database: WatchlistDatabase,
    private val sessions: SessionManager
) {
    private val network = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    fun request() {
        workManager.enqueueUniqueWork(
            IMMEDIATE,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<ProfileSyncWorker>().setConstraints(network).build()
        )
    }

    suspend fun monitorForeground() {
        sessions.driveProfileAccess.collectLatest { access ->
            if (access != DriveProfileAccess.GRANTED) {
                workManager.cancelUniqueWork(IMMEDIATE)
                workManager.cancelUniqueWork(PERIODIC)
                return@collectLatest
            }
            workManager.enqueueUniquePeriodicWork(
                PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<ProfileSyncWorker>(15, TimeUnit.MINUTES)
                    .setInitialDelay(15, TimeUnit.MINUTES)
                    .setConstraints(network)
                    .build()
            )
            coroutineScope {
                launch {
                    database.invalidationTracker.createFlow(
                        "movies", "shows", "watched_movies", "watched_shows", emitInitialState = false
                    ).collectLatest {
                        delay(750)
                        request()
                    }
                }
                while (isActive) {
                    request()
                    delay(45_000)
                }
            }
        }
    }

    private companion object {
        const val IMMEDIATE = "movynex-profile-sync"
        const val PERIODIC = "movynex-profile-sync-periodic"
    }
}
