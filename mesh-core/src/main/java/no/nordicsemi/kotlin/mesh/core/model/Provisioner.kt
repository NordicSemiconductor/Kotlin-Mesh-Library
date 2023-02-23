@file:Suppress("MemberVisibilityCanBePrivate", "PropertyName")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nordicsemi.kotlin.mesh.core.exception.AddressAlreadyInUse
import no.nordicsemi.kotlin.mesh.core.exception.AddressNotInAllocatedRanges
import no.nordicsemi.kotlin.mesh.core.exception.DoesNotBelongToNetwork
import no.nordicsemi.kotlin.mesh.core.exception.OverlappingProvisionerRanges
import no.nordicsemi.kotlin.mesh.core.model.serialization.UUIDSerializer
import no.nordicsemi.kotlin.mesh.crypto.Crypto
import java.util.*

/**
 * A Provisioner is capable of provisioning a device to a mesh network and is represented by a
 * provisioner object in the Mesh Configuration Database. A provisioner is represented as a node in
 * mesh network only if it is assigned a unicast address. Having a unicast address assigned allows
 * configuring nodes in the mesh network. Otherwise, a provisioner can only provision nodes to a
 * mesh network.
 *
 * @property name                      Provisioner name.
 * @property uuid                      UUID of the provisioner.
 * @property allocatedUnicastRanges    List of allocated unicast ranges for a given provisioner.
 * @property allocatedGroupRanges      List of allocated group ranges for a given provisioner.
 * @property allocatedSceneRanges      List of allocated scene ranges for a given provisioner.
 * @constructor Creates a Provisioner object.
 */
