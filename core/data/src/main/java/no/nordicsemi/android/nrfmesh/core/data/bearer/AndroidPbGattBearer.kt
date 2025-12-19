package no.nordicsemi.android.nrfmesh.core.data.bearer

import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.ConjunctionFilterScope
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.mesh.bearer.pbgatt.PbGattBearerImpl
import kotlin.uuid.ExperimentalUuidApi

class AndroidPbGattBearer(
    centralManager: CentralManager,
    peripheral: Peripheral,
) : PbGattBearerImpl<
        String,
        CentralManager,
        Peripheral,
        Peripheral.Executor,
        ConjunctionFilterScope,
        ScanResult
        >(
    centralManager = centralManager,
    peripheral = peripheral
) {
    @OptIn(ExperimentalUuidApi::class)
    override suspend fun configurePeripheral(peripheral: Peripheral) {
        // Request highest connection parameters after connect in the super.open()
        peripheral.requestHighestValueLength()

    }
}