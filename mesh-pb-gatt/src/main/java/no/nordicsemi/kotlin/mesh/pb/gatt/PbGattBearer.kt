@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.pb.gatt

import no.nordicsemi.kotlin.mesh.bearer.BaseGattProxyBearer
import no.nordicsemi.kotlin.mesh.bearer.PduTypes
import no.nordicsemi.kotlin.mesh.bearer.utils.MeshProvisioningService
import no.nordicsemi.kotlin.mesh.provisioning.bearer.ProvisioningBearer

/**
 * Responsible for receiving and sending mesh provisioning messages to and from the GATT Proxy Node.
 */
class PbGattBearer : BaseGattProxyBearer<MeshProvisioningService>(), ProvisioningBearer {
    override val supportedTypes: Array<PduTypes>
        get() = arrayOf(PduTypes.ProvisioningPdu)
}