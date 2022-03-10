@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

/**
 * The publish object represents parameters that define how the messages are published by a mesh model.
 *
 * @property address            The address property contains the publication [MeshAddress] for the model containing a [Address].
 * @property index              The index property contains an integer that represents an [ApplicationKey] index, indicating which
 *                              Application Key to use for the publication. The application key index corresponds to the index value
 *                              of one of the application key entries in the node’s appKeys array.
 * @property ttl                The ttl property contains an integer from 0 to 127 that represents the Time to Live (TTL) value for
 *                              published messages or an integer with a value of 255 that indicates that the node’s default TTL is used
 *                              for publication.
 * @property period             The period property refers to [PublishPeriod] that describes the interval between subsequent publications.
 *                              If the value of this property is 0, the periodic publication is disabled.
 * @property credentials        The [Credentials] property contains an integer of 0 or 1 that represents whether managed flooding security
 *                              material (0) or friendship security material (1) is used.
 * @property retransmit         The [Retransmit] property describes the number of times a message is published and the interval between
 *                              retransmissions of the published messages.
 */
@Serializable
data class Publish(
    val address: PublicationAddress,
    val index: Int,
    val ttl: Int,
    val period: PublishPeriod,
    val credentials: Int,
    val retransmit: Retransmit
) {
    @Transient
    var label: UUID? = null
        internal set
}