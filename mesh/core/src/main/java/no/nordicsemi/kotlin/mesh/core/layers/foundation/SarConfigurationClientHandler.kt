package no.nordicsemi.kotlin.mesh.core.layers.foundation

import no.nordicsemi.kotlin.mesh.core.messages.HasInitializer
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.MessageComposer
import no.nordicsemi.kotlin.mesh.core.ModelEvent
import no.nordicsemi.kotlin.mesh.core.ModelEventHandler

class SarConfigurationClientHandler(override val meshNetwork: MeshNetwork) : ModelEventHandler() {
    override val messageTypes: Map<UInt, HasInitializer>
        get() = mapOf()
    override val isSubscriptionSupported = true
    override val publicationMessageComposer: MessageComposer
        get() = TODO("Not yet implemented")

    override fun handle(event: ModelEvent) {
        // TODO("Not yet implemented")
    }
}