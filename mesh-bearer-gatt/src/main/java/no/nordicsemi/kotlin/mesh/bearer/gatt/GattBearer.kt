@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.bearer.gatt

import no.nordicsemi.kotlin.mesh.bearer.BaseGattProxyBearer
import no.nordicsemi.kotlin.mesh.bearer.MeshBearer
import no.nordicsemi.kotlin.mesh.bearer.PduTypes
import no.nordicsemi.kotlin.mesh.bearer.utils.MeshProxyService

/**
 * Responsible for receiving and sending mesh messages to and from the GATT Proxy Node.
 */
class GattBearer : BaseGattProxyBearer<MeshProxyService>(), MeshBearer {

    override val supportedTypes: Array<PduTypes>
        get() = arrayOf(PduTypes.NetworkPdu, PduTypes.MeshBeacon, PduTypes.ProxyConfiguration)
}