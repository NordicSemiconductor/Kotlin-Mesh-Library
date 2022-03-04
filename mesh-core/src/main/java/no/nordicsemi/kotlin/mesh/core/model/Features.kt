@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.*

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
@Serializable
data class Features internal constructor(
    @Serializable(with = RelaySerializer::class)
    val relay: Relay?,
    @Serializable(with = ProxySerializer::class)
    val proxy: Proxy?,
    @Serializable(with = FriendSerializer::class)
    val friend: Friend?,
    @Serializable(with = LowPowerSerializer::class)
    val lowPower: LowPower?
)

/**
 * Feature
 */
sealed class Feature(
    @Serializable(with = FeatureStateSerializer::class)
    open val featureState: FeatureState
)

/**
 * Relay feature is the ability to receive and retransmit mesh messages over the advertising
 * bearer to enable larger networks.
 *
 * @property featureState State of the relay feature.
 */
data class Relay internal constructor(
    @Serializable(with = FeatureStateSerializer::class)
    override val featureState: FeatureState
) : Feature(featureState = featureState)

/**
 * Proxy feature is the ability to receive and retransmit mesh messages between GATT and
 * advertising bearers.
 *
 * @property featureState State of the proxy feature.
 */
data class Proxy internal constructor(
    @Serializable(with = FeatureStateSerializer::class)
    override val featureState: FeatureState
) : Feature(featureState = featureState)

/**
 * Friend feature is the ability to operate within a mesh network at significantly
 * reduced receiver duty cycles only in conjunction with a node supporting the Friend feature.
 *
 * @property featureState State of friend feature.
 */
data class Friend internal constructor(
    @Serializable(with = FeatureStateSerializer::class)
    override val featureState: FeatureState
) : Feature(featureState = featureState)

/**
 * LowPower feature is the ability to help a node supporting the Low Power feature
 * to operate by storing messages destined for those nodes.
 *
 * @property featureState State of low power feature.
 */
data class LowPower internal constructor(
    @Serializable(with = FeatureStateSerializer::class)
    override val featureState: FeatureState
) : Feature(featureState = featureState)

/**
 * FeatureState describes the state of a given [Feature].
 *
 * @property state 0 = disabled, 1 = enabled, 2 = unsupported
 */
@Serializable
sealed class FeatureState private constructor(val state: Int) {

    companion object {
        /**
         * Returns the feature state for a given a feature.
         *
         * @param state                         Integer value describing the state.
         * @throws IllegalArgumentException     if the feature value is not 0, 1 or 2.
         */
        fun from(state: Int): FeatureState = when (state) {
            0 -> Disabled
            1 -> Enabled
            2 -> Unsupported
            else -> throw IllegalArgumentException("Feature value should be from 0 to 2!")
        }
    }
}

/** Disabled state. */
object Disabled : FeatureState(state = 0)

/** Enabled state. */
object Enabled : FeatureState(state = 1)

/** Unsupported state. */
object Unsupported : FeatureState(state = 2)