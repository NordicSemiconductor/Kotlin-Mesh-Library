@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
            require(value.isNotBlank()) { "Name cannot be empty!" }
            MeshNetwork.onChange(oldValue = _name, newValue = value) { network?.updateTimestamp() }
        }
    var addresses = listOf<UnicastAddress>()
        private set

    @Transient
    var isUsed: Boolean = false
        get() = addresses.isNotEmpty()
        private set

    @Transient
    internal var network: MeshNetwork? = null

    init {
        require(number in LOWER_BOUND..HIGHER_BOUND) {
            "Scene number must be within $LOWER_BOUND and $HIGHER_BOUND!"
        }
    }

    /**
     * Adds the given unicast address to a scene.
     *
     * @param address Unicast address to be added.
     * @return true if the address was added or false if it alraedy exists.
     */
    internal fun add(address: UnicastAddress) {
        if (addresses.none { it == address }) {
            addresses = addresses + address
            network?.updateTimestamp()
        }
    }

    /**
     * Adds the given list of unicast addresses to a scene.
     *
     * @param addresses List of unicast address.
     */
    internal fun add(addresses: List<UnicastAddress>) {
        this.addresses = this.addresses.union(addresses).toList()
        network?.updateTimestamp()
    }

    /**
     * Removes the given unicast address from the list of addresses.
     *
     * @param address Address to be removed.
     */
    internal fun remove(address: UnicastAddress) {
        addresses = addresses - address
        network?.updateTimestamp()
    }

    /**
     * Returns a list of nodes registered to a given scene address.
     *
     * @param sceneNumber Registered scene number.
     */
    fun nodes(sceneNumber: SceneNumber) = network?.scenes?.filter { scene ->
        scene.number == sceneNumber
    } ?: listOf()

    companion object {
        const val LOWER_BOUND = 0x0001u
        const val HIGHER_BOUND = 0xFFFFu
    }
}