@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    ExperimentalLifecycleComposeApi::class
)

package no.nordicsemi.android.nrfmesh.feature.provisioners

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.feature.provisioners.R
import no.nordicsemi.android.nrfmesh.core.ui.*
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import java.util.*

@Composable
internal fun ProvisionersRoute(
    viewModel: ProvisionersViewModel = hiltViewModel(),
    navigateToProvisioner: (UUID) -> Unit,
    onBackClicked: () -> Unit
) {
    val uiState: ProvisionersScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProvisionersScreen(
        uiState = uiState,
        navigateToProvisioner = navigateToProvisioner,
        onAddProvisionerClicked = viewModel::addProvisioner,
        onSwiped = viewModel::onSwiped,
        onUndoClicked = viewModel::onUndoSwipe,
        remove = viewModel::remove,
        onBackPressed = {
            viewModel.removeProvisioners()
            onBackClicked()
        }
    )
}

@Composable
private fun ProvisionersScreen(
    uiState: ProvisionersScreenUiState,
    navigateToProvisioner: (UUID) -> Unit,
    onAddProvisionerClicked: () -> Provisioner,
    onSwiped: (Provisioner) -> Unit,
    onUndoClicked: (Provisioner) -> Unit,
    remove: (Provisioner) -> Unit,
    onBackPressed: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            MeshLargeTopAppBar(
                title = stringResource(id = R.string.label_provisioners),
                navigationIcon = {
                    IconButton(onClick = {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        onBackPressed()
                    }) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = {
                navigateToProvisioner(onAddProvisionerClicked().uuid)
            }) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(R.string.action_add_provisioner)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (uiState.provisioners.isEmpty()) {
            true -> MeshNoItemsAvailable(
                imageVector = Icons.Outlined.AutoAwesome,
                title = stringResource(R.string.no_provisioners_currently_added)
            )
            false -> Provisioners(
                padding = padding,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState,
                provisioners = uiState.provisioners,
                navigateToProvisioner = navigateToProvisioner,
                onSwiped = onSwiped,
                onUndoClicked = onUndoClicked,
                remove = remove
            )
        }

    }
}

@Composable
private fun Provisioners(
    padding: PaddingValues,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    provisioners: List<Provisioner>,
    navigateToProvisioner: (UUID) -> Unit,
    onSwiped: (Provisioner) -> Unit,
    onUndoClicked: (Provisioner) -> Unit,
    remove: (Provisioner) -> Unit
) {
    val listState = rememberLazyListState()
    LazyColumn(
        contentPadding = padding,
        modifier = Modifier.fillMaxSize(),
        state = listState
    ) {
        items(items = provisioners, key = { it.uuid }) { provisioner ->
            // Hold the current state from the Swipe to Dismiss composable
            val dismissState = rememberDismissState()
            var keyDismissed by remember { mutableStateOf(false) }
            if (keyDismissed) {
                showSnackbar(
                    scope = coroutineScope,
                    snackbarHostState = snackbarHostState,
                    message = stringResource(R.string.label_provisioner_deleted),
                    actionLabel = stringResource(R.string.action_undo),
                    onDismissed = { remove(provisioner) },
                    onActionPerformed = {
                        onUndoClicked(provisioner)
                        coroutineScope.launch {
                            dismissState.reset()
                        }
                    },
                    withDismissAction = true
                )
            }
            SwipeDismissItem(
                dismissState = dismissState,
                background = { offsetX ->
                    val color = if (offsetX < (-30).dp) Color.Red else Color.DarkGray
                    val scale by animateFloatAsState(if (offsetX < (-50).dp) 1f else 0.75f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color)
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            modifier = Modifier.scale(scale),
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "null"
                        )
                    }
                },
                content = { isDismissed ->
                    if (isDismissed) {
                        onSwiped(provisioner)
                    }
                    keyDismissed = isDismissed
                    Surface(color = MaterialTheme.colorScheme.background) {
                        MeshTwoLineListItem(
                            modifier = Modifier.clickable {
                                navigateToProvisioner(provisioner.uuid)
                            },
                            leadingIcon = {
                                Icon(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    imageVector = Icons.Outlined.Groups,
                                    contentDescription = null,
                                    tint = LocalContentColor.current.copy(alpha = 0.6f)
                                )
                            },
                            title = provisioner.name,
                            subtitle = provisioner.uuid.toString().uppercase(Locale.US)
                        )
                    }
                }
            )
        }
    }
}