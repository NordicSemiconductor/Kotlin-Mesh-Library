package no.nordicsemi.kotlin.mesh.core.layers.foundation

import no.nordicsemi.kotlin.mesh.core.messages.HasInitializer
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.util.MessageComposer
import no.nordicsemi.kotlin.mesh.core.util.ModelEventHandler

class SceneClientHandler(override val meshNetwork: MeshNetwork) : ModelEventHandler() {
    override val messageTypes: Map<UInt, HasInitializer>
        get() = TODO("Not yet implemented")
    override val isSubscriptionSupported: Boolean
        get() = TODO("Not yet implemented")
    override val publicationMessageComposer: MessageComposer
        get() = TODO("Not yet implemented")
}