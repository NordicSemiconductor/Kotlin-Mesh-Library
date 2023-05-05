@file:Suppress("unused")

package no.nordicsemi.android.kotlin.mesh.bearer.pbgatt

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.flow.*
import no.nordicsemi.android.kotlin.ble.client.main.callback.BleGattClient
import no.nordicsemi.android.kotlin.ble.client.main.connect
import no.nordicsemi.android.kotlin.ble.client.main.service.BleGattServices
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
    private val context: Context,
    private val device: ServerDevice
) : BaseGattProxyBearer<MeshProvisioningService>(), MeshProvisioningBearer {
    override val supportedTypes: Array<PduTypes>
        get() = arrayOf(PduTypes.ProvisioningPdu)

    private var client: BleGattClient? = null

    @SuppressLint("MissingPermission")
    override suspend fun open() {
        val client = device.connect(context)
        this.client = client
        client.discoverServices()
            .filterNotNull()
            .onEach { configureGatt(it) }
            .launchIn(scope = scope)
        mtu = client.requestMtu(517) - 3
        super.open()
    }

    override suspend fun close() {
        client?.disconnect()
        super.close()
    }

    @SuppressLint("MissingPermission")
    override suspend fun send(pdu: ByteArray, type: PduType) {
        super.send(pdu, type)
    }

    private suspend fun configureGatt(services: BleGattServices) {
        services.findService(uuid)?.let { service ->
            service.findCharacteristic(dataInUuid)?.let { dataInCharacteristic = it }
            service.findCharacteristic(dataOutUuid)?.let { dataOutCharacteristic = it }
        }
        awaitNotifications()
    }
}