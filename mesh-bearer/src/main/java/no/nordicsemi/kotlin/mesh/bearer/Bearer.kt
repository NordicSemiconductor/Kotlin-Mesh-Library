@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.bearer

import no.nordicsemi.kotlin.mesh.bearer.BearerError.PduTypeNotSupported

/**
 * A transmitter is responsible for delivering messages to the mesh network.
 */
interface Transmitter {

    /**
     * Sends the given data over the bearer. Data longer than MTU will automatically be segmented
     * using the bearer protocol if bearer implements segmentation.
     *
     * @param pdu     Data to be sent.
     * @param pduType Type of the PDU.
     * @throws PduTypeNotSupported if the PDU type is not supported by the bearer.
     */
    fun send(pdu: ByteArray, pduType: PduType)
}

interface Bearer : Transmitter {

    var supportedTypes: PduTypes

    var isOpen: Boolean

    /**
     * Opens the bearer.
     */
    fun open()

    /**
     * Closes the bearer.
     */
    fun close()

    /**
     * Returns whether the bearer supports the given message type.
     * @param pduType PDU type.
     * @return True if the bearer supports the given message type, false otherwise.
     */
    fun supports(pduType: PduType): Boolean = runCatching {
        PduTypes.from(pduType.type)
    }.isSuccess
}

interface MeshBearer : Bearer