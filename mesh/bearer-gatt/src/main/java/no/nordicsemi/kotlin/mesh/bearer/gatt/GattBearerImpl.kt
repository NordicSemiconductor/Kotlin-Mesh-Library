@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.bearer.gatt

import kotlinx.coroutines.CoroutineDispatcher
import no.nordicsemi.kotlin.ble.client.CentralManager
import no.nordicsemi.kotlin.ble.client.Peripheral
import no.nordicsemi.kotlin.ble.client.ScanResult
import no.nordicsemi.kotlin.mesh.bearer.MeshBearer
import no.nordicsemi.kotlin.mesh.bearer.PduTypes
import no.nordicsemi.kotlin.mesh.bearer.gatt.utils.MeshProxyService
import no.nordicsemi.kotlin.mesh.bearer.gatt.utils.MeshService

/**
 * Responsible for receiving and sending mesh messages to and from the GATT Proxy Node.
 *
 * @param supportedTypes    List of supported PDU types.
 * @param logger            Logger receives logs sent from the bearer. The logs will contain raw
 *                          data of sent and received packets, as well as connection events.
 */
open class GattBearerImpl<
        ID : Any,
        C : CentralManager<ID, P, EX, F, SR>,
        P : Peripheral<ID, EX>,
        EX : Peripheral.Executor<ID>,
        F : CentralManager.ScanFilterScope,
        SR : ScanResult<*, *>,
        >(
    peripheral: P,
    centralManager: C,
    ioDispatcher: CoroutineDispatcher,
) : BaseGattBearer<ID, C, P, EX, F, SR>(
    centralManager = centralManager,
    peripheral = peripheral,
    ioDispatcher = ioDispatcher
), MeshBearer {
    override val supportedTypes: Array<PduTypes> =
        arrayOf(PduTypes.NetworkPdu, PduTypes.MeshBeacon, PduTypes.ProxyConfiguration)

    final override val meshService: MeshService = MeshProxyService
}

