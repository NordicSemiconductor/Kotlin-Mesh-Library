package no.nordicsemi.android.nrfmesh.feature.scanner

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.common.scanner.rememberFilterState
import no.nordicsemi.android.common.scanner.view.DeviceListItem
import no.nordicsemi.android.common.scanner.view.ScannerView
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.mesh.bearer.gatt.utils.MeshProvisioningService
import no.nordicsemi.kotlin.mesh.bearer.gatt.utils.MeshProxyService
import no.nordicsemi.kotlin.mesh.bearer.gatt.utils.MeshService
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.util.NetworkIdentity
import no.nordicsemi.kotlin.mesh.core.util.NodeIdentity
import no.nordicsemi.kotlin.mesh.core.util.networkIdentity
import no.nordicsemi.kotlin.mesh.core.util.nodeIdentity
import no.nordicsemi.kotlin.mesh.provisioning.UnprovisionedDevice
import kotlin.collections.isNotEmpty
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalUuidApi::class)
@Composable
fun ScannerContent(
    meshNetwork: MeshNetwork? = null,
    service: MeshService,
    onScanResultSelected: (ScanResult) -> Unit,
) {
    ScannerView(
        state = rememberFilterState(filter = { ServiceUuid(uuid = service.uuid) }),
        onScanningStateChanged = {},
        deviceItem = { scanResult ->
            when (service) {
                MeshProvisioningService -> {
                    runCatching {
                        UnprovisionedDevice
                            .from(advertisementData = scanResult.advertisingData.raw)
                            .let { device ->
                                DeviceListItem(
                                    peripheralIcon = rememberVectorPainter(Icons.Default.Bluetooth),
                                    title = when {
                                        scanResult.advertisingData.name.isNullOrEmpty() -> device.name
                                        else -> scanResult.advertisingData.name
                                            ?: stringResource(R.string.label_unknown_device)
                                    },
                                    subtitle = device.uuid.toString().uppercase()
                                )
                            }
                    }.onFailure {
                        println("Failed to parse device: ${it.localizedMessage}")
                    }
                }

                MeshProxyService -> {
                    scanResult.advertisingData.serviceData[MeshProxyService.uuid]
                        ?.takeIf { it.isNotEmpty() }
                        ?.run {
                            meshNetwork?.let {
                                DeviceListItem(
                                    peripheralIcon = rememberVectorPainter(
                                        image = Icons.Default.Bluetooth
                                    ),
                                    title = scanResult.advertisingData.name
                                        ?: scanResult.peripheral.name
                                        ?: stringResource(R.string.label_unknown_device),
                                    subtitle = nodeIdentity()
                                        ?.createMatchingDescription(network = it)
                                        ?: networkIdentity()
                                            ?.createMatchingDescription(network = it)
                                        ?: return@run
                                )
                            }
                        }

                }
            }
        },
        onScanResultSelected = onScanResultSelected
    )
}

private fun NodeIdentity.createMatchingDescription(network: MeshNetwork) = when {
    matches(nodes = network.nodes) != null -> toHexString()
    else -> null
}


private fun NetworkIdentity?.createMatchingDescription(network: MeshNetwork) = this
    ?.takeIf { matches(networkKeys = network.networkKeys) != null }
    ?.toHexString()

