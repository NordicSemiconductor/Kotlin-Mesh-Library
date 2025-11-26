package no.nordicsemi.android.nrfmesh.core.data.bearer

import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.ConjunctionFilterScope
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.mesh.bearer.gatt.GattBearerImpl
import kotlin.uuid.ExperimentalUuidApi

class AndroidGattBearer(
    centralManager: CentralManager,
    peripheral: Peripheral,
) : GattBearerImpl<
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
    override suspend fun open() {
        super.open()
        // Request highest connection parameters after connect in the super.open()
        peripheral.requestHighestValueLength()
    }
}