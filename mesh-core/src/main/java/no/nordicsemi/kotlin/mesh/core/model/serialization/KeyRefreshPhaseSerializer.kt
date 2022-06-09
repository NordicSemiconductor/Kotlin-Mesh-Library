package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import no.nordicsemi.kotlin.mesh.core.exception.ImportError
import no.nordicsemi.kotlin.mesh.core.model.KeyRefreshPhase

/**
 * Custom JSON serializer/deserializer for Key refresh phases.
 */
internal object KeyRefreshPhaseSerializer : KSerializer<KeyRefreshPhase> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "Phase", kind = PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder) = runCatching {
        KeyRefreshPhase.from(decoder.decodeInt())
    }.getOrElse {
        throw ImportError(
            "Error while deserializing Key Refresh Phase " +
                    "${(decoder as JsonDecoder).decodeJsonElement()}", it
        )
    }


    override fun serialize(encoder: Encoder, value: KeyRefreshPhase) {
        encoder.encodeInt(value = value.phase)
    }
}