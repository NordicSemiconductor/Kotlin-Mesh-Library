@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core

import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress

/**
 *
 * Storage class used to load and store generate sequence numbers for each unicast address.
 *
 * This is further extended with helper methods for handling Sequence Numbers of outgoing messages
 * from the local Node. Each message must contain a unique 24-bit Sequence Number, which together
 * with 32-bit IV Index ensure that replay attacks are not possible.
 * @see [SequenceNumberStorage.next], [SequenceNumberStorage.reset], [SequenceNumberStorage.remove]
 *
 * @property values Contains the sequence number for each unicast address.
 */
interface SequenceNumberStorage {

    val values: MutableMap<UnicastAddress, UInt>

    /**
     * Loads the sequence number from the local Sequence number storage. This method should be
     * called when loading
     */
    suspend fun load()

    /**
     * Stores the sequence number in the local Sequence number storage.
     */
    suspend fun save()
}

/**
 * Returns the next SEQ number to be used to send a message from the given Unicast Address. Each
 * time this method is called returned value is incremented by 1. Size of SEQ is 24 bits.
 *
 * @param address Unicast address of the node or the element.
 * @return next SEQ number to be used.
 */
internal suspend fun SequenceNumberStorage.next(address: UnicastAddress): UInt {
    // Get the current sequence number for the given address
    val sequenceNumber = (values[address] ?: 0u)
    // As the sequence number was used , it has to be incremented
    values[address] = sequenceNumber + 1u
    save()
    return sequenceNumber
}

/**
 * Resets the SEQ associated with all Elements of the given Node to 0. This method should be called
 * when the IV Index is incremented and SEQ number should be reset.
 *
 * @param node Node whose sequence number must be reset.
 */
internal suspend fun SequenceNumberStorage.reset(node: Node) {
    node.elements.forEach {
        values[it.unicastAddress] = 0u
    }
    save()
}

/**
 * Removes the sequence number associated with the given unicast address.
 *
 * @param unicastAddress Unicast address of the node or the element.
 */
internal suspend fun SequenceNumberStorage.remove(unicastAddress: UnicastAddress) {
    values.remove(unicastAddress)
    save()
}
