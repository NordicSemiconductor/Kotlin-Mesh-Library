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
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId

/**
 * Custom JSON serializer/deserializer for ModelID.
 */
internal object ModelIdSerializer : KSerializer<ModelId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "ModelId", kind = PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ModelId = try {
        decoder.decodeString().toUInt(radix = 16).let { modelId ->
            when (modelId and 0xFFFF0000u) {
                0u -> SigModelId(modelIdentifier = modelId.toUShort())
                else -> VendorModelId(modelId = modelId)
            }
        }
    } catch (ex: Exception) {
        throw ImportError(
            "Error while deserializing model id " +
                    "${(decoder as JsonDecoder).decodeJsonElement()}", ex
        )
    }

    override fun serialize(encoder: Encoder, value: ModelId) {
        encoder.encodeString(value = value.toHex())
    }
}