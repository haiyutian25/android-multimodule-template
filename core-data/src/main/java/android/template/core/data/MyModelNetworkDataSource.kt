package android.template.core.data

import android.template.core.model.MyModel

/**
 * Remote data source for [MyModel].
 *
 * The template ships with a fake implementation (bound in
 * [android.template.core.data.di.DataModule]). Replace it with a real network client
 * (e.g. Retrofit) for production apps.
 */
interface MyModelNetworkDataSource {
    suspend fun fetchMyModels(): List<MyModel>
}
