package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import no.nordicsemi.kotlin.mesh.core.exception.ImportError
import no.nordicsemi.kotlin.mesh.core.model.ModelId
import no.nordicsemi.kotlin.mesh.core.model.ModelId.Companion.decode

/**
 * Custom JSON serializer/deserializer for ModelID.
 */
internal object ModelIdSerializer : KSerializer<ModelId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "ModelId", kind = PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ModelId = runCatching {
        decoder.decodeString().decode()
    }.getOrElse {
        throw ImportError(
            "Error while deserializing model id " +
                    "${(decoder as JsonDecoder).decodeJsonElement()}", it
        )
    }

    override fun serialize(encoder: Encoder, value: ModelId) {
        encoder.encodeString(value = value.toHex())
    }
}