package no.nordicsemi.android.nrfmesh.ui

import android.os.ParcelUuid
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.common.ui.scanner.ScannerView
import no.nordicsemi.android.common.ui.scanner.main.DeviceListItem
import no.nordicsemi.android.common.ui.scanner.view.ScannerAppBar
import no.nordicsemi.android.kotlin.ble.scanner.data.BleScanResults
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProvisioningService
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshService
import no.nordicsemi.android.nrfmesh.R
import no.nordicsemi.kotlin.mesh.provisioning.UnprovisionedDevice

@Composable
fun ScannerSheet(
    service: MeshService,
    onDeviceFound: (BleScanResults) -> Unit,
    hideScanner: () -> Unit
) {
    Surface {
        Column {
            var unprovisionedDevice by remember { mutableStateOf<UnprovisionedDevice?>(null) }
            ScannerAppBar(
                text = stringResource(R.string.title_scanning),
                backButtonIcon = Icons.Default.Close,
                onNavigationButtonClick = hideScanner
            )
            ScannerView(
                uuid = ParcelUuid(service.uuid),
                onResult = { result ->
                    result.lastScanResult?.scanRecord?.bytes?.let { bytes ->
                        unprovisionedDevice = UnprovisionedDevice.from(bytes)
                    }?.let {
                        onDeviceFound(result)
                    }
                },
                deviceItem = {
                    DeviceListItem(
                        name = it.device.name,
                        address = if (service is MeshProvisioningService) {
                            it.lastScanResult?.scanRecord?.bytes?.let { bytes ->
                                UnprovisionedDevice.from(bytes).uuid.toString().uppercase()
                            } ?: it.device.address
                        } else it.device.address
                    )
                }
            )
        }
    }
}