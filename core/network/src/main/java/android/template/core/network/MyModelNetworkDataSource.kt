package android.template.core.network

import android.template.core.network.model.NetworkMyModel

/**
 * Interface representing network calls to the backend.
 */
interface MyModelNetworkDataSource {
    suspend fun fetchMyModels(): List<NetworkMyModel>
}
