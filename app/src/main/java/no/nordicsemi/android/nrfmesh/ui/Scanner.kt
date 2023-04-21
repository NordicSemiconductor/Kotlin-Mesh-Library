package no.nordicsemi.android.nrfmesh.ui

import android.os.ParcelUuid
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import no.nordicsemi.android.common.ui.scanner.ScannerView
import no.nordicsemi.android.common.ui.scanner.main.DeviceListItem
import no.nordicsemi.android.common.ui.scanner.view.ScannerAppBar
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProvisioningService
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshService
import no.nordicsemi.kotlin.mesh.provisioning.UnprovisionedDevice

@Composable
fun ScannerSheet(hideScanner: () -> Unit, service: MeshService) {
    Surface {
        Column {
            ScannerAppBar(text = "Scanning", onNavigationButtonClick = hideScanner)
            ScannerView(
                uuid = ParcelUuid(service.uuid),
                onResult = {},
                deviceItem = {
                    DeviceListItem(
                        name = it.displayName,
                        address = if (service is MeshProvisioningService) {
                            it.scanResult?.scanRecord?.bytes?.let { bytes ->
                                UnprovisionedDevice.from(bytes).uuid.toString().uppercase()
                            } ?: it.address
                        } else it.address
                    )
                }
            )
        }
    }
}