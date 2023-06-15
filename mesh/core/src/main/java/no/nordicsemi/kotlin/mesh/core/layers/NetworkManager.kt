@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers

import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.layers.network.NetworkLayer

/**
 * Network Manager contains the different layers of the mesh network architecture.
 *
 * @property manager Mesh network manager
 * @constructor Constructs the network manager.
 */
internal class NetworkManager(private val manager: MeshNetworkManager) {
    var networkLayer = NetworkLayer(this)
    // var lowerTransportLayer = LowerTransportLayer(this)
    // var upperTransportLayer = UpperTransportLayer(this)
    // var accessLayer = AccessLayer(this)


}