@Suppress("unused")
@Serializable
data class Provisioner internal constructor(
    @SerialName(value = "UUID")
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    @SerialName(value = "allocatedUnicastRange")
    internal var _allocatedUnicastRanges: MutableList<UnicastRange> = mutableListOf(),
    @SerialName(value = "allocatedGroupRange")
    internal var _allocatedGroupRanges: MutableList<GroupRange> = mutableListOf(),
    @SerialName(value = "allocatedSceneRange")
    internal var _allocatedSceneRanges: MutableList<SceneRange> = mutableListOf()
) {

    constructor(uuid: UUID = UUID.randomUUID()) : this(
        uuid,
        mutableListOf<UnicastRange>(),
        mutableListOf<GroupRange>(),
        mutableListOf<SceneRange>()
    )

    @SerialName(value = "provisionerName")
    var name: String = "nRF Mesh Provisioner"
        set(value) {
            require(value = value.isNotBlank()) { "Name cannot be empty!" }
            MeshNetwork.onChange(oldValue = field, newValue = value) { network?.updateTimestamp() }
            field = value
        }

    val allocatedUnicastRanges: List<UnicastRange>
        get() = _allocatedUnicastRanges

    val allocatedGroupRanges: List<GroupRange>
        get() = _allocatedGroupRanges

    val allocatedSceneRanges: List<SceneRange>
        get() = _allocatedSceneRanges

    val node: Node?
        get() = network?._nodes?.find { it.uuid == uuid }

    @Transient
    internal var network: MeshNetwork? = null

    /**
     * Allocates the given range to a provisioner.
     *
     * @param range Allocated range could [UnicastRange], [GroupRange] or a [SceneRange].
     * @throws OverlappingProvisionerRanges if the given range is allocated to another provisioner.
     */
    @Throws(OverlappingProvisionerRanges::class)
    fun allocate(range: Range) {
        // TODO clarify the api with iOS version
        when (range) {
            is UnicastRange -> allocate(range)
            is GroupRange -> allocate(range)
            is SceneRange -> allocate(range)
        }
    }

    /**
     * Allocates the given unicast range to a provisioner.
     *
     * @param range Allocated unicast range.
     * @throws OverlappingProvisionerRanges if the given range is allocated to another provisioner.
     */
    @Throws(OverlappingProvisionerRanges::class)
    fun allocate(range: UnicastRange) {
        // If the provisioner is not a part of network we don't have to validate for overlapping
        // unicast ranges. This will be validated when the provisioner is added to the network.
        network?.let { network ->
            require(
                network.provisioners
                    .filter { it.uuid != uuid }
                    .none { it._allocatedUnicastRanges.overlaps(range) }
            ) { throw OverlappingProvisionerRanges }
            _allocatedUnicastRanges.add(range).also { network.updateTimestamp() }
        } ?: run {
            _allocatedUnicastRanges.add(range)
        }
    }

    /**
     * Allocates the given group range to a provisioner.
     *
     * @param range Allocated group range.
     * @throws OverlappingProvisionerRanges if the given range is allocated to another provisioner.
     */
    @Throws(OverlappingProvisionerRanges::class)
    fun allocate(range: GroupRange) {
        // If the provisioner is not a part of network we don't have to validate for overlapping
        // group ranges. This will be validated when the provisioner is added to the network.
        network?.let { network ->
            require(network.provisioners
                .filter { it.uuid != uuid }
                .none { it._allocatedGroupRanges.overlaps(range) }) {
                throw OverlappingProvisionerRanges
            }
            _allocatedGroupRanges.add(range).also { network.updateTimestamp() }
        } ?: run { _allocatedGroupRanges.add(range) }
    }

    /**
     * Allocates the given scene range to a provisioner.
     *
     * @param range Allocated scene range.
     * @throws OverlappingProvisionerRanges if the given range is allocated to another provisioner.
     */
    @Throws(OverlappingProvisionerRanges::class)
    fun allocate(range: SceneRange) {
        // If the provisioner is not a part of network we don't have to validate for overlapping
        // scene ranges. This will be validated when the provisioner is added to the network.
        network?.let { network ->
            require(network.provisioners
                .filter { it.uuid != uuid }
                .none { it._allocatedSceneRanges.overlaps(range) }) {
                throw OverlappingProvisionerRanges
            }
            _allocatedSceneRanges.add(range).also { network.updateTimestamp() }
        } ?: run { _allocatedSceneRanges.add(range) }
    }

    /**
     * Checks if the given range is within the Allocated ranges.
     *
     * @param range Range to be checked.
     * @return true if the range is within the allocated range.
     */
    fun hasAllocatedRange(range: Range) = when (range) {
        is UnicastRange -> _allocatedUnicastRanges
        is GroupRange -> _allocatedGroupRanges
        is SceneRange -> _allocatedSceneRanges
    }.contains(range)

    /**
     * Checks if the current provisioner has overlapping unicast, group or scene ranges with the
     * given provisioner.
     *
     * @param provisioner Other provisioner.
     * @return true if there are any overlapping unicast, groups or scene ranges or false otherwise.
     */
    fun hasOverlappingRanges(provisioner: Provisioner) =
        hasOverlappingUnicastRanges(provisioner) ||
                hasOverlappingGroupRanges(provisioner) ||
                hasOverlappingSceneRanges(provisioner)

    /**
     * Checks if the current provisioner has overlapping unicast ranges with the given provisioner.
     *
     * @param other Other provisioner.
     * @return true if there are any overlapping unicast ranges or false otherwise.
     */
    fun hasOverlappingUnicastRanges(other: Provisioner) = _allocatedUnicastRanges.any { range ->
        other._allocatedUnicastRanges.any { otherRange -> otherRange.overlaps(range) }
    }

    /**
     * Checks if the current provisioner has overlapping group ranges with the given provisioner.
     *
     * @param other Other provisioner.
     * @return true if there are any overlapping group ranges or false otherwise.
     */

    fun hasOverlappingGroupRanges(other: Provisioner) = _allocatedGroupRanges.any { range ->
        other._allocatedGroupRanges.any { otherRange -> otherRange.overlaps(range) }
    }

    /**
     * Checks if the current provisioner has overlapping scene ranges with the given provisioner.
     *
     * @param other Other provisioner.
     * @return true if there are any overlapping scene ranges or false otherwise.
     */
    fun hasOverlappingSceneRanges(other: Provisioner) = _allocatedSceneRanges.any { range ->
        other._allocatedSceneRanges.any { otherRange -> otherRange.overlaps(range) }
    }

    /**
     * Assigns the unicast address to the given Provisioner. If the provisioner did not have a
     * unicast address assigned, the method will create a Node with the address. This will enable
     * configuration capabilities for the provisioner. The provisioner must be in the mesh network.
     *
     * @param                              address Unicast address to assign.
     * @throws DoesNotBelongToNetwork      If the provisioner does not belong to this network.
     * @throws AddressNotInAllocatedRanges If the address is not in the provisioner's allocated
     *                                     address range.
     * @throws AddressAlreadyInUse         If the address is already in use
     */
    @Throws(DoesNotBelongToNetwork::class)
    fun assign(address: UnicastAddress) {
        network?.run {
            require(has(this@Provisioner))
            var isNewNode = false
            val node = node(this@Provisioner) ?: Node(
                this@Provisioner,
                Crypto.generateRandomKey(),
                address,
                listOf(
                    Element(
                        Location.UNKNOWN,
                        listOf(
                            Model(SigModelId(Model.CONFIGURATION_SERVER_MODEL_ID)),
                            Model(SigModelId(Model.CONFIGURATION_CLIENT_MODEL_ID))
                        )
                    )
                ),
                _networkKeys,
                _applicationKeys
            ).apply {
                companyIdentifier = 0x00E0u //Google
                replayProtectionCount = maxUnicastAddress
            }.also { isNewNode = true }

            // Is it in Provisioner's range?
            val newRange = UnicastRange(address, node.elementsCount)
            require(hasAllocatedRange(newRange)) { throw AddressNotInAllocatedRanges }

            // Is there any other node using the address?
            require(isAddressAvailable(address, node)) { throw AddressAlreadyInUse }

            when (isNewNode) {
                true -> add(node)
                else -> node._primaryUnicastAddress = address
            }
            updateTimestamp()
        }
    }

    /**
     * Disables the configuration capabilities by un-assigning provisioner's address. Un-assigning
     * an address will delete the provisioner's node. This results in the provisioner not being
     * able to send or receive mesh messages in the mesh network. However, the provisioner will
     * still retain it's provisioning capabilities.
     */
    fun disableConfigurationCapabilities() {
        network?.removeNode(uuid)
    }

    /**
     * Checks if the given range is allocatable a provisioner.
     *
     * @param range Ranges to be allocated.
     * @return true if the range is not in use by another provisioner or false otherwise.
     * @throws DoesNotBelongToNetwork if the provisioner does not belong to the network.
     */
    @Throws(DoesNotBelongToNetwork::class)
    fun isRangeAvailableForAllocation(range: Range) = let { provisioner ->
        network?.run {
            when (range) {
                is UnicastRange ->
                    provisioners
                        .filter { it.uuid != provisioner.uuid }
                        .none { it._allocatedUnicastRanges.overlaps(range) }
                is GroupRange ->
                    provisioners
                        .filter { it.uuid != provisioner.uuid }
                        .none { it._allocatedGroupRanges.overlaps(range) }
                is SceneRange ->
                    provisioners
                        .filter { it.uuid != provisioner.uuid }
                        .none { it._allocatedSceneRanges.overlaps(range) }
            }
        } ?: false
    }

    /**
     * Check if the given list of ranges are allocatable to a provisioner.
     *
     * @param ranges Ranges to be allocated.
     * @return true if the given ranges are not in use by another provisioner or false otherwise.
     */
    fun areRangesAvailableForAllocation(ranges: List<Range>) = let { provisioner ->
        try {
            network?.run {
                when (ranges.first()) {
                    is UnicastRange ->
                        provisioners
                            .filter { it.uuid != provisioner.uuid }
                            .none { it._allocatedUnicastRanges.overlaps(ranges) }
                    is GroupRange ->
                        provisioners
                            .filter { it.uuid != provisioner.uuid }
                            .none { it._allocatedGroupRanges.overlaps(ranges) }
                    is SceneRange ->
                        provisioners
                            .filter { it.uuid != provisioner.uuid }
                            .none { it._allocatedSceneRanges.overlaps(ranges) }
                }
            } ?: false
        } catch (e: NoSuchElementException) {
            false
        }
    }
}