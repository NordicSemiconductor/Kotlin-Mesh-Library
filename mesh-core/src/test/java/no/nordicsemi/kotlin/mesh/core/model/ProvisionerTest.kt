package no.nordicsemi.kotlin.mesh.core.model

import org.junit.Assert
import org.junit.Test
import java.util.*

class ProvisionerTest {

    private val provisioner = Provisioner(UUID.randomUUID())
    private val other = Provisioner(UUID.randomUUID())

    @Test
    fun testHasOverlappingUnicastRanges_1() {
        // Non overlapping ranges.
        provisioner.allocate(UnicastAddress(1u)..UnicastAddress(10u))
        other.allocate(UnicastAddress(20u)..UnicastAddress(30u))
        Assert.assertEquals(false, provisioner.hasOverlappingUnicastRanges(other))

        other.allocate(UnicastAddress(1u)..UnicastAddress(10u))
        Assert.assertEquals(true, provisioner.hasOverlappingUnicastRanges(other))
    }

    @Test
    fun testHasOverlappingGroupRanges_2() {
        // Non overlapping ranges.
        provisioner.allocate(GroupAddress(49152u)..GroupAddress(49200u))
        other.allocate(GroupAddress(49250u)..GroupAddress(49350u))
        Assert.assertEquals(false, provisioner.hasOverlappingGroupRanges(other))

        other.allocate(GroupAddress(49152u)..GroupAddress(49200u))
        Assert.assertEquals(true, provisioner.hasOverlappingGroupRanges(other))
    }

    @Test
    fun testHasOverlappingSceneRanges_3() {
        // Non overlapping ranges.
        provisioner.allocate(SceneRange(1u, 10u))
        other.allocate(SceneRange(20u, 30u))
        Assert.assertEquals(false, provisioner.hasOverlappingSceneRanges(other))

        // Overlapping ranges
        other.allocate(SceneRange(1u, 10u))
        Assert.assertEquals(true, provisioner.hasOverlappingSceneRanges(other))
    }

    @Test
    fun testHasOverlappingRanges_4() {
        // Non overlapping ranges.
        Assert.assertEquals(false, provisioner.hasOverlappingRanges(other))
    }
}
