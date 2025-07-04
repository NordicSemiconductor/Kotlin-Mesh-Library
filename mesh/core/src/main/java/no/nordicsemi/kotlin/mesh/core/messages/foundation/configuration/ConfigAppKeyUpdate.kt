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
 * This message is used to update an Application Key value on the AppKey List on a mesh node.
 *
 * This message initiates a Key Refresh Procedure. The message can be sent to remote nodes which are
 * not scheduled for exclusion.
 *
 * To transition to the next phases of the Key Refresh Procedure use [ConfigKeyRefreshPhaseSet].
 *
 * @property keyIndex  Index of the application key to be added.
 * @property index      Index of the bound network key.
 * @property key                  The application key to be added.
 * @property opCode               Message op code.
 * @property parameters           Message parameters.
 * @property responseOpCode       Op Code of the response message.
 * @constructor Constructs the ConfigAppKeyAdd message.
 */
class ConfigAppKeyUpdate(
    override val keyIndex: KeyIndex,
    override val index: KeyIndex,
    val key: ByteArray
) : AcknowledgedConfigMessage, ConfigNetAndAppKeyMessage {
    override val opCode: UInt = Initializer.opCode

    override val parameters = encodeNetAndAppKeyIndex(
        appKeyIndex = keyIndex,
        netKeyIndex = index
    ) + key

    override val responseOpCode = ConfigAppKeyStatus.opCode

    /**
     * Convenience constructor to create a [ConfigAppKeyUpdate] message.
     *
     * @param applicationKey Application key to be updated.
     * @param newKey         New key value to be updated with.
     * @constructor Constructs the ConfigAppKeyAdd message.
     */
    constructor(applicationKey: ApplicationKey, newKey: ByteArray) : this(
        keyIndex = applicationKey.index,
        key = newKey,
        index = applicationKey.boundNetKeyIndex
    )

    init {
        require(key.size == 16) { throw InvalidKeyLength() }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString() = "ConfigAppKeyUpdate(applicationKeyIndex: $keyIndex, " +
            "networkKeyIndex: $index, key: ${key.toHexString()})"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x01u

        /**
         * Initializes the [ConfigAppKeyUpdate] based on the given parameters.
         *
         * @param parameters Message parameters.
         * @return ConfigAppKeyAdd or null if the parameters are invalid.
         */
        override fun init(parameters: ByteArray?) = parameters?.takeIf {
            it.size == 19
        }?.let {
            val decodedIndexes = decodeNetAndAppKeyIndex(data = it, offset = 0)
            ConfigAppKeyUpdate(
                index = decodedIndexes.networkKeyIndex,
                keyIndex = decodedIndexes.applicationKeyIndex,
                key = it.copyOfRange(3, 19)
            )
        }
    }
}