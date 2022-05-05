@file:Suppress("unused", "EXPERIMENTAL_API_USAGE", "SERIALIZER_TYPE_INCOMPATIBLE")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nordicsemi.kotlin.mesh.core.model.serialization.UShortAsStringSerializer

/**
 * Allocated Range.
 *
 * @property low       Low value for a given range.
 * @property high      High value for a given  range.
 */
@Serializable
sealed class Range {
    internal abstract val range: UIntRange
    internal val low: UShort
        get() = range.first.toUShort()
    internal val high: UShort
        get() = range.last.toUShort()

    /**
     * Checks if a given value is in the range.
     *
     * @param value Value to be checked.
     * @return true if the value is in the range and false otherwise.
     */
    fun contains(value: UShort) = range.contains(value)

    /**
     * Checks if a given range overlaps.
     *
     * @param other Range to check for overlapping elements.
     * @return true if there are overlapping elements.
     */
    fun overlaps(other: Range) = range.intersect(other.range).isNotEmpty()

    /**
     * Returns the closest distance between this and the given range.
     *
     * When range 1 ends at 0x1000 and range 2 starts at 0x1002, the distance between them is 1. If
     * the range 2 starts at 0x0001, the distance is 0 and they can be merged. If ranges overlap
     * each other, the distance is 0.
     *
     * @param other The range to check distance to.
     * @return The distance between ranges in units.
     */
    fun distance(other: Range) = when {
        high < other.low -> (other.low - high - 1u).toInt()
        low > other.high -> (low - other.high - 1u).toInt()
        else -> 0
    }

    /**
     * Returns the minimum of the given values.
     *
     * @param a UShort value.
     * @param b UShort value.
     * @return the minimum value.
     */
    private fun min(a: UShort, b: UShort): UShort = kotlin.math.min(a.toInt(), b.toInt()).toUShort()

    /**
     * Returns the maximum of the given values.
     *
     * @param a UShort value.
     * @param b UShort value.
     * @return the maximum value.
     */
    private fun max(a: UShort, b: UShort): UShort = kotlin.math.max(a.toInt(), b.toInt()).toUShort()

    /**
     * Adds a range to another.
     *
     * @param other Range to be added.
     * @return a list of ranges.
     */
    operator fun plus(other: Range) =
        if (distance(other) == 0)
            listOf(
                when {
                    this is UnicastRange && other is UnicastRange ->
                        UnicastAddress(min(low, other.low))..
                                UnicastAddress(max(high, other.high))

                    this is GroupRange && other is GroupRange ->
                        GroupAddress(min(low, other.low))..
                                GroupAddress(max(high, other.high))

                    this is SceneRange && other is SceneRange -> SceneRange(
                        min(low, other.low),
                        max(high, other.high)
                    )
                    else -> throw IllegalArgumentException(
                        "Left and Right ranges must be of same range type!"
                    )
                }
            )
        else listOf(this, other)

    /**
     * Removes one range from the other.
     *
     * @param other Range to be added.
     * @return a list of ranges.
     */
    operator fun minus(other: Range): List<Range> {
        var result = listOf<Range>()
        // Left:   |------------|                    |-----------|                 |---------|
        //                  -                              -                            -
        // Right:      |-----------------|   or                     |---|   or        |----|
        //                  =                              =                            =
        // Result: |---|                             |-----------|                 |--|
        // Left:   |------------|                    |-----------|                 |---------|
        //                  -                              -                            -
        // Right:      |-----------------|   or                     |---|   or        |----|
        //                  =                              =                            =
        // Result: |---|                             |-----------|                 |--|
        if (other.low > low) {
            result = result + when {
                this is UnicastRange && other is UnicastRange ->
                    UnicastAddress(low)..
                            UnicastAddress(min(high, (other.low - 1u).toUShort()))

                this is GroupRange && other is GroupRange ->
                    GroupAddress(low)..
                            GroupAddress(min(high, (other.low - 1u).toUShort()))

                this is SceneRange && other is SceneRange ->
                    SceneRange(low, min(high, (other.low - 1u).toUShort()))
                else -> throw IllegalArgumentException(
                    "Left and Right ranges must be of same range type!"
                )
            }
        }
        // Left:                |----------|             |-----------|                     |--------|
        //                         -                          -                             -
        // Right:      |----------------|           or       |----|          or     |---|
        //                         =                          =                             =
        // Result:                      |--|                      |--|                     |--------|
        if (other.high < high) {
            result = result + when {
                this is UnicastRange && other is UnicastRange ->
                    UnicastAddress(max((other.high + 1u).toUShort(), low))..
                            UnicastAddress(high)

                this is GroupRange && other is GroupRange ->
                    GroupAddress(max((other.high + 1u).toUShort(), low))..
                            GroupAddress(high)

                this is SceneRange && other is SceneRange ->
                    SceneRange(max((other.high + 1u).toUShort(), low), high)
                else -> throw IllegalArgumentException(
                    "Left and Right ranges must be of same range type!"
                )
            }
        }
        return result
    }
}

