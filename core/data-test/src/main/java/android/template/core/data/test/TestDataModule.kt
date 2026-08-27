package android.template.core.data.test

import android.template.core.data.GreetingRepository
import android.template.core.data.di.DataModule
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class],
)
interface TestDataModule {

    @Binds
    fun bindsGreetingRepository(
        fakeGreetingRepository: FakeGreetingRepository,
    ): GreetingRepository
}
