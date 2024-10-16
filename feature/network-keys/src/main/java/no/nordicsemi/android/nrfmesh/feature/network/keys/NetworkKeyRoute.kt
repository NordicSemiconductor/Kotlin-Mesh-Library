package no.nordicsemi.android.nrfmesh.feature.network.keys

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssistWalker
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.LocalPolice
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Instant
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyScreen
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.exception.InvalidKeyLength
import no.nordicsemi.kotlin.mesh.core.exception.KeyInUse
import no.nordicsemi.kotlin.mesh.core.model.Insecure
import no.nordicsemi.kotlin.mesh.core.model.KeyDistribution
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.KeyRefreshPhase
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.NormalOperation
import no.nordicsemi.kotlin.mesh.core.model.Secure
import no.nordicsemi.kotlin.mesh.core.model.Security
import no.nordicsemi.kotlin.mesh.core.model.UsingNewKeys
import java.text.DateFormat
import java.util.Date

@Composable
internal fun NetworkKeyRoute(
    appState: AppState,
    uiState: NetworkKeyScreenUiState,
    onNameChanged: (String) -> Unit,
    onKeyChanged: (ByteArray) -> Unit,
    onBackPressed: () -> Unit,
) {
    val screen = appState.currentScreen as? NetworkKeyScreen
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach { button ->
            when (button) {
                NetworkKeyScreen.Actions.BACK -> onBackPressed()
            }

        }?.launchIn(this)
    }
    NetworkKeyScreen(
        keyState = uiState.keyState,
        onNameChanged = onNameChanged,
        onKeyChanged = onKeyChanged
    )
}

@Composable
private fun NetworkKeyScreen(
    keyState: KeyState,
    onNameChanged: (String) -> Unit,
    onKeyChanged: (ByteArray) -> Unit
) {
    var isCurrentlyEditable by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (keyState) {
            KeyState.Loading -> { /* Do nothing */
            }

            is KeyState.Success -> NetworkKeyInfo(
                networkKey = keyState.key,
                isCurrentlyEditable = isCurrentlyEditable,
                onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable },
                onNameChanged = onNameChanged,
                onKeyChanged = onKeyChanged
            )

            is KeyState.Error -> when (keyState.throwable) {
                is KeyInUse -> {}
                is InvalidKeyLength -> {}
            }
        }
    }
}

@Composable
private fun NetworkKeyInfo(
    networkKey: NetworkKey,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
    onNameChanged: (String) -> Unit,
    onKeyChanged: (ByteArray) -> Unit
) {
    Name(
        name = networkKey.name,
        onNameChanged = onNameChanged,
        isCurrentlyEditable = isCurrentlyEditable,
        onEditableStateChanged = onEditableStateChanged
    )
    Key(
        networkKey = networkKey.key,
        onKeyChanged = onKeyChanged,
        isCurrentlyEditable = isCurrentlyEditable,
        onEditableStateChanged = onEditableStateChanged
    )
    OldKey(oldKey = networkKey.oldKey)
    KeyIndex(index = networkKey.index)
    KeyRefreshPhase(phase = networkKey.phase)
    Security(security = networkKey.security)
    LastModified(networkKey.timestamp)
}

@Composable
fun Name(
    name: String,
    onNameChanged: (String) -> Unit,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
) {
    ElevatedCardItemTextField(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Badge,
        title = stringResource(id = R.string.label_name),
        subtitle = name,
        onValueChanged = onNameChanged,
        isEditable = isCurrentlyEditable,
        onEditableStateChanged = onEditableStateChanged,
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun Key(
    networkKey: ByteArray,
    onKeyChanged: (ByteArray) -> Unit,
    isCurrentlyEditable: Boolean,
    onEditableStateChanged: () -> Unit,
) {

    ElevatedCardItemTextField(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.VpnKey,
        title = stringResource(id = R.string.label_key),
        subtitle = networkKey.toHexString(),
        onValueChanged = { onKeyChanged(it.toByteArray()) },
        isEditable = isCurrentlyEditable,
        onEditableStateChanged = onEditableStateChanged,
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun OldKey(oldKey: ByteArray?) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.AssistWalker,
        title = stringResource(id = R.string.label_old_key),
        subtitle = oldKey?.toHexString()
            ?: stringResource(id = R.string.label_na)
    )
}

@Composable
fun KeyIndex(index: KeyIndex) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.FormatListNumbered,
        title = stringResource(id = R.string.label_key_index),
        subtitle = index.toString()
    )
}

@Composable
fun KeyRefreshPhase(phase: KeyRefreshPhase) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.AutoMode,
        title = stringResource(id = R.string.label_key_refresh_phase),
        subtitle = phase.description()
    )
}

@Composable
fun Security(security: Security) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.LocalPolice,
        title = stringResource(id = R.string.label_security),
        subtitle = security.description()
    )
}

@Composable
fun LastModified(timestamp: Instant) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Update,
        title = stringResource(id = R.string.label_last_modified),
        subtitle = DateFormat
            .getDateTimeInstance()
            .format(Date(timestamp.toEpochMilliseconds()))
    )
}

@Suppress("unused")
@Composable
fun KeyRefreshPhase.description(): String = when (this) {
    NormalOperation -> stringResource(id = R.string.label_normal_operation)
    KeyDistribution -> stringResource(id = R.string.label_key_distribution)
    UsingNewKeys -> stringResource(id = R.string.label_using_new_keys)
}

@Composable
fun Security.description(): String = when (this) {
    Secure -> stringResource(id = R.string.label_secure)
    Insecure -> stringResource(id = R.string.label_insecure)
}