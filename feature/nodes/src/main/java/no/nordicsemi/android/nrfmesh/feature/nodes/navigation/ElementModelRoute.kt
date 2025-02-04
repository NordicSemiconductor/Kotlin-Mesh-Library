package no.nordicsemi.android.nrfmesh.feature.nodes.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class ElementModelRoute internal constructor(open val address: UShort) : Parcelable

@Parcelize
data class ElementRoute(override val address: UShort) : ElementModelRoute(address = address), Parcelable

@Parcelize
data class ModelRoute(
    val modelId: UInt,
    override val address: UShort,
) : ElementModelRoute(address = address), Parcelable