package no.nordicsemi.android.nrfmesh.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.viewmodel.NetworksViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworksScreen(viewModel: NetworksViewModel, modifier: Modifier = Modifier) {
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
    Button(onClick = {}) {
        Text(modifier = Modifier.padding(all = 8.dp), text = "Import Network")
    }
}