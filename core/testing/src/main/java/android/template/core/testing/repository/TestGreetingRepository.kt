package android.template.core.testing.repository

import android.template.core.data.GreetingRepository
import android.template.core.data.Synchronizer
import android.template.core.model.Greeting
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class TestGreetingRepository : GreetingRepository {

    /**
     * The backing hot flow for the list of models for testing.
     */
    private val greetingsFlow: MutableSharedFlow<List<Greeting>> =
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val items = mutableListOf<Greeting>()

    override val greetings: Flow<List<Greeting>> = greetingsFlow

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true

    /**
     * A test-only API to allow controlling the list of models from tests.
     */
    fun sendGreetings(models: List<Greeting>) {
        items.clear()
        items.addAll(models)
        greetingsFlow.tryEmit(items.toList())
    }
}
