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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.kotlin.mesh.bearer.*
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import no.nordicsemi.kotlin.mesh.logger.Logger

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
    protected val device: ServerDevice
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
    protected lateinit var dataInCharacteristic: ClientBleGattCharacteristic
    protected lateinit var dataOutCharacteristic: ClientBleGattCharacteristic

    private var client: ClientBleGatt? = null

    var logger: Logger? = null

    abstract suspend fun configureGatt(services: ClientBleGattServices)

    @SuppressLint("MissingPermission")
    override suspend fun open() {
        client = ClientBleGatt.connect(context = context, device = device).takeIf {
            it.isConnected
        }?.let { client ->
            observeConnectionState(client)
            // Discover services on the Bluetooth LE Device.
            val services = client.discoverServices()
            configureGatt(services)
            mtu = client.requestMtu(517) - 3
            client
        }
    }

    override suspend fun close() {
        client?.disconnect()
    }

    /**
     * Observes the connection state of the GATT client and the bearer state.
     *
     * @param client the GATT client.
     */
    private fun observeConnectionState(client: ClientBleGatt) {
        client.connectionState.takeWhile {
            it != GattConnectionState.STATE_DISCONNECTED
        }.onEach { state ->
            if (state == GattConnectionState.STATE_CONNECTED) onConnected()
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
            client = null
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun send(pdu: ByteArray, type: PduType) {
        require(supports(type)) { throw BearerError.PduTypeNotSupported }
        require(isOpen) { throw BearerError.Closed }
        proxyProtocolHandler.segment(pdu, type, mtu).forEach {
            dataInCharacteristic.write(DataByteArray(it), BleWriteType.NO_RESPONSE)
        }
    }

    protected suspend fun awaitNotifications() {
        dataOutCharacteristic.getNotifications().onEach { data ->
            proxyProtocolHandler.reassemble(data.value)?.let { reassembledPdu ->
                _pdus.emit(reassembledPdu)
            }
        }.launchIn(scope)
    }

    companion object {
        const val DEFAULT_MTU = 23 - 3
    }
}