@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.foundation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.BaseMeshMessageInitializer
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
import no.nordicsemi.kotlin.mesh.core.util.MessageComposer
import no.nordicsemi.kotlin.mesh.core.util.ModelEvent
import no.nordicsemi.kotlin.mesh.core.util.ModelEventHandler

internal class ConfigurationClientHandler(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
    override val meshNetwork: MeshNetwork
) : ModelEventHandler() {

    val types = listOf<BaseMeshMessageInitializer>(
        ConfigCompositionDataStatus.Initializer,
        ConfigNetKeyStatus.Initializer,
        ConfigGattProxyStatus.Initializer,
        ConfigHeartbeatPublicationStatus.Initializer,
        ConfigModelPublicationStatus.Initializer,
        ConfigNodeResetStatus.Initializer
    )

    override val messageTypes: Map<UInt, HasInitializer>
        get() = TODO("Not yet implemented")
    override val isSubscriptionSupported: Boolean
        get() = TODO("Not yet implemented")
    override val publicationMessageComposer: MessageComposer
        get() = TODO("Not yet implemented")

    init {
        observeEvents()
    }

    private fun observeEvents() {
        modelEventFlow.onEach {
            when (it) {
                is ModelEvent.AcknowledgedMessageReceived -> { /* Ignore */
                }

                is ModelEvent.UnacknowledgedMessageReceived -> { /* Ignore */
                }

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
    ) {
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
                // TODO
            }

            is ConfigHeartbeatPublicationStatus -> {
                // TODO
            }

            is ConfigModelPublicationStatus -> {
                // TODO
            }

            is ConfigNodeResetStatus -> {
                // TODO
            }

            else -> {}
        }
    }
}