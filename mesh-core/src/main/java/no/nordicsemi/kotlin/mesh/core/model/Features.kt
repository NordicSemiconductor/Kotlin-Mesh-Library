@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

/**
 * Features represents the functionality of a [Node] that is determined by the set features that the node supports.
 *
 * @param relay        Relay [FeatureState] of a given node, or null if the current state of the feature is unknown.
 * @param proxy        Proxy [FeatureState] of a given node, or null if the current state of the feature is unknown.
 * @param friend       Friend [FeatureState] of a given node, or null if the current state of the feature is unknown.
 * @param lowPower     Low Power [FeatureState] of a given node, or null if the current state of the feature is unknown.
 */
data class Features internal constructor(
    val relay: FeatureState?,
    val proxy: FeatureState?,
    val friend: FeatureState?,
    val lowPower: FeatureState?
)

/**
 * FeatureState describes the state of [Features].
 */
sealed class FeatureState private constructor(val state: Int)

/** Disabled state. */
object Disabled : FeatureState(state = 0)

/** Enabled state. */
object Enabled : FeatureState(state = 1)

/** Unsupported state. */
object Unsupported : FeatureState(state = 2)