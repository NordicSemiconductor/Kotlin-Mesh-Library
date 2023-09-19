@file:Suppress("MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core.messages.proxy

import no.nordicsemi.kotlin.mesh.core.ProxyFilterType
import no.nordicsemi.kotlin.mesh.core.util.Utils.toByteArray

/**
 * The Filter Status message is sent by a Proxy Server to report the status of the Proxy Filter.
 *
 * @property filterType Filter type.
 * @property listSize   Size of the filter list.
 */

data class FilterStatus(
    val filterType: ProxyFilterType,
    val listSize: UShort
) : ProxyConfigurationMessage {

    override val opCode: UByte = Initializer.opCode

    override val parameters: ByteArray
        get() = byteArrayOf(filterType.type.toByte()) + listSize.toByteArray()

    companion object Initializer : ProxyConfigurationMessageInitializer {
        override val opCode: UByte = 0x03u

        override fun init(parameters: ByteArray) = when (parameters.size == 1) {
            true -> SetFilterType(ProxyFilterType.from(parameters[0].toUByte()))
            false -> null
        }
    }
}
