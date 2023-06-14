package no.nordicsemi.android.nrfmesh.feature.groups.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createSimpleDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.groups.GroupsRoute
import no.nordicsemi.android.nrfmesh.feature.groups.GroupsViewModel

val groups = createSimpleDestination("groups")

val groupsDestination = defineDestination(groups) {
    val viewModel = hiltViewModel<GroupsViewModel>()
    GroupsRoute(viewModel = viewModel)
}

val groupsDestinations = groupsDestination + groupDestination