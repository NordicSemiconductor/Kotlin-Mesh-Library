package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.nordicsemi.kotlin.mesh.core.model.*

/**
 * Serializer used to serialize and deserialize address objects in mesh cdb json.
 */
internal object MeshAddressSerializer : KSerializer<MeshAddress> {
    override val descriptor =
        PrimitiveSerialDescriptor(serialName = "MeshAddress", kind = PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): MeshAddress = parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: MeshAddress) {
        encoder.encodeString(value = when(value) {
            is VirtualAddress -> {UUIDSerializer.encode(uuid = value.uuid)}
            else -> { value.address.toHex()}
        })
    }

    /**
     * Parses the 4-character or a 32-character hexadecimal string to a Mesh address.
     * @param hexAddress Hex address.
     */
    private fun parse(hexAddress: String) = hexAddress.takeIf {
        it.length == 4
    }?.let { it ->
        val address = it.toUInt(16).toUShort()
        when {
            UnassignedAddress.isValid(address = address) -> UnassignedAddress
            UnicastAddress.isValid(address = address) -> UnicastAddress(address = address)
            GroupAddress.isValid(address = address) -> GroupAddress(address = address)
            else -> throw IllegalArgumentException("Error while parsing address!")
        }
    } ?: run {
        VirtualAddress(UUIDSerializer.decode(uuid = hexAddress))
    }
}

