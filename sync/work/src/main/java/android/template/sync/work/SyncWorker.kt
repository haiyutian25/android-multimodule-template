package android.template.sync.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import android.template.core.data.MyModelRepository
import android.template.core.data.Synchronizer
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Syncs the data layer by delegating to the appropriate repository instances with
 * sync functionality.
 */
@HiltWorker
internal class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val myModelRepository: MyModelRepository,
) : CoroutineWorker(appContext, workerParams), Synchronizer {

    override suspend fun doWork(): Result {
        // With more than one Syncable repository they can be synced in parallel with
        // awaitAll(async { repository.sync() }, ...)
        val syncedSuccessfully = myModelRepository.sync()

        return if (syncedSuccessfully) {
            Result.success()
        } else {
            Result.retry()
        }
    }

    companion object {
        /**
         * One time work to sync data on app startup
         */
        fun startUpSyncWork() = OneTimeWorkRequestBuilder<DelegatingWorker>()
            .setConstraints(SyncConstraints)
            .setInputData(SyncWorker::class.delegatedData())
            .build()
    }
}
