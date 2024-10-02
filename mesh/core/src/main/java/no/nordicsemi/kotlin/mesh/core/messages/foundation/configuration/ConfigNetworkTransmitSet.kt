package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.data.shl
import no.nordicsemi.kotlin.data.shr
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import kotlin.experimental.and
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * This message is used to get the network transmit settings of the node. The response to this
 * message would be a [ConfigNetworkTransmitStatus].
 *
 * @property count    Number of transmissions of Network PDU originating from the node. Possible
 *                    value are 0...7, which correspond to 1 - 8 transmissions in total.
 * @property steps    Number of 10-millisecond steps between transmissions, decremented by 1.
 *                    Possible values are 0...31, which correspond to 10 ms to 320 ms in 10 ms
 *                    steps.
 */
class ConfigNetworkTransmitSet(val count : UByte, val steps: UByte) : AcknowledgedConfigMessage {
    override val opCode = Initializer.opCode
    override val responseOpCode = ConfigNetworkTransmitStatus.opCode
    override val parameters = byteArrayOf(((count and 0x07u) or (steps shl 3)).toByte())

    val interval: Duration
        get() = (steps + 1u).toInt().toDuration(unit = DurationUnit.SECONDS) / 100

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString() = "ConfigNetworkTransmitGet(opCode: 0x${opCode.toHexString()})"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x8024u

        override fun init(parameters: ByteArray?) = parameters?.takeIf {
            it.size == 1
        }?.let { params ->
            val first = params.first()
            ConfigNetworkTransmitStatus(
                count = (first and 0x07).toUByte(),
                steps = (first shr 3).toUByte()
            )
        }
    }
}