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
import android.template.core.data.DefaultMyModelRepository
import android.template.core.data.Synchronizer
import android.template.core.data.di.fakeMyModels
import android.template.core.database.MyModelDao
import android.template.core.database.MyModelEntity
import android.template.core.network.MyModelNetworkDataSource
import android.template.core.network.model.NetworkMyModel

/**
 * Unit tests for [DefaultMyModelRepository].
 */
@OptIn(ExperimentalCoroutinesApi::class) // TODO: Remove when stable
class DefaultMyModelRepositoryTest {

    @Test
    fun myModels_newItemSaved_itemIsReturned() = runTest {
        val repository = DefaultMyModelRepository(FakeMyModelDao(), FakeMyModelNetworkDataSource(), Dispatchers.Unconfined)

        repository.add("Repository")

        assertEquals(repository.myModels.first().size, 1)
    }

    @Test
    fun syncWith_emptyDatabase_seedsRemoteData() = runTest {
        val repository = DefaultMyModelRepository(FakeMyModelDao(), FakeMyModelNetworkDataSource(), Dispatchers.Unconfined)

        val result = repository.syncWith(FakeSynchronizer())

        assertTrue(result)
        assertEquals(repository.myModels.first().size, fakeMyModels.size)
    }
}

private class FakeMyModelDao : MyModelDao {

    private val data = mutableListOf<MyModelEntity>()

    override fun getMyModels(): Flow<List<MyModelEntity>> = flow {
        emit(data)
    }

    override suspend fun insertMyModel(item: MyModelEntity) {
        data.add(0, item)
    }
}

private class FakeMyModelNetworkDataSource : MyModelNetworkDataSource {
    override suspend fun fetchMyModels(): List<NetworkMyModel> =
        fakeMyModels.map { NetworkMyModel(name = it) }
}

private class FakeSynchronizer : Synchronizer
