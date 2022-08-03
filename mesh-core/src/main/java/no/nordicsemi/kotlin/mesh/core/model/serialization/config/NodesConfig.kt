@file:Suppress("MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core.model.serialization.config

import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.DeviceKeyConfig.WITHOUT_DEVICE_KEY
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.DeviceKeyConfig.WITH_DEVICE_KEY

/**
 * Contains the configuration required when exporting a selected number of mesh nodes in a mesh network.
 */
sealed class NodesConfig {

    data class All(val deviceKeyConfig: DeviceKeyConfig = WITH_DEVICE_KEY) : NodesConfig()

    /**
     * Use this class to configure when exporting some of the Nodes.
     *
     * @property withDeviceKey    List of nodes to be exported with their device keys.
     * @property withoutDeviceKey List of nodes to be exported without their device keys.
     * @constructor Constructs ExportSome to export only a selected number of Nodes when exporting a
     *              mesh network.
     */
    data class Some(
        val withDeviceKey: List<Node>,
        val withoutDeviceKey: List<Node>
    ) : NodesConfig()

}

/**
 * Specifies the Device Key configuration to be used when exporting a partial network.
 * @property WITH_DEVICE_KEY Use this constant to export nodes with their device keys.
 * @property WITHOUT_DEVICE_KEY Use this constant to export nodes without their device keys.
 */
enum class DeviceKeyConfig {
    WITH_DEVICE_KEY,
    WITHOUT_DEVICE_KEY;
}
