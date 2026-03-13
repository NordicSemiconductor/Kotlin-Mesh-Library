package no.nordicsemi.android.nrfmesh.core.data.configurator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.Diversity1
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.NetworkCell
import androidx.compose.material.icons.outlined.NetworkPing
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.WifiTethering
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigBeaconSet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigCompositionDataGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigDefaultTtlGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigDefaultTtlSet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigFriendSet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigGattProxySet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigHeartbeatPublicationSet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelAppBind
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelPublicationSet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelPublicationVirtualAddressSet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelSubscriptionAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelSubscriptionVirtualAddressAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetworkTransmitSet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigRelaySet
import no.nordicsemi.kotlin.mesh.core.model.FeatureState
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.PublicationAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.VirtualAddress
import no.nordicsemi.kotlin.mesh.core.model.knownTo
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class Configurator(private val meshNetworkManager: MeshNetworkManager) {
    private val _tasks = mutableMapOf<Uuid, MutableList<ConfigTask>>()
    private val tasks: Map<Uuid, List<ConfigTask>>
        get() = _tasks.toMap()

    fun queue(uuid: Uuid) {
        val tasksList = mutableListOf<ConfigTask>()
        tasksList.add(
            element = ConfigTask(
                icon = Icons.Outlined.Timer,
                label = "Reading default TTL",
                message = ConfigDefaultTtlGet()
            )
        )
        tasksList.add(
            element = ConfigTask(
                icon = Icons.Outlined.DeviceHub,
                label = "Reading composition of the node",
                message = ConfigCompositionDataGet(page = 0x00u)
            )
        )
        _tasks[uuid] = tasksList
    }

    @OptIn(ExperimentalUuidApi::class)
    fun queueForReconfiguration(meshNetwork: MeshNetwork, originalNode: Node, newNode: Node) {
        originalNode.run {
            val tasksList = mutableListOf<ConfigTask>()
            defaultTTL?.let {
                tasksList.add(
                    element = ConfigTask(
                        icon = Icons.Outlined.Timer,
                        label = "Set default TTL to $it",
                        message = ConfigDefaultTtlSet(ttl = it)
                    )
                )
            }
            tasksList.add(
                element = ConfigTask(
                    icon = Icons.Outlined.DeviceHub,
                    label = "Reading composition of the node",
                    message = ConfigCompositionDataGet(page = 0x00u)
                )
            )
            secureNetworkBeacon?.let {
                tasksList.add(
                    element = ConfigTask(
                        icon = Icons.Outlined.WifiTethering,
                        label = "${if (it) "Enable" else "Disable"} Secure Network Beacon state",
                        message = ConfigBeaconSet(enable = it)
                    )
                )
            }
            networkTransmit?.let {
                tasksList.add(
                    element = ConfigTask(
                        icon = Icons.Outlined.NetworkPing,
                        label = "Set default TTL",
                        message = ConfigNetworkTransmitSet(networkTransmit = it)
                    )
                )
            }
            features.run { ->
                relay?.let { relay ->
                    when (relay.state) {
                        FeatureState.Enabled -> {
                            relayRetransmit?.let {
                                tasksList.add(
                                    element = ConfigTask(
                                        icon = Icons.Outlined.NetworkPing,
                                        label = "Setting retransmit to $it",
                                        message = ConfigRelaySet(relayRetransmit = it)
                                    )
                                )
                            }
                        }

                        FeatureState.Disabled -> tasksList.add(
                            element = ConfigTask(
                                icon = Icons.Outlined.NetworkCell,
                                label = "Disabling relay feature",
                                message = ConfigRelaySet()
                            )
                        )

                        FeatureState.Unsupported -> {
                            // Do nothing
                        }
                    }
                }

                proxy?.let { proxy ->
                    if (proxy.state is FeatureState.Unsupported) {
                        tasksList.add(
                            element = ConfigTask(
                                icon = Icons.Outlined.Hub,
                                label = "${if (proxy.state is FeatureState.Enabled) "Enabling" else "Disabling"} Gatt Proxy state",
                                message = ConfigGattProxySet(enable = proxy.state is FeatureState.Enabled)
                            )
                        )
                    }
                }

                friend?.let { friend ->
                    if (friend.state != FeatureState.Unsupported) {
                        tasksList.add(
                            element = ConfigTask(
                                icon = Icons.Outlined.Diversity1,
                                label = "${if (friend.state is FeatureState.Enabled) "Enabling" else "Disabling"} Friend feature state",
                                message = ConfigFriendSet(enable = friend.state is FeatureState.Enabled)
                            )
                        )
                    }
                }
            }
            meshNetwork.networkKeys.knownTo(originalNode).forEach {
                tasksList.add(
                    element = ConfigTask(
                        icon = Icons.Outlined.VpnKey,
                        label = "Adding $it",
                        message = ConfigNetKeyAdd(key = it)
                    )
                )
            }
            meshNetwork.applicationKeys.knownTo(originalNode).forEach {
                tasksList.add(
                    element = ConfigTask(
                        icon = Icons.Outlined.VpnKey,
                        label = "Adding $it",
                        message = ConfigAppKeyAdd(key = it)
                    )
                )
            }

            heartbeatPublication?.let { publication ->
                meshNetwork.networkKey(publication.index)?.let {
                    tasksList.add(
                        element = ConfigTask(
                            icon = Icons.Outlined.MonitorHeart,
                            label = "Setting up heartbeat publications",
                            message = ConfigHeartbeatPublicationSet(
                                index = publication.index,
                                destination = publication.address,
                                countLog = 0u,
                                periodLog = 0u,
                                ttl = publication.ttl,
                                features = publication.features
                            )
                        )
                    )
                }
            }
            // Let's not set the heartbeat subscription as the current subscription period is
            // unknown
            // Application key Bindings
            val elementCount = min(originalNode.elements.size, newNode.elements.size)
            repeat(times = elementCount) { index ->
                val originalElement = originalNode.elements[index]
                val targetElement = newNode.elements[index]

                originalElement
                    .models
                    .filter { it.supportsApplicationKeyBinding }
                    .forEach { originalModel ->
                        targetElement
                            .model(modelId = originalModel.modelId)
                            ?.let { targetModel ->
                                meshNetwork.applicationKeys
                                    .filter { it.isBoundTo(originalModel) }
                                    .forEach { key ->
                                        tasksList.add(
                                            element = ConfigTask(
                                                icon = Icons.Outlined.VpnKey,
                                                label = "Binding $key to ${targetModel.modelId}",
                                                message = ConfigModelAppBind(
                                                    model = targetModel,
                                                    applicationKey = key
                                                )
                                            )
                                        )
                                    }
                            }
                    }
            }
            // Model publications
            repeat(times = elementCount) { index ->
                val originalElement = originalNode.elements[index]
                val targetElement = newNode.elements[index]
                originalElement
                    .models
                    .filter { it.supportsModelPublication == true }
                    .forEach { originalModel ->
                        originalModel.publish?.let { publish ->
                            targetElement
                                .model(modelId = originalModel.modelId)
                                ?.let { targetModel ->
                                    val newPublication = publish.copy(
                                        address = translate(
                                            address = publish.address,
                                            oldNode = originalNode,
                                            newNode = newNode
                                        )
                                    )
                                    tasksList.add(
                                        element = ConfigTask(
                                            icon = Icons.Outlined.VpnKey,
                                            label = "Publishing to ${publish.address}",
                                            message = if (publish.address is VirtualAddress) {
                                                ConfigModelPublicationVirtualAddressSet(
                                                    publish = newPublication,
                                                    model = targetModel
                                                )
                                            } else {
                                                ConfigModelPublicationSet(
                                                    publish = newPublication,
                                                    model = targetModel
                                                )
                                            }
                                        )
                                    )
                                }
                        }
                    }
            }
            // Model subscriptions
            repeat(times = elementCount) { index ->
                val originalElement = originalNode.elements[index]
                val targetElement = newNode.elements[index]
                originalElement
                    .models
                    .filter { it.supportsModelSubscription == true }
                    .forEach { originalModel ->
                        targetElement
                            .model(modelId = originalModel.modelId)
                            ?.let { targetModel ->
                                originalModel.subscribe.forEach { address ->
                                    tasksList.add(
                                        element = ConfigTask(
                                            icon = Icons.Outlined.VpnKey,
                                            label = "Subscribing to ${address.address}",
                                            message = if (address is VirtualAddress) {
                                                ConfigModelSubscriptionVirtualAddressAdd(
                                                    virtualAddress = address,
                                                    model = targetModel
                                                )
                                            } else {
                                                ConfigModelSubscriptionAdd(
                                                    address = address,
                                                    model = targetModel
                                                )
                                            }
                                        )
                                    )
                                }
                            }
                    }
            }
            // Reconfigure other Nodes that may be publishing to the previous address of the Node.
            originalNode.network?.nodes
                .orEmpty()
                .filter { it.uuid != newNode.uuid }
                .flatMap { it.elements }
                .flatMap { it.models }
                .filter { model ->
                    model.publish?.address?.let { publicationAddress ->
                        originalNode.containsElementWithAddress(address = publicationAddress.address)
                    } ?: false
                }.forEach { model ->
                    model.publish?.let { publish ->
                        val newPublication = publish.copy(
                            address = translate(
                                address = publish.address,
                                oldNode = originalNode,
                                newNode = newNode
                            )
                        )
                        tasksList.add(
                            element = ConfigTask(
                                icon = Icons.Outlined.VpnKey,
                                label = "Publishing to ${publish.address}",
                                message = if (publish.address is VirtualAddress) {
                                    ConfigModelPublicationVirtualAddressSet(
                                        publish = newPublication,
                                        model = model
                                    )
                                } else {
                                    ConfigModelPublicationSet(
                                        publish = newPublication,
                                        model = model
                                    )
                                }
                            )
                        )
                    }
                }

            _tasks[originalNode.uuid] = tasksList
        }
    }

    private fun translate(
        address: PublicationAddress,
        oldNode: Node,
        newNode: Node,
    ): PublicationAddress {
        if (oldNode.containsElementWithAddress(address = address.address)) {
            return newNode.primaryUnicastAddress + address as UnicastAddress - oldNode.primaryUnicastAddress
        }
        return address
    }

    suspend fun configure(node: Node) = withContext(context = Dispatchers.IO) {
        _tasks[node.uuid]?.forEachIndexed { index, task ->
            _tasks[node.uuid]!![index] = task.copy(status = TaskStatus.InProgress)
            try {
                meshNetworkManager.send(task.message, node, null)?.let {
                    _tasks[node.uuid]!![index] = task.copy(status = TaskStatus.Completed)
                } ?: run {
                    _tasks[node.uuid]!![index] = task.copy(status = TaskStatus.Skipped)
                }
            } catch (e: Exception) {
                _tasks[node.uuid]!![index] = task.copy(status = TaskStatus.Error(error = e))
            }
            delay(duration = 0.5.seconds)
        }
    }
}