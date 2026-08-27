package android.template.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import android.template.core.data.GreetingRepository
import android.template.core.data.DefaultGreetingRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Singleton
    @Binds
    fun bindsGreetingRepository(
        greetingRepository: DefaultGreetingRepository
    ): GreetingRepository
}
