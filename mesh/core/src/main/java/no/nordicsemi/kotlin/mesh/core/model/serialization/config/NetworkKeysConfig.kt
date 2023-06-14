@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.model.serialization.config

import no.nordicsemi.kotlin.mesh.core.exception.AtLeastOneNetworkKeyMustBeSelected
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

/**
 * Network key configuration used when exporting the Network Keys in a mesh network.
 */
sealed class NetworkKeysConfig {

    /**
     * Use this class to configure when exporting all the Network Keys.
     */
    object All : NetworkKeysConfig()

    /**
     * Use this class to configure when exporting some of the Network Keys in network.
     *
     * @property keys                             List of Network Keys to export.
     * @throws AtLeastOneNetworkKeyMustBeSelected If the list does not contain at least one network
     *                                            key.
     */
    data class Some(val keys: List<NetworkKey>) : NetworkKeysConfig() {
        init { require(keys.isNotEmpty()) { throw AtLeastOneNetworkKeyMustBeSelected } }
    }
}
