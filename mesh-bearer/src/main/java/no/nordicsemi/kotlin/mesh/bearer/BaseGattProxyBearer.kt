@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.bearer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Base implementation of the GATT Proxy Bearer.
 *
 * @property bearerState           Flow that emits events whenever the bearer state changes.
 * @property pdu                   Flow that emits events whenever a PDU is received.
 * @property supportedTypes        List of supported PDU types.
 * @property isOpen                Returns true if the bearer is open, false otherwise.
 */
open class BaseGattProxyBearer<MeshService> : Bearer {
    override val bearerState: Flow<BearerEvent> = MutableSharedFlow()
    override val pdu: Flow<BearerPdu> = MutableSharedFlow()
    override val supportedTypes: Array<PduTypes> = arrayOf(
        PduTypes.NetworkPdu,
        PduTypes.MeshBeacon,
        PduTypes.ProxyConfiguration,
        PduTypes.ProvisioningPdu
    )

    override val isOpen: Boolean
        get() = TODO("Not yet implemented")

    override fun open() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun send(pdu: ByteArray, type: PduType) {
        TODO("Not yet implemented")
    }
}