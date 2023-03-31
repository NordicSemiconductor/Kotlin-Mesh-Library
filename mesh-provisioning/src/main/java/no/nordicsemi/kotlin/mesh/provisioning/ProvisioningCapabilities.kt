@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.provisioning

import no.nordicsemi.kotlin.mesh.core.util.Utils.toByteArray
import no.nordicsemi.kotlin.mesh.core.util.Utils.toUShort
import no.nordicsemi.kotlin.mesh.crypto.Algorithms
import no.nordicsemi.kotlin.mesh.crypto.Algorithms.Companion.toUShort
import no.nordicsemi.kotlin.mesh.provisioning.InputOobActions.Companion.toUShort
import no.nordicsemi.kotlin.mesh.provisioning.OobType.Companion.toByte
import no.nordicsemi.kotlin.mesh.provisioning.OutputOobActions.Companion.toUShort
import no.nordicsemi.kotlin.mesh.provisioning.PublicKeyType.Companion.toByte


/**
 * The device sends this PDU to indicate the supported capabilities to a provisioner.
 *
 * @property numberOfElements                 Number of elements supported by the device.
 * @property algorithms                       Algorithms supported by the device.
 * @property publicKeyType                    Public key type supported by the device.
 * @property oobTypes                          OOB type supported by the device.
 * @property outputOobSize                    Output OOB size supported by the device.
 * @property outputOobActions                 Output OOB actions supported by the device.
 * @property inputOobSize                     Input OOB size supported by the device.
 * @property inputOobActions                  Input OOB actions supported by the device.
 * @property value                            The raw data pdu of the provisioning capabilities.
 * @constructor Creates a [ProvisioningCapabilities] object.
 */
data class ProvisioningCapabilities(
    val numberOfElements: Int,
    val algorithms: List<Algorithms>,
    val publicKeyType: List<PublicKeyType>,
    val oobTypes: List<OobType>,
    val outputOobSize: UByte,
    val outputOobActions: List<OutputOobActions>,
    val inputOobSize: UByte,
    val inputOobActions: List<InputOobActions>
) {

    constructor(data: ProvisioningPdu) : this(
        numberOfElements = data[1].toUByte().toInt(),
        algorithms = Algorithms.from(data.toUShort(2)),
        publicKeyType = PublicKeyType.from(data[4].toUByte()),
        oobTypes = OobType.from(data[5].toUByte()),
        outputOobSize = data[6].toUByte(),
        outputOobActions = OutputOobActions.from(data.toUShort(7)),
        inputOobSize = data[9].toUByte(),
        inputOobActions = InputOobActions.from(data.toUShort(10))
    )

    val value: ProvisioningPdu
        get() = byteArrayOf(numberOfElements.toByte()) +
                algorithms.toUShort().toByteArray() +
                byteArrayOf(publicKeyType.toByte(), oobTypes.toByte(), outputOobSize.toByte()) +
                outputOobActions.toUShort().toByteArray() +
                byteArrayOf(inputOobSize.toByte()) +
                inputOobActions.toUShort().toByteArray()
}
