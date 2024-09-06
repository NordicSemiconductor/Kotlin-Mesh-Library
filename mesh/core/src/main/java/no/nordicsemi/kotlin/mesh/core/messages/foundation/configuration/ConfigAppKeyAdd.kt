@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.exception.InvalidKeyLength
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetAndAppKeyMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetAndAppKeyMessage.Companion.decodeNetAndAppKeyIndex
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetAndAppKeyMessage.Companion.encodeNetAndAppKeyIndex
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

/**
 * This message is used to add a network key to a mesh node.
 *
 * @property networkKeyIndex      Index of the network key to be added.
 * @property key                  The network key to be added.
 * @property opCode               Message op code.
 * @property parameters           Message parameters.
 * @property responseOpCode       Op Code of the response message.
 * @constructor Constructs the ConfigAppKeyAdd message.
 */
class ConfigAppKeyAdd(
    override val applicationKeyIndex: KeyIndex,
    val key: ByteArray,
    override val networkKeyIndex: KeyIndex
) : AcknowledgedConfigMessage, ConfigNetAndAppKeyMessage {
    override val opCode: UInt = Initializer.opCode

    override val parameters = encodeNetAndAppKeyIndex(
        appKeyIndex = applicationKeyIndex,
        netKeyIndex = networkKeyIndex
    ) + key

    override val responseOpCode = ConfigAppKeyStatus.opCode

    /**
     * Convenience constructor to create a [ConfigAppKeyAdd] message.
     *
     * @param applicationKey Network key to be added.
     * @constructor Constructs the ConfigAppKeyAdd message.
     */
    constructor(applicationKey: ApplicationKey) : this(
        applicationKeyIndex = applicationKey.index,
        key = applicationKey.key,
        networkKeyIndex = applicationKey.boundNetKeyIndex
    )

    init {
        require(key.size == 16) { throw InvalidKeyLength }
    }

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x00u

        /**
         * Initializes the [ConfigAppKeyAdd] based on the given parameters.
         *
         * @param parameters Message parameters.
         * @return ConfigAppKeyAdd or null if the parameters are invalid.
         */
        override fun init(parameters: ByteArray?) = parameters?.takeIf {
            it.size == 19
        }?.let {
            val decodedIndexes = decodeNetAndAppKeyIndex(data = it, offset = 0)
            ConfigAppKeyAdd(
                networkKeyIndex = decodedIndexes.networkKeyIndex,
                applicationKeyIndex = decodedIndexes.applicationKeyIndex,
                key = it.copyOfRange(3, 19)
            )
        }
    }
}