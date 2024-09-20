package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer

/**
 * Defines message sent to request the Current Friend state of the Node. [ConfigFriendStatus]
 * will be the response to this message.
 */
class ConfigFriendGet : AcknowledgedConfigMessage {
    override val opCode = Initializer.opCode
    override val parameters = null
    override val responseOpCode: UInt = ConfigGattProxyStatus.opCode

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String =
        "ConfigFriendStatus(opCode: 0x${opCode.toHexString()}, parameters: $parameters)"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x800Fu

        override fun init(parameters: ByteArray?)  = if(parameters == null) {
            ConfigFriendGet()
        } else {
            null
        }
    }
}