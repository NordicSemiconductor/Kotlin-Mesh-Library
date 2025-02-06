@file:Suppress("ClassName", "unused")

package no.nordicsemi.kotlin.mesh.provisioning

/**
 * Set of errors which may be thrown during provisioning a device.
 */
sealed class ProvisioningError : Exception()

/**
 * Thrown when the ProvisioningManager is in an invalid state.
 */
data object InvalidState : ProvisioningError() {
    private fun readResolve(): Any = InvalidState
}

/**
 * The received PDU is invalid.
 */
data object InvalidPdu : ProvisioningError() {
    private fun readResolve(): Any = InvalidPdu
}

/**
 * The received Public Key is invalid or not equal to Provisioner's Public Key.
 */
data object InvalidPublicKey : ProvisioningError() {
    private fun readResolve(): Any = InvalidPublicKey
}

/**
 * Thrown when the Unprovisioned Device is not supported by the manager.
 */
data object UnsupportedDevice : ProvisioningError() {
    private fun readResolve(): Any = UnsupportedDevice
}

/**
 * Thrown when the provided alphanumeric value could not be converted into bytes using ASCII
 * encoding.
 */
data object InvalidOobValueFormat : ProvisioningError() {
    private fun readResolve(): Any = InvalidOobValueFormat
}

/**
 * Thrown when no available Unicast Address was found in the Provisioner's range that could be
 * allocated for the device.
 */
data object NoAddressAvailable : ProvisioningError() {
    private fun readResolve(): Any = NoAddressAvailable
}

/**
 * Thrown when the unicast address is invalid.
 */
data object InvalidAddress : ProvisioningError() {
    private fun readResolve(): Any = InvalidAddress
}

/**
 * Throws when the Unicast Address has not been set.
 */
data object AddressNotSpecified : ProvisioningError() {
    private fun readResolve(): Any = AddressNotSpecified
}

/**
 * Throws when the Network Key has not been set.
 */
data object NetworkKeyNotSpecified : ProvisioningError() {
    private fun readResolve(): Any = NetworkKeyNotSpecified
}

/**
 * Thrown when confirmation value received from the device does not match calculated value.
 * Authentication failed.
 */
data object ConfirmationFailed : ProvisioningError() {
    private fun readResolve(): Any = ConfirmationFailed
}

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
