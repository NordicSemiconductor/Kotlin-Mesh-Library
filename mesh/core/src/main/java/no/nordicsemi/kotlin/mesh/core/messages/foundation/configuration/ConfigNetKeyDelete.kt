@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage.Companion.decodeNetKeyIndex
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

/**
 * This message is used to delete a network key from the mesh network.
 *
 * @property networkKeyIndex      Index of the network key to be deleted.
 * @property opCode               Message op code.
 * @property parameters           Message parameters.
 * @property responseOpCode       Op Code of the response message.
 * @constructor Constructs the ConfigNetKeyDelete message.
 */
data class ConfigNetKeyDelete(
    override val networkKeyIndex: KeyIndex
) : AcknowledgedConfigMessage, ConfigNetKeyMessage {
    override val opCode: UInt = Initializer.opCode
    override val parameters: ByteArray
        get() = encodeNetKeyIndex()

    override val responseOpCode = ConfigNetKeyStatus.opCode

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x8041u
        override fun init(parameters: ByteArray) = if (parameters.size == 2) {
            ConfigNetKeyDelete(networkKeyIndex = decodeNetKeyIndex(data = parameters, offset = 0))
        } else null
    }
}