package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.nordicsemi.kotlin.mesh.core.model.toHex

/**
 * Custom JSON serializer/deserializer for Identifier.
 */
object IdentifiersSerializer : KSerializer<UShort?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "Identifiers", kind = PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UShort =
        decoder.decodeString().toUInt(radix = 16).toUShort()

    override fun serialize(encoder: Encoder, value: UShort?) {
        value?.let {
            encoder.encodeString(it.toHex())
        }
    }
}