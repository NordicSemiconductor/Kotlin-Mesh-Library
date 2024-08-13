package no.nordicsemi.android.nrfmesh.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import no.nordicsemi.android.nrfmesh.navigation.MeshAppState
import no.nordicsemi.android.nrfmesh.navigation.rememberMeshAppState
import no.nordicsemi.android.nrfmesh.ui.network.NetworkRoute

@Composable
fun MeshApp(
    appState: MeshAppState = rememberMeshAppState(
        scope = rememberCoroutineScope()
    )
) {
    NetworkRoute(appState = appState)
}