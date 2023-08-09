@file:Suppress("unused", "MemberVisibilityCanBePrivate")
@file:OptIn(DelicateCoroutinesApi::class)

package no.nordicsemi.kotlin.mesh.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.kotlin.mesh.bearer.BearerError
import no.nordicsemi.kotlin.mesh.core.messages.proxy.AddAddressesToFilter
import no.nordicsemi.kotlin.mesh.core.messages.proxy.FilterStatus
import no.nordicsemi.kotlin.mesh.core.messages.proxy.ProxyConfigurationMessage
import no.nordicsemi.kotlin.mesh.core.messages.proxy.RemoveAddressesFromFilter
import no.nordicsemi.kotlin.mesh.core.messages.proxy.SetFilterType
import no.nordicsemi.kotlin.mesh.core.model.AllNodes
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.ProxyFilterAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import no.nordicsemi.kotlin.mesh.logger.Logger

/**
 * Enum class that defines Proxy filter types.
 *
 * @property type Filter type.
 */
enum class ProxyFilterType(val type: UByte) {

    /**
     * An inclusion list filter has an associated inclusion list containing destination addresses
     * that are of interest for the Proxy Client.
     *
     * The inclusion list filter blocks all messages except those targeting addresses added to the
     * list.
     */
    INCLUSION_LIST(0x00u),

    /**
     * An exclusion list filter has an associated exclusion list containing destination addresses
     * that are NOT of the Proxy Client interest.
     *
     * The exclusion list filter forwards all messages except those targeting addresses added to the
     * list.
     */
    EXCLUSION_LIST(0x01u);

    companion object {

        /**
         * Returns the [ProxyFilterType] from the given filter type.
         *
         * @param filterType Filter type.
         * @return [ProxyFilterType] or if invalid throws an [IllegalArgumentException] exception.
         * @throws IllegalArgumentException if the filter type is invalid.
         */
        @Throws(IllegalArgumentException::class)
        fun from(filterType: UByte) = ProxyFilterType.values().find {
            it.type == filterType
        } ?: throw IllegalArgumentException("Illegal filter type")
    }

    override fun toString() = when (this) {
        INCLUSION_LIST -> "Inclusion List"
        EXCLUSION_LIST -> "Exclusion List"
    }
}

/**
 * Proxy filter state defines the state of the proxy filter.
 */
sealed class ProxyFilterState {

    /**
     * State defining when the Proxy filter has been sent to the proxy.
     *
     * @property type      Filter type.
     * @property addresses List of addresses.
     */
    data class ProxyFilterUpdated internal constructor(
        val type: ProxyFilterType,
        val addresses: List<ProxyFilterAddress>
    ) : ProxyFilterState()

    /**
     * State defining when the Proxy filter has been acknowledged by the proxy.
     *
     * @property type     Filter type.
     * @property listSize List of addresses.
     */
    data class ProxyFilterUpdateAcknowledged internal constructor(
        val type: ProxyFilterType,
        val listSize: UShort
    ) : ProxyFilterState()

    /**
     * Defines a state where the connected proxy defines supports only one single address in the
     * Proxy Filter list.
     *
     * @property maxSize Number of addresses that can be added to the proxy filter list of the Proxy
     *                   node.
     */
    data class LimitedProxyFilterDetected(val maxSize: Int) : ProxyFilterState()
}

/**
 * An enumeration defining possible initial configuration states of the proxy filter.
 */
enum class ProxyFilterSetup {

    /**
     * This setup will set to [ProxyFilterType.INCLUSION_LIST] with [UnicastAddress]es of all local
     * elements, all [GroupAddress]es with at least one local model subscribed to the [AllNodes]
     * address.
     */
    AUTOMATIC,

    /**
     * The Proxy Filter on each connected Proxy Node will be set to [ProxyFilterType.INCLUSION_LIST]
     * with the given set of addresses.
     */
    INCLUSION_LIST,

    /**
     * The Proxy Filter on each connected Proxy Node will be set to [ProxyFilterType.EXCLUSION_LIST]
     * with the given set of addresses.
     */
    EXCLUSION_LIST
}

