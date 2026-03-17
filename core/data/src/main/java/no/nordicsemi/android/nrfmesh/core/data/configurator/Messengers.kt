package no.nordicsemi.android.nrfmesh.core.data.configurator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Diversity1
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.NetworkCell
import androidx.compose.material.icons.outlined.NetworkPing
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.outlined.WifiTethering
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigBeaconSet
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
import no.nordicsemi.kotlin.mesh.core.model.AllNodes
import no.nordicsemi.kotlin.mesh.core.model.FeatureState
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.PublicationAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.VirtualAddress
import no.nordicsemi.kotlin.mesh.core.model.knownTo
import kotlin.collections.set
import kotlin.math.min
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class Messengers(private val meshNetworkManager: MeshNetworkManager) {
    private var _messengers = mutableMapOf<Uuid, Messenger>()

    /**
     * Creates a new configurator for the given node.
     */
    fun createMessenger(nodeUuid: Uuid) {
        _messengers[nodeUuid] = Messenger(meshNetworkManager = meshNetworkManager)
    }

    fun messenger(uuid: Uuid) = _messengers[uuid]

    fun removeMessenger(nodeUuid: Uuid) {
        _messengers.remove(nodeUuid)
    }
}


/**
 * Configurator is responsible for configuring a node.
 *
 * @param meshNetworkManager MeshNetworkManager to be used.
 * @param originalNode       Original Node to be configured.
 */
@OptIn(ExperimentalUuidApi::class)
class Messenger(private val meshNetworkManager: MeshNetworkManager) {
    private val _configTasks = mutableListOf<ConfigTask>()
    private val _reconfigTasks = mutableListOf<ConfigTask>()
    private val _appTasks = mutableListOf<AppTask>()

    private val _configTaskFlow = MutableStateFlow<List<ConfigTask>>(_configTasks)
    private val _reconfigTaskFlow = MutableStateFlow<List<MeshTask>>(_reconfigTasks)
    private val _appTaskFlow = MutableStateFlow<List<MeshTask>>(_appTasks)

    val tasks: List<MeshTask>
        get() = _configTasks + _reconfigTasks + _appTasks
    val isReconfigurationRequested: Boolean
        get() = _reconfigTasks.isNotEmpty()
    private var originalNode: Node? = null

    fun meshTaskFlow() = combine(_configTaskFlow, _reconfigTaskFlow, _appTaskFlow) {
        _configTaskFlow.value + _reconfigTaskFlow.value + _appTaskFlow.value
    }

    /**
     * Enqueues the configuration tasks
     */
    fun enqueueTask(task: MeshTask) {
        when (task) {
            is ConfigTask -> _configTasks.add(element = task)
            is AppTask -> _appTasks.add(element = task)
        }
    }

