package no.nordicsemi.android.nrfmesh.feature.automation

import no.nordicsemi.kotlin.mesh.core.model.Node

class Configurator(private val originalNode: Node, private val newNode: Node) {

    private val tasks = mutableListOf<MeshTask>()

    fun queue() {
        // TODO queue tasks here
    }

    fun configure() {
        // TODO start configuring here
    }
}