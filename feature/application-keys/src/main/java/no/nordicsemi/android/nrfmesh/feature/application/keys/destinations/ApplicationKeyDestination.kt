@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.application.keys.destinations

import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.createDestination
import no.nordicsemi.android.common.navigation.defineDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeyRoute
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeyViewModel

val applicationKey = createDestination<Int, Unit>("application_key")

val applicationKeyDestination = defineDestination(applicationKey) {
    val viewModel: ApplicationKeyViewModel = hiltViewModel()
    ApplicationKeyRoute(viewModel = viewModel)
}