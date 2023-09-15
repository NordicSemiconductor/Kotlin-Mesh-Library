@file:Suppress("unused", "SERIALIZER_TYPE_INCOMPATIBLE")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.FeaturesSerializer
import kotlin.jvm.Throws

/**
 * Features represents the functionality of a [Node] that is determined by the set features that the
 * node supports.
 *
 * @property relay      Ability to receive and retransmit mesh messages over the advertising bearer
 *                      to enable larger networks. Null if the current [FeatureState] of the [Relay]
 *                      feature is unknown.
 * @property proxy      Ability to receive and retransmit mesh messages between GATT and advertising
 *                      bearers. Null if the current [FeatureState] of the [Proxy] feature is
 *                      unknown.
 * @property friend     Ability to operate within a mesh network at significantly reduced receiver
 *                      duty cycles only in conjunction with a node supporting the Friend feature.
 *                      Null if the current [FeatureState] of the [Friend] feature is unknown.
 * @property lowPower   Ability to help a node supporting the Low Power feature to operate by
 *                      storing messages destined for those nodes. Null if the current
 *                      [FeatureState] of the [LowPower] feature is unknown.
 */
@Serializable(with = FeaturesSerializer::class)
data class Features internal constructor(
    val relay: Relay? = null,
    val proxy: Proxy? = null,
    val friend: Friend? = null,
    val lowPower: LowPower? = null
) {

    val rawValue = ((relay?.state?.value ?: 2) shr 0 or
            (proxy?.state?.value ?: 2) shr 1 or
            (friend?.state?.value ?: 2) shr 2 or
            (lowPower?.state?.value ?: 2) shr 3).toUShort()

    /**
     * Constructs a Features object from the given raw value.
     *
     * @param rawValue  Raw value of the features.
     */
    internal constructor(rawValue: UShort) : this(
        relay = Relay(FeatureState.from(rawValue.toInt() shl 0)),
        proxy = Proxy(FeatureState.from(rawValue.toInt() shl 1)),
        friend = Friend(FeatureState.from(rawValue.toInt() shl 2)),
        lowPower = LowPower(FeatureState.from(rawValue.toInt() shl 3))
    )

    /**
     * Converts the features to an array of [Feature]s.
     */
    fun toArray(): Array<Feature> = arrayOf(
        relay ?: Relay(FeatureState.Unsupported),
        proxy ?: Proxy(FeatureState.Unsupported),
        friend ?: Friend(FeatureState.Unsupported),
        lowPower ?: LowPower(FeatureState.Unsupported)
    )
}

/**
 * Represents a type feature.
 *
 * @property state    Defines the state of the feature.
 * @property rawValue Raw value of the feature state.
 */
@Serializable
sealed class Feature {
    abstract val state: FeatureState
    abstract val rawValue: UShort
}

/**
 * Relay feature is the ability to receive and retransmit mesh messages over the advertising
 * bearer to enable larger networks.
 *
 * @property state State of the relay feature.
 */
@Serializable
data class Relay internal constructor(override val state: FeatureState) : Feature() {
    override val rawValue: UShort = (state.value shr 0).toUShort()
}

/**
 * Proxy feature is the ability to receive and retransmit mesh messages between GATT and
 * advertising bearers.
 *
 * @property state State of the proxy feature.
 */
@Serializable
data class Proxy internal constructor(override val state: FeatureState) : Feature() {
    override val rawValue: UShort = (state.value shr 1).toUShort()
}

/**
 * Friend feature is the ability to operate within a mesh network at significantly
 * reduced receiver duty cycles only in conjunction with a node supporting the Friend feature.
 *
 * @property state State of friend feature.
 */
@Serializable
data class Friend internal constructor(override val state: FeatureState) : Feature() {
    override val rawValue: UShort = (state.value shr 2).toUShort()
}

/**
 * LowPower feature is the ability to help a node supporting the Low Power feature
 * to operate by storing messages destined for those nodes.
 *
 * @property state State of low power feature.
 */
@Serializable
data class LowPower internal constructor(override val state: FeatureState) : Feature() {
    override val rawValue: UShort = (state.value shr 3).toUShort()
}

/**
 * FeatureState describes the state of a given [Feature].
 *
 * @property value 0 = disabled, 1 = enabled, 2 = unsupported
 */
@Serializable
sealed class FeatureState(val value: Int) {

    /** Disabled state. */
    @Serializable
    data object Disabled : FeatureState(value = DISABLED)

    /** Enabled state. */
    @Serializable
    data object Enabled : FeatureState(value = ENABLED)

    /** Unsupported state. */
    @Serializable
    data object Unsupported : FeatureState(value = UNSUPPORTED)

    companion object {

        private const val DISABLED = 0
        private const val ENABLED = 1
        private const val UNSUPPORTED = 2

        /**
         * Returns the feature state for a given a feature.
         *
         * @param value                         Integer value describing the state.
         * @throws IllegalArgumentException     if the feature value is not 0, 1 or 2.
         */
        @Throws(IllegalArgumentException::class)
        fun from(value: Int) = when (value) {
            DISABLED -> Disabled
            ENABLED -> Enabled
            UNSUPPORTED -> Unsupported
            else -> throw IllegalArgumentException(
                "Feature value should be from $DISABLED, $ENABLED or $UNSUPPORTED!"
            )
        }
    }
}

/**
 * Converts an array of [Feature]s to a raw value.
 */
fun Array<Feature>.toUShort(): UShort {
    var rawValue: UShort = 0u
    for (feature in this) {
        rawValue = rawValue or feature.rawValue
    }
    return rawValue
}

/**
 * Converts an array of Features to a [Features] object.
 */
fun Array<Feature>.toFeatures() = Features(
    relay = this[0] as Relay,
    proxy = this[1] as Proxy,
    friend = this[2] as Friend,
    lowPower = this[3] as LowPower
)