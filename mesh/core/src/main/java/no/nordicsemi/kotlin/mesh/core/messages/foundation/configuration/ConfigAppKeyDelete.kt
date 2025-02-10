@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetAndAppKeyMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetAndAppKeyMessage.Companion.decodeNetAndAppKeyIndex
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetAndAppKeyMessage.Companion.encodeNetAndAppKeyIndex
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

/**
 * This message is used to delete an application key from a mesh node.
 *
 * @property keyIndex  Index of the application key to be deleted.
 * @property index      Index of the bound network key.
 * @property opCode               Message op code.
 * @property parameters           Message parameters.
 * @property responseOpCode       Op Code of the response message.
 * @constructor Constructs the ConfigAppKeyAdd message.
 */
class ConfigAppKeyDelete(
    override val keyIndex: KeyIndex,
    override val index: KeyIndex
) : AcknowledgedConfigMessage, ConfigNetAndAppKeyMessage {
    override val opCode: UInt = Initializer.opCode

    override val parameters = encodeNetAndAppKeyIndex(
        appKeyIndex = keyIndex,
        netKeyIndex = index
    )

    override val responseOpCode = ConfigAppKeyStatus.opCode

    /**
     * Convenience constructor to create a [ConfigAppKeyDelete] message.
     *
     * @param key Application key to be added.
     * @constructor Constructs the [ConfigAppKeyDelete] message.
     */
    constructor(key: ApplicationKey) : this(
        keyIndex = key.index,
        index = key.boundNetKeyIndex
    )

    override fun toString() = "ConfigAppKeyDelete(applicationKeyIndex: $keyIndex, " +
            "networkKeyIndex: $index)"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x8000u

        /**
         * Initializes the [ConfigAppKeyDelete] based on the given parameters.
         *
         * @param parameters Message parameters.
         * @return ConfigAppKeyAdd or null if the parameters are invalid.
         */
        override fun init(parameters: ByteArray?) = parameters?.takeIf {
            it.size == 3
        }?.let {
            val decodedIndexes = decodeNetAndAppKeyIndex(data = it, offset = 0)
            ConfigAppKeyDelete(
                index = decodedIndexes.networkKeyIndex,
                keyIndex = decodedIndexes.applicationKeyIndex
            )
        }
    }
}