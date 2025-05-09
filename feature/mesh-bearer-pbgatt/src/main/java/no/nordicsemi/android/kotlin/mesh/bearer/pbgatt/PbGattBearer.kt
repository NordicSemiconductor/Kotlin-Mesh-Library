@file:Suppress("unused")

package no.nordicsemi.android.kotlin.mesh.bearer.pbgatt

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import no.nordicsemi.android.kotlin.mesh.bearer.android.BaseGattProxyBearer
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProvisioningService
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.bearer.PduTypes
import no.nordicsemi.kotlin.mesh.bearer.provisioning.MeshProvisioningBearer
import kotlin.uuid.ExperimentalUuidApi

/**
 * Responsible for receiving and sending mesh provisioning messages to and from the GATT Proxy Node.
 */
open class PbGattBearer(
    dispatcher: CoroutineDispatcher,
    context: Context,
    centralManager: CentralManager,
    peripheral: Peripheral,
) : BaseGattProxyBearer<MeshProvisioningService>(
    dispatcher = dispatcher,
    context = context,
    centralManager = centralManager,
    peripheral = peripheral
), MeshProvisioningBearer {
    override val supportedTypes: Array<PduTypes>
        get() = arrayOf(PduTypes.ProvisioningPdu)

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun configureGatt(services: List<RemoteService>) {
        services.forEach { service ->
            service.characteristics.forEach { characteristic ->
                if (characteristic.uuid == MeshProvisioningService.dataInUuid) {
                    dataInCharacteristic = characteristic
                } else if (characteristic.uuid == MeshProvisioningService.dataOutUuid) {
                    awaitNotifications(dataOutCharacteristic = characteristic)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    internal suspend fun send(pdu: ByteArray) {
        send(pdu, PduType.PROVISIONING_PDU)
    }
}