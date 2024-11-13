package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer

/**
 * This message is used to get the beacon state of the mesh network. The response to this message
 * would be a [ConfigBeaconStatus].
 */
class ConfigBeaconGet : AcknowledgedConfigMessage {
    override val opCode = Initializer.opCode
    override val responseOpCode = ConfigBeaconStatus.opCode
    override val parameters = null

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        return "ConfigBeaconGet(opCode: 0x${opCode.toHexString()})"
    }

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x8009u

        override fun init(parameters: ByteArray?) = if (parameters == null || parameters.isEmpty())
            ConfigBeaconGet()
        else null
    }
}