package no.nordicsemi.android.nrfmesh

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import no.nordicsemi.android.nrfmesh.ui.NetworksScreen

@Composable
fun Navigation(
    onCancelled: () -> Unit = {},
) {
    val navController = rememberNavController()
    NetworksScreen(viewModel = hiltViewModel())
}
