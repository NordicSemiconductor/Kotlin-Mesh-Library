@file:Suppress("unused")

package no.nordicsemi.android.mesh.provisioning


/**
 * The device sends this PDU to indicate the supported capabilities to a provisioner.
 *
 * @property numberOfElements                 Number of elements supported by the device.
 * @property algorithms                       Algorithms supported by the device.
 * @property publicKeyType                    Public key type supported by the device.
 * @property oobType                          OOB type supported by the device.
 * @property outputOobSize                    Output OOB size supported by the device.
 * @property outputOobActions                 Output OOB actions supported by the device.
 * @property inputOobSize                     Input OOB size supported by the device.
 * @property inputOobActions                  Input OOB actions supported by the device.
 * @constructor Creates a [ProvisioningCapabilities] object.
 */
data class ProvisioningCapabilities(
    val numberOfElements: Int,
    val algorithms: Algorithms,
    val publicKeyType: PublicKeyType,
    val oobType: OobType,
    val outputOobSize: UByte,
    val outputOobActions: OutputOobActions,
    val inputOobSize: UByte,
    val inputOobActions: InputOobActions
) {

    constructor(data: ProvisioningPdu) : this(
        numberOfElements = data[1].toUByte().toInt(),
        algorithms = Algorithms.from(data, 2),
        publicKeyType = PublicKeyType.from(data, 4),
        oobType = OobType.from(data, 5),
        outputOobSize = data[6].toUByte(),
        outputOobActions = OutputOobActions.from(data, 7),
        inputOobSize = data[9].toUByte(),
        inputOobActions = InputOobActions.from(data, 10)
    )
}
