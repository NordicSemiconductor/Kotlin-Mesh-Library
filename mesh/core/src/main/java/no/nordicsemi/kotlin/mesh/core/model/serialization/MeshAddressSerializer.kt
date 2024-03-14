package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import no.nordicsemi.kotlin.mesh.core.exception.ImportError
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.UnassignedAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.VirtualAddress
import no.nordicsemi.kotlin.mesh.core.util.Utils

/**
 * Serializer used to serialize and deserialize address objects in mesh cdb json.
 */
internal object MeshAddressSerializer : KSerializer<MeshAddress> {
    override val descriptor =
        PrimitiveSerialDescriptor(serialName = "MeshAddress", kind = PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): MeshAddress = runCatching {
        parse(decoder.decodeString())
    }.getOrElse {
        throw ImportError(
            "Error while deserializing Address " +
                    "${(decoder as JsonDecoder).decodeJsonElement()}", it
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun serialize(encoder: Encoder, value: MeshAddress) {
        encoder.encodeString(
            value = when (value) {
                is VirtualAddress -> Utils.encode(uuid = value.uuid)
                else -> value.address.toHexString()
            }
        )
    }

    /**
     * Parses the 4-character or a 32-character hexadecimal string to a Mesh address.
     * @param hexAddress Hex address.
     */
    private fun parse(hexAddress: String): MeshAddress = hexAddress.takeIf {
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
        VirtualAddress(Utils.decode(uuid = hexAddress))
    }
}

