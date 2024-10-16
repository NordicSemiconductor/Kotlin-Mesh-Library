package no.nordicsemi.android.nrfmesh.feature.scenes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.SceneScreen
import no.nordicsemi.kotlin.mesh.core.model.Scene
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber

@Composable
internal fun SceneRoute(
    appState: AppState,
    uiState: SceneScreenUiState,
    onNameChanged: (String) -> Unit,
    onBackPressed: () -> Unit
) {
    val screen = appState.currentScreen as? SceneScreen
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach { button ->
            when (button) {
                SceneScreen.Actions.BACK -> onBackPressed()
            }
        }?.launchIn(this)
    }
    SceneScreen(sceneState = uiState.sceneState, onNameChanged = onNameChanged)
}

@Composable
private fun SceneScreen(sceneState: SceneState, onNameChanged: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (sceneState) {
            SceneState.Loading -> { /* Do nothing */
            }

            is SceneState.Success -> {
                sceneInfo(
                    scene = sceneState.scene,
                    onNameChanged = onNameChanged
                )
            }

            is SceneState.Error -> {}
        }
    }
}

private fun LazyListScope.sceneInfo(scene: Scene, onNameChanged: (String) -> Unit) {
    item { Name(name = scene.name, onNameChanged = onNameChanged) }
    item { Number(number = scene.number) }
}

@Composable
fun Name(name: String, onNameChanged: (String) -> Unit) {
    ElevatedCardItemTextField(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Badge,
        title = stringResource(id = R.string.label_name),
        subtitle = name,
        onValueChanged = onNameChanged,
        isEditable = true,
        onEditableStateChanged = { },
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun Number(number: SceneNumber) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.AutoAwesome,
        title = stringResource(id = R.string.label_scene_number),
        subtitle = number.toHexString()
    )
}