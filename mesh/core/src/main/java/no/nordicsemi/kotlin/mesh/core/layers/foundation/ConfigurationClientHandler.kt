@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.foundation

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage
import no.nordicsemi.kotlin.mesh.core.messages.HasInitializer
import no.nordicsemi.kotlin.mesh.core.messages.MeshResponse
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyDelete
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyList
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigAppKeyUpdate
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigCompositionDataStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigFriendGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigFriendStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigGattProxyStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigHeartbeatPublicationStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelPublicationStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyDelete
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyList
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyUpdate
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNodeResetStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigRelayStatus
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.FeatureState
import no.nordicsemi.kotlin.mesh.core.model.Friend
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.Proxy
import no.nordicsemi.kotlin.mesh.core.model.Relay
import no.nordicsemi.kotlin.mesh.core.model.RelayRetransmit
import no.nordicsemi.kotlin.mesh.core.util.MessageComposer
import no.nordicsemi.kotlin.mesh.core.util.ModelError
import no.nordicsemi.kotlin.mesh.core.util.ModelEvent
import no.nordicsemi.kotlin.mesh.core.util.ModelEventHandler

/**
 * ConfigurationClientHandler class handles the configuration messages sent from the provisioner
 *
 * @property meshNetwork                   Mesh network.
 * @property messageTypes                  Message types supported by the handler.
 * @property isSubscriptionSupported       True if the model supports subscription.
 * @property publicationMessageComposer    Message composer for publication messages.
 * @constructor Initialize ConfigurationClientHandler
 */
internal class ConfigurationClientHandler(
    override val meshNetwork: MeshNetwork
) : ModelEventHandler() {

    override val messageTypes: Map<UInt, HasInitializer> = mapOf(
        ConfigCompositionDataStatus.opCode to ConfigCompositionDataStatus,
        ConfigNetKeyStatus.opCode to ConfigNetKeyStatus,
        ConfigNetKeyList.opCode to ConfigNetKeyList,
        ConfigAppKeyStatus.opCode to ConfigAppKeyStatus,
        ConfigAppKeyList.opCode to ConfigAppKeyList,
        ConfigFriendStatus.opCode to ConfigFriendStatus,
        ConfigGattProxyStatus.opCode to ConfigGattProxyStatus,
        ConfigHeartbeatPublicationStatus.opCode to ConfigHeartbeatPublicationStatus,
        ConfigModelPublicationStatus.opCode to ConfigModelPublicationStatus,
        ConfigNodeResetStatus.opCode to ConfigNodeResetStatus
    )
    override val isSubscriptionSupported: Boolean = false
    override val publicationMessageComposer: MessageComposer? = null

    /**
     * Handles the model events.
     *
     * @param event Event to be handled.
     * @throws ModelError if an acknowledged message is received.
     */
    override fun handle(event: ModelEvent) {
        when (event) {
            is ModelEvent.AcknowledgedMessageReceived -> throw ModelError.InvalidMessage(
                msg = event.request
            )

            is ModelEvent.ResponseReceived -> handleResponses(
                model = event.model,
                response = event.response,
                request = event.request,
                source = event.source
            )

            is ModelEvent.UnacknowledgedMessageReceived -> {
                // Ignore do nothing
            }
        }
    }

    /**
     * Handles the responses received by the client model.
     *
     * @param model     Model that received the message.
     * @param response  Response that was received by the model.
     * @param request   Request that was sent.
     * @param source    Address of the Element from which the message was sent.
     */
    private fun handleResponses(
        model: Model,
        response: MeshResponse,
        request: AcknowledgedMeshMessage,
        source: Address
    ) = meshNetwork.run {
        when (response) {
            // Composition Data
            is ConfigCompositionDataStatus -> {
                require(localProvisioner?.primaryUnicastAddress?.address != source) {
                    return
                }
                node(address = source)?.apply(compositionData = response)
            }
            // Network Keys Management
            is ConfigNetKeyStatus -> if (response.isSuccess) {
                node(address = source)?.apply {
                    when (request as ConfigNetKeyMessage) {
                        is ConfigNetKeyAdd -> addNetKey(response.networkKeyIndex)
                        is ConfigNetKeyDelete -> removeNetKey(response.networkKeyIndex)
                        is ConfigNetKeyUpdate -> updateNetKey(response.networkKeyIndex)
                    }
                }
            }

            is ConfigNetKeyList -> node(address = source)?.apply {
                setNetKeys(
                    netKeyIndexes = response.networkKeyIndexes.toList()
                )
            }

            // Application Keys Management
            is ConfigAppKeyStatus -> if (response.isSuccess) node(address = source)?.apply {
                when (request as ConfigNetKeyMessage) {
                    is ConfigAppKeyAdd -> addAppKey(index = response.applicationKeyIndex)
                    is ConfigAppKeyUpdate -> updateAppKey(index = response.applicationKeyIndex)
                    is ConfigAppKeyDelete -> removeAppKey(response.applicationKeyIndex)
                }
            }

            is ConfigAppKeyList -> node(address = source)?.apply {
                setAppKeys(
                    appKeyIndexes = response.applicationKeyIndexes.toList(),
                    netKeyIndex = response.networkKeyIndex
                )
            }

            is ConfigFriendStatus -> {
                node(address = source)?.apply {
                    features._friend = Friend(state = response.state)
                    updateTimestamp()
                }
            }

            is ConfigGattProxyStatus -> {
                node(address = source)?.apply {
                    features._proxy = Proxy(state = response.state)
                    updateTimestamp()
                }
            }

            is ConfigRelayStatus -> {
                node(address = source)?.apply {
                    features._relay = Relay(state = response.state)
                    relayRetransmit = when (response.state) {
                        FeatureState.Unsupported -> null
                        FeatureState.Disabled, FeatureState.Enabled -> RelayRetransmit(response)
                    }
                    updateTimestamp()
                }
            }

            is ConfigHeartbeatPublicationStatus -> {
                // TODO
            }

            is ConfigModelPublicationStatus -> {
                // TODO
            }

            is ConfigNodeResetStatus -> {
                node(address = source)?.let { remove(it) }
            }

            else -> {}
        }
        Unit
    }
}