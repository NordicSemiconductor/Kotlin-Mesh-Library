@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigAnyModelMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.model.Credentials
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.ModelId
import no.nordicsemi.kotlin.mesh.core.model.PublicationAddress
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
import kotlin.experimental.and
import kotlin.experimental.or

/**
 * This message is used to set the publication state of a model.
 *
 * @property publish               Contains the publication state.
 */
data class ConfigModelPublicationSet(
    val publish: Publish,
    override val companyIdentifier: UShort?,
    override val modelIdentifier: UShort,
    override val elementAddress: UnicastAddress,
) : AcknowledgedConfigMessage, ConfigAnyModelMessage {
    override val opCode: UInt = Initializer.opCode
    override val responseOpCode: UInt = ConfigModelPublicationStatus.opCode

    override val modelId: ModelId = if (companyIdentifier == null) {
        SigModelId(modelIdentifier)
    } else {
        VendorModelId(modelIdentifier = modelIdentifier, companyIdentifier = companyIdentifier)
    }
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
            data += (publish.retransmit.count.toInt() shl 3).toByte() or
                    (publish.retransmit.steps.toInt() shl 3).toByte()
            data += companyIdentifier?.let {
                it.toByteArray() + modelIdentifier.toByteArray()
            } ?: modelIdentifier.toByteArray()
            return data
        }


    companion object Initializer : ConfigMessageInitializer {
        override val opCode: UInt = 0x03u

        /**
         * Constructs the ConfigModelPublicationSet message using the given parameters.
         *
         * @param publish Publish state to set.
         * @param model   Model to set the publication for.
         */
        fun init(publish: Publish, model: Model): ConfigModelPublicationSet? {
            require(publish.address !is VirtualAddress) { return null }
            val elementAddress = model.parentElement?.unicastAddress ?: return null
            val modelId = model.modelId
            return ConfigModelPublicationSet(
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
        fun init(model: Model): ConfigModelPublicationSet? = model.takeIf {
            it.parentElement?.unicastAddress != null
        }?.let {
            val modelId = model.modelId
            ConfigModelPublicationSet(
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

        /**
         * Constructs the ConfigModelPublicationSet message using the given parameters.
         *
         * @param payload The message parameters.
         * @return A ConfigModelPublicationSet message or null if parameters are invalid.
         */
        override fun init(payload: ByteArray): ConfigModelPublicationSet? {
            require(payload.size == 11 || payload.size == 13) { return null }

            val elementAddress = payload.toUShort(offset = 0)
            val address = MeshAddress.create(payload.toUShort(2))
            val index = payload.toUShort(4) and 0x0FFFu
            val flag = (payload.toUShort(5) and 0x10u).toInt() shr 4
            val ttl = payload[6].toUByte()
            val periodSteps = (payload.toUShort(7) and 0x3Fu).toUByte()
            val periodResolution = StepResolution.from((payload[7].toInt() shr 6))
            val period = PublishPeriod(periodSteps, periodResolution)
            val count = (payload[8] and 0x07).toUByte()
            val intervalSteps = (payload[8].toInt() shr 3).toUByte()

            val retransmit = Retransmit(count = count, intervalSteps = intervalSteps)
            val publish = Publish(
                address = address as PublicationAddress,
                index = index,
                credentials = Credentials.from(flag),
                ttl = ttl,
                period = period,
                retransmit = retransmit
            )

            return if (payload.size == 13) {
                ConfigModelPublicationSet(
                    publish = publish,
                    companyIdentifier = payload.toUShort(9),
                    modelIdentifier = payload.toUShort(11),
                    elementAddress = UnicastAddress(elementAddress)
                )
            } else {
                ConfigModelPublicationSet(
                    publish = publish,
                    companyIdentifier = null,
                    modelIdentifier = payload.toUShort(9),
                    elementAddress = UnicastAddress(elementAddress)
                )
            }
        }
    }
}