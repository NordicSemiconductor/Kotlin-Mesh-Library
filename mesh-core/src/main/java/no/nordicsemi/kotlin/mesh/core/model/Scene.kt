@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.UShortAsStringSerializer

typealias SceneNumber = UShort

/**
 * Scene
 *
 * @property name          Scene name.
 * @property number        Scene number.
 * @property addresses     Addresses containing the scene.
 */
@Serializable
data class Scene(
    val name: String,
    @Serializable(with = UShortAsStringSerializer::class)
    val number: SceneNumber,
) {
    var addresses = listOf<UnicastAddress>()
        private set

    init {
        require(name.isNotBlank()) { "Scene name cannot be blank!" }
        require(number in LOWER_BOUND..HIGHER_BOUND) { "Scene number must be within $LOWER_BOUND and $HIGHER_BOUND!" }
    }

    /**
     * Adds the given unicast address to a scene
     */
    fun add(address: UnicastAddress) = when {
        addresses.contains(address) -> false
        else -> {
            addresses = addresses + address
            true
        }
    }

    /**
     * Adds the given list of unicast addresses to a scene
     *
     * @param addresses List of unicast address.
     */
    fun add(addresses: List<UnicastAddress>) {
        this.addresses = this.addresses.union(addresses).toList()
    }

    companion object {
        const val LOWER_BOUND = 0x0001u
        const val HIGHER_BOUND = 0xFFFFu
    }
}