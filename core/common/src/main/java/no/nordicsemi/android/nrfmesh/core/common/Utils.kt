@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.core.common

import no.nordicsemi.kotlin.mesh.core.exception.AddressAlreadyInUse
import no.nordicsemi.kotlin.mesh.core.exception.AddressNotInAllocatedRanges
import no.nordicsemi.kotlin.mesh.core.exception.AtLeastOneNetworkKeyMustBeSelected
import no.nordicsemi.kotlin.mesh.core.exception.AtLeastOneProvisionerMustBeSelected
import no.nordicsemi.kotlin.mesh.core.exception.CannotRemove
import no.nordicsemi.kotlin.mesh.core.exception.DoesNotBelongToNetwork
import no.nordicsemi.kotlin.mesh.core.exception.DuplicateKeyIndex
import no.nordicsemi.kotlin.mesh.core.exception.GroupAlreadyExists
import no.nordicsemi.kotlin.mesh.core.exception.GroupInUse
import no.nordicsemi.kotlin.mesh.core.exception.ImportError
import no.nordicsemi.kotlin.mesh.core.exception.InvalidKeyLength
import no.nordicsemi.kotlin.mesh.core.exception.InvalidPdu
import no.nordicsemi.kotlin.mesh.core.exception.InvalidPduType
import no.nordicsemi.kotlin.mesh.core.exception.KeyInUse
import no.nordicsemi.kotlin.mesh.core.exception.KeyIndexOutOfRange
import no.nordicsemi.kotlin.mesh.core.exception.MeshNetworkException
import no.nordicsemi.kotlin.mesh.core.exception.NoAddressesAvailable
import no.nordicsemi.kotlin.mesh.core.exception.NoGroupRangeAllocated
import no.nordicsemi.kotlin.mesh.core.exception.NoLocalProvisioner
import no.nordicsemi.kotlin.mesh.core.exception.NoNetwork
import no.nordicsemi.kotlin.mesh.core.exception.NoNetworkKeysAdded
import no.nordicsemi.kotlin.mesh.core.exception.NoSceneRangeAllocated
import no.nordicsemi.kotlin.mesh.core.exception.NoUnicastRangeAllocated
import no.nordicsemi.kotlin.mesh.core.exception.NodeAlreadyExists
import no.nordicsemi.kotlin.mesh.core.exception.OverlappingProvisionerRanges
import no.nordicsemi.kotlin.mesh.core.exception.ProvisionerAlreadyExists
import no.nordicsemi.kotlin.mesh.core.exception.RangeAlreadyAllocated
import no.nordicsemi.kotlin.mesh.core.exception.SceneAlreadyExists
import no.nordicsemi.kotlin.mesh.core.exception.SceneInUse
import no.nordicsemi.kotlin.mesh.core.exception.SecurityException
import no.nordicsemi.kotlin.mesh.core.layers.access.AccessError
import no.nordicsemi.kotlin.mesh.logger.LogLevel

/**
 * Helper object containing utility methods.
 */
object Utils {

    /**
     * Converts the [LogLevel] to an Android log level.
     *
     * @receiver LogLevel
     * @return the Android log level
     */
    fun LogLevel.toAndroidLogLevel(): Int = when (this) {
        LogLevel.VERBOSE -> android.util.Log.VERBOSE
        LogLevel.DEBUG -> android.util.Log.DEBUG
        LogLevel.INFO -> android.util.Log.INFO
        LogLevel.APPLICATION -> android.util.Log.INFO
        LogLevel.WARNING -> android.util.Log.WARN
        LogLevel.ERROR -> android.util.Log.ERROR
    }

    fun Throwable.describe(): String {
        return when(this) {
            is AccessError -> this.toString()

            is MeshNetworkException -> when(this) {
                AddressAlreadyInUse -> "Address already in use."
                AddressNotInAllocatedRanges -> "Address not in allocated ranges."
                AtLeastOneNetworkKeyMustBeSelected -> "At least one network key must be selected."
                AtLeastOneProvisionerMustBeSelected -> "At least one provisioner must be selected."
                CannotRemove -> "Cannot remove."
                DoesNotBelongToNetwork -> "Does not belong to network."
                DuplicateKeyIndex -> "Duplicate key index."
                GroupAlreadyExists -> "Group already exists."
                GroupInUse -> "Group in use."
                is ImportError -> "Import error: $error"
                InvalidKeyLength -> "Invalid key length."
                InvalidPdu -> "Invalid PDU."
                InvalidPduType -> "Invalid PDU type."
                KeyInUse -> "Key in use."
                KeyIndexOutOfRange -> "Key index out of range."
                NoAddressesAvailable -> "Addresses not available."
                NoGroupRangeAllocated -> "Group range not allocated."
                NoLocalProvisioner -> "No local provisioner."
                NoNetwork -> "No network."
                NoNetworkKeysAdded -> "No network keys added."
                NoSceneRangeAllocated -> "Scene range not allocated."
                NoUnicastRangeAllocated -> "Unicast range not allocated."
                NodeAlreadyExists -> "Node already exists."
                OverlappingProvisionerRanges -> "Overlapping provisioner ranges."
                ProvisionerAlreadyExists -> "Provisioner already exists."
                RangeAlreadyAllocated -> "Range already allocated."
                SceneAlreadyExists -> "Scene already exists."
                SceneInUse -> "Scene in use."
                SecurityException -> "Security exception."
                else -> "Unknown error: ${this.message}"
            }

            else -> "Unknown error: ${this.message}"
        }
    }
}