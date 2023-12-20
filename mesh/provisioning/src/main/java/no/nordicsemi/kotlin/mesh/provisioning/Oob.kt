@file:Suppress("ClassName", "MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.provisioning

import kotlin.math.pow

/**
 * The type  authentication method chosen for provisioning.
 */
sealed class AuthenticationMethod {

    /**
     * No OOB authentication method is used.
     */
    object NoOob : AuthenticationMethod() {
        override fun toString() = "No OOB"
    }

    /**
     * Static OOB authentication method is used.
     */
    object StaticOob : AuthenticationMethod() {
        override fun toString() = "Static OOB"
    }

    /**
     * Output OOB authentication method is used. Size must be in range 1...8.
     *
     * @property action        Output action type.
     * @property length        Size of the input.
     * @constructor Constructs a new OutputOob.
     */
    data class OutputOob(val action: OutputAction, val length: UByte) : AuthenticationMethod() {
        constructor(action: OutputAction) : this(action, action.rawValue)

        override fun toString() = "Output Action: $action (size $length)"
    }

    /**
     * Input OOB authentication method is used. Size must be in range 1...8.
     *
     * @property action        Input action type.
     * @property length        Size of the input.
     * @constructor Constructs a new InputOob.
     */
    data class InputOob(val action: InputAction, val length: UByte) : AuthenticationMethod() {
        override fun toString() = "Input Action: $action (size $length)"
    }

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
    object StaticOobInformationAvailable : OobType(rawValue = 1 shl 0) {
        override fun toString() = "Static OOB information is available"
    }

    /**
     * Only OOB authenticated provisioning is supported. Introduced in Mesh Protocol 1.1.0
     */
    object OnlyOobAuthenticatedProvisioningSupported : OobType(rawValue = 1 shl 1) {
        override fun toString() = "Only OOB authenticated provisioning is supported"
    }

    internal companion object {

        private val oobTypes = listOf(
            StaticOobInformationAvailable,
            OnlyOobAuthenticatedProvisioningSupported
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

    object Blink : OutputOobActions(rawValue = 1 shl 0) {
        override fun toString() = "Blink"
    }

    object Beep : OutputOobActions(rawValue = 1 shl 1) {
        override fun toString() = "Beep"
    }

    object Vibrate : OutputOobActions(rawValue = 1 shl 2) {
        override fun toString() = "Vibrate"
    }

    object OutputNumeric : OutputOobActions(rawValue = 1 shl 3) {
        override fun toString() = "Output Numeric"
    }

    object OutputAlphanumeric : OutputOobActions(rawValue = 1 shl 4) {
        override fun toString() = "Output Alphanumeric"
    }

    internal companion object {
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

    object Push : InputOobActions(rawValue = 1 shl 0) {
        override fun toString() = "Push"
    }

    object Twist : InputOobActions(rawValue = 1 shl 1) {
        override fun toString() = "Twist"
    }

    object InputNumeric : InputOobActions(rawValue = 1 shl 2) {
        override fun toString() = "Input Numeric"
    }

    object InputAlphanumeric : InputOobActions(rawValue = 1 shl 3) {
        override fun toString() = "Input Alphanumeric"
    }

    internal companion object {
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
        internal fun from(outputOobAction: OutputOobActions) = when (outputOobAction) {
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
        internal fun from(inputOobActions: InputOobActions) = when (inputOobActions) {
            InputOobActions.Push -> PUSH
            InputOobActions.Twist -> TWIST
            InputOobActions.InputNumeric -> INPUT_NUMERIC
            InputOobActions.InputAlphanumeric -> INPUT_ALPHANUMERIC
        }
    }
}