@file:Suppress("unused", "PropertyName")

package no.nordicsemi.android.kotlin.mesh.bearer.android

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.WriteType
import no.nordicsemi.kotlin.mesh.bearer.Bearer
import no.nordicsemi.kotlin.mesh.bearer.BearerError
import no.nordicsemi.kotlin.mesh.bearer.BearerEvent
import no.nordicsemi.kotlin.mesh.bearer.Pdu
import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.bearer.PduTypes
import no.nordicsemi.kotlin.mesh.bearer.ProxyProtocolHandler
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import no.nordicsemi.kotlin.mesh.logger.Logger
import kotlin.uuid.ExperimentalUuidApi

/**
 * Base implementation of the GATT Proxy Bearer.
 *
 * @property state             Flow that emits events whenever the bearer state changes.
 * @property pdus              Flow that emits events whenever a PDU is received.
 * @property supportedTypes    List of supported PDU types.
 * @property logger            Logger receives logs sent from the bearer. The logs will contain raw
 *                             data of sent and received packets, as well as connection events.
 * @property isOpen            Returns true if the bearer is open, false otherwise.
 */
abstract class BaseGattProxyBearer<MeshService>(
    protected val context: Context,
    protected val centralManager: CentralManager,
    protected val peripheral: Peripheral,
) : Bearer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _pdus = MutableSharedFlow<Pdu>()
    override val pdus: Flow<Pdu> = _pdus.asSharedFlow()
    private val _state = MutableStateFlow<BearerEvent>(BearerEvent.Closed(BearerError.Closed))
    override val state: StateFlow<BearerEvent> = _state.asStateFlow()
    override val supportedTypes: Array<PduTypes> = arrayOf(
        PduTypes.NetworkPdu,
        PduTypes.MeshBeacon,
        PduTypes.ProxyConfiguration,
        PduTypes.ProvisioningPdu
    )
    override val isOpen: Boolean
        get() = isOpened
    private var mtu: Int = DEFAULT_MTU

    private val proxyProtocolHandler = ProxyProtocolHandler()
    override val isGatt: Boolean = true
    private var isOpened = false
    private lateinit var queue: Array<ByteArray>
    protected lateinit var dataInCharacteristic: RemoteCharacteristic

    private var logger: Logger? = null

    abstract suspend fun configureGatt(services: List<RemoteService>)

    @OptIn(ExperimentalUuidApi::class)
    @SuppressLint("MissingPermission")
    override suspend fun open() {
        // Observe the connection state
        observeConnectionState(peripheral)
        // Connect to the peripheral
        centralManager.connect(peripheral = peripheral)
        // Request the maximum transmission unit (MTU) size.
        peripheral.requestHighestValueLength()
        // Start observing the discovered services
        peripheral.services()
            .first { it.isNotEmpty() }
            .let { configureGatt(services = it) }
    }

    override suspend fun close() {
        peripheral.disconnect()
    }

    /**
     * Observes the connection state of the GATT client and the bearer state.
     *
     * @param peripheral Peripheral to observe.
     */
    private fun observeConnectionState(peripheral: Peripheral) {
        peripheral.state.takeWhile {
            it !is ConnectionState.Disconnected
        }.onEach {
            if (it is ConnectionState.Connected) onConnected()
        }.onCompletion { throwable ->
            throwable?.let { logger?.e(LogCategory.BEARER) { "Something went wrong $it" } }
            onDisconnected()
        }.launchIn(scope)
    }

    /**
     * Invoked when the bearer is opened
     */
    private fun onConnected() {
        isOpened = true
        _state.value = BearerEvent.Opened
        logger?.v(LogCategory.BEARER) { "Bearer opened." }
    }


    /**
     * Invoked when the bearer is closed
     */
    private fun onDisconnected() {
        if (isOpened) {
            isOpened = false
            _state.value = BearerEvent.Closed(BearerError.Closed)
            logger?.v(LogCategory.BEARER) { "Bearer closed." }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun send(pdu: ByteArray, type: PduType) {
        require(supports(type)) { throw BearerError.PduTypeNotSupported }
        require(isOpen) { throw BearerError.Closed }
        proxyProtocolHandler.segment(pdu, type, mtu)
            .forEach { dataInCharacteristic.write(it, WriteType.WITHOUT_RESPONSE) }
    }

    protected suspend fun awaitNotifications(dataOutCharacteristic: RemoteCharacteristic) {
        dataOutCharacteristic.subscribe()
            .onEach {
                proxyProtocolHandler.reassemble(data = it)?.let { pdu ->
                    _pdus.emit(pdu)
                }
            }
            .onCompletion { logger?.v(LogCategory.BEARER) { "AAA Device disconnected" } }
            .launchIn(scope)
    }

    companion object {
        const val DEFAULT_MTU = 23 - 3
    }
}