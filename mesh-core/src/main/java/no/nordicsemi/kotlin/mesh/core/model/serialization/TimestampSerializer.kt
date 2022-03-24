package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Custom serializer/deserializer for timestamp.
 */
internal object TimestampSerializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(serialName = "timestamp", kind = PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant = Instant.parse(isoString = decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value = value.toString())
    }
}
