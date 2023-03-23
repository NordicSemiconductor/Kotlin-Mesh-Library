@file:Suppress("ClassName", "MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.provisioning

import no.nordicsemi.kotlin.mesh.core.util.Utils.toShort


/**
 * Information that points to the out-of-band information that the device can provide.
 * This si needed for provisioning.
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
         * @return The OobInformation or null if the raw value is not valid.
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

/**
 * The type  authentication method chosen for provisioning.
 */
sealed class AuthenticationMethod {

    /**
     * No OOB authentication method is used.
     */
    object NoOob : AuthenticationMethod()

    /**
     * Static OOB authentication method is used.
     */
    object StaticOob : AuthenticationMethod()

    /**
     * Output OOB authentication method is used. Size must be in range 1...8.
     *
     * @property action      Output action type.
     * @property size        Size of the input.
     */
    data class OutputOob(val action: OutputAction, val size: UByte) : AuthenticationMethod()

    /**
     * Input OOB authentication method is used. Size must be in range 1...8.
     *
     * @property action      Input action type.
     * @property size        Size of the input.
     */
    data class InputOob(val action: InputAction, val size: UByte) : AuthenticationMethod()

    val value: ByteArray
        get() = when (this) {
            NoOob -> byteArrayOf(0, 0, 0)
            StaticOob -> byteArrayOf(1, 0, 0)
            is OutputOob -> byteArrayOf(2, action.rawValue.toByte(), size.toByte())
            is InputOob -> byteArrayOf(3, action.rawValue.toByte(), size.toByte())
        }

    companion object {
        fun from(pdu: ProvisioningPdu): AuthenticationMethod? = when (pdu[3]) {
            0x00.toByte() -> NoOob
            0x01.toByte() -> StaticOob
            0x02.toByte() ->
                OutputAction.values()
                    .find {
                        it.rawValue == pdu[4].toUByte() && pdu[5].toInt() in 1..8
                    }?.let { outputAction ->
                        OutputOob(
                            action = outputAction,
                            size = pdu[5].toUByte()
                        )
                    }
            0x03.toByte() ->
                InputAction.values()
                    .find {
                        it.rawValue == pdu[4].toUByte() && pdu[5].toInt() in 1..8
                    }?.let {
                        InputOob(
                            action = it,
                            size = pdu[5].toUByte()
                        )
                    }
            else -> null
        }
    }
}

/**
 * A set of Out-of-band types.
 *
 * @property rawValue The raw value of the oob type.
 * @constructor Creates a new OobType.
 */
sealed class OobType(val rawValue: UByte) {
    constructor(rawValue: Int) : this(rawValue.toUByte())

    /**
     * Static OOB information is available.
     */
    object staticOobInformationAvailable : OobType(rawValue = 1 shl 0)

    /**
     * Only OOB authenticated provisioning is supported. Introduced in Mesh Protocol 1.1.0
     */
    object onlyOobAuthenticatedProvisioningSupported : OobType(rawValue = 1 shl 1)

    companion object {

        /**
         * Returns OobType from a given provisioning pdu.
         *
         * @param pdu      Provisioning pdu.
         * @param offset   Offset of the oob type.
         * @return OobType
         * @throws IllegalArgumentException if the oob type is invalid.
         */
        @Throws(IllegalArgumentException::class)
        fun from(pdu: ProvisioningPdu, offset: Int) = when (pdu[offset]) {
            0x01.toByte() -> staticOobInformationAvailable
            0x02.toByte() -> onlyOobAuthenticatedProvisioningSupported
            else -> throw IllegalArgumentException("Invalid OobType.")
        }
    }
}

/**
 * A set of support Output out-of-band actions.
 *
 *  @property rawValue The raw value of the output oob action.
 */
sealed class OutputOobActions(val rawValue: UShort) {

    constructor(rawValue: Int) : this(rawValue.toUShort())

    object blink : OutputOobActions(rawValue = 1 shl 0)
    object beep : OutputOobActions(rawValue = 1 shl 1)
    object vibrate : OutputOobActions(rawValue = 1 shl 2)
    object outputNumeric : OutputOobActions(rawValue = 1 shl 3)
    object outputAlphanumeric : OutputOobActions(rawValue = 1 shl 4)

    companion object {

        /**
         * Returns OutputOobActions from a given provisioning pdu.
         *
         * @param pdu      Provisioning pdu.
         * @param offset   Offset of the output oob action.
         * @return OutputOobActions.
         * @throws IllegalArgumentException if the output oob action is invalid.
         */
        @Throws(IllegalArgumentException::class)
        fun from(pdu: ProvisioningPdu, offset: Int) = when (pdu.toShort(offset).toUShort()) {
            0x0001.toUShort() -> blink
            0x0002.toUShort() -> beep
            0x0003.toUShort() -> vibrate
            0x0004.toUShort() -> outputNumeric
            0x0005.toUShort() -> outputAlphanumeric
            else -> throw IllegalArgumentException("Invalid output oob action.")
        }
    }
}

/**
 * A set of support Input out-of-band actions.
 *
 *  @property rawValue The raw value of the input oob action.
 */
sealed class InputOobActions(val rawValue: UShort) {

    constructor(rawValue: Int) : this(rawValue.toUShort())

    object push : InputOobActions(rawValue = 1 shl 0)
    object twist : InputOobActions(rawValue = 1 shl 1)
    object outputNumeric : InputOobActions(rawValue = 1 shl 2)
    object outputAlphanumeric : InputOobActions(rawValue = 1 shl 3)

    companion object {

        /**
         * Returns InputOobActions from a given provisioning pdu.
         *
         * @param pdu      Provisioning pdu.
         * @param offset   Offset of the input oob action.
         * @return InputOobActions.
         * @throws IllegalArgumentException if the input oob action is invalid.
         */
        @Throws(IllegalArgumentException::class)
        fun from(pdu: ProvisioningPdu, offset: Int) = when (pdu[offset]) {
            0x01.toByte() -> push
            0x02.toByte() -> twist
            0x03.toByte() -> outputNumeric
            0x04.toByte() -> outputAlphanumeric
            else -> throw IllegalArgumentException("Invalid input oob action.")
        }
    }
}

enum class OutputAction constructor(val rawValue: UByte) {
    BLINK(0.toUByte()),
    BEEP(1.toUByte()),
    VIBRATE(2.toUByte()),
    OUTPUT_NUMERIC(3.toUByte()),
    OUTPUT_ALPHANUMERIC(4.toUByte())
}

enum class InputAction(val rawValue: UByte) {
    PUSH(0.toUByte()),
    TWIST(1.toUByte()),
    INPUT_NUMERIC(2.toUByte()),
    INPUT_ALPHANUMERIC(3.toUByte())
}