@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import java.util.UUID

/**
 *
 * Storage class used to load and store generate sequence numbers for each unicast address.
 *
 * This is further extended with helper methods for handling Sequence Numbers of outgoing messages
 * from the local Node. Each message must contain a unique 24-bit Sequence Number, which together
 * with 32-bit IV Index ensure that replay attacks are not possible.
 * @see [NetworkPropertiesStorage.nextSequenceNumber], [NetworkPropertiesStorage.resetSequenceNumber], [NetworkPropertiesStorage.removeSequenceNumber]
 *
 * @property sequenceNumbers Contains the sequence number for each unicast address.
 */
interface NetworkPropertiesStorage {

    val sequenceNumbers: MutableMap<UnicastAddress, UInt>
    var ivIndex: IvIndex
    var lastTransitionDate: Instant
    var isIvRecoveryActive: Boolean


    /**
     * Loads Network Properties for a given [uuid].
     *
     * @param scope     Coroutine scope.
     * @param uuid      UUID of the mesh network
     * @param addresses List of unicast addresses.
     */
    suspend fun load(scope: CoroutineScope, uuid: UUID, addresses: List<UnicastAddress>)

    /**
     * Stores the network properties for the given [uuid].
     *
     * @param uuid UUID of the mesh network.
     */
    suspend fun save(uuid: UUID)

    /**
     * Returns the next SEQ number to be used to send a message from the given Unicast Address. Each
     * time this method is called returned value is incremented by 1. Size of SEQ is 24 bits.
     *
     * @param uuid     UUID of the mesh network.
     * @param address  Unicast address of the node or the element.
     * @return next SEQ number to be used.
     */
    suspend fun nextSequenceNumber(uuid: UUID, address: UnicastAddress): UInt {
        // Get the current sequence number for the given address
        val sequenceNumber = (sequenceNumbers[address] ?: 0u)
        // As the sequence number was used , it has to be incremented
        sequenceNumbers[address] = sequenceNumber + 1u
        save(uuid)
        return sequenceNumber
    }

    /**
     * Resets the SEQ associated with all Elements of the given Node to 0. This method should be called
     * when the IV Index is incremented and SEQ number should be reset.
     *
     * @param uuid     UUID of the mesh network.
     * @param node     Node whose sequence number must be reset.
     */
    suspend fun resetSequenceNumber(uuid: UUID, node: Node) {
        node.elements.forEach {
            sequenceNumbers[it.unicastAddress] = 0u
        }
        save(uuid)
    }

    /**
     * Removes the sequence number associated with the given unicast address.
     *
     * @param uuid     UUID of the mesh network.
     * @param address  Unicast address of the node or the element.
     */
    suspend fun removeSequenceNumber(uuid: UUID, address: UnicastAddress) {
        sequenceNumbers.remove(address)
        save(uuid)
    }

    /**
     * Returns the last received SeqAuth value for the given source address or null if no message
     * has ever been received from the given source address.
     *
     * @param source Source address.
     * @return last SeqAuth value or null if no message has ever been received from the given source
     *         address.
     */
    fun lastSeqAuthValue(source: Address): Flow<ULong?>

    /**
     * Stores the last received SeqAuth value for the given source address.
     *
     * @param lastSeqAuth SeqAuth value.
     * @param source      Source address.
     */
    suspend fun storeLastSeqAuthValue(lastSeqAuth: ULong, source: Address)

    /**
     * Returns the previous SeqAuth value for the given source address.
     *
     * @param source Source address
     * @return previous SeqAuth value or null if no more than 1 message has ever been received from
     *         the given source address.
     */
    fun previousSeqAuthValue(source: Address): Flow<ULong?>

    /**
     * Stores the previously received SeqAuth value for the given source address, or nil if no more
     * than 1 message has ever been received from the given source address.
     *
     * @param seqAuth SeqAuth value.
     * @param source  Source address.
     */
    suspend fun storePreviousSeqAuthValue(seqAuth: ULong, source: Address)

    /**
     * Removes all SeqAuth values associated with any of the elements of a given node.
     *
     * @param node Node whose SeqAuth values must be removed.
     */
    suspend fun removeSeqAuthValues(node: Node)

    /**
     * Removes all known SeqAuth values associated with any of the Elements of the given remote
     * Node.
     *
     * @param source Source address of the node or it's element.
     */
    suspend fun removeSeqAuthValues(source: Address)
}

