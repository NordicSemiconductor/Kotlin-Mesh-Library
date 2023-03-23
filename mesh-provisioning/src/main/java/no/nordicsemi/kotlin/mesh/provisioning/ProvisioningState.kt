@file:Suppress("ClassName", "unused", "MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.provisioning

/**
 * Defines possible state of provisioning process.
 *
 * @property debugDescription Debug description of the state.
 */
sealed class ProvisioningState {

    /**
     * Provisioning Manager is ready to start.
     */
    object Ready : ProvisioningState()

    /**
     * The manager is requesting Provisioning Capabilities from the device.
     */
    object RequestingCapabilities : ProvisioningState()

    /**
     * Provisioning Capabilities were received.
     *
     * @property capabilities Capabilities of the device.
     */
    data class CapabilitiesReceived(
        val capabilities: ProvisioningCapabilities
    ) : ProvisioningState()

    /**
     * Provisioning has been started.
     */
    object Provisioning : ProvisioningState()

    /**
     * The provisioning process is complete.
     */
    object Complete : ProvisioningState()

    /**
     * The provisioning has failed because of a local error.
     */
    data class Failed(val error: Throwable) : ProvisioningState()

    val debugDescription: String
        get() = when (this) {
            Ready -> "Provisioner is ready"
            RequestingCapabilities -> "Requesting provisioning capabilities"
            is CapabilitiesReceived -> "Provisioning capabilities received"
            Provisioning -> "Provisioning started"
            Complete -> "Provisioning complete"
            is Failed -> "Provisioning failed: $error"
        }
}