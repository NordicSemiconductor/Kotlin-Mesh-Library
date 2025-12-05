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
class ProvisioningParameters internal constructor(
    private val capabilities: ProvisioningCapabilities,
    var unicastAddress: UnicastAddress?,
    var networkKey: NetworkKey,
    var algorithm: Algorithm = capabilities.algorithms.strongest(),
    var publicKey: PublicKey = PublicKey.NoOobPublicKey,
    var authMethod: AuthenticationMethod = capabilities.supportedAuthMethods.first(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProvisioningParameters) return false

        if (capabilities != other.capabilities) return false
        if (unicastAddress != other.unicastAddress) return false
        if (networkKey != other.networkKey) return false
        if (algorithm != other.algorithm) return false
        if (publicKey != other.publicKey) return false
        if (authMethod != other.authMethod) return false

        return true
    }

    override fun hashCode(): Int {
        var result = capabilities.hashCode()
        result = 31 * result + (unicastAddress?.hashCode() ?: 0)
        result = 31 * result + networkKey.hashCode()
        result = 31 * result + algorithm.hashCode()
        result = 31 * result + publicKey.hashCode()
        result = 31 * result + authMethod.hashCode()
        return result
    }
}