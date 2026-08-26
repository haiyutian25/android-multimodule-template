package android.template.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import android.template.core.data.MyModelRepository
import android.template.core.data.DefaultMyModelRepository
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
