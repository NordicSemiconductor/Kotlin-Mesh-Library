@file:Suppress("unused")

package no.nordicsemi.android.kotlin.mesh.bearer.pbgatt

import no.nordicsemi.android.kotllin.mesh.bearer.ble.BaseGattProxyBearer
import no.nordicsemi.android.kotllin.mesh.bearer.ble.utils.MeshProvisioningService
import no.nordicsemi.kotlin.mesh.bearer.PduTypes
import no.nordicsemi.kotlin.mesh.bearer.provisioning.MeshProvisioningBearer

/**
 * Responsible for receiving and sending mesh provisioning messages to and from the GATT Proxy Node.
 */
open class PbGattBearer : BaseGattProxyBearer<MeshProvisioningService>(), MeshProvisioningBearer {
    override val supportedTypes: Array<PduTypes>
        get() = arrayOf(PduTypes.ProvisioningPdu)


}