package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.data.getShort
import no.nordicsemi.kotlin.data.getUShort
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageStatus
import no.nordicsemi.kotlin.mesh.core.messages.ConfigModelMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigModelSubscriptionList
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.ConfigStatusMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigVendorModelMessage
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId
import java.nio.ByteOrder

/**
 * This message is the status message to the [ConfigVendorModelSubscriptionGet] message. This lists
 * the subscription list of a vendor model.
 */
class ConfigVendorModelSubscriptionList(
    override val status: ConfigMessageStatus,
    override val elementAddress: UnicastAddress,
    override val modelId: VendorModelId,
    override val addresses: List<Address>,
) : ConfigResponse, ConfigStatusMessage, ConfigModelSubscriptionList, ConfigVendorModelMessage {
    override val opCode = Initializer.opCode
    override val modelIdentifier = modelId.modelIdentifier
    override val companyIdentifier = modelId.companyIdentifier
    override val parameters: ByteArray
        get() = byteArrayOf() +
                elementAddress.address.toByteArray(order = ByteOrder.LITTLE_ENDIAN) +
                modelIdentifier.toByteArray(order = ByteOrder.LITTLE_ENDIAN)

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x802Au

        override fun init(parameters: ByteArray?) = parameters?.takeIf {
            it.size >= 7
        }?.let { params ->
            ConfigMessageStatus.from(params[0].toUByte())?.let { status ->
                ConfigVendorModelSubscriptionList(
                    status = status,
                    elementAddress = UnicastAddress(
                        address = params.getUShort(offset = 1, order = ByteOrder.LITTLE_ENDIAN)
                    ),
                    modelId = VendorModelId(
                        companyIdentifier = params.getUShort(3, ByteOrder.LITTLE_ENDIAN),
                        modelIdentifier = params.getUShort(5, ByteOrder.LITTLE_ENDIAN)
                    ),
                    addresses = mutableListOf<Address>().apply {
                        var index = 7
                        while (index < params.size) {
                            add((params.getUShort(offset = index, order = ByteOrder.LITTLE_ENDIAN)))
                            index += 2
                        }
                    }
                )
            }
        }
    }
}