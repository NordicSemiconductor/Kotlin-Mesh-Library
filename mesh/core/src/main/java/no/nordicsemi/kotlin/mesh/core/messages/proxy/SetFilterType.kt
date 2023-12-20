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

    override val opCode: UByte = Initializer.opCode

    override val responseOpCode: UByte = FilterStatus.opCode
    override val parameters: ByteArray
        get() = byteArrayOf(filterType.type.toByte())

    companion object Initializer : ProxyConfigurationMessageInitializer {
        override val opCode: UByte = 0x00u
        override fun init(parameters: ByteArray?) = parameters?.takeIf {
            it.size == 1
        }?.let {
            SetFilterType(ProxyFilterType.from(it[0].toUByte()))
        }
    }
}