@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.mesh.core.messages.BaseMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageStatus
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetAndAppKeyMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetAndAppKeyMessage.Companion.decodeNetAndAppKeyIndex
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetAndAppKeyMessage.Companion.encodeNetAndAppKeyIndex
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.ConfigStatusMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

/**
 * Status declaring if the the [ConfigNetAndAppKeyMessage] operation succeeded or not.
 *
 * @property keyIndex Index of the application key.
 * @property index     Index of the network key.
 * @property status              Status of the message.
 * @property opCode              Message op code.
 * @property parameters          Message parameters.
 * @constructor Constructs the ConfigAppKeyStatus message.
 */
class ConfigAppKeyStatus(
    override val keyIndex: KeyIndex,
    override val index: KeyIndex,
    override val status: ConfigMessageStatus
) : ConfigResponse, ConfigStatusMessage, ConfigNetAndAppKeyMessage {

    override val opCode = Initializer.opCode
    override val parameters: ByteArray
        get() = status.value.toByteArray() + encodeNetAndAppKeyIndex(
            appKeyIndex = keyIndex,
            netKeyIndex = index
        )

    /**
     * Constructs the ConfigAppKeyStatus message.
     *
     * @param applicationKey Application key to confirm
     * @constructor Constructs the ConfigAppKeyStatus message.
     */
    constructor(applicationKey: ApplicationKey) : this(
        keyIndex = applicationKey.index,
        index = applicationKey.boundNetKeyIndex,
        status = ConfigMessageStatus.SUCCESS
    )

    /**
     * Constructs the ConfigAppKeyStatus message.
     *
     * @param request [ConfigNetAndAppKeyMessage] operation that was sent to the mesh node.
     * @param status  [ConfigMessageStatus] for a given [request].
     * @constructor Constructs the ConfigAppKeyStatus message.
     */
    constructor(
        request: ConfigNetAndAppKeyMessage,
        status: ConfigMessageStatus
    ) : this(
        keyIndex = request.keyIndex,
        index = request.index,
        status = status
    )

    override fun toString() = "ConfigAppKeyStatus(applicationKeyIndex: $keyIndex, " +
            "networkKeyIndex: $index, status: $status)"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x8003u

        /**
         * Initializes the ConfigAppKeyStatus message.
         *
         * @param parameters Message parameters.
         * @return ConfigAppKeyStatus or null if the parameters are invalid.
         */
        override fun init(parameters: ByteArray?): BaseMeshMessage? = parameters?.takeIf {
            it.size == 4
        }?.let { params ->
            val status = ConfigMessageStatus.from(
                value = params.first().toUByte()
            ) ?: return null
            val decodedNetAndAppKeyIndex = decodeNetAndAppKeyIndex(data = params, offset = 1)
            ConfigAppKeyStatus(
                keyIndex = decodedNetAndAppKeyIndex.applicationKeyIndex,
                index = decodedNetAndAppKeyIndex.networkKeyIndex,
                status = status
            )
        }
    }
}