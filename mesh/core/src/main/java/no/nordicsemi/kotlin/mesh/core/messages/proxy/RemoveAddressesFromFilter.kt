@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.proxy

import no.nordicsemi.kotlin.data.getUShort
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.mesh.core.model.ProxyFilterAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress

/**
 * A message sent by a Proxy Client to remove destination addresses from the proxy filter list.
 *
 * @property addresses List of addresses.
 * "
 */
data class RemoveAddressesFromFilter(
    val addresses: List<ProxyFilterAddress>
) : AcknowledgedProxyConfigurationMessage {
    override val opCode: UByte = Initializer.opCode
    override val responseOpCode: UByte = FilterStatus.opCode
    override val parameters: ByteArray
        get() {
            var byteArray = ByteArray(0)
            addresses.forEach {
                byteArray += it.address.toByteArray()
            }
            return byteArray
        }

    companion object Initializer : ProxyConfigurationMessageInitializer {
        override val opCode: UByte = 0x02u
        override fun init(parameters: ByteArray?) = parameters?.takeIf {
            it.size % 2 == 0
        }?.let {
            val addresses = mutableListOf<UnicastAddress>()
            var i = 0
            while (i < it.size) {
                addresses.add(UnicastAddress(it.getUShort(i)))
                i += 2
            }
            RemoveAddressesFromFilter(addresses)
        }
    }
}