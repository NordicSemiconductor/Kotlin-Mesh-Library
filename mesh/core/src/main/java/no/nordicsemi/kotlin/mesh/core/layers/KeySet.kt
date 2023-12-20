@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers

import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyDistribution
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node

/**
 * KeySet defines the credentials used to encrypt a message.
 *
 * @property networkKey       Network key used to encrypt the message.
 * @property accessKey        Access Layer key used to encrypt the message.
 * @property aid              Application key identifier, or 'nil' for Device Key.
 */
internal interface KeySet {
    val networkKey: NetworkKey
    val accessKey: ByteArray
    val aid: UByte?
}

/**
 * AccessKeySet defines the credentials used to by the Access Layer to encrypt a message.
 *
 * @param applicationKey Application key used to encrypt the message.
 * @constructor Creates an AccessKeySet.
 */
internal data class AccessKeySet(val applicationKey: ApplicationKey) : KeySet {
    override val networkKey: NetworkKey
        get() = applicationKey.boundNetworkKey!!
    override val accessKey: ByteArray
        get() = if (networkKey.phase == KeyDistribution)
            applicationKey.oldKey ?: applicationKey.key
        else applicationKey.key
    override val aid: UByte
        get() = if (networkKey.phase == KeyDistribution)
            applicationKey.oldAid ?: applicationKey.aid
        else applicationKey.aid

    override fun toString() = applicationKey.toString()
}

/**
 * DeviceKeySet defines the credentials used to by the Foundation Layer to encrypt a message.
 *
 * @property node Node containing the device key.
 * @constructor Creates a DeviceKeySet.
 */
internal class DeviceKeySet private constructor(
    override val networkKey: NetworkKey,
    val node: Node,
    val deviceKey: ByteArray,
    override val accessKey: ByteArray
) : KeySet {

    override val aid: UByte? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceKeySet

        if (networkKey != other.networkKey) return false
        if (node != other.node) return false
        if (!accessKey.contentEquals(other.accessKey)) return false
        if (aid != other.aid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = networkKey.hashCode()
        result = 31 * result + node.hashCode()
        result = 31 * result + accessKey.contentHashCode()
        result = 31 * result + aid.hashCode()
        return result
    }

    override fun toString(): String {
        return "${node.name}'s Device Key."
    }

    companion object {
        fun init(networkKey: NetworkKey, node: Node): DeviceKeySet? {
            val deviceKey = node.deviceKey ?: return null
            return DeviceKeySet(
                networkKey = networkKey,
                node = node,
                deviceKey = deviceKey,
                accessKey = deviceKey
            )
        }
    }
}