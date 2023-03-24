@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.provisioning.bearer

import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.bearer.provisioning.MeshProvisioningBearer
import no.nordicsemi.kotlin.mesh.core.exception.InvalidPduType
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningRequest


/**
 * Provisioning bearer is used to send provisioning messages to provisioned nodes.
 */
interface ProvisioningBearer : MeshProvisioningBearer {

    /**
     * Sends the given provisioning request using the provisioning bearer.
     *
     * @param request Provisioning request to be sent.
     * @throws InvalidPduType if the PDU type is not supported by the bearer.
     */
    @Throws(InvalidPduType::class)
    fun send(request: ProvisioningRequest) {
        send(request.pdu, PduType.PROVISIONING_PDU)
    }
}