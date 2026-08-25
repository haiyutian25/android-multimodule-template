package android.template.test.app.testdi

import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import android.template.core.data.MyModelRepository
import android.template.core.data.di.DataModule
import android.template.core.data.di.FakeMyModelRepository

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class]
)
interface FakeDataModule {

    @Binds
    abstract fun bindRepository(
        fakeRepository: FakeMyModelRepository
    ): MyModelRepository
}
