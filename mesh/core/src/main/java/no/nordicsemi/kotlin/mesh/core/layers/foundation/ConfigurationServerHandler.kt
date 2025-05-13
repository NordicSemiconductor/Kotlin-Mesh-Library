@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.foundation

import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigCompositionDataGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigGattProxyGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigHeartbeatPublicationGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigHeartbeatPublicationSet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelPublicationGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelPublicationSet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNetKeyDelete
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigNodeReset
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.ModelEvent
import no.nordicsemi.kotlin.mesh.core.ModelEventHandler

/**
 * TODO
 */
internal class ConfigurationServerHandler : ModelEventHandler() {
    override val messageTypes = mapOf(
        ConfigCompositionDataGet.opCode to ConfigCompositionDataGet,
        ConfigNetKeyDelete.opCode to ConfigNetKeyDelete,
        ConfigNodeReset.opCode to ConfigNodeReset,
        ConfigModelPublicationGet.opCode to ConfigModelPublicationGet,
        ConfigModelPublicationSet.opCode to ConfigModelPublicationSet,
        ConfigHeartbeatPublicationGet.opCode to ConfigHeartbeatPublicationGet,
        ConfigHeartbeatPublicationSet.opCode to ConfigHeartbeatPublicationSet,
        ConfigGattProxyGet.opCode to ConfigGattProxyGet
    )
    override val isSubscriptionSupported = false
    override val publicationMessageComposer = null
    override fun handle(event: ModelEvent) {
        // TODO("Not yet implemented")
    }
}