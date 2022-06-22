package no.nordicsemi.android.nrfmesh.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import no.nordicsemi.android.material.you.NordicTheme
import no.nordicsemi.android.nrfmesh.core.ui.MeshButton
import no.nordicsemi.android.nrfmesh.viewmodel.NetworksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    viewModel: NetworksViewModel,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text(text = "Networks") })
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
        ) {
            ImportNetwork()
        }
    }
}

@Composable
fun ImportNetwork() {
    MeshButton(text = "Import Network", onClick = {})
}

@Preview
@Composable
fun DefaultPreview() {
    NordicTheme {
        ImportNetwork()
    }
}