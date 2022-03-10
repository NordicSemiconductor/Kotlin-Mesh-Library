package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.nordicsemi.kotlin.mesh.core.model.ModelId
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId

/**
 * Custom JSON serializer/deserializer for Security.
 */
object ModelIdSerializer : KSerializer<ModelId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "ModelId", kind = PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ModelId {
        val modelId = decoder.decodeString().toUInt(radix = 16)
        return when (modelId and 0xFFFF0000u) {
            0u -> SigModelId(modelIdentifier = modelId.toUShort())
            else -> VendorModelId(
                modelIdentifier = (modelId and 0x0000FFFFu).toUShort(),
                companyIdentifier = ((modelId and 0xFFFF0000u) shr 16).toUShort()
            )
        }
    }

    override fun serialize(encoder: Encoder, value: ModelId) {
        TODO("Not yet implemented")
    }

    fun isSigModel(modelId: UInt){

    }
}