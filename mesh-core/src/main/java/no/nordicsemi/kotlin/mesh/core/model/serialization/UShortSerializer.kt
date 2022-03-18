package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.nordicsemi.kotlin.mesh.core.model.toHex

/**
 * Custom JSON serializer/deserializer for all properties formatted as a 4-digit hexadecimal string.
 */
internal object UShortSerializer : KSerializer<UShort> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "UShort", kind = PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UShort =
        decoder.decodeString().toUInt(radix = 16).toUShort()

    override fun serialize(encoder: Encoder, value: UShort) {
        encoder.encodeString(value = value.toHex())
    }
}