internal sealed interface ProxyFilterEventHandler {

    /**
     * Clears the current proxy filter state.
     */
    fun onNewNetworkCreated()

    /**
     * Invoked when a possible change of Proxy Node have been discovered.
     *
     * This method is called in two cases: when the first Secure Network beacon was received (which
     * indicates the first successful connection to a Proxy since app was started) or when the
     * received Secure Network beacon contained information about the same Network Key as one
     * received before. This happens during a reconnection to the same or a different Proxy on the
     * same subnetwork, but may also happen in other Circumstances, for example when the IV Update
     * or Key Refresh Procedure is in progress, or a Network Key was removed and added again.
     *
     * This method reloads the Proxy Filter for the local Provisioner, adding all the addresses the
     * Provisioner is subscribed to, including its Unicast Addresses and All Nodes address.
     */
    suspend fun newProxyDidConnect()

    /**
     * Invoked when a Proxy Configuration Message has been sent. This method refreshes the local
     * type and list of addresses.
     *
     * @param message The message sent.
     */
    suspend fun onManagerDidDeliverMessage(message: ProxyConfigurationMessage)

    /**
     * Invoked when the manager failed to send the Proxy Configuration Message.
     *
     * This method clears the local filter and sets it back to [ProxyFilterType.INCLUSION_LIST].
     * All the messages waiting to be sent are cancelled.
     *
     * @param message Message that has not been sent.
     * @param error Error received.
     */
    suspend fun onManagerFailedToDeliverMessage(
        message: ProxyConfigurationMessage,
        error: Throwable
    )

    /**
     * Handler for the received Proxy Configuration Messages.
     *
     * This method notifies the delegate about changes in the Proxy Filter.
     *
     * If a mismatch is detected between the local list of services and the list size received, the
     * method will try to clear the remote filter and send all the addresses again.
     *
     * @param message Message received.
     * @param proxy   Connected Proxy Node, or `null` if the Node is unknown.
     */
    suspend fun handle(message: ProxyConfigurationMessage, proxy: Node?)
}

/**
 * Proxy filter allows modifying the proxy filter of the connected Proxy Node.
 *
 * Initially, upon connection to a Proxy Node, the manager will automatically subscribe to the
 * [UnicastAddress]es of all local Elements and all [GroupAddress]es with at least one local Model
 * is subscribed to, including [AllNodes] address.
 *
 * Node: When a local Model gets subscribed to a new Group, or is unsubscribed from a Group that no
 *       other local Model is subscribed to, the proxy filter needs to be modified manually by
 *       calling proper add/remove methods.
 *
 *
 * @property scope Coroutine scope
 * @property mutex Mutex for internal synchronization
 * @property manager Mesh network manager
 */
class ProxyFilter internal constructor(val scope: CoroutineScope) : ProxyFilterEventHandler {

    private val _proxyFilterStateFlow = MutableSharedFlow<ProxyFilterState>()
    val proxyFilterStateFlow = _proxyFilterStateFlow.asSharedFlow()
    internal var manager: MeshNetworkManager? = null

    // A mutex for internal synchronization.
    private val mutex = Mutex(locked = true)

    // The counter is used to prevent from refreshing the filter in a loop when the Proxy Server
    // responds with an unexpected list size.
    private var counter = 0

    // The flag is set to 'true' when a request has been sent to the connected proxy. It is cleared
    // when a response was received, or in case of an error.
    private var busy = false

    // A queue of proxy configuration messages enqueued to be sent.
    private var buffer = mutableListOf<ProxyConfigurationMessage>()
    private val logger: Logger?
        get() = manager?.logger

    var state: ProxyFilterState? = null
    var initializeState: ProxyFilterSetup = ProxyFilterSetup.AUTOMATIC

    private var _addresses = mutableListOf<ProxyFilterAddress>()
    val addresses: List<ProxyFilterAddress>
        get() = _addresses

    var type: ProxyFilterType = ProxyFilterType.INCLUSION_LIST
        private set
    var proxy: Node? = null
        private set

