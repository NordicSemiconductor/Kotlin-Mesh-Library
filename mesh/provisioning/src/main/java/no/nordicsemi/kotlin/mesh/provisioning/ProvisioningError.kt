@file:Suppress("ClassName", "unused")

package no.nordicsemi.kotlin.mesh.provisioning

/**
 * Set of errors which may be thrown during provisioning a device.
 */
sealed class ProvisioningError : Exception()

/**
 * Thrown when the ProvisioningManager is in an invalid state.
 */
class InvalidState : ProvisioningError()

/**
 * The received PDU is invalid.
 */
class InvalidPdu : ProvisioningError()

/**
 * The received Public Key is invalid or not equal to Provisioner's Public Key.
 */
class InvalidPublicKey : ProvisioningError()

/**
 * The received Public Key is invalid or not equal to Provisioner's Public Key.
 */
class InvalidConfirmation : ProvisioningError()

/**
 * Thrown when the Unprovisioned Device is not supported by the manager.
 */
class UnsupportedDevice : ProvisioningError()

/**
 * Thrown when the provided alphanumeric value could not be converted into bytes using ASCII
 * encoding.
 */
class InvalidOobValueFormat : ProvisioningError()

/**
 * Thrown when no available Unicast Address was found in the Provisioner's range that could be
 * allocated for the device.
 */
class NoAddressAvailable : ProvisioningError()

/**
 * Thrown when the unicast address is invalid.
 */
class InvalidAddress : ProvisioningError()

/**
 * Throws when the Unicast Address has not been set.
 */
class AddressNotSpecified : ProvisioningError()

/**
 * Throws when the Network Key has not been set.
 */
class NetworkKeyNotSpecified : ProvisioningError()

/**
 * Thrown when confirmation value received from the device does not match calculated value.
 * Authentication failed.
 */
class ConfirmationFailed : ProvisioningError()

/**
 * Thrown when the remove device sent a failure indication.
 *
 * @param error The error received from the remote device.
 */
data class RemoteError(val error: RemoteProvisioningError) : ProvisioningError()

/**
 * Thrown when the key pair generation has failed.
 *
 * @param throwable The exception that caused the failure.
 */
data class KeyGenerationFailed(val throwable: Throwable) : ProvisioningError()
