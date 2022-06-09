package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import no.nordicsemi.kotlin.mesh.core.exception.ImportError
import no.nordicsemi.kotlin.mesh.core.model.Security

/**
 * Custom JSON serializer/deserializer for Security.
 */
internal object SecuritySerializer : KSerializer<Security> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "Security", kind = PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Security = try {
        Security.from(decoder.decodeString())
    } catch (ex: Exception) {
        throw ImportError(
            "Error while deserializing Security " +
                    "${(decoder as JsonDecoder).decodeJsonElement()}", ex
        )
    }


    override fun serialize(encoder: Encoder, value: Security) {
        encoder.encodeString(value = value.value)
    }
}