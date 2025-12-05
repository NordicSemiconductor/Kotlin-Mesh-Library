package no.nordicsemi.android.nrfmesh.feature.scanner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.scanner.rememberFilterState
import no.nordicsemi.android.common.scanner.view.DeviceListItem
import no.nordicsemi.android.common.scanner.view.ScannerView
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.mesh.bearer.gatt.utils.MeshProvisioningService
import no.nordicsemi.kotlin.mesh.bearer.gatt.utils.MeshProxyService
import no.nordicsemi.kotlin.mesh.bearer.gatt.utils.MeshService
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.util.NetworkIdentity
import no.nordicsemi.kotlin.mesh.core.util.NodeIdentity
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
        state = rememberFilterState(filter = { ServiceUuid(uuid = service.uuid) }),
        onScanningStateChanged = {},
        deviceItem = { scanResult ->
            when (service) {
                MeshProvisioningService -> {
                    runCatching {
                        UnprovisionedDevice
                            .from(advertisementData = scanResult.advertisingData.raw)
                            .let { device ->
                                OutlinedCard(
                                    modifier = Modifier
                                        .height(height = 80.dp)
                                        .padding(horizontal = 8.dp)
                                        .padding(bottom = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        DeviceListItem(
                                            peripheralIcon = rememberVectorPainter(
                                                image = Icons.Default.Bluetooth
                                            ),
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
                    }.onFailure {
                        println("Failed to parse device: ${it.localizedMessage}")
                    }
                }

                MeshProxyService -> {
                    scanResult.advertisingData.serviceData[MeshProxyService.uuid]
                        ?.takeIf { it.isNotEmpty() }
                        ?.run {
                            OutlinedCard(
                                modifier = Modifier
                                    .height(height = 80.dp)
                                    .padding(horizontal = 8.dp)
                                    .padding(bottom = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    nodeIdentity()?.matches(nodes = nodes)?.let {
                                        DeviceListItem(
                                            peripheralIcon = rememberVectorPainter(
                                                image = Icons.Default.Bluetooth
                                            ),
                                            title = it.name,
                                            subtitle = it.primaryUnicastAddress.address.toHexString(
                                                format = HexFormat {
                                                    number.prefix = "Address: 0x"
                                                    upperCase = true
                                                }
                                            )
                                        )
                                    } ?: run {
                                        DeviceListItem(
                                            peripheralIcon = rememberVectorPainter(
                                                image = Icons.Default.Bluetooth
                                            ),
                                            title = scanResult.advertisingData.name
                                                ?: scanResult.peripheral.name
                                                ?: stringResource(R.string.label_unknown_device),
                                            subtitle = networkIdentity()
                                                ?.createMatchingDescription(networkKeys = networkKeys)
                                                ?: return@OutlinedCard
                                        )
                                    }
                                }
                            }
                        }

                }
            }
        },
        onScanResultSelected = onScanResultSelected
    )
}

private fun NodeIdentity.createMatchingSubtitle(nodes: List<Node>) =
    matches(nodes = nodes)?.primaryUnicastAddress?.address?.let {
        "Unicast Address: ${
            it.toHexString(
                format = HexFormat {
                    number.prefix = "0x"
                    upperCase = true
                }
            )
        }"
    } ?: "Node Identity: ${toHexString()}"


private fun NetworkIdentity?.createMatchingDescription(networkKeys: List<NetworkKey>) = this
    ?.takeIf { matches(networkKeys = networkKeys) != null }
    ?.let { "Network Identity: ${toHexString()}" }

