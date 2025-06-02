@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.bearer.gatt

import kotlinx.coroutines.CoroutineDispatcher
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProxyService
import no.nordicsemi.kotlin.ble.client.CentralManager
import no.nordicsemi.kotlin.ble.client.Peripheral
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.ScanResult
import no.nordicsemi.kotlin.mesh.bearer.MeshBearer
import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.bearer.PduTypes
import kotlin.uuid.ExperimentalUuidApi

/**
 * Responsible for receiving and sending mesh messages to and from the GATT Proxy Node.
 *
 * @param state             Flow that emits events whenever the bearer state changes.
 * @param pdus              Flow that emits events whenever a PDU is received.
 * @param supportedTypes    List of supported PDU types.
 * @param logger            Logger receives logs sent from the bearer. The logs will contain raw
 *                          data of sent and received packets, as well as connection events.
 * @param isOpen            Returns true if the bearer is open, false otherwise.
 */
open class GattBearerImpl<
        ID : Any,
        P : Peripheral<ID, EX>,
        EX : Peripheral.Executor<ID>,
        F : CentralManager.ScanFilterScope,
        SR : ScanResult<*, *>,
        >(
    dispatcher: CoroutineDispatcher,
    centralManager: CentralManager<ID, P, EX, F, SR>,
    peripheral: P,
) : BaseGattBearer<MeshProxyService, ID, P, EX, F, SR>(
    dispatcher = dispatcher,
    centralManager = centralManager,
    peripheral = peripheral
), MeshBearer {
    override val supportedTypes: Array<PduTypes> =
        arrayOf(PduTypes.NetworkPdu, PduTypes.MeshBeacon, PduTypes.ProxyConfiguration)

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun configureGatt(services: List<RemoteService>) {
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

    internal suspend fun sendPdu(pdu: ByteArray, type: PduType) {
        send(pdu, type)
    }
}