package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.nordicsemi.kotlin.mesh.core.model.*

/**
 * Custom serializer/deserializer for [Relay] feature.
 */
internal object RelaySerializer : KSerializer<Relay?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "relay", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder) =
        Relay(featureState = FeatureState.from(decoder.decodeInt()))

    override fun serialize(encoder: Encoder, value: Relay?) {
        value?.let { relay -> encoder.encodeInt(relay.featureState.state) }
    }
}

/**
 * Custom serializer/deserializer for [Proxy] feature.
 */
internal object ProxySerializer : KSerializer<Proxy?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "proxy", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder) =
        Proxy(featureState = FeatureState.from(decoder.decodeInt()))

    override fun serialize(encoder: Encoder, value: Proxy?) {
        value?.let { proxy -> encoder.encodeInt(proxy.featureState.state) }
    }
}

/**
 * Custom serializer/deserializer for [Friend] feature.
 */
internal object FriendSerializer : KSerializer<Friend?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "friend", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Friend =
        Friend(featureState = FeatureState.from(decoder.decodeInt()))

    override fun serialize(encoder: Encoder, value: Friend?) {
        value?.let { friend -> encoder.encodeInt(friend.featureState.state) }
    }
}

/**
 * Custom serializer/deserializer for [LowPower] feature.
 */
internal object LowPowerSerializer : KSerializer<LowPower?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "lowPower", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): LowPower =
        LowPower(featureState = FeatureState.from(decoder.decodeInt()))

    override fun serialize(encoder: Encoder, value: LowPower?) {
        value?.let { lowPower -> encoder.encodeInt(lowPower.featureState.state) }
    }
}