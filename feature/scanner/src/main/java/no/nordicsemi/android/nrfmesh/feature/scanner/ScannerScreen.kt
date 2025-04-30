package no.nordicsemi.android.nrfmesh.feature.scanner

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.permissions.ble.RequireBluetooth
import no.nordicsemi.android.common.permissions.ble.RequireLocation
import no.nordicsemi.android.common.ui.view.CircularIcon
import no.nordicsemi.android.nrfmesh.core.ui.MeshTwoLineListItem
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
internal fun ScannerScreen(
    uuid: Uuid,
    uiState: ScannerUiState,
    startScanning: (Uuid) -> Unit,
    onRefreshScan: () -> Unit,
    onScanResultSelected: (ScanResult) -> Unit,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize()) {
        RequireBluetooth {
            RequireLocation { isLocationRequiredAndDisabled ->
                // Both Bluetooth and Location permissions are granted.
                // If the permission is not granted then the scanning will not start.
                // So to start scanning we need to check if the location permission is granted.
                LaunchedEffect(isLocationRequiredAndDisabled) {
                    startScanning(uuid)
                }
                Column(modifier = Modifier.fillMaxSize()) {
                    PullToRefreshBox(
                        isRefreshing = uiState.scanningState is ScanningState.Loading,
                        onRefresh = {
                            onRefreshScan()
                            scope.launch {
                                pullToRefreshState.animateToHidden()
                            }
                        },
                        state = pullToRefreshState,
                        content = {
                            DeviceListView(
                                isLocationRequiredAndDisabled = isLocationRequiredAndDisabled,
                                scanningState = uiState.scanningState,
                                modifier = Modifier.fillMaxSize(),
                                onClick = onScanResultSelected,
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
internal fun DeviceListView(
    isLocationRequiredAndDisabled: Boolean,
    scanningState: ScanningState,
    modifier: Modifier = Modifier,
    onClick: (ScanResult) -> Unit,
) {
    val context = LocalContext.current
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        when (scanningState) {
            is ScanningState.Loading -> item {
                ScanEmptyView(
                    locationRequiredAndDisabled = isLocationRequiredAndDisabled,
                    navigateToLocationSettings = { openLocationSettings(context = context) }
                )
            }

            is ScanningState.ScanResultsDiscovered -> if (scanningState.results.isEmpty()) {
                item {
                    ScanEmptyView(
                        locationRequiredAndDisabled = isLocationRequiredAndDisabled,
                        navigateToLocationSettings = { openLocationSettings(context = context) }
                    )
                }
            } else {
                DeviceListItems(scanningState.results, onClick)
            }

            is ScanningState.Error -> item { ScanErrorView(scanningState.error) }
        }
    }

}

@Suppress("FunctionName")
internal fun LazyListScope.DeviceListItems(
    results: List<ScanResult>,
    onScanResultSelected: (ScanResult) -> Unit,
) {
    items(results.size) { index ->
        DeviceListItem(
            name = results[index].peripheral.name,
            address = results[index].peripheral.address,
            onClick = { onScanResultSelected(results[index]) }
        )
    }
}

@Composable
private fun DeviceListItem(
    peripheralIcon: ImageVector = Icons.Default.Bluetooth,
    name: String?,
    address: String,
    onClick: () -> Unit,
) {
    MeshTwoLineListItem(
        modifier = Modifier.clickable(onClick = onClick),
        leadingComposable = {
            CircularIcon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = peripheralIcon
            )
        },
        title = name ?: "Unknown device",
        subtitle = address,
        subtitleMaxLines = 1,
    )
}

private fun openLocationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    context.startActivity(intent)
}