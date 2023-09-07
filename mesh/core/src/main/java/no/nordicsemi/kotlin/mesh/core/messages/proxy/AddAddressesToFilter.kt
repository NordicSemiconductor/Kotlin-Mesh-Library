@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.proxy

import no.nordicsemi.kotlin.mesh.core.model.ProxyFilterAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.util.Utils.toByteArray
import no.nordicsemi.kotlin.mesh.core.util.Utils.toUShort

/**
 * This is a message sent by a Proxy Client to add destination addresses to the proxy filter list.
 *
 * @property addresses List of addresses.
 */
data class AddAddressesToFilter(
    val addresses: List<ProxyFilterAddress>
) : AcknowledgedProxyConfigurationMessage {
    override val opCode: UByte = Initializer.opCode
    override val responseOpCode: UByte = FilterStatus.opCode
    override val parameters: ByteArray
        get() {
            var byteArray = ByteArray(0)
            // Send addresses sorted. The primary element will be added as a the first one, in case
            // the Proxy Filter supports only on address.
            addresses.sortedBy { it.address }.forEach {
                byteArray += it.address.toByteArray()
            }
            return byteArray
        }

    companion object Initializer : ProxyConfigurationMessageInitializer {
        override val opCode: UByte = 0x01u
        override fun init(payload: ByteArray) = payload.takeIf {
            it.size % 2 == 0
        }?.let {
            val addresses = mutableListOf<UnicastAddress>()
            var i = 0
            while (i < it.size) {
                addresses.add(UnicastAddress(it.toUShort(i)))
                i += 2
            }
            AddAddressesToFilter(addresses)
        }
    }
}