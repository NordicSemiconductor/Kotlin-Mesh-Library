@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.BaseMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigAnyModelMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.ModelId
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId
import no.nordicsemi.kotlin.mesh.core.util.Utils.toByteArray
import no.nordicsemi.kotlin.mesh.core.util.Utils.toUShort

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
    override val modelId: ModelId
        get() = companyIdentifier?.let { VendorModelId(modelIdentifier, it) }
            ?: SigModelId(modelIdentifier)

    override val parameters: ByteArray
        get() = elementAddress.address.toByteArray() + (companyIdentifier?.let {
            it.toByteArray() + modelIdentifier.toByteArray()
        } ?: modelIdentifier.toByteArray())

    companion object Initializer : ConfigMessageInitializer {
        override val opCode: UInt = 0x8018u

        override fun init(payload: ByteArray): BaseMeshMessage? {
            require(payload.size == 4 || payload.size == 6) { return null }
            val elementAddress = UnicastAddress(payload.toUShort(0))
            var companyIdentifier: UShort? = null
            val modelIdentifier: UShort
            if (payload.size == 6) {
                companyIdentifier = payload.toUShort(2)
                modelIdentifier = payload.toUShort(4)
            } else {
                modelIdentifier = payload.toUShort(2)
            }
            return ConfigModelPublicationGet(
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