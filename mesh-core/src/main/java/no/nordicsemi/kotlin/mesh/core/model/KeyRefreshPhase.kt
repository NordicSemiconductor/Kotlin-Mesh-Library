package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.KeyRefreshPhaseSerializer

/**
 * The Key Refresh Phase state indicates and controls the Key Refresh procedure for each NetKey in
 * the NetKey List.
 */
@Serializable(with = KeyRefreshPhaseSerializer::class)
sealed class KeyRefreshPhase(val phase: Int) {
    // TODO to be verified when Key refresh support is added.
    // https://github.com/NordicSemiconductor/IOS-nRF-Mesh-Library/blob/
    // c1755555f76fb6f393bfdad37a23566ddd581536/nRFMeshProvision/Classes/Mesh%20Model/KeyRefreshPhase.swift#L60
    companion object {
        /**
         * Parses the phase of the key refresh procedure.
         *
         * @param phase Key refresh procedure phase.
         * @return [KeyRefreshPhase]
         * @throws IllegalArgumentException  if the phase level value is not 0, 1 or 2.
         */
        @Suppress("unused")
        internal fun from(phase: Int) = when (phase) {
            NORMAL_OPERATION -> NormalOperation
            KEY_DISTRIBUTION -> KeyDistribution
            USING_NEW_KEYS -> UsingNewKeys
            else -> throw IllegalArgumentException(
                "Invalid value, phase must be an integer of value " +
                        "$NORMAL_OPERATION, $KEY_DISTRIBUTION or $USING_NEW_KEYS!"
            )
        }
    }
}

/**
 * Normal operation; Key Refresh procedure is not active.
 */
object NormalOperation : KeyRefreshPhase(phase = NORMAL_OPERATION)

/**
 * First phase of Key Refresh procedure, distributes new keys to all nodes. Nodes will transmit
 * using old keys, but can receive using old and new keys.
 */
object KeyDistribution : KeyRefreshPhase(phase = KEY_DISTRIBUTION)

/**
 * Second phase of Key Refresh procedure, nodes will use the new keys when encrypting messages but
 * will still receive using the old or new keys. Nodes shall only receive Secure Network beacons
 * secured using the new Network Key.
 */
object UsingNewKeys : KeyRefreshPhase(phase = USING_NEW_KEYS)

private const val NORMAL_OPERATION = 0
private const val KEY_DISTRIBUTION = 1
private const val USING_NEW_KEYS = 2