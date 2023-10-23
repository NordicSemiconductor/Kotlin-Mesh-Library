package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer

data object ConfigGattProxyGet : AcknowledgedConfigMessage {
    override val opCode = Initializer.opCode
    override val parameters = null
    override val responseOpCode: UInt = ConfigGattProxyStatus.opCode

    internal object Initializer : ConfigMessageInitializer {
        override val opCode = 0x8012u

        override fun init(parameters: ByteArray?) = parameters?.let {
            ConfigGattProxyGet
        }
    }
}