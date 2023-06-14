@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.model.serialization.config

import no.nordicsemi.kotlin.mesh.core.model.Scene

/**
 * Allows configuring All, Related or Some scenes.
 */
sealed class ScenesConfig {

    /**
     * Configures all scenes to be exported. Exported scenes will not contain addresses of
     * excluded nodes.
     */
    object All : ScenesConfig()

    /**
     * Configures exporting related scenes. Exported scenes will not contain addresses of excluded
     * nodes.
     */
    // TODO is this required?
    object Related : ScenesConfig()

    /**
     * Allows to configure exporting of some of the scenes. Exported scenes will not contain addresses of
     * excluded nodes.
     *
     * @property scenes List of Scenes to export.
     * @constructor Constructs ExportSome to export only a selected number of Scenes when exporting
     *              a mesh network.
     */
    data class Some(val scenes: List<Scene>) : ScenesConfig()
}