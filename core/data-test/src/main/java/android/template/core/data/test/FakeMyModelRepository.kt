package android.template.core.data.test

import android.template.core.data.MyModelRepository
import android.template.core.data.Synchronizer
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fake implementation of the [MyModelRepository]
 *
 * This allows us to run the app with fake data, without needing an internet connection or working
 * backend.
 */
class FakeMyModelRepository @Inject constructor() : MyModelRepository {
    override val myModels: Flow<List<String>> = flowOf(fakeMyModels)

    override suspend fun add(name: String) {
        throw NotImplementedError()
    }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true
}

val fakeMyModels = listOf("One", "Two", "Three")
