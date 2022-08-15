@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model.serialization.config

import no.nordicsemi.kotlin.mesh.core.model.Group

/**
 * Contains the configuration required when exporting a selected number of Groups in a mesh network.
 */
sealed class GroupsConfig {

    /**
     * Use this class to configure when exporting all the Groups.
     */
    object All : GroupsConfig()

    /**
     * Use this class to configure when exporting the related Groups i.e. exported configuration
     * will only contain those groups that any exported model is subscribed or publishing to.
     */
    object Related : GroupsConfig()

    /**
     * Use this class to configure when exporting some of the groups.
     *
     * @param groups List of groups to be exported.
     * @constructor Constructs ExportSome to export only a selected number of Groups when exporting
     *              a mesh network. Excluded groups will also be excluded from subscription lists
     *              and publish information in exported Models.
     */
    data class Some(val groups: List<Group>) : GroupsConfig()
}
