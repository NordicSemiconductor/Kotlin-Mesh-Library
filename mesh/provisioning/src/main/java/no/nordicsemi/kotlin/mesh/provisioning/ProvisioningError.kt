@file:Suppress("ClassName", "unused")

package no.nordicsemi.kotlin.mesh.provisioning

/**
 * Set of errors which may be thrown during provisioning a device.
 */
sealed class ProvisioningError : Exception()

/**
 * Thrown when the ProvisioningManager is in an invalid state.
 */
data object InvalidState : ProvisioningError()

/**
 * The received PDU is invalid.
 */
data object InvalidPdu : ProvisioningError()

/**
 * The received Public Key is invalid or not equal to Provisioner's Public Key.
 */
data object InvalidPublicKey : ProvisioningError()

/**
 * Thrown when the Unprovisioned Device is not supported by the manager.
 */
data object UnsupportedDevice : ProvisioningError()

/**
 * Thrown when the provided alphanumeric value could not be converted into bytes using ASCII
 * encoding.
 */
data object InvalidOobValueFormat : ProvisioningError()

/**
 * Thrown when no available Unicast Address was found in the Provisioner's range that could be
 * allocated for the device.
 */
data object NoAddressAvailable : ProvisioningError()

/**
 * Thrown when the unicast address is invalid.
 */
data object InvalidAddress : ProvisioningError()

/**
 * Throws when the Unicast Address has not been set.
 */
data object AddressNotSpecified : ProvisioningError()

/**
 * Throws when the Network Key has not been set.
 */
data object NetworkKeyNotSpecified : ProvisioningError()

/**
 * Thrown when confirmation value received from the device does not match calculated value.
 * Authentication failed.
 */
data object ConfirmationFailed : ProvisioningError()

/**
 * Thrown when the remove device sent a failure indication.
 */
data class RemoteError(val error: RemoteProvisioningError) : ProvisioningError()

/**
 * Thrown when the key pair generation has failed.
 */
data class KeyGenerationFailed(val throwable: Throwable) : ProvisioningError()
