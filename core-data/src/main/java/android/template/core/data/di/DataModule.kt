package android.template.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import android.template.core.data.MyModelRepository
import android.template.core.data.DefaultMyModelRepository
import android.template.core.data.Synchronizer
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
}

class FakeMyModelRepository @Inject constructor() : MyModelRepository {
    override val myModels: Flow<List<String>> = flowOf(fakeMyModels)

    override suspend fun add(name: String) {
        throw NotImplementedError()
    }

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = true
}

val fakeMyModels = listOf("One", "Two", "Three")
