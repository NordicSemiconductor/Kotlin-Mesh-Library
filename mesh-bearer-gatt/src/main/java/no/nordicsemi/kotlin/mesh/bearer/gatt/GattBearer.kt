@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.bearer.gatt

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.kotlin.mesh.bearer.android.BaseGattProxyBearer
import no.nordicsemi.android.kotllin.mesh.bearer.ble.utils.MeshProxyService
import no.nordicsemi.kotlin.mesh.bearer.BearerPdu
import no.nordicsemi.kotlin.mesh.bearer.MeshBearer
import no.nordicsemi.kotlin.mesh.bearer.PduTypes

/**
 * Responsible for receiving and sending mesh messages to and from the GATT Proxy Node.
 */
class GattBearer : BaseGattProxyBearer<MeshProxyService>(), MeshBearer {

    override val pdus: Flow<BearerPdu> = _pdu.asSharedFlow()

    override val supportedTypes: Array<PduTypes> =
        arrayOf(PduTypes.NetworkPdu, PduTypes.MeshBeacon, PduTypes.ProxyConfiguration)
}