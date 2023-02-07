package no.nordicsemi.android.nrfmesh.feature.nodes.destinations

import androidx.compose.foundation.layout.Column
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import java.util.*

val node = createDestination<UUID, Unit>("node")

val nodeDestination = defineDestination(node) {
    Column {

    }
}