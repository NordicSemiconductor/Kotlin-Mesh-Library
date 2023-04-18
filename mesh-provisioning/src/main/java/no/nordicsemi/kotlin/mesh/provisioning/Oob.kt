@file:Suppress("ClassName", "MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.provisioning

import kotlin.math.pow


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
     * @property action        Output action type.
     * @property length        Size of the input.
     * @constructor Constructs a new OutputOob.
     */
    data class OutputOob(val action: OutputAction, val length: UByte) : AuthenticationMethod() {
        constructor(action: OutputAction) : this(action, action.rawValue)
    }

    /**
     * Input OOB authentication method is used. Size must be in range 1...8.
     *
     * @property action        Input action type.
     * @property length        Size of the input.
     * @constructor Constructs a new InputOob.
     */
    data class InputOob(val action: InputAction, val length: UByte) : AuthenticationMethod()

    internal val value: ByteArray
        get() = when (this) {
            NoOob -> byteArrayOf(0, 0, 0)
            StaticOob -> byteArrayOf(1, 0, 0)
            is OutputOob -> byteArrayOf(2, action.rawValue.toByte(), length.toByte())
            is InputOob -> byteArrayOf(3, action.rawValue.toByte(), length.toByte())
        }

    companion object {

        /**
         * Returns the authentication method from a given provisioning pdu.
         *
         * @param pdu Provisioning pdu.
         * @return AuthenticationMethod or null otherwise.
         */
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
                            length = pdu[5].toUByte()
                        )
                    }

            0x03.toByte() ->
                InputAction.values()
                    .find {
                        it.rawValue == pdu[4].toUByte() && pdu[5].toInt() in 1..8
                    }?.let {
                        InputOob(
                            action = it,
                            length = pdu[5].toUByte()
                        )
                    }

            else -> null
        }

        fun randomAlphaNumeric(length: Int): String {
            val letters = ('0'..'9') + ('A'..'Z')
            return (1 until length).map {
                letters.random()
            }.joinToString("")
        }

        /**
         * Returns a random integer with the given length.
         *
         * @param length The length of the integer.
         */
        fun randomInt(length: Int) = (0 until 10.0.pow(length.toDouble()).toInt()).random()
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
    object StaticOobInformationAvailable : OobType(rawValue = 1 shl 0)

    /**
     * Only OOB authenticated provisioning is supported. Introduced in Mesh Protocol 1.1.0
     */
    object OnlyOobAuthenticatedProvisioningSupported : OobType(rawValue = 1 shl 1)

    companion object {

        private val oobTypes = listOf(
            StaticOobInformationAvailable, OnlyOobAuthenticatedProvisioningSupported
        )

        /**
         * Returns the supported oob types based on the give value.
         *
         * @param value    Supported OobTypes value contained from the provisioning capabilities.
         * @return List a of supported OobTypes or an empty list if none are supported.
         */
        @Throws(IllegalArgumentException::class)
        fun from(value: UByte) = oobTypes.filter {
            it.rawValue.toInt() and value.toInt() != 0
        }

        /**
         * Converts a list of supported oob types to a UByte value.
         *
         * @receiver List of OOB types.
         * @return UByte containing the raw value of the list of algorithms.
         */
        fun List<OobType>.toByte(): Byte {
            var value = 0
            forEach {
                value = value or it.rawValue.toInt()
            }
            return value.toByte()
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

    object Blink : OutputOobActions(rawValue = 1 shl 0)
    object Beep : OutputOobActions(rawValue = 1 shl 1)
    object Vibrate : OutputOobActions(rawValue = 1 shl 2)
    object OutputNumeric : OutputOobActions(rawValue = 1 shl 3)
    object OutputAlphanumeric : OutputOobActions(rawValue = 1 shl 4)

    companion object {
        private val actions = listOf(Blink, Beep, Vibrate, OutputNumeric, OutputAlphanumeric)

        /**
         * Returns the list supported OutputOobActions from a given Output OOB Actions value.
         *
         * @param value Output oob actions value from provisioning capabilities pdu.
         * @return a list of supported OutputOobActions or an empty list if none is supported.
         */
        fun from(value: UShort) = actions.filter {
            it.rawValue.toInt() and value.toInt() != 0
        }

        /**
         * Converts a list of OutputOobActions to a UShort value.
         *
         * @receiver List of OutputOobActions.
         * @return UShort containing the raw value of the list of OutputOobActions.
         */
        fun List<OutputOobActions>.toUShort(): UShort {
            var value = 0
            forEach {
                value = value or it.rawValue.toInt()
            }
            return value.toUShort()
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

    object Push : InputOobActions(rawValue = 1 shl 0)
    object Twist : InputOobActions(rawValue = 1 shl 1)
    object InputNumeric : InputOobActions(rawValue = 1 shl 2)
    object InputAlphanumeric : InputOobActions(rawValue = 1 shl 3)

    companion object {
        private val actions = listOf(Push, Twist, InputNumeric, InputAlphanumeric)

        /**
         * Returns the list supported InputOobActions from a given provisioning pdu.
         *
         * @param value Input oob actions value from provisioning capabilities pdu.
         * @return a list of supported OutputOobActions or an empty list if none is supported.
         */
        fun from(value: UShort) = actions.filter {
            it.rawValue.toInt() and value.toInt() != 0
        }

        /**
         * Converts a list of InputOobActions to a UShort.
         *
         * @receiver List of InputOobActions.
         * @return UShort containing the raw value of the input oob actions.
         */
        fun List<InputOobActions>.toUShort() = run {
            var value = 0
            forEach {
                value = value or it.rawValue.toInt()
            }
            value.toUShort()
        }
    }
}

enum class OutputAction constructor(val rawValue: UByte) {
    BLINK(0u),
    BEEP(1u),
    VIBRATE(2u),
    OUTPUT_NUMERIC(3u),
    OUTPUT_ALPHANUMERIC(4u);

    companion object {

        /**
         * Converts a list of OutputOobActions to a list of OutputActions.
         *
         * @receiver List of OutputOobActions.
         */
        fun List<OutputOobActions>.toOutputActions() = map { from(it) }

        /**
         * Returns the OutputAction from given OutputOobActions.
         *
         * @param outputOobAction OutputOobActions to convert.
         * @return OutputAction
         */
        fun from(outputOobAction: OutputOobActions) = when (outputOobAction) {
            OutputOobActions.Blink -> BLINK
            OutputOobActions.Beep -> BEEP
            OutputOobActions.Vibrate -> VIBRATE
            OutputOobActions.OutputNumeric -> OUTPUT_NUMERIC
            OutputOobActions.OutputAlphanumeric -> OUTPUT_ALPHANUMERIC
        }
    }
}

enum class InputAction(val rawValue: UByte) {
    PUSH(0u),
    TWIST(1u),
    INPUT_NUMERIC(2u),
    INPUT_ALPHANUMERIC(3u);

    companion object {

        /**
         * Converts a list of InputOobActions to a list of InputActions.
         *
         * @receiver List of InputOobActions.
         */
        fun List<InputOobActions>.toInputActions() = map { from(it) }

        /**
         * Returns the InputAction from given InputOobActions.
         *
         * @param inputOobActions InputOobActions to convert.
         */
        fun from(inputOobActions: InputOobActions) = when (inputOobActions) {
            InputOobActions.Push -> PUSH
            InputOobActions.Twist -> TWIST
            InputOobActions.InputNumeric -> INPUT_NUMERIC
            InputOobActions.InputAlphanumeric -> INPUT_ALPHANUMERIC
        }
    }
}