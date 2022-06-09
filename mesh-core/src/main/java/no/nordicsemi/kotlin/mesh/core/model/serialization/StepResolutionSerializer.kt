package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import no.nordicsemi.kotlin.mesh.core.exception.ImportError
import no.nordicsemi.kotlin.mesh.core.model.StepResolution

/**
 * Custom JSON serializer/deserializer for StepResolution.
 */
internal object StepResolutionSerializer : KSerializer<StepResolution> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "StepResolution", kind = PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): StepResolution = try {
        StepResolution.from((decoder.decodeInt()))
    } catch (ex: Exception) {
        throw ImportError(
            "Error while deserializing publish period Step Resolution " +
                    "${(decoder as JsonDecoder).decodeJsonElement()}", ex
        )
    }

    override fun serialize(encoder: Encoder, value: StepResolution) {
        encoder.encodeLong(
            value.toMilliseconds(StepResolution.HUNDREDS_OF_MILLISECONDS.value)
                .inWholeMilliseconds
        )
    }
}