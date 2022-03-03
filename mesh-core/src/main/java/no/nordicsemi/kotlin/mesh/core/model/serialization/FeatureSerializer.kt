package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.nordicsemi.kotlin.mesh.core.model.*

/**
 * Custom serializer/deserializer for [Relay] feature.
 */
internal object RelaySerializer : KSerializer<Relay?> {

    override fun deserialize(decoder: Decoder): Relay? {
        TODO("Not yet implemented")
    }

    override val descriptor: SerialDescriptor
        get() = TODO("Not yet implemented")

    override fun serialize(encoder: Encoder, value: Relay?) {
        TODO("Not yet implemented")
    }
}

/**
 * Custom serializer/deserializer for [Proxy] feature.
 */
internal object ProxySerializer : KSerializer<Proxy?> {

    override fun deserialize(decoder: Decoder): Proxy? {
        TODO("Not yet implemented")
    }

    override val descriptor: SerialDescriptor
        get() = TODO("Not yet implemented")

    override fun serialize(encoder: Encoder, value: Proxy?) {
        TODO("Not yet implemented")
    }
}

/**
 * Custom serializer/deserializer for [Friend] feature.
 */
internal object FriendSerializer : KSerializer<Friend?> {

    override fun deserialize(decoder: Decoder): Friend? {
        TODO("Not yet implemented")
    }

    override val descriptor: SerialDescriptor
        get() = TODO("Not yet implemented")

    override fun serialize(encoder: Encoder, value: Friend?) {
        TODO("Not yet implemented")
    }
}

/**
 * Custom serializer/deserializer for [LowPower] feature.
 */
internal object LowPowerSerializer : KSerializer<LowPower?> {

    override fun deserialize(decoder: Decoder): LowPower? {
        TODO("Not yet implemented")
    }

    override val descriptor: SerialDescriptor
        get() = TODO("Not yet implemented")

    override fun serialize(encoder: Encoder, value: LowPower?) {
        TODO("Not yet implemented")
    }
}

internal object FeatureSerializer : KSerializer<Feature?> {

    override fun deserialize(decoder: Decoder): Feature? {
        TODO("Not yet implemented")
    }

    override val descriptor: SerialDescriptor
        get() = TODO("Not yet implemented")

    override fun serialize(encoder: Encoder, value: Feature?) {
        TODO("Not yet implemented")
    }
}