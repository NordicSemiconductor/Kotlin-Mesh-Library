package no.nordicsemi.kotlin.mesh.core.model

/**
 * Node Identity state
 *
 * @property value               Value of the state.
 * @property STOPPED             Advertising with Node Identity for a subnet is stopped.
 * @property RUNNING             Advertising with Node Identity for a subnet is running.
 * @property NOT_SUPPORTED       Advertising with Node Identity for a subnet is not supported.
 * @property isSupported         Returns true if the state is supported.
 * @property isRunning           Returns true if the state is running.
 */
enum class NodeIdentityState(val value: UByte) {
    STOPPED(0x00u),
    RUNNING(0x01u),
    NOT_SUPPORTED(0x02u);

    val isSupported: Boolean
        get() = this == STOPPED || this == RUNNING

    val isRunning: Boolean
        get() = this == RUNNING

    companion object {

        /**
         * Returns the [NodeIdentityState] for the given value.
         *
         * @param value Value of the state.
         */
        fun from(value: UByte) = entries.first { it.value == value }
    }
}