/**
 * Allocated address range.
 *
 * @property lowAddress       Low value for a given range.
 * @property highAddress      High value for a given  range.
 */
@Serializable
sealed class AddressRange : Range() {
    abstract val lowAddress: MeshAddress
    abstract val highAddress: MeshAddress

    override val range
        get() = lowAddress.address..highAddress.address
}

/**
 * The AllocatedUnicastRange represents the range of unicast addresses that the Provisioner can
 * allocate to new devices when they are provisioned onto the mesh network, without needing to
 * coordinate the node additions with other Provisioners. The lowAddress and highAddress represent
 * values from 0x0001 to 0x7FFF. The value of the lowAddress property shall be less than or equal to
 * the value of the highAddress property.
 *
 * @property lowAddress        Low address for a given range.
 * @property highAddress       High address for a given  range.
 */
@Serializable
data class UnicastRange(
    override val lowAddress: UnicastAddress,
    override val highAddress: UnicastAddress
) : AddressRange()

/**
 * The AllocatedGroupRange represents the range of group addresses that the Provisioner can allocate
 * to newly created groups, without needing to coordinate the group additions with other
 * Provisioners. The lowAddress and highAddress properties represent values from 0xC000 to 0xFEFF.
 * The value of the lowAddress property shall be less than or equal to the value of the highAddress
 * property.
 *
 * @property lowAddress        Low address for a given range.
 * @property highAddress       High address for a given  range.
 */
@Serializable
data class GroupRange(
    override val lowAddress: GroupAddress,
    override val highAddress: GroupAddress
) : AddressRange()

/**
 * The AllocatedSceneRange represents the range of scene numbers that the Provisioner can use to
 * register new scenes in the mesh network, without needing to coordinate the allocated scene
 * numbers with other Provisioners. The firstScene and lastScene represents values from 0x0001 to
 * 0xFFFF. The value of the firstScene property shall be less than or equal to the value of the
 * lastScene property.
 *
 * @property firstScene    First scene a given range.
 * @property lastScene     Last scene for a given  range.
 */
@Serializable
data class SceneRange(
    @Serializable(with = UShortAsStringSerializer::class)
    val firstScene: SceneNumber,
    @Serializable(with = UShortAsStringSerializer::class)
    val lastScene: SceneNumber
) : Range() {

    @Transient
    override var range = firstScene..lastScene
}

operator fun List<Range>.plus(other: Range): List<Range> {
    val result = ArrayList<Range>(size + 1)
    result.addAll(this)
    result.add(other)
    return result.merged()
}

operator fun List<Range>.plus(other: List<Range>): List<Range> {
    val result = mutableListOf<Range>()
    result.addAll(this)
    result += other
    return result
}

operator fun MutableList<Range>.plusAssign(other: Range) {
    this.add(other)
    this.merged()
}

operator fun MutableList<Range>.plusAssign(other: List<Range>) {
    this.addAll(other)
    this.merged()
}

operator fun List<Range>.minus(other: Range): List<Range> {
    val result = mutableListOf<Range>()
    result.addAll(this)
    result -= other
    return result.toList()
}

operator fun List<Range>.minus(other: List<Range>): List<Range> {
    val result = mutableListOf<Range>()
    result.addAll(this)
    result -= other
    return result.toList()
}

operator fun MutableList<Range>.minusAssign(other: Range) {
    val result = mutableListOf<Range>()
    //result.addAll(this)
    for (range in this) {
        result += range - other
    }
    clear()
    addAll(result)
}

operator fun MutableList<Range>.minusAssign(other: List<Range>) {
    other.map { this -= it }
}

fun List<Range>.merged(): List<Range> {
    if (size <= 1)
        return this
    val result = mutableListOf<Range>()
    var accumulator: Range? = null

    for (range in sortedBy { it.low }) {
        if (accumulator == null) {
            accumulator = range
        }
        if (accumulator.high >= range.high) {
            // Do nothing
        } else if (accumulator.high + 1u >= range.low) {
            accumulator = when (accumulator) {
                is UnicastRange -> UnicastAddress(accumulator.low)..UnicastAddress(range.high)
                is GroupRange -> GroupAddress(accumulator.low)..GroupAddress(range.high)
                is SceneRange -> SceneRange(accumulator.low, range.high)
            }
        } else {
            result.add(accumulator)
            accumulator = range
        }
    }

    if (accumulator != null) {
        result.add(accumulator)
    }
    return result.toList()
}