@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.data.getUShort
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigAnyModelMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId

/**
 * This message is used to get the publication state of a model.
 */
data class ConfigModelPublicationGet(
    override val elementAddress: UnicastAddress,
    override val modelIdentifier: UShort,
    override val companyIdentifier: UShort?
) : AcknowledgedConfigMessage, ConfigAnyModelMessage {

    override val opCode: UInt = Initializer.opCode
    override val responseOpCode: UInt = ConfigModelPublicationStatus.opCode

    override val parameters: ByteArray
        get() = elementAddress.address.toByteArray() + (companyIdentifier?.let {
            it.toByteArray() + modelIdentifier.toByteArray()
        } ?: modelIdentifier.toByteArray())

    companion object Initializer : ConfigMessageInitializer {
        override val opCode: UInt = 0x8018u

        override fun init(parameters: ByteArray?) = parameters?.takeIf {
            it.size == 4 || it.size == 6
        }?.let {
            val elementAddress = UnicastAddress(parameters.getUShort(0))
            var companyIdentifier: UShort? = null
            val modelIdentifier: UShort
            if (parameters.size == 6) {
                companyIdentifier = parameters.getUShort(2)
                modelIdentifier = parameters.getUShort(4)
            } else {
                modelIdentifier = parameters.getUShort(2)
            }
            ConfigModelPublicationGet(
                elementAddress = elementAddress,
                modelIdentifier = modelIdentifier,
                companyIdentifier = companyIdentifier
            )
        }

        /**
         * Constructs the ConfigModelPublicationGet message.
         *
         * @param model Model to get the publication state for.
         * @return A ConfigModelPublicationGet message.
         */
        fun init(model: Model): ConfigModelPublicationGet? {
            val elementAddress = requireNotNull(model.parentElement?.unicastAddress) {
                return null
            }
            val modelId = model.modelId
            return ConfigModelPublicationGet(
                elementAddress = elementAddress,
                companyIdentifier = when (modelId) {
                    is VendorModelId -> modelId.companyIdentifier
                    else -> null
                },
                modelIdentifier = when (modelId) {
                    is SigModelId -> modelId.modelIdentifier
                    is VendorModelId -> modelId.modelIdentifier
                }
            )
        }
    }

}