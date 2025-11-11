package no.nordicsemi.kotlin.mesh.provisioning

import no.nordicsemi.kotlin.mesh.core.exception.NoLocalProvisioner
import no.nordicsemi.kotlin.mesh.core.exception.NoNetworkKeysAdded
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.crypto.Algorithm
import no.nordicsemi.kotlin.mesh.crypto.Algorithm.Companion.strongest


/**
 * Configuration class that contains all the necessary information to provision a device.
 *
 * @property meshNetwork       Mesh Network to which the device will be provisioned.
 * @property capabilities      Capabilities of the device to be provisioned.
 * @property unicastAddress    Unicast address to be assigned to the device.
 * @property networkKey        Network key to be used for provisioning.
 * @property algorithm         Algorithm to be used for provisioning.
 * @property publicKey         Public key to be used for provisioning.
 * @property authMethod        Authentication method to be used for provisioning.
 * @throws NoNetworkKeysAdded  Exception thrown when there are no network keys added to the mesh
 *                             network.
 * @throws NoLocalProvisioner  Exception thrown when there is no local provisioner added to the mesh
 *                             network.
 */
@ConsistentCopyVisibility
data class ProvisioningParameters internal constructor(
    private val meshNetwork: MeshNetwork,
    private val capabilities: ProvisioningCapabilities
) {
    var unicastAddress: UnicastAddress? = meshNetwork.localProvisioner?.let {
        // Calculates the unicast address automatically based ont he number of elements.
        meshNetwork.nextAvailableUnicastAddress(
            elementCount = capabilities.numberOfElements,
            provisioner = it
        ) ?: throw NoAddressAvailable()
    } ?: throw NoLocalProvisioner()

    var networkKey: NetworkKey = meshNetwork.networkKeys.firstOrNull() ?: throw NoNetworkKeysAdded()

    var algorithm: Algorithm = capabilities.algorithms.strongest()

    var publicKey: PublicKey = PublicKey.NoOobPublicKey

    var authMethod: AuthenticationMethod =
        capabilities.supportedAuthMethods.first()
}