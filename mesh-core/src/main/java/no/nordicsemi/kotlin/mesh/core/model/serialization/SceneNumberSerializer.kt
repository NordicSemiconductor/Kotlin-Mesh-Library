package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber
import no.nordicsemi.kotlin.mesh.core.model.toHex

/**
 * Custom JSON serializer/deserializer for Scene number property of a Scene.
 */
internal object SceneNumberSerializer : KSerializer<SceneNumber> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "SceneNumber", kind = PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): SceneNumber =
        decoder.decodeString().toUInt(radix = 16).toUShort()

    override fun serialize(encoder: Encoder, value: SceneNumber) {
        encoder.encodeString(value = value.toHex())
    }
}