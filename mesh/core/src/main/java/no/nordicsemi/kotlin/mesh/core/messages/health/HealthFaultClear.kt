@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.health

import no.nordicsemi.kotlin.data.getUShort
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.BaseMeshMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.HasOpCode
import java.nio.ByteOrder

/**
 * A Health Fault Clear is an acknowledged message used to clear the current Registered Fault
 * state identified by Company ID of an element.
 *
 * @property companyIdentifier 16-bit Bluetooth assigned Company Identifier.
 */
class HealthFaultClear(
    val companyIdentifier: UShort
) : AcknowledgedMeshMessage {
    override val opCode: UInt = Initializer.opCode
    override val responseOpCode: UInt = HealthFaultStatus.opCode
    override val parameters: ByteArray = companyIdentifier.toByteArray(ByteOrder.LITTLE_ENDIAN)

    override fun toString() = "HealthFaultClear(companyIdentifier: $companyIdentifier)"

    companion object Initializer : BaseMeshMessageInitializer, HasOpCode {
        override val opCode = 0x802Fu

        override fun init(parameters: ByteArray?) = parameters
            ?.takeIf { it.size == 2 }
            ?.let { params ->
                HealthFaultClear(params.getUShort(offset = 0, order = ByteOrder.LITTLE_ENDIAN))
            }
    }
}
