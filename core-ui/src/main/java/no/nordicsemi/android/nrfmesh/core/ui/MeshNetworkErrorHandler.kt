package no.nordicsemi.android.nrfmesh.core.ui

import android.content.Context
import no.nordicsemi.kotlin.mesh.core.exception.AddressAlreadyInUse
import no.nordicsemi.kotlin.mesh.core.exception.AddressNotInAllocatedRanges

// TODO needs a better name, toString was getting overshadowed.
fun Throwable.convertToString(context: Context) = when (this) {
    is AddressNotInAllocatedRanges -> context.getString(
        R.string.address_not_in_allocated_range
    )
    is AddressAlreadyInUse -> context.getString(
        R.string.address_already_in_use
    )
    else -> localizedMessage ?: context.getString(R.string.unknown_error)
}