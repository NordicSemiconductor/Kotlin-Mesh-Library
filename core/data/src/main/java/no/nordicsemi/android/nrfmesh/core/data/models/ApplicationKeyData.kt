@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package no.nordicsemi.android.nrfmesh.core.data.models

import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

/**
 * Application Keys are used to secure communications at the upper transport layer.
 * The application key (AppKey) shall be generated using a random number generator
 * compatible with the requirements in Volume 2, Part H, Section 2 of the Core Specification [1].
 *
 * @property index              The index property contains an integer from 0 to 4095 that
 *                              represents the NetKey index for this network key.
 * @property name               Human-readable name for the application functionality associated
 *                              with this application key.
 * @property boundNetKeyIndex   The boundNetKey property contains a corresponding Network Key index
 *                              of the network key in the mesh network.
 * @property key                128-bit application key.
 * @property oldKey             OldKey property contains the previous application key.
 * @property boundNetworkKey             Network key to which this application key is bound to.
 * @param    key               128-bit application key.
 */
data class ApplicationKeyData(
    val name: String,
    val index: KeyIndex,
    val key: ByteArray,
    val oldKey: ByteArray? = null,
    val boundNetKeyIndex: KeyIndex,
    val boundNetworkKey: NetworkKey?,
    val isInUse: Boolean
) {
    constructor(key: ApplicationKey) : this(
        name = key.name,
        index = key.index,
        key = key.key,
        oldKey = key.oldKey,
        boundNetKeyIndex = key.boundNetKeyIndex,
        boundNetworkKey = key.boundNetworkKey,
        isInUse = key.isInUse
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApplicationKeyData

        if (name != other.name) return false
        if (index != other.index) return false
        if (!key.contentEquals(other.key)) return false
        if (oldKey != null) {
            if (other.oldKey == null) return false
            if (!oldKey.contentEquals(other.oldKey)) return false
        } else if (other.oldKey != null) return false
        if (boundNetKeyIndex != other.boundNetKeyIndex) return false
        if (boundNetworkKey != other.boundNetworkKey) return false
        if (isInUse != other.isInUse) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + index.hashCode()
        result = 31 * result + key.contentHashCode()
        result = 31 * result + (oldKey?.contentHashCode() ?: 0)
        result = 31 * result + boundNetKeyIndex.hashCode()
        result = 31 * result + (boundNetworkKey?.hashCode() ?: 0)
        result = 31 * result + isInUse.hashCode()
        return result
    }
}