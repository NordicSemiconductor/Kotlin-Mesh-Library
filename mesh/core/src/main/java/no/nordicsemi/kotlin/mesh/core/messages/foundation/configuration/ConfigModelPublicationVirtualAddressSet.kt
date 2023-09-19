@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigAnyModelMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.model.Credentials
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.Publish
import no.nordicsemi.kotlin.mesh.core.model.PublishPeriod
import no.nordicsemi.kotlin.mesh.core.model.Retransmit
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.StepResolution
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId
import no.nordicsemi.kotlin.mesh.core.model.VirtualAddress
import no.nordicsemi.kotlin.mesh.core.util.Utils.toByteArray
import no.nordicsemi.kotlin.mesh.core.util.Utils.toUShort
import no.nordicsemi.kotlin.mesh.core.util.Utils.toUuid
import kotlin.experimental.and
import kotlin.experimental.or

/**
 * This message is used to set the publication state of a model.
 *
 * @property publish               Contains the publication state.
 */
data class ConfigModelPublicationVirtualAddressSet(
    val publish: Publish,
    override val companyIdentifier: UShort?,
    override val modelIdentifier: UShort,
    override val elementAddress: UnicastAddress,
) : AcknowledgedConfigMessage, ConfigAnyModelMessage {
    override val opCode: UInt = Initializer.opCode
    override val responseOpCode: UInt = ConfigModelPublicationStatus.opCode

    override val parameters: ByteArray
        get() {
            var data = elementAddress.address.toByteArray() +
                    publish.address.address.toByteArray()

            data += (publish.index and 0xFFu).toByte()
            data += (publish.index.toInt() shr 8).toByte() or
                    (publish.credentials.credential shl 4).toByte()
            data += publish.ttl.toByte()
            data += (publish.period.steps and 0x3Fu).toByte() or
                    (publish.period.resolution.value.toInt() shl 6).toByte()
            data += (publish.retransmit.count.toInt() and 0x07).toByte() or
                    (publish.retransmit.steps.toInt() shl 3).toByte()
            data += companyIdentifier?.let {
                it.toByteArray() + modelIdentifier.toByteArray()
            } ?: modelIdentifier.toByteArray()
            return data
        }


    companion object Initializer : ConfigMessageInitializer {
        override val opCode: UInt = 0x801Au

        /**
         * Constructs the ConfigModelPublicationSet message using the given parameters.
         *
         * @param publish          Publish settings.
         * @param model            Model with the Publish settings.
         * @return A ConfigModelPublicationSet message or null if parameters are invalid.
         */
        fun init(publish: Publish, model: Model): ConfigModelPublicationVirtualAddressSet? {
            require(publish.address is VirtualAddress) { return null }
            val elementAddress = model.parentElement?.unicastAddress ?: return null
            val modelId = model.modelId
            return ConfigModelPublicationVirtualAddressSet(
                publish = publish,
                companyIdentifier = when (modelId) {
                    is VendorModelId -> modelId.companyIdentifier
                    else -> null
                },
                modelIdentifier = when (modelId) {
                    is SigModelId -> modelId.modelIdentifier
                    is VendorModelId -> modelId.modelIdentifier
                },
                elementAddress = elementAddress
            )
        }

        /**
         * Constructs the ConfigModelPublicationSet message using the given model.
         *
         * @param model The model to set the publication for.
         * @return A ConfigModelPublicationSet message or null if parameters are invalid.
         */
        fun init(model: Model): ConfigModelPublicationVirtualAddressSet? = model.takeIf {
            it.parentElement?.unicastAddress != null
        }?.let {
            val modelId = model.modelId
            ConfigModelPublicationVirtualAddressSet(
                publish = Publish(),
                companyIdentifier = when (modelId) {
                    is VendorModelId -> modelId.companyIdentifier
                    else -> null
                },
                modelIdentifier = when (modelId) {
                    is SigModelId -> modelId.modelIdentifier
                    is VendorModelId -> modelId.modelIdentifier
                },
                elementAddress = it.parentElement!!.unicastAddress
            )
        }

        override fun init(parameters: ByteArray): ConfigModelPublicationVirtualAddressSet? {
            require(parameters.size == 25 || parameters.size == 27) { return null }


            val elementAddress = parameters.toUShort(offset = 0)
            val label = VirtualAddress(parameters.sliceArray(2 until 17).toUuid())
            val index = parameters.toUShort(18) and 0x0FFFu
            val flag = (parameters.toUShort(19) and 0x10u).toInt() shr 4
            val ttl = parameters[20].toUByte()
            val periodSteps = (parameters.toUShort(21) and 0x3Fu).toUByte()
            val periodResolution = StepResolution.from((parameters[21].toInt() shr 6))
            val count = (parameters[22] and 0x07).toUByte()
            val intervalSteps = (parameters[22].toInt() shr 3).toUByte()

            val publish = Publish(
                address = label,
                index = index,
                credentials = Credentials.from(flag),
                ttl = ttl,
                period = PublishPeriod(periodSteps, periodResolution),
                retransmit = Retransmit(count = count, intervalSteps = intervalSteps)
            )

            return if (parameters.size == 27) {
                ConfigModelPublicationVirtualAddressSet(
                    publish = publish,
                    companyIdentifier = parameters.toUShort(23),
                    modelIdentifier = parameters.toUShort(25),
                    elementAddress = UnicastAddress(elementAddress)
                )
            } else {
                ConfigModelPublicationVirtualAddressSet(
                    publish = publish,
                    companyIdentifier = null,
                    modelIdentifier = parameters.toUShort(23),
                    elementAddress = UnicastAddress(elementAddress)
                )
            }
        }
    }
}