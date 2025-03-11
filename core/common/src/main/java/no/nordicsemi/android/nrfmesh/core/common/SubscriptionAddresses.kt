package no.nordicsemi.android.nrfmesh.core.common

import no.nordicsemi.kotlin.mesh.core.model.AllFriends
import no.nordicsemi.kotlin.mesh.core.model.AllProxies
import no.nordicsemi.kotlin.mesh.core.model.AllRelays
import no.nordicsemi.kotlin.mesh.core.model.FixedGroupAddress
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.SubscriptionAddress

/**
 * A list of fixed group addresses that are used for subscriptions.
 */
val fixedGroupAddressesForSubscriptions =
    listOf<FixedGroupAddress>(AllRelays, AllFriends, AllProxies)

/**
 * Returns a list of groups that is not already subscribed to this model.
 */
fun Model.unsubscribedGroups(): List<Group> =
    parentElement?.parentNode?.network?.groups
        ?.filter { it.address as SubscriptionAddress !in subscribe }.orEmpty()
