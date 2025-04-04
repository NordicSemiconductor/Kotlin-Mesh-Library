package no.nordicsemi.android.nrfmesh.feature.nodes.node.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * ElementModelRouteKey is a base class that defines the content key that is used to identify an
 * element or a model in the NavigableListDetailPaneScaffold placed in the NodeListDetailsScreen.
 *
 * @property address The address of the element.
 */
@Parcelize
internal open class ElementModelRouteKey(open val address: UShort) : Parcelable

/**
 * ElementRouteKeyKey is the content key that is used to identify an element in the
 * NavigableListDetailPaneScaffold placed in the NodeListDetailsScreen.
 *
 * @property address The address of the element.
 */
@Parcelize
internal data class ElementRouteKeyKey(override val address: UShort) :
    ElementModelRouteKey(address = address), Parcelable


/**
 * ModelRouteKeyKey is the content key that is used to identify a model in the
 * NavigableListDetailPaneScaffold placed in the NodeListDetailsScreen.
 *
 * @property modelId The model id.
 * @property address The address of the parent element the model belongs to.
 */
@Parcelize
internal data class ModelRouteKeyKey(
    val modelId: UInt,
    override val address: UShort,
) : ElementModelRouteKey(address = address), Parcelable