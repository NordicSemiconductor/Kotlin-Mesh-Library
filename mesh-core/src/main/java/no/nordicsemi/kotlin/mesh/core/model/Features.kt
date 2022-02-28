@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

/**
 * Features represents the functionality of a [Node] that is determined by the set features that the node supports.
 *
 * @property relay          Ability to receive and retransmit mesh messages over the advertising bearer to enable larger networks.
 *                          Null if the current [FeatureState] of the [Relay] feature is unknown.
 * @property proxy          Ability to receive and retransmit mesh messages between GATT and advertising bearers.
 *                          Null if the current [FeatureState] of the [Proxy] feature is unknown.
 * @property friend         Ability to operate within a mesh network at significantly reduced receiver duty cycles only in conjunction
 *                          with a node supporting the Friend feature. Null if the current [FeatureState] of the [Friend] feature is unknown.
 * @property lowPower       Ability to help a node supporting the Low Power feature to operate by storing messages destined for those nodes.
 *                          Null if the current [FeatureState] of the [LowPower] feature is unknown.
 */
data class Features constructor(
    val relay: Relay?,
    val proxy: Proxy?,
    val friend: Friend?,
    val lowPower: LowPower?
)

/**
 * Feature
 */
sealed class Feature(open val state: FeatureState)

/**
 * Relay feature.
 *
 * @property state State of the relay feature.
 */
data class Relay internal constructor(override val state: FeatureState) : Feature(state = state)

/**
 * Proxy feature.
 *
 * @property state State of the proxy feature.
 */
data class Proxy internal constructor(override val state: FeatureState) : Feature(state = state)

/**
 * Friend feature.
 *
 * @property state State of friend feature.
 */
data class Friend internal constructor(override val state: FeatureState) : Feature(state = state)

/**
 * LowPower feature.
 *
 * @property state State of low power feature.
 */
data class LowPower internal constructor(override val state: FeatureState) : Feature(state = state)

/**
 * FeatureState describes the state of a given [Feature].
 *
 * @property state 0 = disabled, 1 = enabled, 2 = unsupported
 */
sealed class FeatureState private constructor(val state: Int)

/** Disabled state. */
object Disabled : FeatureState(state = 0)

/** Enabled state. */
object Enabled : FeatureState(state = 1)

/** Unsupported state. */
object Unsupported : FeatureState(state = 2)