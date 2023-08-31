@file:Suppress("UNUSED_PARAMETER")

package no.nordicsemi.kotlin.mesh.core.layers.access

import no.nordicsemi.kotlin.mesh.core.layers.NetworkManager
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Element

/**
 * Defines the behaviour of the Access Layer of the Mesh Networking Stack.
 *
 * @property networkManager The network manager
 */
internal class AccessLayer(private val networkManager: NetworkManager) {

    fun send(
        message: MeshMessage,
        localElement: Element,
        address: Address,
        ttl: UByte?,
        applicationKey: ApplicationKey,
        retransmit: Boolean
    ) {
        TODO("Not yet implemented")
    }

    fun send(
        message: ConfigMessage,
        localElement: Element,
        address: Address,
        ttl: UByte?
    ) {
        TODO("Not yet implemented")
    }
}