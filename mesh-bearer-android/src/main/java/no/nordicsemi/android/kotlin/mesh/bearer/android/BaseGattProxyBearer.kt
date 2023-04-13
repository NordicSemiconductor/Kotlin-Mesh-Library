@file:Suppress("unused", "PropertyName")

package no.nordicsemi.android.kotlin.mesh.bearer.android

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import no.nordicsemi.kotlin.mesh.bearer.*
import no.nordicsemi.kotlin.mesh.logger.Logger

/**
 * Base implementation of the GATT Proxy Bearer.
 *
 * @property state                 Flow that emits events whenever the bearer state changes.
 * @property pdus                  Flow that emits events whenever a PDU is received.
 * @property supportedTypes        List of supported PDU types.
 * @property logger                Logger receives logs sent from the bearer. The logs will contain
 *                                 raw data of sent and received packets, as well as connection
 *                                 events.
 * @property isOpen                Returns true if the bearer is open, false otherwise.
 */
abstract class BaseGattProxyBearer<MeshService> : Bearer {
    protected val _pdu = MutableSharedFlow<BearerPdu>()
    override val state: Flow<BearerEvent> = MutableSharedFlow()
    override val supportedTypes: Array<PduTypes> = arrayOf(
        PduTypes.NetworkPdu,
        PduTypes.MeshBeacon,
        PduTypes.ProxyConfiguration,
        PduTypes.ProvisioningPdu
    )
    override val isOpen: Boolean
        get() = isOpened

    private val proxyProtocolHandler = ProxyProtocolHandler()
    private lateinit var queue: Array<ByteArray>
    private var isOpened = false

    var logger: Logger? = null

    override suspend fun open() {
        isOpened = true
        // TODO("Not yet implemented")
    }

    override suspend fun close() {
        isOpened = false
        // TODO("Not yet implemented")
    }

    override suspend fun send(pdu: ByteArray, type: PduType) {
        require(supports(type)) { throw BearerError.PduTypeNotSupported }

        require(isOpen) { throw BearerError.BearerClosed }

    }
}