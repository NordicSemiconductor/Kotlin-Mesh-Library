@file:Suppress("unused", "EXPERIMENTAL_API_USAGE", "SERIALIZER_TYPE_INCOMPATIBLE")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nordicsemi.kotlin.mesh.core.model.serialization.UShortAsStringSerializer

/**
 * Type range containing a lower bound and higher bound for an allocated address range or a scene range.
 *
 * @property low    Lower bound of a given range.
 * @property high   Higher bound of a given range.
 */
sealed interface Range {
    val low: UShort
    val high: UShort

    /**
     * Checks if the low value is lower than the high value.
     */
    fun isValid() = low <= high
}

/**
 * Allocated Range.
 *
 * @property low       Low value for a given range.
 * @property high      High value for a given  range.
 */
// TODO Check IntRange, ClosedRange as an option when implementing mering/resolving allocated ranges.
@Serializable
sealed class AllocatedRange : Range {
    abstract override val low: UShort
    abstract override val high: UShort
}

/**
 * Allocated address range
 *
 * @property lowAddress       Low value for a given range.
 * @property highAddress      High value for a given  range.
 */
@Serializable
sealed class AllocatedAddressRange : AllocatedRange() {
    abstract val lowAddress: MeshAddress
    abstract val highAddress: MeshAddress
}

/**
 * The AllocatedUnicastRange represents the range of unicast addresses that the Provisioner can allocate to
 * new devices when they are provisioned onto the mesh network, without needing to coordinate the node
 * additions with other Provisioners. The lowAddress and highAddress represent values from 0x0001 to 0x7FFF.
 * The value of the lowAddress property shall be less than or equal to the value of the highAddress property.
 *
 * @property lowAddress        Low address for a given range.
 * @property highAddress       High address for a given  range.
 */
@Serializable
data class AllocatedUnicastRange(
    override val lowAddress: UnicastAddress,
    override val highAddress: UnicastAddress
) : AllocatedAddressRange() {
    @Transient
    override val low = lowAddress.address

    @Transient
    override val high = highAddress.address

    init {
        require(isValid()) {
            "Low address must be less than or equal to the higher address!"
        }
    }
}

/**
 * The AllocatedGroupRange represents the range of group addresses that the Provisioner can allocate to
 * newly created groups, without needing to coordinate the group additions with other Provisioners.
 * The lowAddress and highAddress properties represent values from 0xC000 to 0xFFFF. The value of the
 * lowAddress property shall be less than or equal to the value of the highAddress property.
 *
 * @property lowAddress        Low address for a given range.
 * @property highAddress       High address for a given  range.
 */
@Serializable
data class AllocatedGroupRange(
    override val lowAddress: GroupAddress,
    override val highAddress: GroupAddress
) : AllocatedAddressRange() {
    @Transient
    override val low = lowAddress.address

    @Transient
    override val high = highAddress.address

    init {
        require(isValid()) {
            "Low address must be less than or equal to the higher address!"
        }
    }
}

/**
 * The AllocatedSceneRange represents the range of scene numbers that the Provisioner can use to register
 * new scenes in the mesh network, without needing to coordinate the allocated scene numbers with other
 * Provisioners. The firstScene and lastScene represents values from 0x0001 to 0xFFFF. The value of the
 * firstScene property shall be less than or equal to the value of the lastScene property.
 *
 * @property firstScene    First scene a given range.
 * @property lastScene     Last scene for a given  range.
 */
@Serializable
data class AllocatedSceneRange(
    @Serializable(with = UShortAsStringSerializer::class)
    val firstScene: SceneNumber,
    @Serializable(with = UShortAsStringSerializer::class)
    val lastScene: SceneNumber
) : AllocatedRange() {
    @Transient
    override val low = firstScene

    @Transient
    override val high = lastScene

    init {
        require(isValid()) {
            "First scene must be lower than or equal to the last scene!"
        }
    }
}
