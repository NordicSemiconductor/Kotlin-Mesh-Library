package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.nordicsemi.kotlin.mesh.crypto.Utils.decodeHex
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

/**
 * Custom JSON serializer/deserializer for byte arrays.
 */
internal object KeySerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "Key", kind = PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder) =
        decoder.decodeString().decodeHex()

    override fun serialize(encoder: Encoder, value: ByteArray) {
        encoder.encodeString(value.encodeHex())
    }
}