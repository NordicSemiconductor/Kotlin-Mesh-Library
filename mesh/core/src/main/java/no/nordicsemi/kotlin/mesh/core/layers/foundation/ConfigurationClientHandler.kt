@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.foundation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage
import no.nordicsemi.kotlin.mesh.core.messages.HasInitializer
import no.nordicsemi.kotlin.mesh.core.messages.MeshResponse
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigCompositionDataStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigGattProxyStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigHeartbeatPublicationStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelPublicationStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyDelete
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyStatus
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyUpdate
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

    override fun handle(event: ModelEvent) {
        TODO("Not yet implemented")
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
                node(source)?.apply(response)
            }
            // Network Keys Management
            is ConfigNetKeyStatus -> {
                if (response.isSuccess) {
                    node(address = source)?.apply { //node ->
                        // TODO implement missing messages
                        when (request as ConfigNetKeyMessage) {
                            is ConfigNetKeyAdd -> addNetKey(response.networkKeyIndex)

                            is ConfigNetKeyDelete -> removeNetKey(response.networkKeyIndex)

                            is ConfigNetKeyUpdate -> updateNetKey(response.networkKeyIndex)
                        }
                    }
                }
            }

            is ConfigGattProxyStatus -> {
                node(address = source)?.apply {
                    features._proxy = Proxy(state = response.state)
                }
                updateTimestamp()
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