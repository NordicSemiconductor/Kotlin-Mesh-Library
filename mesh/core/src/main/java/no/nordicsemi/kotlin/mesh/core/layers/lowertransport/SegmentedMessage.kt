@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.lowertransport

import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage


/**
 * Interface defining a Segmented message.
 *
 * @property message             Mesh message being sent, or null if the message was received.
 * @property userInitialized     If the message was initialized by the user.
 * @property sequenceZero        13 least significant bits of the SeqAuth.
 * @property segmentOffset       Zero based segment number of segment m of this upper transport PDU.
 * @property lastSegmentNumber   Zero based segment number of the last segment of this upper
 *                               transport PDU.
 * @property isSegmented         True if the message is composed of multiple segments. Single
 *                               segment messages are used to send short, acknowledged messages. The
 *                               maximum size of payload of upper transport control pdu is 8 bytes.
 * @property index               [segmentOffset] as an Int.
 * @property count               Expected number of segments fro this message.
 */
internal interface SegmentedMessage : LowerTransportPdu {
    val message: MeshMessage?
    val userInitialized: Boolean
    val sequenceZero: UShort
    val segmentOffset: UByte
    val lastSegmentNumber: UByte

    val isSegmented: Boolean
        get() = lastSegmentNumber == 0.toUByte()

    val index: Int
        get() = segmentOffset.toInt()

    val count: Int
        get() = lastSegmentNumber.toInt() + 1
}

/**
 * Checks if all segments were received.
 *
 * @receiver List of segmented messages.
 * @return true if all segments were received, false otherwise.
 */
internal fun List<SegmentedMessage?>.isComplete() = none { it == null }

/**
 * Checks if some segments were not yet acknowledged
 *
 * @receiver List of segmented messages.
 * @return true if some segments were not yet acknowledged, false otherwise.
 */
internal fun List<SegmentedMessage?>.hasMore() = any { it != null }

/**
 * Returns the first segment that isn't acknowledged yet.
 *
 * @receiver List of segmented messages.
 * @return The first segment that isn't acknowledged yet or null if all segments were acknowledged.
 */
internal fun List<SegmentedMessage?>.firstNotAcknowledged() = first { it != null }

/**
 * Returns a list of unacknowledged segments
 *
 * @receiver List of segmented messages.
 * @return List of unacknowledged segments.
 */
internal fun List<SegmentedMessage?>.unacknowledged() = filterNotNull()
