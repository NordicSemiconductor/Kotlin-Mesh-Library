package no.nordicsemi.android.nrfmesh.feature.scanner

 import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.WavingHand
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.scanner.R.drawable
import no.nordicsemi.android.common.scanner.rememberFilterState
import no.nordicsemi.android.common.scanner.view.DeviceListItem
import no.nordicsemi.android.common.scanner.view.ScannerView
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.mesh.bearer.gatt.utils.MeshProvisioningService
import no.nordicsemi.kotlin.mesh.bearer.gatt.utils.MeshProxyService
import no.nordicsemi.kotlin.mesh.bearer.gatt.utils.MeshService
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.util.networkIdentity
import no.nordicsemi.kotlin.mesh.core.util.nodeIdentity
import no.nordicsemi.kotlin.mesh.provisioning.UnprovisionedDevice
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalUuidApi::class)
@Composable
fun ScannerContent(
    nodes: List<Node>,
    networkKeys: List<NetworkKey>,
    service: MeshService,
    onScanResultSelected: (ScanResult) -> Unit,
) {
    ScannerView(
        modifier = Modifier.padding(horizontal = 16.dp),
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
                                    iconPainter = rememberVectorPainter(Icons.Outlined.Bluetooth),
                                    title = when {
                                        scanResult.advertisingData.name.isNullOrEmpty() -> device.name
                                        else -> scanResult.advertisingData.name
                                            ?: stringResource(R.string.label_unknown_device)
                                    },
                                    subtitle = device.uuid.toString().uppercase()
                                )
                            }
                    }
                }

                MeshProxyService -> {
                    scanResult.advertisingData.serviceData[MeshProxyService.uuid]
                        ?.takeIf { it.isNotEmpty() }
                        ?.run {
                            nodeIdentity()?.matches(nodes = nodes)?.let {
                                DeviceListItem(
                                    iconPainter = rememberVectorPainter(Icons.Outlined.WavingHand),
                                    title = it.name,
                                    subtitle = it.primaryUnicastAddress.address.toHexString(
                                        format = HexFormat {
                                            number.prefix = "Address: 0x"
                                            upperCase = true
                                        }
                                    )
                                )
                            } ?: run {
                                networkIdentity()?.matches(networkKeys = networkKeys)?.let { netKey ->
                                    DeviceListItem(
                                        iconPainter = painterResource(drawable.ic_mesh),
                                        title = scanResult.advertisingData.name
                                            ?: scanResult.peripheral.name
                                            ?: stringResource(R.string.label_unknown_device),
                                        subtitle = netKey.name
                                    )
                                }
                            }
                        }

                }
            }
        },
        onScanResultSelected = onScanResultSelected
    )
}
