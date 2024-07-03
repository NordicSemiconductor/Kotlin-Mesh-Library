@file:Suppress("ConvertSecondaryConstructorToPrimary", "unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.messages.BaseMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse

/**
 * Status message received when after sending a [ConfigNodeReset] message.
 */
class ConfigNodeResetStatus : ConfigResponse {
    override val opCode: UInt = Initializer.opCode
    override val parameters: ByteArray? = null

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String =
        "ConfigNodeResetStatus(opCode=${opCode.toHexString()}, parameters=${parameters?.toHexString()})"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode: UInt = 0x804Au

        override fun init(parameters: ByteArray?): BaseMeshMessage? = parameters.takeIf {
            it != null && it.isEmpty()
        }?.let {
            ConfigNodeResetStatus()
        }
    }
}