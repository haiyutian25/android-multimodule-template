package android.template.core.testing.repository

import android.template.core.data.MyModelRepository
import android.template.core.data.Synchronizer
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class TestMyModelRepository : MyModelRepository {

    /**
     * The backing hot flow for the list of models for testing.
     */
    private val myModelsFlow: MutableSharedFlow<List<String>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val items = mutableListOf<String>()

    override val myModels: Flow<List<String>> = myModelsFlow

    override suspend fun add(name: String) {
        items.add(0, name)
        myModelsFlow.tryEmit(items.toList())
    }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true

    /**
     * A test-only API to allow controlling the list of models from tests.
     */
    fun sendMyModels(models: List<String>) {
        items.clear()
        items.addAll(models)
        myModelsFlow.tryEmit(items.toList())
    }
}
