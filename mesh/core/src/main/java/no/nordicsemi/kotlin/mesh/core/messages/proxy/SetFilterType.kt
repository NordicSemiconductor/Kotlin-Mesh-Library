@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.proxy

import no.nordicsemi.kotlin.mesh.core.ProxyFilterType

/**
 * The Set Filter Type message can be sent by a Proxy Client to change the proxy filter type and
 * clear the proxy filter list.
 *
 * @property filterType Filter type.
 * @constructor Creates a SetFilterType message.
 */
data class SetFilterType(val filterType: ProxyFilterType) :
        AcknowledgedProxyConfigurationMessage {

    override val opCode: UByte = Decoder.opCode

    override val responseOpCode: UByte = FilterStatus.opCode
    override val parameters: ByteArray
        get() = byteArrayOf(filterType.type.toByte())

    companion object Decoder : ProxyConfigurationMessageDecoder {
        override val opCode: UByte = 0x00u
        override fun decode(payload: ByteArray) = when (payload.size == 1) {
            true -> SetFilterType(ProxyFilterType.from(payload[0].toUByte()))
            false -> null
        }
    }
}