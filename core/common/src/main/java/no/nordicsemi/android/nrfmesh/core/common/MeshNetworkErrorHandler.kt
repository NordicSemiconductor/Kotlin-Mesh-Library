package no.nordicsemi.android.nrfmesh.core.common

import android.content.Context
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
import no.nordicsemi.kotlin.mesh.core.exception.KeyInUse
import no.nordicsemi.kotlin.mesh.core.exception.KeyIndexOutOfRange
import no.nordicsemi.kotlin.mesh.core.exception.NoAddressesAvailable
import no.nordicsemi.kotlin.mesh.core.exception.NoGroupRangeAllocated
import no.nordicsemi.kotlin.mesh.core.exception.NoNetworkKeysAdded
import no.nordicsemi.kotlin.mesh.core.exception.NoSceneRangeAllocated
import no.nordicsemi.kotlin.mesh.core.exception.NoUnicastRangeAllocated
import no.nordicsemi.kotlin.mesh.core.exception.NodeAlreadyExists
import no.nordicsemi.kotlin.mesh.core.exception.OverlappingProvisionerRanges
import no.nordicsemi.kotlin.mesh.core.exception.ProvisionerAlreadyExists
import no.nordicsemi.kotlin.mesh.core.exception.SceneAlreadyExists
import no.nordicsemi.kotlin.mesh.core.exception.SceneInUse

// TODO needs a better name, toString was getting overshadowed.
fun Throwable.convertToString(context: Context) = when (this) {
    is KeyIndexOutOfRange -> context.getString(R.string.key_index_out_of_range)
    is DuplicateKeyIndex -> context.getString(R.string.duplicate_key_index)
    is InvalidKeyLength -> context.getString(R.string.invalid_key_length)
    is KeyInUse -> context.getString(R.string.key_in_use)
    is NoNetworkKeysAdded -> context.getString(R.string.no_network_keys_added)
    is CannotRemove -> context.getString(R.string.cannot_remove)
    is NodeAlreadyExists -> context.getString(R.string.node_already_exists)
    is ProvisionerAlreadyExists -> context.getString(R.string.provisioner_already_exists)
    is OverlappingProvisionerRanges -> context.getString(R.string.overlapping_provisioner_ranges)
    is AddressNotInAllocatedRanges -> context.getString(R.string.address_not_in_allocated_ranges)
    is AddressAlreadyInUse -> context.getString(R.string.address_already_in_use)
    is NoAddressesAvailable -> context.getString(R.string.no_addresses_available)
    is NoUnicastRangeAllocated -> context.getString(R.string.no_unicast_range_allocated)
    is GroupAlreadyExists -> context.getString(R.string.group_already_exists)
    is GroupInUse -> context.getString(R.string.group_in_use)
    is NoGroupRangeAllocated -> context.getString(R.string.no_group_range_allocated)
    is SceneAlreadyExists -> context.getString(R.string.scene_already_exists)
    is SceneInUse -> context.getString(R.string.scene_in_use)
    is NoSceneRangeAllocated -> context.getString(R.string.no_scene_range_allocated)
    is AtLeastOneNetworkKeyMustBeSelected -> context.getString(
        R.string.at_least_one_net_key_must_be_selected
    )
    is AtLeastOneProvisionerMustBeSelected -> context.getString(
        R.string.at_least_one_provisioner_must_be_selected
    )
    is ImportError -> error
    is DoesNotBelongToNetwork -> context.getString(R.string.does_not_belong_to_network)
    else -> localizedMessage ?: context.getString(R.string.unknown_error)
}