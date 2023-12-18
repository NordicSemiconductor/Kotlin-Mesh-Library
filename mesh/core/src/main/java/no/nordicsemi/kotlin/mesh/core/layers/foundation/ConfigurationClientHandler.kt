@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.foundation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.HasInitializer
import no.nordicsemi.kotlin.mesh.core.messages.MeshResponse
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigCompositionDataStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigGattProxyStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigHeartbeatPublicationStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelPublicationStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNodeResetStatus
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.Proxy
import no.nordicsemi.kotlin.mesh.core.util.MessageComposer
import no.nordicsemi.kotlin.mesh.core.util.ModelEvent
import no.nordicsemi.kotlin.mesh.core.util.ModelEventHandler

internal class ConfigurationClientHandler(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
    override val meshNetwork: MeshNetwork
) : ModelEventHandler() {

    override val messageTypes: Map<UInt, HasInitializer>
        get() = mapOf(
            ConfigCompositionDataStatus.opCode to ConfigCompositionDataStatus,
            ConfigNetKeyStatus.opCode to ConfigNetKeyStatus,
            ConfigGattProxyStatus.opCode to ConfigGattProxyStatus,
            ConfigHeartbeatPublicationStatus.opCode to ConfigHeartbeatPublicationStatus,
            ConfigModelPublicationStatus.opCode to ConfigModelPublicationStatus,
            ConfigNodeResetStatus.opCode to ConfigNodeResetStatus
        )
    override val isSubscriptionSupported: Boolean = false
    override val publicationMessageComposer: MessageComposer? = null

    init {
        observeEvents()
    }

    private fun observeEvents() {
        modelEventFlow.onEach {
            when (it) {
                is ModelEvent.AcknowledgedMessageReceived -> { /* Ignore */ }
                is ModelEvent.UnacknowledgedMessageReceived -> { /* Ignore */ }
                is ModelEvent.ResponseReceived -> {
                    handleResponses(
                        model = it.model,
                        response = it.response,
                        request = it.request,
                        source = it.source
                    )
                }

            }
        }.launchIn(scope)
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
                meshNetwork.localProvisioner?.primaryUnicastAddress?.takeIf {
                    it.address != source
                }?.let {
                    meshNetwork.node(it)?.apply(response)
                }
            }
            // Network Keys Management
            is ConfigNetKeyStatus -> {
                if (response.isSuccess) {
                    meshNetwork.node(address = source)?.let { node ->
                        // TODO implement missing messages
                        /*when(request) {
                            is ConfigNetKeyAdd -> {
                                node.netKeys.add(request.netKeyIndex, request.netKey)
                            }
                            is ConfigNetKeyUpdate -> {
                                node.netKeys[request.netKeyIndex] = request.netKey
                            }
                            is ConfigNetKeyDelete -> {
                                node.netKeys.removeAt(request.netKeyIndex)
                            }

                        }*/
                    }
                }
            }

            is ConfigGattProxyStatus -> {
                meshNetwork.apply {
                    node(address = source)?.also { node ->
                        node.features._proxy = Proxy(state = response.state)
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
                meshNetwork.apply {
                    node(address = source)?.let { remove(it) }
                }
            }

            else -> {}
        }
        Unit
    }
}