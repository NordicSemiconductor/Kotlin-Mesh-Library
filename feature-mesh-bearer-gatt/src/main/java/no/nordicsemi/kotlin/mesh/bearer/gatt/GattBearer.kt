@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.bearer.gatt

import android.annotation.SuppressLint
import android.content.Context
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.mesh.bearer.android.BaseGattProxyBearer
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProxyService
import no.nordicsemi.kotlin.mesh.bearer.MeshBearer
import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.bearer.PduTypes

/**
 * Responsible for receiving and sending mesh messages to and from the GATT Proxy Node.
 */
class GattBearer(
    context: Context,
    device: ServerDevice
) : BaseGattProxyBearer<MeshProxyService>(
    context = context,
    device = device
), MeshBearer {

    override val supportedTypes: Array<PduTypes> =
        arrayOf(PduTypes.NetworkPdu, PduTypes.MeshBeacon, PduTypes.ProxyConfiguration)

    override suspend fun configureGatt(services: ClientBleGattServices) {
        services.findService(MeshProxyService.uuid)?.let { service ->
            service.findCharacteristic(MeshProxyService.dataInUuid)
                ?.let { dataInCharacteristic = it }
            service.findCharacteristic(MeshProxyService.dataOutUuid)
                ?.let { dataOutCharacteristic = it }
        }
        awaitNotifications()
    }

    @SuppressLint("MissingPermission")
    suspend fun sendPdu(pdu: ByteArray, type: PduType) {
        send(pdu, type)
    }
}