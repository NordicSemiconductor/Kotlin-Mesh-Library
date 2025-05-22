@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage.Companion.decodeNetKeyIndex
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

/**
 * This is the response message to [ConfigDefaultTtlGet] and [ConfigDefaultTtlSet] messages.
 *
 * @property ttl The default TTL value.
 */
class ConfigDefaultTtlStatus(val ttl: UByte) : ConfigResponse {
    override val opCode: UInt = Initializer.opCode

    override val parameters: ByteArray? = ttl.toByteArray()

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString() = "ConfigDefaultTtlStatus(opCode: 0x${opCode.toHexString()}, ttl: $ttl)"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode = 0x800Eu

        override fun init(parameters: ByteArray?) = parameters
            ?.takeIf { it.size == 1 }
            ?.let { ConfigDefaultTtlStatus(parameters[0].toUByte()) }
    }
}