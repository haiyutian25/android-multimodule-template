package android.template.core.data.test

import android.template.core.data.GreetingRepository
import android.template.core.data.Synchronizer
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fake implementation of the [GreetingRepository]
 *
 * This allows us to run the app with fake data, without needing an internet connection or working
 * backend.
 */
class FakeGreetingRepository @Inject constructor() : GreetingRepository {
    override val greetings: Flow<List<String>> = flowOf(fakeGreetings)

    override suspend fun add(message: String) {
        throw NotImplementedError()
    }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true
}

val fakeGreetings = listOf("One", "Two", "Three")
