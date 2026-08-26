package android.template.core.testing.util

import android.template.core.data.util.SyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TestSyncManager : SyncManager {

    private val syncStatusFlow = MutableStateFlow(false)

    override val isSyncing: Flow<Boolean> = syncStatusFlow

    override fun requestSync() = Unit

    /**
     * A test-only API to set the sync status from tests.
     */
    fun setSyncing(isSyncing: Boolean) {
        syncStatusFlow.value = isSyncing
    }
}
