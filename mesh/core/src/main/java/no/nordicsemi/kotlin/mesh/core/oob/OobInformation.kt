package no.nordicsemi.kotlin.mesh.core.oob

/**
 * Information that points to the out-of-band information that the device can provide.
 * This is needed for provisioning.
 *
 * @property rawValue The raw value of the OOB information.
 * @constructor Creates a new OobInformation.
 */
sealed class OobInformation(val rawValue: UShort) {

    constructor(rawValue: Int) : this(rawValue.toUShort())

    object None : OobInformation(rawValue = 0)
    object Other : OobInformation(rawValue = 1 shl 0)
    object ElectronicURI : OobInformation(rawValue = 1 shl 1)
    object QrCode : OobInformation(rawValue = 1 shl 2)
    object BarCode : OobInformation(rawValue = 1 shl 3)
    object Nfc : OobInformation(rawValue = 1 shl 4)
    object Number : OobInformation(rawValue = 1 shl 5)
    object String : OobInformation(rawValue = 1 shl 6)

    // Bits 7-10 are reserved for future use.
    object OnBox : OobInformation(rawValue = 1 shl 11)
    object InsideBox : OobInformation(rawValue = 1 shl 12)
    object OnPieceOfPaper : OobInformation(rawValue = 1 shl 13)
    object InsideManual : OobInformation(rawValue = 1 shl 14)
    object OnDevice : OobInformation(rawValue = 1 shl 15)

    companion object {

        /**
         * Creates an OobInformation from the raw value.
         *
         * @param rawValue The raw value of the OOB information.
         * @return The Oob Information or null if the raw value is not valid.
         * @throws IllegalArgumentException If the raw value is not valid.
         */
        @Throws(IllegalArgumentException::class)
        fun from(rawValue: UShort) = when (rawValue) {
            None.rawValue -> None
            Other.rawValue -> Other
            ElectronicURI.rawValue -> ElectronicURI
            QrCode.rawValue -> QrCode
            BarCode.rawValue -> BarCode
            Nfc.rawValue -> Nfc
            Number.rawValue -> Number
            String.rawValue -> String
            OnBox.rawValue -> OnBox
            InsideBox.rawValue -> InsideBox
            OnPieceOfPaper.rawValue -> OnPieceOfPaper
            InsideManual.rawValue -> InsideManual
            OnDevice.rawValue -> OnDevice
            else -> throw IllegalArgumentException("Invalid advertisement packet")
        }
    }
}