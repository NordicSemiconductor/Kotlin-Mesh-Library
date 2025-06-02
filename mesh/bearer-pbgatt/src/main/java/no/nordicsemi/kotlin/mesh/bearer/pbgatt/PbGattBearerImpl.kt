@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.bearer.pbgatt

import kotlinx.coroutines.CoroutineDispatcher
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProvisioningService
import no.nordicsemi.kotlin.ble.client.CentralManager
import no.nordicsemi.kotlin.ble.client.Peripheral
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.ScanResult
import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.bearer.PduTypes
import no.nordicsemi.kotlin.mesh.bearer.gatt.BaseGattBearer
import no.nordicsemi.kotlin.mesh.bearer.provisioning.ProvisioningBearer
import kotlin.uuid.ExperimentalUuidApi

/**
 * Responsible for receiving and sending mesh provisioning messages to and from the GATT Proxy Node.
 */
open class PbGattBearerImpl<
        ID : Any,
        P : Peripheral<ID, EX>,
        EX : Peripheral.Executor<ID>,
        F : CentralManager.ScanFilterScope,
        SR : ScanResult<*, *>,
        >(
    dispatcher: CoroutineDispatcher,
    centralManager: CentralManager<ID, P, EX, F, SR>,
    peripheral: P,
) : BaseGattBearer<MeshProvisioningService, ID, P, EX, F, SR>(
    dispatcher = dispatcher,
    centralManager = centralManager,
    peripheral = peripheral
), ProvisioningBearer {
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

    internal suspend fun send(pdu: ByteArray) {
        send(pdu, PduType.PROVISIONING_PDU)
    }
}