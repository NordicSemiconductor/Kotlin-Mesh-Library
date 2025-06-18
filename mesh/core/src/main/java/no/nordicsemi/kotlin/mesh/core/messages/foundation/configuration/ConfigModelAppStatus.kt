@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.data.getUInt
import no.nordicsemi.kotlin.data.getUShort
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.mesh.core.messages.BaseMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigAnyAppKeyModelMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigAnyModelMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigAppKeyMessage.Companion.decodeAppKeyIndex
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageStatus
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.ConfigStatusMessage
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.ModelId
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId
import java.nio.ByteOrder

/**
 * Status declaring if the the [ConfigModelAppStatus] operation succeeded or not.
 *
 * @constructor Constructs the ConfigAppKeyStatus message.
 */
class ConfigModelAppStatus(
    override val status: ConfigMessageStatus,
    override val keyIndex: KeyIndex,
    override val elementAddress: UnicastAddress,
    override val modelId: ModelId,
) : ConfigResponse, ConfigStatusMessage, ConfigAnyAppKeyModelMessage, ConfigAnyModelMessage {
    override val opCode = Initializer.opCode

    override val parameters: ByteArray
        get() = status.value.toByteArray() +
                elementAddress.address.toByteArray(order = ByteOrder.LITTLE_ENDIAN) +
                encodeAppKeyIndex(applicationKeyIndex = keyIndex) +
                when (modelId) {
                    is SigModelId -> modelId.modelIdentifier.toByteArray(order = ByteOrder.LITTLE_ENDIAN)
                    is VendorModelId -> modelId.id.toByteArray(order = ByteOrder.LITTLE_ENDIAN)
                }

    override val modelIdentifier: UShort = when {
        modelId.isBluetoothSigAssigned -> (modelId as SigModelId).modelIdentifier
        else -> (modelId as VendorModelId).id.toUShort()
    }

    override val companyIdentifier: UShort?
        get() = when (modelId.isBluetoothSigAssigned) {
            true -> null
            else -> (modelId as VendorModelId).companyIdentifier
        }

    /**
     * Constructs the ConfigAppBindStatus message.
     *
     * @param request [ConfigAnyAppKeyModelMessage] operation that was sent to the mesh node.
     */
    constructor(request: ConfigAnyAppKeyModelMessage) : this(
        request = request,
        status = ConfigMessageStatus.SUCCESS
    )

    /**
     * Constructs the ConfigAppBindStatus message with the given request and the status
     *
     * @param request [ConfigAnyAppKeyModelMessage] operation that was sent to the mesh node.
     * @param status  [ConfigMessageStatus] for a given [request].
     */
    constructor(request: ConfigAnyAppKeyModelMessage, status: ConfigMessageStatus) : this(
        status = status,
        keyIndex = request.keyIndex,
        elementAddress = request.elementAddress,
        modelId = request.modelId
    )

    override fun toString() = "ConfigModelAppStatus(status: ${status}, " +
            "applicationKeyIndex: $keyIndex, " +
            "elementAddress: ${elementAddress.toHexString()}, " +
            "modelId: $modelId)"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x803Eu

        /**
         * Initializes the ConfigAppKeyStatus message.
         *
         * @param parameters Message parameters.
         * @return ConfigAppKeyStatus or null if the parameters are invalid.
         */
        override fun init(parameters: ByteArray?): BaseMeshMessage? = parameters?.takeIf {
            it.size == 7 || it.size == 9
        }?.let { params ->
            val status = ConfigMessageStatus
                .from(value = params.first().toUByte()) ?: return null
            ConfigModelAppStatus(
                status = status,
                elementAddress = UnicastAddress(
                    address = params.getUShort(
                        offset = 1,
                        order = ByteOrder.LITTLE_ENDIAN
                    )
                ),
                keyIndex = decodeAppKeyIndex(data = params, offset = 3),
                modelId = when (params.size) {
                    9 -> VendorModelId(
                        id = params.getUInt(
                            offset = 5,
                            order = ByteOrder.LITTLE_ENDIAN
                        )
                    )

                    else -> SigModelId(
                        modelIdentifier = params.getUShort(
                            offset = 5,
                            order = ByteOrder.LITTLE_ENDIAN
                        )
                    )
                }
            )
        }
    }
}