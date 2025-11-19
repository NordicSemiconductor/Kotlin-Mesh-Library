package no.nordicsemi.android.nrfmesh.core.data.meshnetwork.vendor

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedVendorMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessageSecurity
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId


/**
 * Implementation of Acknowledged Vendor Message.
 *
 * @param modelId The Vendor Model Identifier.
 * @param sixBitOpcode The 6-bit opcode defined for the message.
 * @param sixBitResponseOpCode The 6-bit opcode defined for the response message.
 * @param parameters The parameters for the message.
 * @param isSegmented Boolean to determine whether the message is segmented.
 * @param security The security level of the message.
 */
class AcknowledgedVendorMessageImpl(
    modelId: VendorModelId,
    sixBitOpcode: UByte,
    sixBitResponseOpCode: UByte,
    override val parameters: ByteArray?,
    override val isSegmented: Boolean = false,
    override val security: MeshMessageSecurity = MeshMessageSecurity.Low,
) : AcknowledgedVendorMessage {
    override val opCode =
        ((0xC0u or sixBitOpcode.toUInt()) shl 16) or
                modelId.companyIdentifier.toUInt()
    override val responseOpCode =
        ((0xC0u or sixBitResponseOpCode.toUInt()) shl 16) or
                modelId.companyIdentifier.toUInt()

    override fun toString() = "RuntimeAcknowledgedVendorMessage(" +
            "opCode: 0x${opCode.toString(16)}, " +
            "parameters:${parameters?.toHexString()}, " +
            "responseOpCode: 0x${responseOpCode.toHexString()}, " +
            "isSegmented: $isSegmented, " +
            "security: $security)"
}