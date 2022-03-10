@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


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
    val number: SceneNumber,
) {
    var addresses = listOf<UnicastAddress>()
        private set

    init {
        require(name.isNotBlank()) { "Scene name cannot be blank!" }
        require(number in LOWER_BOUND..HIGHER_BOUND) { "Scene number must be within 0xC000 and 0xFFFF!" }
    }

    fun addAddress(address: UnicastAddress) {
        this.addresses += address
    }

    fun addAddresses(addresses: List<UnicastAddress>) {
        this.addresses += addresses
    }

    companion object {
        const val LOWER_BOUND = 0x0001u
        const val HIGHER_BOUND = 0xFFFFu
    }
}