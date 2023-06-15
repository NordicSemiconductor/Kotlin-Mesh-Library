package no.nordicsemi.kotlin.mesh.core.layers.network

import no.nordicsemi.kotlin.mesh.core.layers.NetworkManager

/**
 * Network Layer of the mesh networking stack
 *
 * @property networkManager Network manager containing the different layers of the mesh networking
 *                          stack.
 * @constructor Constructs the network layer.
 */
internal class NetworkLayer(val networkManager: NetworkManager) {
}