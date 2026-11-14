package zechs.zplex.service

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dagger.Lazy
import zechs.zplex.data.profile.ProfileSyncRepository
import javax.inject.Inject

class DelegatingWorkerFactory @Inject constructor(
    private val downloadWorkerFactory: DownloadWorkerFactory,
    private val offlineDatabaseWorkerFactory: OfflineDatabaseWorkerFactory,
    private val cacheCleanupWorkerFactory: CacheCleanupWorkerFactory,
    private val profiles: Lazy<ProfileSyncRepository>
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            DownloadWorker::class.java.name ->
                downloadWorkerFactory.createWorker(appContext, workerClassName, workerParameters)

            OfflineDatabaseWorker::class.java.name ->
                offlineDatabaseWorkerFactory.createWorker(
                    appContext,
                    workerClassName,
                    workerParameters
                )

            CacheCleanupWorker::class.java.name ->
                cacheCleanupWorkerFactory.createWorker(
                    appContext,
                    workerClassName,
                    workerParameters
                )

            ProfileSyncWorker::class.java.name ->
                ProfileSyncWorker(appContext, workerParameters, profiles.get())

            else -> null
        }
    }
}
