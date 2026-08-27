package android.template.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import android.template.core.data.DefaultGreetingRepository
import android.template.core.data.Synchronizer
import android.template.core.database.GreetingDao
import android.template.core.database.GreetingEntity
import android.template.core.network.GreetingNetworkDataSource
import android.template.core.network.model.NetworkGreeting

/**
 * Unit tests for [DefaultGreetingRepository].
 */
@OptIn(ExperimentalCoroutinesApi::class) // TODO: Remove when stable
class DefaultGreetingRepositoryTest {

    @Test
    fun greetings_newItemSaved_itemIsReturned() = runTest {
        val repository = DefaultGreetingRepository(FakeGreetingDao(), FakeGreetingNetworkDataSource(), Dispatchers.Unconfined)

        repository.add("Repository")

        assertEquals(repository.greetings.first().size, 1)
    }

    @Test
    fun syncWith_emptyDatabase_seedsRemoteData() = runTest {
        val repository = DefaultGreetingRepository(FakeGreetingDao(), FakeGreetingNetworkDataSource(), Dispatchers.Unconfined)

        val result = repository.syncWith(FakeSynchronizer())

        assertTrue(result)
        assertEquals(repository.greetings.first().size, fakeModels.size)
    }
}

private val fakeModels = listOf("One", "Two", "Three")

private class FakeGreetingDao : GreetingDao {

    private val data = mutableListOf<GreetingEntity>()

    override fun getGreetings(): Flow<List<GreetingEntity>> = flow {
        emit(data)
    }

    override suspend fun insertGreeting(item: GreetingEntity) {
        data.add(0, item)
    }
}

private class FakeGreetingNetworkDataSource : GreetingNetworkDataSource {
    override suspend fun fetchGreetings(): List<NetworkGreeting> =
        fakeModels.map { NetworkGreeting(message = it) }
}

private class FakeSynchronizer : Synchronizer
