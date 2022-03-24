@file:Suppress("unused", "SERIALIZER_TYPE_INCOMPATIBLE")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.FeaturesSerializer

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
@Serializable(with = FeaturesSerializer::class)
data class Features internal constructor(
    val relay: Relay? = null,
    val proxy: Proxy? = null,
    val friend: Friend? = null,
    val lowPower: LowPower? = null
)

/**
 * Represents a type feature.
 */
@Serializable
sealed class Feature {
    abstract val state: FeatureState
}

/**
 * Relay feature is the ability to receive and retransmit mesh messages over the advertising
 * bearer to enable larger networks.
 *
 * @property state State of the relay feature.
 */
@Serializable
data class Relay internal constructor(
    override val state: FeatureState
) : Feature()

/**
 * Proxy feature is the ability to receive and retransmit mesh messages between GATT and
 * advertising bearers.
 *
 * @property state State of the proxy feature.
 */
@Serializable
data class Proxy internal constructor(
    override val state: FeatureState
) : Feature()

/**
 * Friend feature is the ability to operate within a mesh network at significantly
 * reduced receiver duty cycles only in conjunction with a node supporting the Friend feature.
 *
 * @property state State of friend feature.
 */
@Serializable
data class Friend internal constructor(
    override val state: FeatureState
) : Feature()

/**
 * LowPower feature is the ability to help a node supporting the Low Power feature
 * to operate by storing messages destined for those nodes.
 *
 * @property state State of low power feature.
 */
@Serializable
data class LowPower internal constructor(
    override val state: FeatureState
) : Feature()

/**
 * FeatureState describes the state of a given [Feature].
 *
 * @property value 0 = disabled, 1 = enabled, 2 = unsupported
 */
@Serializable
sealed class FeatureState private constructor(val value: Int) {

    companion object {
        /**
         * Returns the feature state for a given a feature.
         *
         * @param value                         Integer value describing the state.
         * @throws IllegalArgumentException     if the feature value is not 0, 1 or 2.
         */
        fun from(value: Int): FeatureState = when (value) {
            0 -> Disabled
            1 -> Enabled
            2 -> Unsupported
            else -> throw IllegalArgumentException("Feature value should be from $DISABLED, $ENABLED or $UNSUPPORTED!")
        }
    }
}

/** Disabled state. */
@Serializable
object Disabled : FeatureState(value = DISABLED)

/** Enabled state. */
@Serializable
object Enabled : FeatureState(value = ENABLED)

/** Unsupported state. */
@Serializable
object Unsupported : FeatureState(value = UNSUPPORTED)

private const val DISABLED = 0
private const val ENABLED = 1
private const val UNSUPPORTED = 2