    /**
     * Enqueues the reconfiguration tasks based on the given original node
     */
    fun enqueueReconfigurationWith(originalNode: Node) {
        this.originalNode = originalNode
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun enqueueReconfiguration(meshNetwork: MeshNetwork, newNode: Node) {
        originalNode?.run {
            defaultTTL?.let {
                _reconfigTasks.add(
                    element = ConfigTask(
                        icon = Icons.Outlined.Timer,
                        label = "Set default TTL to $it",
                        message = ConfigDefaultTtlSet(ttl = it)
                    )
                )
            }
            secureNetworkBeacon?.let {
                _reconfigTasks.add(
                    element = ConfigTask(
                        icon = Icons.Outlined.WifiTethering,
                        label = "${if (it) "Enable" else "Disable"} Secure Network Beacon state",
                        message = ConfigBeaconSet(enable = it)
                    )
                )
            }
            networkTransmit?.let {
                _reconfigTasks.add(
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
                                _reconfigTasks.add(
                                    element = ConfigTask(
                                        icon = Icons.Outlined.NetworkPing,
                                        label = "Setting retransmit to $it",
                                        message = ConfigRelaySet(relayRetransmit = it)
                                    )
                                )
                            }
                        }

                        FeatureState.Disabled -> _reconfigTasks.add(
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
                        _reconfigTasks.add(
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
                        _reconfigTasks.add(
                            element = ConfigTask(
                                icon = Icons.Outlined.Diversity1,
                                label = "${if (friend.state is FeatureState.Enabled) "Enabling" else "Disabling"} Friend feature state",
                                message = ConfigFriendSet(enable = friend.state is FeatureState.Enabled)
                            )
                        )
                    }
                }
            }
            meshNetwork.networkKeys.knownTo(this).forEach {
                _reconfigTasks.add(
                    element = ConfigTask(
                        icon = Icons.Outlined.VpnKey,
                        label = "Adding $it",
                        message = ConfigNetKeyAdd(key = it)
                    )
                )
            }
            meshNetwork.applicationKeys.knownTo(this).forEach {
                _reconfigTasks.add(
                    element = ConfigTask(
                        icon = Icons.Outlined.VpnKey,
                        label = "Adding $it",
                        message = ConfigAppKeyAdd(key = it)
                    )
                )
            }

            heartbeatPublication?.let { publication ->
                meshNetwork.networkKey(publication.index)?.let {
                    _reconfigTasks.add(
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
            val elementCount = min(elements.size, newNode.elements.size)
            repeat(times = elementCount) { index ->
                val originalElement = elements[index]
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
                                        _reconfigTasks.add(
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
                val originalElement = this.elements[index]
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
                                            oldNode = this,
                                            newNode = newNode
                                        )
                                    )
                                    _reconfigTasks.add(
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
                val originalElement = this.elements[index]
                val targetElement = newNode.elements[index]
                originalElement
                    .models
                    .filter { it.supportsModelSubscription == true }
                    .forEach { originalModel ->
                        targetElement
                            .model(modelId = originalModel.modelId)
                            ?.let { targetModel ->
                                originalModel
                                    .subscribe
                                    .filter { it !is AllNodes }
                                    .forEach { address ->
                                        _reconfigTasks.add(
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
            meshNetwork
                .nodes
                .filter { it.uuid != newNode.uuid }
                .flatMap { it.elements }
                .flatMap { it.models }
                .filter { model ->
                    model.publish?.address?.let { publicationAddress ->
                        containsElementWithAddress(address = publicationAddress.address)
                    } ?: false
                }.forEach { model ->
                    model.publish?.let { publish ->
                        val newPublication = publish.copy(
                            address = translate(
                                address = publish.address,
                                oldNode = this,
                                newNode = newNode
                            )
                        )
                        _reconfigTasks.add(
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

    /**
     * Executes the enqueued tasks
     *
     * @param meshNetwork MeshNetwork to be configured.
     * @param newNode     Node to be configured.
     */
    suspend fun execute(meshNetwork: MeshNetwork, newNode: Node) =
        withContext(context = Dispatchers.IO) {
            _configTasks.forEachIndexed { index, task ->
                _configTasks[index] = task.copy(status = TaskStatus.InProgress)
                try {
                    meshNetworkManager.send(
                        message = task.message,
                        node = newNode,
                        initialTtl = null
                    )?.let {
                        _configTasks[index] = task.copy(status = TaskStatus.Completed)
                    } ?: run {
                        _configTasks[index] = task.copy(status = TaskStatus.Skipped)
                    }
                } catch (e: Exception) {
                    _configTasks[index] = task.copy(status = TaskStatus.Error(error = e))
                }
            }
            enqueueReconfiguration(meshNetwork = meshNetwork, newNode = newNode)
            _reconfigTasks.forEachIndexed { index, task ->
                _reconfigTasks[index] = task.copy(status = TaskStatus.InProgress)
                try {
                    meshNetworkManager.send(
                        message = task.message,
                        node = newNode,
                        initialTtl = null
                    )?.let {
                        _reconfigTasks[index] = task.copy(status = TaskStatus.Completed)
                    } ?: run {
                        _reconfigTasks[index] = task.copy(status = TaskStatus.Skipped)
                    }
                } catch (e: Exception) {
                    _reconfigTasks[index] = task.copy(status = TaskStatus.Error(error = e))
                }
            }
            _appTasks.forEachIndexed { index, task ->
                _appTasks[index] = task.copy(status = TaskStatus.InProgress)
                try {
                    if(task.message is AcknowledgedMeshMessage){
                        meshNetworkManager.send(
                            message = task.message,
                            model = task.model,
                            localElement = task.element,
                            applicationKey = task.applicationKey,
                            initialTtl = null
                        )?.let {
                            _appTasks[index] = task.copy(status = TaskStatus.Completed)
                        } ?: run {
                            _appTasks[index] = task.copy(status = TaskStatus.Skipped)
                        }
                    } else {
                        meshNetworkManager.send(
                            message = task.message as UnacknowledgedMeshMessage,
                            model = task.model,
                            localElement = task.element,
                            applicationKey = task.applicationKey,
                            initialTtl = null
                        )
                        _appTasks[index] = task.copy(status = TaskStatus.Completed)
                    }
                } catch (e: Exception) {
                    _appTasks[index] = task.copy(status = TaskStatus.Error(error = e))
                }
            }
        }
}