@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.model.FeatureState
import no.nordicsemi.kotlin.mesh.core.model.Node

/**
 * Defines the response to [ConfigGattProxyGet]
 *
 * @property state The state of the GATT Proxy feature.
 */
data class ConfigGattProxyStatus(val state: FeatureState) : ConfigResponse {
    override val opCode = Initializer.opCode
    override val parameters: ByteArray = byteArrayOf(state.value.toByte())

    constructor(node: Node) : this(node.features.proxy?.state ?: FeatureState.Unsupported)

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x8014u

        override fun init(parameters: ByteArray?) = parameters?.takeIf {
            it.size == 1
        }?.let {
            ConfigGattProxyStatus(state = FeatureState.from(it[0].toUInt().toInt()))
        }
    }
}