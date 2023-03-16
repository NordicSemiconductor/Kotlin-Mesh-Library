@file:Suppress("unused")

package no.nordicsemi.android.mesh.provisioning.bearer

import no.nordicsemi.android.mesh.provisioning.ProvisioningRequest
import no.nordicsemi.kotlin.mesh.core.bearer.Bearer
import no.nordicsemi.kotlin.mesh.core.bearer.PduType
import no.nordicsemi.kotlin.mesh.core.exception.InvalidPduType


/**
 * Provisioning bearer is used to send provisioning messages to provisioned nodes.
 */
interface ProvisioningBearer : Bearer {

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