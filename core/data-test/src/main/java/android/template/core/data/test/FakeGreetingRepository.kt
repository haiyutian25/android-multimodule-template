package android.template.core.data.test

import android.template.core.data.GreetingRepository
import android.template.core.data.Synchronizer
import android.template.core.model.Greeting
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
    override val greetings: Flow<List<Greeting>> = flowOf(fakeGreetings)

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true
}

val fakeGreetings = listOf(Greeting("Hello"), Greeting("World"), Greeting("Template"))
