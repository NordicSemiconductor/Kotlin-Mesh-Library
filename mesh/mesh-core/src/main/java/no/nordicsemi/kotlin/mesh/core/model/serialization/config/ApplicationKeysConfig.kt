@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model.serialization.config

import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

/**
 * Contains the configuration required when exporting a selected number of Application Keys in a
 * mesh network.
 */
sealed class ApplicationKeysConfig {

    object All : ApplicationKeysConfig()

    /**
     * Use this class to configure when exporting all the Application Keys.
     *
     * @property applicationKeys List of Application Keys to export.
     * @constructor Constructs ExportSome to export only a selected number of Application Keys when
     *              exporting a mesh network.
     */
    data class Some(val applicationKeys: List<ApplicationKey>) : ApplicationKeysConfig() {

        /**
         * Excludes application keys that are bound to excluded network keys for a partial export.
         *
         * @param keys List of network keys to be exported.
         * @return List of application keys that are bound to the network keys to be exported.
         */
        internal fun excludeAppKeysBoundToExcludedNetKeys(
            keys: List<NetworkKey>
        ) = applicationKeys.filter { applicationKey ->
            applicationKey.netKey?.let {
                it in keys
            } ?: false
        }
    }
}