    internal fun use(manager: MeshNetworkManager) {
        this.manager = manager
    }

    /**
     * Sets the Filter Type on the connected GATT Proxy node. The filter will be emptied.
     *
     * @param type Filter type.
     */
    suspend fun setType(type: ProxyFilterType) {
        send(SetFilterType(type))
    }

    /**
     * Resets the filter to an empty inclusion list filter.
     */
    suspend fun reset() {
        send(SetFilterType(ProxyFilterType.INCLUSION_LIST))
    }

    /**
     * Clears the current filter
     */
    suspend fun clear() {
        send(SetFilterType(type))
    }

    /**
     * Adds the given address to the active filter.
     *
     * @param address Address to be added to the filter.
     */
    suspend fun add(address: ProxyFilterAddress) {
        send(AddAddressesToFilter(listOf(address)))
    }

    /**
     * Adds the given addresses to the active filter.
     *
     * Proxy message must fit in a single Network PDU, therefore may contain maximum of 5 addresses.
     *
     * @param addresses List of addresses to be added to the filter.
     */
    suspend fun add(addresses: List<ProxyFilterAddress>) {
        addresses.chunked(5).forEach { chunk ->
            send(AddAddressesToFilter(chunk))
        }
    }

    /**
     * Adds the given group to the active filter.
     *
     * @param group Group to be added to the filter.
     */
    suspend fun add(group: Group) {
        add(listOf(group))
    }

    /**
     * Adds the given list of groups to the active filter.
     *
     * @param groups Group to be added to the filter.
     */
    suspend fun add(groups: List<Group>) {
        add(groups.map { it.address as ProxyFilterAddress })
    }

    /**
     * Removes the given address from the active filter.
     *
     * @param address Address to be removed from the filter.
     */
    suspend fun remove(address: ProxyFilterAddress) {
        send(RemoveAddressesFromFilter(listOf(address)))
    }

    /**
     * Removes the given list of given addresses from the active filter.
     *
     * @param addresses List of addresses to be removed from the filter.
     */
    suspend fun remove(addresses: List<ProxyFilterAddress>) {
        addresses.chunked(5).forEach { chunk ->
            send(RemoveAddressesFromFilter(chunk))
        }
    }

    /**
     * Adds all the addresses the provisioner is subscribed to the proxy filter.
     *
     * @param provisioner Provisioner to be added to the filter.
     */
    suspend fun setup(provisioner: Provisioner) = provisioner.node?.run {
        val addresses = mutableListOf<ProxyFilterAddress>()
        addresses.addAll(elements.map { it.unicastAddress })
        addresses.addAll(elements
            .flatMap { it.models }
            .flatMap { it.subscribe }
            .filter { it is ProxyFilterAddress }.map { it as ProxyFilterAddress })

        addresses.add(AllNodes)

        add(addresses.distinct())
    }

    suspend fun proxyDidDisconnect() {
        mutex.withLock {
            busy = false
            proxy = null
        }
    }

    /**
     * Sends the given message to the Proxy Server. If a previous message is still waiting for
     * status, this will buffer the message and send it after the status is received.
     *
     * @param message Message to be sent.
     */
    private suspend fun send(message: ProxyConfigurationMessage) {
        manager?.let { manager ->
            val wasBusy = mutex.withLock { busy }

            require(!wasBusy) {
                mutex.withLock { buffer.add(message) }
                return
            }
            mutex.withLock { busy = true }

            try {
                manager.send(message)
            } catch (e: Exception) {
                mutex.withLock { busy = false }
            }
        }
    }

    override fun onNewNetworkCreated() {
        type = ProxyFilterType.INCLUSION_LIST
        _addresses.clear()
        buffer.clear()
        busy = false
        counter = 0
        proxy = null
    }

    override suspend fun newProxyDidConnect() {
        manager?.let {
            proxyDidDisconnect()
            logger?.i(LogCategory.PROXY) { "New Proxy connected." }

            manager?.network?.localProvisioner?.let { provisioner ->
                when (initializeState) {
                    ProxyFilterSetup.AUTOMATIC -> setup(provisioner = provisioner)
                    ProxyFilterSetup.EXCLUSION_LIST -> {
                        setType(type = ProxyFilterType.EXCLUSION_LIST)
                        add(addresses = addresses)
                    }

                    ProxyFilterSetup.INCLUSION_LIST -> add(addresses = addresses)
                }
            }
        }
    }

