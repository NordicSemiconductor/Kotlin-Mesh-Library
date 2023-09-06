@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.foundation

import no.nordicsemi.kotlin.mesh.core.messages.BaseMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageDecoder
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageStatus
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage.Companion.decodeNetKeyIndex
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage.Companion.encodeNetKeyIndex
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.ConfigStatusMessage
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.util.Utils.toByteArray

/**
 * Status declaring if the the [ConfigNetKeyMessage] operation succeeded or not.
 *
 * @property opCode           Message op code.
 * @property networkKeyIndex  Index of the network key.
 * @property parameters       Message parameters.
 * @property status           Status of the message.
 * @constructor Constructs the ConfigNetKeyStatus message.
 */
data class ConfigNetKeyStatus(
    override val networkKeyIndex: KeyIndex,
    override val status: ConfigMessageStatus
) : ConfigResponse, ConfigStatusMessage, ConfigNetKeyMessage {

    override val opCode = Decoder.opCode
    override val parameters: ByteArray
        get() = status.value.toByteArray() + encodeNetKeyIndex(networkKeyIndex)

    /**
     * Constructs the ConfigNetKeyStatus message.
     *
     * @param request ConfigNetKeyMessage operation that was sent to the mesh node.
     * @param status  Status of the message.
     * @constructor Constructs the ConfigNetKeyStatus message.
     */
    constructor(
        request: ConfigNetKeyMessage,
        status: ConfigMessageStatus
    ) : this(request.networkKeyIndex, status)

    constructor(networkKey: NetworkKey) : this(networkKey.index, ConfigMessageStatus.SUCCESS)

    companion object Decoder : ConfigMessageDecoder {

        override val opCode = 0x8044u

        override fun decode(payload: ByteArray): BaseMeshMessage? = if (payload.size == 3) {
            ConfigMessageStatus.from(payload.first().toUByte())?.let {
                ConfigNetKeyStatus(
                    networkKeyIndex = decodeNetKeyIndex(payload, 1), status = it
                )
            }
        } else null
    }
}