package android.template.core.network.model

import kotlinx.serialization.Serializable

/**
 * Network representation of a model returned by the backend. Kept separate from the domain
 * model so the wire format can evolve independently.
 */
@Serializable
data class NetworkMyModel(
    val name: String,
)