    override suspend fun onManagerDidDeliverMessage(message: ProxyConfigurationMessage) {
        mutex.withLock {
            when (message) {
                is AddAddressesToFilter -> {
                    _addresses = _addresses.distinctBy { message.addresses }.toMutableList()
                }

                is RemoveAddressesFromFilter -> {
                    _addresses.removeAll { message.addresses.contains(it) }
                }

                is SetFilterType -> {
                    type = message.filterType
                    _addresses.clear()
                }

                else -> {

                }
            }

            // Notify the app about the current state
            scope.launch {
                _proxyFilterStateFlow.emit(
                    value = ProxyFilterState.ProxyFilterUpdated(type = type, addresses = addresses)
                )
            }
        }
    }

    override suspend fun onManagerFailedToDeliverMessage(
        message: ProxyConfigurationMessage,
        error: Throwable
    ) {
        mutex.withLock {
            type = ProxyFilterType.INCLUSION_LIST
            _addresses.clear()
            buffer.clear()
            busy = false
        }

        if (error is BearerError.Closed) proxy = null
        // Notify the app about the current state
        scope.launch {
            _proxyFilterStateFlow.emit(
                value = ProxyFilterState.ProxyFilterUpdated(type = type, addresses = addresses)
            )
        }
    }

    override suspend fun handle(message: ProxyConfigurationMessage, proxy: Node?) {
        manager?.let { manager ->
            when (message) {
                is FilterStatus -> {
                    this.proxy = proxy
                    mutex.withLock {
                        buffer.takeIf {
                            it.isNotEmpty()
                        }?.let { it ->
                            try {
                                manager.send(it.removeFirst())
                            } catch (e: Exception) {
                                // Handle send error if needed
                            }
                            busy = false
                        }
                    }

                    require(type == message.filterType && addresses.count() == message.listSize.toInt()) {
                        // The counter is used to prevent from refreshing the filter in a loop when
                        // the Proxy Server responds with an unexpected list size.
                        require(counter == 0) {
                            logger?.e(LogCategory.PROXY) {
                                "Proxy Filter lost track of devices"
                            }
                            counter = 0
                            return
                        }
                        counter += 1

                        // Some devices support just a single address in Proxy Filter. After adding
                        // 2+ devices they will reply with list size = 1. In that case we could
                        // either switch to an exclusion list to get all the traffic, or add only 1
                        // address. By default, this library will add the 0th Element's Unicast
                        // Address to allow configuration, as this is the most common use case. If
                        // you need to receive messages sent to group addresses or other Elements,
                        // switch to exclusion list filter.
                        when (message.listSize.toInt() == 1) {
                            true -> {
                                logger?.w(LogCategory.PROXY) { "Limited Proxy Filter detected." }
                                reset()
                                this.manager?.network?.localProvisioner?.primaryUnicastAddress?.let {
                                    mutex.withLock {
                                        _addresses.add(it)
                                    }
                                    add(addresses = addresses)
                                }
                                setType(type = ProxyFilterType.EXCLUSION_LIST)
                                add(addresses = listOf(addresses[0]))
                                _proxyFilterStateFlow.emit(
                                    ProxyFilterState.LimitedProxyFilterDetected(maxSize = 1)
                                )
                            }

                            false -> {
                                logger?.e(LogCategory.PROXY) { "Refreshing Proxy Filter..." }
                                val addresses = addresses
                                reset()
                                add(addresses = addresses)
                            }
                        }
                        return
                    }
                    counter = 0
                    scope.launch {
                        _proxyFilterStateFlow.emit(
                            value = ProxyFilterState.ProxyFilterUpdateAcknowledged(
                                type = type,
                                listSize = message.listSize
                            )
                        )
                    }
                }

                else -> {
                    // Ignore
                }
            }
        }
    }
}