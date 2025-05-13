@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.foundation

import no.nordicsemi.kotlin.mesh.core.MessageComposer
import no.nordicsemi.kotlin.mesh.core.ModelEvent
import no.nordicsemi.kotlin.mesh.core.ModelEventHandler
import no.nordicsemi.kotlin.mesh.core.messages.HasInitializer

class PrivateBeaconHandler : ModelEventHandler() {
    override val messageTypes = mapOf<UInt, HasInitializer>()
    override val isSubscriptionSupported = false
    override val publicationMessageComposer: MessageComposer? = null

    override fun handle(event: ModelEvent) {
        // TODO("Not yet implemented")
    }
}