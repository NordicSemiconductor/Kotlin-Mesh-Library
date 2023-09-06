package no.nordicsemi.kotlin.mesh.core.messages.proxy

import no.nordicsemi.kotlin.mesh.core.messages.BaseMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.BaseMeshMessageDecoder


/**
 * A base interface for all Proxy configuration messages that have a op code.
 *
 * @property opCode Message op code.
 */
interface HasProxyConfigurationOpCode {
    val opCode: UByte
}

/**
 * A base class for all Proxy configuration messages.
 */
interface ProxyConfigurationMessage : BaseMeshMessage, HasProxyConfigurationOpCode

/**
 * A base class for acknowledged Proxy configuration messages.
 *
 * An acknowledged message is transmitted and acknowledged by each receiving element by responding
 * to that message. The response is typically a status message. If a response is not received within
 * an arbitrary time period, the message will be transmitted automatically until the timeout occurs.
 *
 * @property responseOpCode Op Code of the response message.
 */
interface AcknowledgedProxyConfigurationMessage : ProxyConfigurationMessage {
    val responseOpCode: UByte
}

/**
 * Proxy Configuration message decoder for proxy configuration messages.
 */
interface ProxyConfigurationMessageDecoder : BaseMeshMessageDecoder, HasProxyConfigurationOpCode