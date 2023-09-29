@file:Suppress("unused")

package no.nordicsemi.android.kotlin.mesh.bearer.pbgatt

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.mesh.bearer.android.BaseGattProxyBearer
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProvisioningService
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProvisioningService.dataInUuid
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProvisioningService.dataOutUuid
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProvisioningService.uuid
import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.bearer.PduTypes
import no.nordicsemi.kotlin.mesh.bearer.provisioning.MeshProvisioningBearer

/**
 * Responsible for receiving and sending mesh provisioning messages to and from the GATT Proxy Node.
 */
open class PbGattBearer(
    context: Context,
    device: ServerDevice
) : BaseGattProxyBearer<MeshProvisioningService>(
    context = context,
    device = device
), MeshProvisioningBearer {
    override val supportedTypes: Array<PduTypes>
        get() = arrayOf(PduTypes.ProvisioningPdu)

    override suspend fun configureGatt(services: ClientBleGattServices) {
        services.findService(uuid)?.let { service ->
            service.findCharacteristic(dataInUuid)?.let { dataInCharacteristic = it }
            service.findCharacteristic(dataOutUuid)?.let { dataOutCharacteristic = it }
        }
        awaitNotifications()
    }

    @SuppressLint("MissingPermission")
    suspend fun send(pdu: ByteArray) {
        send(pdu, PduType.PROVISIONING_PDU)
    }
}