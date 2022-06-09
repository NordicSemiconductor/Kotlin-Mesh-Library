package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import no.nordicsemi.kotlin.mesh.core.exception.ImportError
import no.nordicsemi.kotlin.mesh.core.model.Credentials

/**
 * Custom JSON serializer/deserializer for Credentials.
 */
internal object CredentialsSerializer : KSerializer<Credentials> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "Credentials", kind = PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Credentials = try {
        Credentials.from(decoder.decodeInt())
    } catch (ex: Exception) {
        throw ImportError(
            "Error while deserializing Credentials " +
                    "${(decoder as JsonDecoder).decodeJsonElement()}", ex
        )
    }


    override fun serialize(encoder: Encoder, value: Credentials) {
        encoder.encodeInt(value = value.credential)
    }
}