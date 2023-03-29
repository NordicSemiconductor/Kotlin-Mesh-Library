@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.bearer.gatt

import no.nordicsemi.android.kotllin.mesh.bearer.ble.BaseGattProxyBearer
import no.nordicsemi.android.kotllin.mesh.bearer.ble.utils.MeshProxyService
import no.nordicsemi.kotlin.mesh.bearer.MeshBearer
import no.nordicsemi.kotlin.mesh.bearer.PduTypes

/**
 * Responsible for receiving and sending mesh messages to and from the GATT Proxy Node.
 */
class GattBearer : BaseGattProxyBearer<MeshProxyService>(), MeshBearer {

    override val supportedTypes: Array<PduTypes> =
        arrayOf(PduTypes.NetworkPdu, PduTypes.MeshBeacon, PduTypes.ProxyConfiguration)
}