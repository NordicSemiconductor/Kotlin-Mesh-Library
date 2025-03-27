@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

/**
 * This message returns the list of network key indexes added to the mesh node. This is a response
 * to the [ConfigNetKeyGet] message.
 *
 * @property networkKeyIndexes    List of network key indexes added to the mesh node.
 * @property opCode               Message op code.
 * @property parameters           Message parameters.
 * @constructor Constructs the ConfigNetKeyGet message.
 */
class ConfigNetKeyList(
    val networkKeyIndexes: Array<KeyIndex>
) : ConfigResponse {
    override val opCode: UInt = Initializer.opCode
    override val parameters: ByteArray? = null

    constructor(networkKeys: List<NetworkKey>) : this(
        networkKeyIndexes = networkKeys.map {
            it.index
        }.toTypedArray()
    )

    override fun toString() = "ConfigNetKeyList(opCode: $opCode, networkKeyIndex: ${
        networkKeyIndexes.joinToString(separator = ", ", transform = { it.toString() })
    })"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x8043u

        override fun init(parameters: ByteArray?) = parameters?.takeIf {
            it.isNotEmpty()
        }?.let {
            ConfigNetKeyList(ConfigMessage.decode(data = it, offset = 0))
        }
    }
}