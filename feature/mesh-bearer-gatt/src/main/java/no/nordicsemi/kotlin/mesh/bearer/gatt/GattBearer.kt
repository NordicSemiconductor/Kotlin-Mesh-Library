@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.bearer.gatt

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import no.nordicsemi.android.kotlin.mesh.bearer.android.BaseGattProxyBearer
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProxyService
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.mesh.bearer.MeshBearer
import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.bearer.PduTypes
import kotlin.uuid.ExperimentalUuidApi

/**
 * Responsible for receiving and sending mesh messages to and from the GATT Proxy Node.
 */
class GattBearer(
    dispatcher: CoroutineDispatcher,
    context: Context,
    centralManager: CentralManager,
    peripheral: Peripheral
) : BaseGattProxyBearer<MeshProxyService>(
    dispatcher = dispatcher,
    context = context,
    centralManager = centralManager,
    peripheral = peripheral
), MeshBearer {

    override val supportedTypes: Array<PduTypes> =
        arrayOf(PduTypes.NetworkPdu, PduTypes.MeshBeacon, PduTypes.ProxyConfiguration)

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun configureGatt(services: List<RemoteService>){
        services.forEach { service ->
            service.characteristics.forEach { characteristic ->
                if (characteristic.uuid == MeshProxyService.dataInUuid) {
                    dataInCharacteristic = characteristic
                } else if (characteristic.uuid == MeshProxyService.dataOutUuid) {
                    awaitNotifications(dataOutCharacteristic = characteristic)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    internal suspend fun sendPdu(pdu: ByteArray, type: PduType) {
        send(pdu, type)
    }
}