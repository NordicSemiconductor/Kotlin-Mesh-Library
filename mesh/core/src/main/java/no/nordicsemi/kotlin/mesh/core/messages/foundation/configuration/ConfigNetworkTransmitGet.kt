package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer

/**
 * This message is used to get the network transmit settings of the node. The response to this
 * message would be a [ConfigNetworkTransmitStatus].
 */
class ConfigNetworkTransmitGet : AcknowledgedConfigMessage {
    override val opCode = Initializer.opCode
    override val responseOpCode = ConfigNetworkTransmitStatus.opCode
    override val parameters = null

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString() = "ConfigNetworkTransmitGet(opCode: 0x${opCode.toHexString()})"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x8023u

        override fun init(parameters: ByteArray?) = parameters
            ?.takeIf { it.isEmpty() }
            ?.let { ConfigNetworkTransmitGet() }
    }
}