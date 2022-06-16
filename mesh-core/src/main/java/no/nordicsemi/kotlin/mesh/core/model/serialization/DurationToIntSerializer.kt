package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import no.nordicsemi.kotlin.mesh.core.exception.ImportError
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Custom JSON serializer/deserializer for Duration that relates to time intervals.
 */
internal object DurationToIntSerializer : KSerializer<Duration> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Duration", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Duration = runCatching {
        decoder.decodeInt().toDuration(DurationUnit.MILLISECONDS)
    }.getOrElse {
        throw ImportError(
            "Error while deserializing retransmit interval " +
                    "${(decoder as JsonDecoder).decodeJsonElement()}", it
        )
    }

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeInt(value = value.toInt(DurationUnit.MILLISECONDS))
    }
}