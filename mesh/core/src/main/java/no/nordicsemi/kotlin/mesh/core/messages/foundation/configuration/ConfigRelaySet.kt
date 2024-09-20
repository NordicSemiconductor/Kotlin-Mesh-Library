@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.data.shr
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.model.FeatureState
import no.nordicsemi.kotlin.mesh.core.model.Relay
import no.nordicsemi.kotlin.mesh.core.model.RelayRetransmit
import kotlin.experimental.and

/**
 * Defines a message that's message sent as a response to a [ConfigRelayGet] or [ConfigRelaySet].
 *
 * @property state Feature state of the [Relay] feature.
 * @property count Number of retransmissions.
 * @property steps Number of 10-millisecond steps between retransmissions.
 */
data class ConfigRelaySet(
    val state: FeatureState,
    val count: Int,
    val steps: UByte
) : AcknowledgedConfigMessage {
    override val opCode = Initializer.opCode
    override val parameters = byteArrayOf(state.value.toByte())
    override val responseOpCode: UInt = ConfigFriendStatus.opCode

    /**
     * Constructs a ConfigRelaySet message.
     *
     * @param count Number of retransmissions.
     * @param steps Number of 10-millisecond steps between retransmissions.
     */
    constructor(count: Int, steps: UByte) : this(
        state = FeatureState.Enabled,
        count = count,
        steps = steps
    )

    /**
     * Constructs a ConfigRelaySet message.
     *
     * @param relayRetransmit The relay retransmit parameters.
     */
    constructor(relayRetransmit: RelayRetransmit) : this(
        state = FeatureState.Enabled,
        count = relayRetransmit.count - 1,
        steps = relayRetransmit.steps
    )

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString() = "ConfigRelaySet(opCode: 0x${opCode.toHexString()}, state: $state, " +
            "count: $count, steps: $steps)"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x8027u

        override fun init(parameters: ByteArray?) = parameters
            ?.takeIf { it.size == 2 }
            ?.let { params ->
                val state = FeatureState.from(params[0].toUByte().toInt())
                ConfigRelaySet(
                    state = state,
                    count = (params[1] and 0x07).toInt(),
                    steps = (params[1] shr 3).toUByte()
                )
            }
    }
}