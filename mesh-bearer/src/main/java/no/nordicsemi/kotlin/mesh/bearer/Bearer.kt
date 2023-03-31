@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.bearer

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.kotlin.mesh.bearer.BearerError.PduTypeNotSupported

/**
 * transmitter is responsible for delivering messages to the mesh network.
 */
interface Transmitter {

    /**
     * Sends the given data over the bearer. Data longer than MTU will automatically be segmented
     * using the bearer protocol if bearer implements segmentation.
     *
     * @param pdu     Data to be sent.
     * @param type    Type of the PDU.
     * @throws PduTypeNotSupported if the PDU type is not supported by the bearer.
     */
    suspend fun send(pdu: ByteArray, type: PduType)
}

/**
 * Receiver is responsible for receiving messages from the mesh network.
 * @property pdu A flow that emits events whenever a PDU is received.
 */
interface Receiver {
    /**
     * Returns a flow of received PDUs.
     */
    val pdu: Flow<BearerPdu>

}

/**
 * Bearer is responsible for sending and receiving messages to and from the mesh network.
 *
 * @property state         A flow that emits events whenever the bearer state changes.
 * @property supportedTypes      List of supported PDU types.
 * @property isOpen              Returns true if the bearer is open, false otherwise.
 */
interface Bearer : Transmitter, Receiver {

    val state: Flow<BearerEvent>

    val supportedTypes: Array<PduTypes>

    val isOpen: Boolean

    /**
     * Opens the bearer.
     */
    suspend fun open()

    /**
     * Closes the bearer.
     */
    suspend fun close()

    /**
     * Returns whether the bearer supports the given message type.
     *
     * @param type PDU type.
     * @return True if the bearer supports the given message type, false otherwise.
     */
    fun supports(type: PduType): Boolean = runCatching {
        // TODO: Check against [supportedTypes]
        PduTypes.from(type.value)
    }.isSuccess
}

interface MeshBearer : Bearer