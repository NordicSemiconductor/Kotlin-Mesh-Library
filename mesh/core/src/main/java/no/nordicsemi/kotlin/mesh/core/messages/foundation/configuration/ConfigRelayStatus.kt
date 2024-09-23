@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.data.shr
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.model.FeatureState
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.Relay
import kotlin.experimental.and

/**
 * Defines a message that's message sent as a response to a [ConfigRelayGet] or [ConfigRelaySet].
 *
 * @property state Feature state of the [Relay] feature.
 * @property count Number of retransmissions.
 * @property steps Number of 10-millisecond steps between retransmissions.
 */
data class ConfigRelayStatus(
    val state: FeatureState,
    val count: Int,
    val steps: UByte
) : ConfigResponse {
    override val opCode = Initializer.opCode
    override val parameters = byteArrayOf(state.value.toByte())

    /**
     * Constructs a ConfigGattProxySet message.
     *
     * @param node The node containing the relay retransmit parameters.
     */
    constructor(node: Node) : this(
        state = node.features.relay?.state ?: FeatureState.Unsupported,
        count = (node.relayRetransmit?.count ?: 1) - 1,
        steps = node.relayRetransmit?.steps ?: 0u
    )

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString() = "ConfigRelayStatus(opCode: 0x${opCode.toHexString()}, " +
            "state: $state, count: $count, steps: $steps)"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x8027u

        override fun init(parameters: ByteArray?) = parameters
            ?.takeIf { it.size == 2 }
            ?.let { params ->
                val state = FeatureState.from(params[0].toUByte().toInt())
                ConfigRelayStatus(
                    state = state,
                    count = (params[1] and 0x07).toInt(),
                    steps = (params[1] shr 3).toUByte()
                )
            }
    }
}