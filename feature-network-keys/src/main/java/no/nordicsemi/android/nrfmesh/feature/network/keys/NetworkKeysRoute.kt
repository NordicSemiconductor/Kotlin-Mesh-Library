@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLifecycleComposeApi::class)

package no.nordicsemi.android.nrfmesh.feature.network.keys

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.nrfmesh.core.ui.MeshLargeTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

@Composable
fun NetworkKeysRoute(
    viewModel: NetworkKeysViewModel = hiltViewModel(),
    navigateToNetworkKey: (KeyIndex) -> Unit,
    onBackClicked: () -> Unit
) {
    val uiState: NetworkKeysScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    NetworkKeysScreen(
        uiState = uiState,
        navigateToNetworkKey = navigateToNetworkKey,
        onAddKeyClicked = { viewModel.addNetworkKey() },
        onBackPressed = onBackClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetworkKeysScreen(
    uiState: NetworkKeysScreenUiState,
    navigateToNetworkKey: (KeyIndex) -> Unit,
    onAddKeyClicked: () -> Unit,
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
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { onAddKeyClicked() }) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.action_add_key)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = uiState.keys,
                key = { it.key }
            ) { key ->
                MeshTwoLineListItem(
                    modifier = Modifier.clickable {
                        navigateToNetworkKey(key.index)
                    },
                    leadingIcon = {
                        Icon(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            imageVector = Icons.Outlined.VpnKey,
                            contentDescription = null,
                            tint = LocalContentColor.current.copy(alpha = 0.6f)
                        )
                    },
                    title = key.name,
                    subtitle = key.key.encodeHex()
                )
            }
        }
    }
}