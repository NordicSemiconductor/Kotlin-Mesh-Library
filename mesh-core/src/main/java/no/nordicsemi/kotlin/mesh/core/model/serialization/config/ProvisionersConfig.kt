@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model.serialization.config

import no.nordicsemi.kotlin.mesh.core.exception.AtLeastOneProvisionerMustBeSelected
import no.nordicsemi.kotlin.mesh.core.model.Provisioner

/**
 * Contains the configuration required when exporting a selected number of Provisioners in a mesh
 * network.
 */
sealed class ProvisionersConfig {

    /**
     * Use this class to configure when exporting all the Provisioners.
     */
    object All : ProvisionersConfig()

    /**
     * Use this class to configure when exporting some of the Provisioners.
     *
     * @property provisioners List of Provisioners to export.
     * @throws IllegalArgumentException if the list does not contain at least one provisioner.
     * @constructor Constructs ExportSome to export only a selected number of Provisioners when
     *              exporting a mesh network.
     */
    data class Some(val provisioners: List<Provisioner>) : ProvisionersConfig() {
        init {
            require(provisioners.isNotEmpty()) { throw AtLeastOneProvisionerMustBeSelected() }
        }
    }

    data class One(val provisioner: Provisioner) : ProvisionersConfig()
}
