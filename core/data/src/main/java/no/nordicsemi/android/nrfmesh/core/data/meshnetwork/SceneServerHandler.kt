package no.nordicsemi.android.nrfmesh.core.data.meshnetwork

import no.nordicsemi.kotlin.mesh.core.messages.HasInitializer
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.MessageComposer
import no.nordicsemi.kotlin.mesh.core.ModelEvent
import no.nordicsemi.kotlin.mesh.core.SceneServerModelEventHandler

class SceneServerHandler(override val meshNetwork: MeshNetwork) : SceneServerModelEventHandler() {
    override val messageTypes: Map<UInt, HasInitializer>
        get() = TODO("Not yet implemented")
    override val isSubscriptionSupported: Boolean
        get() = TODO("Not yet implemented")
    override val publicationMessageComposer: MessageComposer
        get() = TODO("Not yet implemented")

    override fun handle(event: ModelEvent) {
        TODO("Not yet implemented")
    }

    override fun networkDidExitStoredWithSceneState() {
        TODO("Not yet implemented")
    }
}