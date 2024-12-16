@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.data.getUShort
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.data.toUuid
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
import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.experimental.or

/**
 * This message is used to set the publication state of a model.
 *
 * @property publish               Contains the publication state.
 */
data class ConfigModelPublicationVirtualAddressSet(
    override val companyIdentifier: UShort?,
    override val modelIdentifier: UShort,
    override val elementAddress: UnicastAddress,
    val publish: Publish,
) : AcknowledgedConfigMessage, ConfigAnyModelMessage {
    override val opCode: UInt = Initializer.opCode
    override val responseOpCode: UInt = ConfigModelPublicationStatus.opCode

    override val parameters: ByteArray
        get() {
            var data = elementAddress.address.toByteArray(order = ByteOrder.LITTLE_ENDIAN) +
                    publish.address.address.toByteArray(order = ByteOrder.LITTLE_ENDIAN)

            data += (publish.index and 0xFFu).toByte()
            data += (publish.index.toInt() shr 8).toByte() or
                    (publish.credentials.credential shl 4).toByte()
            data += publish.ttl.toByte()
            data += (publish.period.steps and 0x3Fu).toByte() or
                    (publish.period.resolution.value.toInt() shl 6).toByte()
            data += (publish.retransmit.count.toInt() and 0x07).toByte() or
                    (publish.retransmit.steps.toInt() shl 3).toByte()
            data += companyIdentifier?.toByteArray(order = ByteOrder.LITTLE_ENDIAN)
                ?.plus(modelIdentifier.toByteArray(order = ByteOrder.LITTLE_ENDIAN))
                ?: modelIdentifier.toByteArray()
            return data
        }

    /**
     * Convenience constructor to create the ConfigModelPublicationSet message.
     *
     * @param model Model to get the publication state for.
     * @throws IllegalArgumentException if the element address is not set.
     */
    @Throws(IllegalArgumentException::class)
    constructor(publish: Publish, model: Model) : this(
        publish = if (publish.address is VirtualAddress) publish else throw IllegalArgumentException(
            "Address must be VirtualAddress or consider sending ConfigModelPublicationSet"
        ),
        elementAddress = model.parentElement?.unicastAddress
            ?: throw IllegalArgumentException("Element address cannot be null"),
        modelIdentifier = when (model.modelId) {
            is SigModelId -> model.modelId.modelIdentifier
            is VendorModelId -> model.modelId.modelIdentifier
        },
        companyIdentifier = (model.modelId as? VendorModelId)?.companyIdentifier
    )

    companion object Initializer : ConfigMessageInitializer {
        override val opCode: UInt = 0x801Au

        override fun init(parameters: ByteArray?) = parameters?.takeIf {
            it.size == 25 || it.size == 27
        }?.let { params ->
            val elementAddress = params.getUShort(offset = 0, order = ByteOrder.LITTLE_ENDIAN)
            val label = VirtualAddress(params.sliceArray(2 until 17).toUuid())
            val index = params.getUShort(offset = 18, order = ByteOrder.LITTLE_ENDIAN) and 0x0FFFu
            val flag = (params.getUShort(offset = 19) and 0x10u).toInt() shr 4
            val ttl = params[20].toUByte()
            val periodSteps = (params.getUShort(offset = 21) and 0x3Fu).toUByte()
            val periodResolution = StepResolution.from((params[21].toInt() shr 6))
            val count = (params[22] and 0x07).toUByte()
            val intervalSteps = (params[22].toInt() shr 3).toUByte()

            val publish = Publish(
                address = label,
                index = index,
                credentials = Credentials.from(flag),
                ttl = ttl,
                period = PublishPeriod(periodSteps, periodResolution),
                retransmit = Retransmit(count = count, intervalSteps = intervalSteps)
            )

            if (params.size == 27) {
                ConfigModelPublicationVirtualAddressSet(
                    publish = publish,
                    companyIdentifier = params.getUShort(
                        offset = 23,
                        order = ByteOrder.LITTLE_ENDIAN
                    ),
                    modelIdentifier = params.getUShort(
                        offset = 25,
                        order = ByteOrder.LITTLE_ENDIAN
                    ),
                    elementAddress = UnicastAddress(elementAddress)
                )
            } else {
                ConfigModelPublicationVirtualAddressSet(
                    publish = publish,
                    companyIdentifier = null,
                    modelIdentifier = params.getUShort(
                        offset = 23,
                        order = ByteOrder.LITTLE_ENDIAN
                    ),
                    elementAddress = UnicastAddress(elementAddress)
                )
            }
        }
    }
}