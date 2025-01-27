@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.application.keys

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssistWalker
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.data.models.ApplicationKeyData
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.showSnackbar
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@OptIn(ExperimentalStdlibApi::class)
@Composable
internal fun ApplicationKeyRoute(
    key: ApplicationKey,
    networkKeys: List<NetworkKey>,
    save: () -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isCurrentlyEditable by rememberSaveable { mutableStateOf(true) }
    val applicationKey by remember(key.index) { derivedStateOf { ApplicationKeyData(key = key) } }
    var boundNetKeyIndex by remember(key.index) { mutableIntStateOf(key.boundNetKeyIndex.toInt()) }
    Scaffold(
        modifier = Modifier.background(color = Color.Red),
        contentWindowInsets = WindowInsets(top = 8.dp),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues)
                .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            SectionTitle(title = stringResource(id = R.string.label_application_key))
            Name(
                name = applicationKey.name,
                onNameChanged = {
                    key.name = it
                    save()
                },
                isCurrentlyEditable = isCurrentlyEditable,
                onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable }
            )
            Key(
                networkKey = applicationKey.key,
                onKeyChanged = {
                    key.setKey(key = it)
                    save()
                },
                isCurrentlyEditable = isCurrentlyEditable,
                onEditableStateChanged = { isCurrentlyEditable = !isCurrentlyEditable }
            )
            OldKey(oldKey = applicationKey.oldKey)
            KeyIndex(index = applicationKey.index)
            SectionTitle(title = stringResource(R.string.label_bound_network_key))
            networkKeys.forEach { networkKey ->
                ElevatedCardItem(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = {
                        if (!applicationKey.isInUse) {
                            boundNetKeyIndex = networkKey.index.toInt()
                            key.boundNetKeyIndex = networkKey.index
                            save()
                        } else showSnackbar(
                            scope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                            message = context.getString(R.string.error_cannot_change_bound_net_key),
                            withDismissAction = true
                        )
                    },
                    imageVector = Icons.Outlined.VpnKey,
                    title = networkKey.name,
                    subtitle = networkKey.key.toHexString(),
                    titleAction = {
                        if (networkKey.index.toInt() == boundNetKeyIndex) {
                            Icon(
                                modifier = Modifier.padding(start = 16.dp),
                                imageVector = Icons.Outlined.CheckCircleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.surfaceTint
                            )
                        }
                    }
                )
            }
        }
    }
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
        subtitle = oldKey?.toHexString() ?: stringResource(id = R.string.label_na)
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