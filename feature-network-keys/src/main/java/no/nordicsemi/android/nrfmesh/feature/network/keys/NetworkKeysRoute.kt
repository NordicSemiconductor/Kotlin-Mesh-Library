@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.network.keys

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.nrfmesh.core.ui.MeshLargeTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.RowItem
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

@Composable
fun NetworkKeysRoute(
    viewModel: NetworkKeysViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    NetworkKeysScreen(
        uiState = viewModel.uiState,
        onBackPressed = onBackPressed
    )
}

@Composable
private fun NetworkKeysScreen(
    uiState: NetworkKeysScreenUiState,
    onBackPressed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            MeshLargeTopAppBar(
                title = stringResource(id = R.string.label_network_keys),
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            contentPadding = padding
        ) {
            items(
                items = uiState.keys,
                key = { it.key }
            ) { key ->
                RowItem(
                    imageVector = Icons.Outlined.Key,
                    title = key.name,
                    subtitle = key.key.encodeHex()
                )
            }
        }
    }
}
