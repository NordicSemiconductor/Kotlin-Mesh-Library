@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
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
data class Scene internal constructor(
    @SerialName(value = "name")
    private var _name: String,
    @Serializable(with = UShortAsStringSerializer::class)
    val number: SceneNumber,
) {
    var name: String
        get() = _name
        set(value) {
            require(value.isNotBlank()) {
                "Scene name empty!"
            }
            MeshNetwork.onChange(
                oldValue = _name,
                newValue = value,
                action = { network?.updateTimestamp() })
        }
    var addresses = listOf<UnicastAddress>()
        private set

    var network: MeshNetwork? = null
        internal set

    init {
        require(number in LOWER_BOUND..HIGHER_BOUND) { "Scene number must be within $LOWER_BOUND and $HIGHER_BOUND!" }
    }

    /**
     * Adds the given unicast address to a scene.
     *
     * @param address Unicast address to be added.
     * @return true if the address was added or false if it alraedy exists.
     */
    fun add(address: UnicastAddress) = when {
        addresses.contains(address) -> false
        else -> {
            addresses = addresses + address
            network?.updateTimestamp()
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
        network?.updateTimestamp()
    }

    companion object {
        const val LOWER_BOUND = 0x0001u
        const val HIGHER_BOUND = 0xFFFFu
    }
}