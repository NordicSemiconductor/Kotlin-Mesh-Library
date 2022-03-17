package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

/**
 * Custom serializer/deserializer for UUID. Mesh Configuration Database contains certain UUIDs
 * as string with and without dashes and this Helper class encodes and decodes them accordingly.
 */

internal object UUIDSerializer : KSerializer<UUID> {

    private val HEX_UUID_PATTERN = Regex("[0-9a-fA-F]{32}")

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(serialName = "UUID", kind = PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID =
        decode(uuid = decoder.decodeString())

    override fun serialize(encoder: Encoder, value: UUID) =
        encoder.encodeString(value = value.toString().uppercase())

    /**
     * Drops the dashes in the UUID.
     *
     * @return a UUID string without dashes.
     */
    internal fun encode(uuid: UUID) = uuid.toString().uppercase().filter { it.isLetterOrDigit() }

    /**
     * Formats a UUID string to a standard UUID format.
     */
    internal fun decode(uuid: String) = UUID.fromString((uuid.uppercase().takeIf {
        HEX_UUID_PATTERN.matches(it)
    }?.run {
        StringBuilder(this).apply {
            insert(8, "-")
            insert(13, "-")
            insert(18, "-")
            insert(23, "-")
        }.toString()
    } ?: uuid))
}
