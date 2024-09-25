package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage.Companion.decodeNetKeyIndex
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NodeIdentityState

/**
 * Defines message sent to set the current Node Identity state. [ConfigNodeIdentityStatus]
 * will be the response to this message.
 *
 * @property identityState Node Identity state.
 */
class ConfigNodeIdentitySet(
    override val networkKeyIndex: KeyIndex,
    val identityState: NodeIdentityState
) : AcknowledgedConfigMessage, ConfigNetKeyMessage {
    override val opCode = Initializer.opCode
    override val parameters = encodeNetKeyIndex()
    override val responseOpCode: UInt = ConfigNodeIdentityStatus.opCode

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String =
        "ConfigNodeIdentityGet(opCode: 0x${opCode.toHexString()}, parameters: $parameters)"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x8047u

        override fun init(parameters: ByteArray?) = parameters
            ?.takeIf { it.size == 2 }
            ?.let {
                ConfigNodeIdentitySet(
                    networkKeyIndex = decodeNetKeyIndex(data = it, offset = 0),
                    identityState = NodeIdentityState.from(it.first().toUByte())
                )
            }
    }
}