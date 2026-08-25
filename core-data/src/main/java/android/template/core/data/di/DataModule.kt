package android.template.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import android.template.core.data.MyModelNetworkDataSource
import android.template.core.data.MyModelRepository
import android.template.core.data.DefaultMyModelRepository
import android.template.core.data.Synchronizer
import android.template.core.model.MyModel
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindsMyModelRepository(
        myModelRepository: DefaultMyModelRepository
    ): MyModelRepository

    @Singleton
    @Binds
    fun bindsMyModelNetworkDataSource(
        networkDataSource: FakeMyModelNetworkDataSource
    ): MyModelNetworkDataSource
}

class FakeMyModelRepository @Inject constructor() : MyModelRepository {
    override val myModels: Flow<List<String>> = flowOf(fakeMyModels)

    override suspend fun add(name: String) {
        throw NotImplementedError()
    }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true
}

/**
 * Placeholder [MyModelNetworkDataSource] returning static data. Replace with a real network
 * implementation (e.g. Retrofit) for production apps.
 */
class FakeMyModelNetworkDataSource @Inject constructor() : MyModelNetworkDataSource {
    override suspend fun fetchMyModels(): List<MyModel> = fakeMyModels.map { MyModel(name = it) }
}

val fakeMyModels = listOf("One", "Two", "Three")
