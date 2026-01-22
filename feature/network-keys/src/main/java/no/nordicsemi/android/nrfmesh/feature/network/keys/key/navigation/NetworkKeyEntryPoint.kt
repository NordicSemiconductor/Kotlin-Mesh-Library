package no.nordicsemi.android.nrfmesh.feature.network.keys.key.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.Navigator
import no.nordicsemi.android.nrfmesh.feature.network.keys.key.NetworkKeyScreen
import no.nordicsemi.android.nrfmesh.feature.network.keys.key.NetworkKeyViewModel
import no.nordicsemi.kotlin.data.HexString
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@Serializable
data class NetworkKeyContentKey(val keyIndex: HexString) : NavKey {
    constructor(keyIndex: KeyIndex) : this(keyIndex.toHexString())
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.networkKeyEntry(appState: AppState, navigator: Navigator) {
    entry<NetworkKeyContentKey>(
        metadata = ListDetailSceneStrategy.extraPane()
    ) { key ->
        val viewModel = hiltViewModel<NetworkKeyViewModel, NetworkKeyViewModel.Factory>(
            key = "NetworkKeyViewModel:${key.keyIndex}"
        ) { factory ->
            factory.create(index = key.keyIndex)
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        NetworkKeyScreen(uiState = uiState, save = viewModel::save)
    }
}