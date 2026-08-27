package android.template.core.testing.repository

import android.template.core.data.GreetingRepository
import android.template.core.data.Synchronizer
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class TestGreetingRepository : GreetingRepository {

    /**
     * The backing hot flow for the list of models for testing.
     */
    private val greetingsFlow: MutableSharedFlow<List<String>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val items = mutableListOf<String>()

    override val greetings: Flow<List<String>> = greetingsFlow

    override suspend fun add(message: String) {
        items.add(0, message)
        greetingsFlow.tryEmit(items.toList())
    }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true

    /**
     * A test-only API to allow controlling the list of models from tests.
     */
    fun sendGreetings(models: List<String>) {
        items.clear()
        items.addAll(models)
        greetingsFlow.tryEmit(items.toList())
    }
}
