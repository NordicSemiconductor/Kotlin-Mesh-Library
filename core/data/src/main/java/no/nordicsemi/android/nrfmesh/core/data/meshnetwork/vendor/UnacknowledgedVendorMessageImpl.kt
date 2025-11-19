package no.nordicsemi.android.nrfmesh.core.data.meshnetwork.vendor

import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedVendorMessage
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId


/**
 * Implementation of Unacknowledged Vendor Message.
 *
 * @param modelId The Vendor Model Identifier.
 * @param sixBitOpCode The 6-bit opcode defined for the message.
 * @param parameters The parameters for the message.
 */
class UnacknowledgedVendorMessageImpl(
    modelId: VendorModelId,
    sixBitOpCode: UByte,
    override val parameters: ByteArray?,
) : UnacknowledgedVendorMessage {

    override val opCode =
        ((0xC0u or sixBitOpCode.toUInt()) shl 16) or modelId.companyIdentifier.toUInt()

    override fun toString() = "RuntimeUnacknowledgedVendorMessage(" +
            "opCode: 0x${opCode.toString(16)}, " +
            "parameters:${parameters?.toHexString()}, " +
            "isSegmented: $isSegmented, " +
            "security: $security)"
}