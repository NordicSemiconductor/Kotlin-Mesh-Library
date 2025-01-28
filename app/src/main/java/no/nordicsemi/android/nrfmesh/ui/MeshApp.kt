package no.nordicsemi.android.nrfmesh.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.nrfmesh.ui.network.NetworkRoute
import no.nordicsemi.android.nrfmesh.viewmodel.NetworkViewModel

@Composable
fun MeshApp(windowSizeClass: WindowSizeClass) {
    val viewModel = hiltViewModel<NetworkViewModel>()
    NetworkRoute(
        windowSizeClass = windowSizeClass,
        importNetwork = viewModel::importNetwork,
    )
}