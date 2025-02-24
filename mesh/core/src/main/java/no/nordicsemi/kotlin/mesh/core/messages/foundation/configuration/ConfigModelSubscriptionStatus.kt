package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.data.getUShort
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.mesh.core.messages.BaseMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigAddressMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigAnyModelMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageStatus
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.ConfigStatusMessage
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import java.nio.ByteOrder

/**
 * Status declaring if the the [ConfigModelSubscriptionStatus] operation succeeded or not.
 * This message is sent as a response to [ConfigModelSubscriptionAdd],
 * [ConfigModelSubscriptionDelete], [ConfigModelSubscriptionDeleteAll]
 * and [ConfigModelSubscriptionOverwrite].
 *
 * @property status           Status of the message.
 * @property address          Address of the group.
 * @property elementAddress   Element address of the model.
 * @property modelIdentifier  Model identifier.
 * @property companyIdentifier Company identifier, if the model is a vendor model.
 *
 */
class ConfigModelSubscriptionStatus(
    override val status: ConfigMessageStatus,
    override val address: Address,
    override val elementAddress: UnicastAddress,
    override val modelIdentifier: UShort,
    override val companyIdentifier: UShort?,
) : ConfigResponse, ConfigStatusMessage, ConfigAddressMessage, ConfigAnyModelMessage {
    override val opCode = Initializer.opCode
    override val parameters: ByteArray
        get() {
            val data = byteArrayOf(status.value.toByte()) +
                    elementAddress.address.toByteArray(order = ByteOrder.LITTLE_ENDIAN) +
                    address.toByteArray(order = ByteOrder.LITTLE_ENDIAN)
            return data.plus(elements = companyIdentifier?.let {
                modelIdentifier.toByteArray(order = ByteOrder.LITTLE_ENDIAN) +
                        it.toByteArray(order = ByteOrder.LITTLE_ENDIAN)
            } ?: modelIdentifier.toByteArray(order = ByteOrder.LITTLE_ENDIAN))
        }

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x801Fu

        override fun init(parameters: ByteArray?) = parameters?.takeIf {
            parameters.size == 7 || parameters.size == 9
        }?.let { params ->
            ConfigMessageStatus.from(value = params[0].toUByte())?.let { status ->
                val address = params.getUShort(offset = 1, order = ByteOrder.LITTLE_ENDIAN)
                val elementAddress = UnicastAddress(
                    address = params.getUShort(
                        offset = 3,
                        order = ByteOrder.LITTLE_ENDIAN
                    )
                )
                val modelIdentifier: UShort
                var companyIdentifier: UShort? = null
                if (params.size == 9) {
                    companyIdentifier =
                        params.getUShort(offset = 5, order = ByteOrder.LITTLE_ENDIAN)
                    modelIdentifier = params.getUShort(offset = 7, order = ByteOrder.LITTLE_ENDIAN)
                } else {
                    modelIdentifier = params.getUShort(offset = 5, order = ByteOrder.LITTLE_ENDIAN)
                }
                ConfigModelSubscriptionStatus(
                    status = status,
                    address = address,
                    elementAddress = elementAddress,
                    modelIdentifier = modelIdentifier,
                    companyIdentifier = companyIdentifier,
                )
            }
        }
    }
}