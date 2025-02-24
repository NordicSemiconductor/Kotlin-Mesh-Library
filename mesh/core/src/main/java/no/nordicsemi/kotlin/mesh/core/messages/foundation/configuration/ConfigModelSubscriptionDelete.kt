package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.data.getUShort
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigAddressMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigAnyModelMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId
import java.nio.ByteOrder

/**
 * This message is used to delete subscription from a model.
 *
 * @property address           Group address to be deleted from subscriptions.
 * @property elementAddress    Element address of the model.
 * @property modelIdentifier   Model identifier.
 * @property companyIdentifier Company identifier, if the model is a vendor model.
 */
class ConfigModelSubscriptionDelete(
    override val address: Address,
    override val elementAddress: UnicastAddress,
    override val modelIdentifier: UShort,
    override val companyIdentifier: UShort?,
) : AcknowledgedConfigMessage, ConfigAddressMessage, ConfigAnyModelMessage {
    override val opCode = Initializer.opCode
    override val responseOpCode = ConfigModelSubscriptionStatus.opCode
    override val parameters: ByteArray
        get() {
            val data = byteArrayOf() +
                    elementAddress.address.toByteArray(order = ByteOrder.LITTLE_ENDIAN) +
                    address.toByteArray(order = ByteOrder.LITTLE_ENDIAN)
            return data.plus(elements = companyIdentifier?.let {
                modelIdentifier.toByteArray(order = ByteOrder.LITTLE_ENDIAN) +
                        it.toByteArray(order = ByteOrder.LITTLE_ENDIAN)
            } ?: modelIdentifier.toByteArray(order = ByteOrder.LITTLE_ENDIAN))
        }

    /**
     * Convenience constructor to create a ConfigModelSubscriptionAdd message.
     *
     * @param group Group to add the model subscription to.
     * @param model Model to add the subscription to.
     * @throws IllegalArgumentException If the model does not have a parent element.
     */
    @Throws(IllegalArgumentException::class)
    constructor(group: Group, model: Model) : this(
        address = group.address.address,
        elementAddress = model.parentElement?.unicastAddress
            ?: throw IllegalArgumentException("Element address cannot be null"),
        modelIdentifier = when (model.modelId) {
            is SigModelId -> model.modelId.modelIdentifier
            is VendorModelId -> model.modelId.modelIdentifier
        },
        companyIdentifier = (model.modelId as? VendorModelId)?.companyIdentifier,
    )

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x801Cu

        override fun init(parameters: ByteArray?) = parameters?.takeIf {
            it.size == 6 || it.size == 8
        }?.let { params ->
            ConfigModelSubscriptionDelete(
                address = params.getUShort(offset = 1, order = ByteOrder.LITTLE_ENDIAN),
                elementAddress = UnicastAddress(
                    address = params.getUShort(offset = 3, order = ByteOrder.LITTLE_ENDIAN)
                ),
                modelIdentifier = params.getUShort(offset = 6, order = ByteOrder.LITTLE_ENDIAN),
                companyIdentifier = if (params.size == 8) params.getUShort(
                    offset = 8,
                    order = ByteOrder.LITTLE_ENDIAN
                ) else null
            )
        }
    }
}