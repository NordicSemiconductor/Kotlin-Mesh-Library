@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages

import no.nordicsemi.kotlin.mesh.core.util.Utils.toByteArray
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

/**
 * Unknown message defines a message that may not be defined in any of the local Models for the
 * received opcode.
 *
 * @property opCode      Opcode of the message.
 * @property parameters  Parameters of the message.
 * @constructor Constructs an UnknownMessage that cannot be parsed by any of the local models.
 */
data class UnknownMessage(
    override val opCode: UInt,
    override val parameters: ByteArray
) : MeshMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnknownMessage

        if (!parameters.contentEquals(other.parameters)) return false
        if (opCode != other.opCode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = parameters.contentHashCode()
        result = 31 * result + opCode.hashCode()
        return result
    }

    override fun toString(): String {
        return "Unknown Message (opcode: ${opCode.toByteArray().encodeHex(prefixOx = true)}, " +
                "parameters: ${parameters.encodeHex(prefixOx = true)})"
    }
}