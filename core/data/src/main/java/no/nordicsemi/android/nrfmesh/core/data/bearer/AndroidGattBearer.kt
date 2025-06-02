package no.nordicsemi.android.nrfmesh.core.data.bearer

import kotlinx.coroutines.CoroutineDispatcher
import no.nordicsemi.android.nrfmesh.core.common.di.DefaultDispatcher
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.ConjunctionFilterScope
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.mesh.bearer.gatt.GattBearerImpl

class AndroidGattBearer(
    @DefaultDispatcher dispatcher: CoroutineDispatcher,
    centralManager: CentralManager,
    peripheral: Peripheral,
) : GattBearerImpl<
        String,
        Peripheral,
        Peripheral.Executor,
        ConjunctionFilterScope,
        ScanResult
>(
    dispatcher = dispatcher,
    centralManager = centralManager,
    peripheral = peripheral
) {
    override suspend fun open() {
        super.open()
        // Request the maximum transmission unit (MTU) size.
        peripheral.requestHighestValueLength()
    }
}