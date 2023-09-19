@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer

/**
 * This message is used to get the Heartbeat Publication state of an element.
 *
 * @property parameters Message parameters.
 * @constructor Creates a ConfigHeartbeatPublicationGet message.
 */
class ConfigHeartbeatPublicationGet : AcknowledgedConfigMessage {

    override val opCode: UInt = Initializer.opCode

    override val responseOpCode = ConfigHeartbeatPublicationStatus.opCode
    override val parameters: ByteArray? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConfigHeartbeatPublicationGet

        if (!parameters.contentEquals(other.parameters)) return false
        if (opCode != other.opCode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = parameters.contentHashCode()
        result = 31 * result + opCode.hashCode()
        return result
    }

    companion object Initializer : ConfigMessageInitializer {
        override val opCode: UInt = 0x8038u

        override fun init(parameters: ByteArray) = if (parameters.isEmpty()) {
            ConfigHeartbeatPublicationGet()
        } else null
    }
}