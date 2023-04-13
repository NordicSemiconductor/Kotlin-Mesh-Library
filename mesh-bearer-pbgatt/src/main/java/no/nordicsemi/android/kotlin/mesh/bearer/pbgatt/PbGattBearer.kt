@file:Suppress("unused")

package no.nordicsemi.android.kotlin.mesh.bearer.pbgatt

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.kotlin.ble.client.main.connect
import no.nordicsemi.android.kotlin.ble.client.main.service.BleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.BleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.mesh.bearer.android.BaseGattProxyBearer
import no.nordicsemi.android.kotllin.mesh.bearer.ble.utils.MeshProvisioningService
import no.nordicsemi.android.kotllin.mesh.bearer.ble.utils.MeshProvisioningService.dataInUuid
import no.nordicsemi.android.kotllin.mesh.bearer.ble.utils.MeshProvisioningService.dataOutUuid
import no.nordicsemi.android.kotllin.mesh.bearer.ble.utils.MeshProvisioningService.uuid
import no.nordicsemi.kotlin.mesh.bearer.BearerPdu
import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.bearer.PduTypes
import no.nordicsemi.kotlin.mesh.bearer.provisioning.MeshProvisioningBearer

/**
 * Responsible for receiving and sending mesh provisioning messages to and from the GATT Proxy Node.
 */
open class PbGattBearer(
    private val context: Context,
    private val device: ServerDevice
) : BaseGattProxyBearer<MeshProvisioningService>(), MeshProvisioningBearer {
    override val pdus: Flow<BearerPdu> = _pdu.asSharedFlow()
    private lateinit var dataInCharacteristic: BleGattCharacteristic
    private lateinit var dataOutCharacteristic: BleGattCharacteristic

    override val supportedTypes: Array<PduTypes>
        get() = arrayOf(PduTypes.ProvisioningPdu)

    @SuppressLint("MissingPermission")
    override suspend fun open() {
        val client = device.connect(context)
        client.requestMtu(517)
        client.services
            .filterNotNull()
            .onEach { configureGatt(it) }
        super.open()
    }

    @SuppressLint("MissingPermission")
    override suspend fun send(pdu: ByteArray, type: PduType) {
        super.send(pdu, type)
        dataOutCharacteristic.write(pdu)
    }

    private suspend fun configureGatt(services: BleGattServices) {
        services.findService(uuid)?.let { service ->
            service.findCharacteristic(dataInUuid)?.let { characteristic ->
                dataInCharacteristic = characteristic
                characteristic.getNotifications().onEach { data ->
                    PduType.from(data[0].toUByte())?.let { pduType ->
                        _pdu.emit(BearerPdu(data, pduType))
                    }
                }
            }
            service.findCharacteristic(dataOutUuid)?.let { characteristic ->
                dataOutCharacteristic = characteristic
            }
        }
    }
}