package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageStatus
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage.Companion.decodeNetKeyIndex
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.ConfigStatusMessage
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NodeIdentityState
import no.nordicsemi.kotlin.mesh.core.util.NodeIdentity

/**
 * Defines message sent to request the current Node Identity state. [ConfigNodeIdentityStatus]
 * will be the response to this message.
 *
 * @property identity Node Identity state.
 * @property networkKeyIndex Network key index.
 * @property status Status of the message.
 */
class ConfigNodeIdentityStatus(
    val identity: NodeIdentityState,
    override val networkKeyIndex: KeyIndex,
    override val status: ConfigMessageStatus
) : ConfigResponse, ConfigStatusMessage, ConfigNetKeyMessage {
    override val opCode = Initializer.opCode
    override val parameters = encodeNetKeyIndex()

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String =
        "ConfigNodeIdentityStatus(opCode: 0x${opCode.toHexString()}, status: $status, " +
                "networkKeyIndex: $networkKeyIndex, identity: $identity)"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x8048u

        override fun init(parameters: ByteArray?) = parameters
            ?.takeIf { it.size == 4 }
            ?.let { params ->
                ConfigMessageStatus.from(params.first().toUByte())?.let {
                    ConfigNodeIdentityStatus(
                        identity = NodeIdentityState.from(params[1].toUByte()),
                        networkKeyIndex = decodeNetKeyIndex(params, 2),
                        status = it
                    )
                }
            }
    }
}