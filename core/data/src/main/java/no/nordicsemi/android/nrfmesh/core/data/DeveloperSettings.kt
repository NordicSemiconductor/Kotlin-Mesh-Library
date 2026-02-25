package no.nordicsemi.android.nrfmesh.core.data

import no.nordicsemi.kotlin.mesh.provisioning.AuthenticationMethod.NoOob

/**
 * Defines a set of configuration options for the application.
 *
 * @property quickProvisioning Allows quick provisioning by always using [NoOob] if it is supported
 *                             by the node.
 * @property alwaysReconfigure Allows always reconfigure where if an existing node is reprovisioned
 *                             the configuration from the existing node will be reapplied.
 */
data class DeveloperSettings(
    val quickProvisioning: Boolean = false,
    val alwaysReconfigure: Boolean = false,
)
