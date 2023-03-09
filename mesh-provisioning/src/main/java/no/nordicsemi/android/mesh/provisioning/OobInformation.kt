@file:Suppress("ClassName", "MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.android.mesh.provisioning


/**
 * Information that points to the out-of-band information that the device can provide.
 * This si needed for provisioning.
 *
 * @property rawValue The raw value of the OOB information.
 * @constructor Creates a new OobInformation.
 */
open class OobInformation(val rawValue: UShort) {

    constructor(rawValue: Int) : this(rawValue.toUShort())

    object other : OobInformation(rawValue = 1 shl 0)
    object electronicURI : OobInformation(rawValue = 1 shl 1)
    object qrCode : OobInformation(rawValue = 1 shl 2)
    object barCode : OobInformation(rawValue = 1 shl 3)
    object nfc : OobInformation(rawValue = 1 shl 4)
    object number : OobInformation(rawValue = 1 shl 5)
    object string : OobInformation(rawValue = 1 shl 6)

    // Bits 7-10 are reserved for future use.
    object onBox : OobInformation(rawValue = 1 shl 11)
    object insideBox : OobInformation(rawValue = 1 shl 12)
    object onPieceOfPaper : OobInformation(rawValue = 1 shl 13)
    object insideManual : OobInformation(rawValue = 1 shl 14)
    object onDevice : OobInformation(rawValue = 1 shl 15)
}