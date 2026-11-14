package zechs.zplex.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import zechs.zplex.data.profile.ProfileSyncRepository
import zechs.zplex.data.profile.ProfileSyncStatus

class ProfileSyncWorker(
    context: Context,
    parameters: WorkerParameters,
    private val profiles: ProfileSyncRepository
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = when (profiles.sync()) {
        ProfileSyncStatus.RETRY -> Result.retry()
        else -> Result.success()
    }
}
