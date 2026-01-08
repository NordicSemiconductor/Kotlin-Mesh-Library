@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.mesh.core.messages.BaseMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageStatus
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage.Companion.decodeNetKeyIndex
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage.Companion.encodeNetKeyIndex
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.ConfigStatusMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

/**
 * ConfigAppKeyList is an unacknowledged message reporting all [ApplicationKey]s bound to requested
 * [NetworkKey] that are known to the Node. This message is a response to the [ConfigAppKeyGet]
 *
 * @property applicationKeyIndexes   Index of the application key.
 * @property index                   Index of the network key.
 * @property status                  Status of the message.
 * @property opCode                  Message op code.
 * @property parameters              Message parameters.
 * @constructor Constructs the ConfigAppKeyList message.
 */
class ConfigAppKeyList(
    override val index: KeyIndex,
    val applicationKeyIndexes: Array<KeyIndex>,
    override val status: ConfigMessageStatus,
) : ConfigResponse, ConfigStatusMessage, ConfigNetKeyMessage {

    override val opCode = Initializer.opCode
    override val parameters = status.value.toByteArray() +
            encodeNetKeyIndex(keyIndex = index) +
            ConfigMessage.encode(indexes = applicationKeyIndexes)

    /**
     * Constructs the ConfigAppKeyList message.
     *
     * @param request             ConfigNetKeyMessage operation that was sent to the mesh node.
     * @param applicationKeys     List of application keys bound to a network key.
     * @constructor Constructs the ConfigAppKeyList message.
     */
    constructor(request: ConfigAppKeyGet, applicationKeys: List<ApplicationKey>) : this(
        index = request.index,
        applicationKeyIndexes = applicationKeys.map { it.index }.toTypedArray(),
        status = ConfigMessageStatus.SUCCESS
    )

    /**
     * Constructs the ConfigAppKeyList message.
     *
     * @param request ConfigAppKeyGet message.
     * @param status  Status of the message.
     */
    constructor(request: ConfigAppKeyGet, status: ConfigMessageStatus) : this(
        index = request.index,
        applicationKeyIndexes = emptyArray(),
        status = status
    )

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString() = "ConfigAppKeyList(networkKeyIndex: $index, " +
            "applicationKeyIndexes: (${
                applicationKeyIndexes.joinToString(
                    separator = ", ",
                    transform = { it.toHexString() }
                )
            }), status: $status)"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x8002u

        /**
         * Initializes the ConfigAppKeyList message.
         *
         * @param parameters Message parameters.
         * @return ConfigAppKeyList or null if the parameters are invalid.
         */
        override fun init(parameters: ByteArray?): BaseMeshMessage? = parameters?.takeIf {
            it.size >= 3
        }?.let { params ->
            val status = ConfigMessageStatus.from(params.first().toUByte()) ?: return null
            ConfigAppKeyList(
                index = decodeNetKeyIndex(data = parameters, offset = 1),
                applicationKeyIndexes = ConfigMessage.decode(data = params, offset = 3),
                status = status
            )
        }
    }
}