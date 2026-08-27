package android.template.core.network

import android.template.core.network.model.NetworkGreeting

/**
 * Interface representing network calls to the backend.
 */
interface GreetingNetworkDataSource {
    suspend fun fetchGreetings(): List<NetworkGreeting>
}
