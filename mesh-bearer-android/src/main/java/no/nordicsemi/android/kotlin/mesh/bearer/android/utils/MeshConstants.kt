@file:Suppress("unused")

package no.nordicsemi.android.kotllin.mesh.bearer.ble.utils

import java.util.*

/**
 * Base interface for Mesh service objects.
 *
 * @property uuid            UUID of the service.
 * @property dataInUuid      UUID of the Data In characteristic
 * @property dataOutUuid     UUID of the Data Out characteristic.
 */
interface MeshService {
    var uuid: UUID
    var dataInUuid: UUID
    var dataOutUuid: UUID
}

/**
 * Mesh Provisioning Service object defines the mesh provisioning service. This service is present
 * on the unprovisioned devices.
 *
 * This is used to send provisioning messages over gatt.
 */
object MeshProvisioningService : MeshService {
    override var uuid: UUID = UUID.fromString("00001828-0000-1000-8000-00805f9b34fb")
    override var dataInUuid: UUID = UUID.fromString("00002adb-0000-1000-8000-00805f9b34fb")
    override var dataOutUuid: UUID = UUID.fromString("00002adc-0000-1000-8000-00805f9b34fb")
}

/**
 * Mesh Proxy Service object defines the mesh proxy service. This service is present
 * on the provisioned devices.
 *
 * This is used to send mesh messages over gatt.
 */
object MeshProxyService : MeshService {
    override var uuid: UUID = UUID.fromString("00001827-0000-1000-8000-00805f9b34fb")
    override var dataInUuid: UUID = UUID.fromString("00002add-0000-1000-8000-00805f9b34fb")
    override var dataOutUuid: UUID = UUID.fromString("00002ade-0000-1000-8000-00805f9b34fb")
}