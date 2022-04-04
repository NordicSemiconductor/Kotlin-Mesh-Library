package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class DurationToIntSerializer : KSerializer<Duration> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Duration", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Duration =
        decoder.decodeInt().toDuration(DurationUnit.MILLISECONDS)

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeInt(value = value.toInt(DurationUnit.MILLISECONDS))
    }
}