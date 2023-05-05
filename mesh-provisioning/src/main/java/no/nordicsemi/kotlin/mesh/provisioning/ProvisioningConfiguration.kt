package no.nordicsemi.kotlin.mesh.provisioning

import no.nordicsemi.kotlin.mesh.core.exception.NoLocalProvisioner
import no.nordicsemi.kotlin.mesh.core.exception.NoNetworkKeysAdded
import no.nordicsemi.kotlin.mesh.core.exception.NoUnicastRangeAllocated
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
 */
data class ProvisioningConfiguration(
    private val meshNetwork: MeshNetwork,
    private val capabilities: ProvisioningCapabilities
) {
    var unicastAddress: UnicastAddress? = meshNetwork.localProvisioner?.let {
        require(it.allocatedUnicastRanges.isNotEmpty()) {
            throw NoUnicastRangeAllocated
        }
        // Calculates the unicast address automatically based ont he number of elements.
        meshNetwork.nextAvailableUnicastAddress(capabilities.numberOfElements, it)
    } ?: run {
        throw NoLocalProvisioner
    }
    var networkKey: NetworkKey = meshNetwork.networkKeys.firstOrNull() ?: throw NoNetworkKeysAdded

    var algorithm: Algorithm = capabilities.algorithms.strongest()

    var publicKey: PublicKey = if (capabilities.publicKeyType.isNotEmpty()) {
        PublicKey.OobPublicKey(ByteArray(16) { 0x00 })
    } else PublicKey.NoOobPublicKey

    var authMethod: AuthenticationMethod =
        capabilities.supportedAuthenticationMethods.first()
}