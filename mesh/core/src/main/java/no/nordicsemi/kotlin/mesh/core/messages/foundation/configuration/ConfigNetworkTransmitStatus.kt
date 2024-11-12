package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.data.shl
import no.nordicsemi.kotlin.data.shr
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import kotlin.experimental.and
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * This message is the status message sent as a response to [ConfigNetworkTransmitGet] and
 * [ConfigNetworkTransmitSet].
 *
 * @property count    Number of transmissions of Network PDU originating from the node. Possible
 *                    value are 0...7, which correspond to 1 - 8 transmissions in total.
 * @property steps    Number of 10-millisecond steps between transmissions, decremented by 1.
 *                    Possible values are 0...31, which correspond to 10 ms to 320 ms in 10 ms
 *                    steps.
 * @property interval Interval between transmissions, in seconds.
 *
 */
@Suppress("MemberVisibilityCanBePrivate")
class ConfigNetworkTransmitStatus(val count: UByte, val steps: UByte) : ConfigResponse {
    override val opCode = Initializer.opCode
    override val parameters = byteArrayOf(((count and 0x07.toUByte()) or (steps shl 3)).toByte())

    val interval: Duration
        get() = (steps + 1u).toInt().toDuration(unit = DurationUnit.SECONDS) / 100

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString() = "ConfigNetworkTransmitStatus(opCode: 0x${opCode.toHexString()} " +
            "parameters: ${parameters.toHexString()})"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x8025u

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