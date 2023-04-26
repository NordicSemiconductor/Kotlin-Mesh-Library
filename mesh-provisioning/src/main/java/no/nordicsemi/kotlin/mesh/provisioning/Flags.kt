package no.nordicsemi.kotlin.mesh.provisioning

import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.UsingNewKeys

/**
 * Flags that define the key refresh phase and the IV update state.
 *
 * @property rawValue raw value of the flags.
 */
internal sealed class Flags(val rawValue: UByte) {

    private constructor(rawValue: Int) : this(rawValue.toUByte())

    /**
     * Defines the key refresh phase in the flags of the Provisioning Data PDU.
     */
    object UseNewKeys : Flags(1 shl 0)

    /**
     * Defines the key refresh phase in the flags of the Provisioning Data PDU.
     */
    object IvUpdateActive : Flags(1 shl 1)

    /**
     * Defines the default state when there is no key refresh or IV Update is active.
     */
    data class Default(val value: UByte) : Flags(value)

    companion object {

        /**
         * Returns the flags based on the iv index and network key.
         *
         * @param ivIndex       IV Index of the network.
         * @param networkKey    Network Key of the network.
         * @throws IllegalArgumentException if the flags are invalid.
         */
        @Throws(IllegalArgumentException::class)
        fun from(ivIndex: IvIndex, networkKey: NetworkKey): Flags {
            var value: UByte = 0u
            if (networkKey.phase is UsingNewKeys) {
                value = value or (1 shl 0).toUByte()
            }
            if (ivIndex.isIvUpdateActive) {
                value = value or (1 shl 1).toUByte()
            }
            return when (value) {
                UseNewKeys.rawValue -> UseNewKeys
                IvUpdateActive.rawValue -> IvUpdateActive
                else -> Default(value)
            }
        }
    }
}