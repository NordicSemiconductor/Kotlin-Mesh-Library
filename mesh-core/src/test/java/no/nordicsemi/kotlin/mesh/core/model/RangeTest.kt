package no.nordicsemi.kotlin.mesh.core.model

import org.junit.Assert
import org.junit.Test

class RangeTest {

    @Test
    fun testOverlaps() {
        // Overlapping ranges
        Assert
            .assertTrue(
                (UnicastAddress(1u)..UnicastAddress(10u))
                    .overlaps(UnicastAddress(5u)..UnicastAddress(20u))
            )

        // Non overlapping ranges
        Assert
            .assertFalse(
                (UnicastAddress(1u)..UnicastAddress(10u))
                    .overlaps(UnicastAddress(11u)..UnicastAddress(20u))
            )
    }


    @Test
    fun testOverlapsRanges() {
        // Overlapping ranges
        val range = UnicastAddress(1u)..UnicastAddress(10u)
        val overlappingRanges = listOf(UnicastAddress(5u)..UnicastAddress(20u))
        Assert.assertTrue(range.overlaps(overlappingRanges))
    }

    @Test
    fun testDistance() {
        Assert.assertEquals(
            0, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(21u)..UnicastAddress(40u)))
        )
        Assert.assertEquals(
            1, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(22u)..UnicastAddress(40u)))
        )
        Assert.assertEquals(
            9, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(30u)..UnicastAddress(40u)))
        )
        Assert.assertEquals(
            0, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(15u)..UnicastAddress(40u)))
        )
        Assert.assertEquals(
            0, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(1u)..UnicastAddress(40u)))
        )
        Assert.assertEquals(
            0, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(1u)..UnicastAddress(9u)))
        )
        Assert.assertEquals(
            1, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(1u)..UnicastAddress(8u)))
        )
        Assert.assertEquals(
            0, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(10u)..UnicastAddress(10u)))
        )
        Assert.assertEquals(
            7, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(1u)..UnicastAddress(2u)))
        )
    }

    @Test
    fun testPlus() {
        // Non overlapping ranges.
        Assert.assertEquals(
            2,
            ((UnicastAddress(10u)..UnicastAddress(20u)) +
                    (UnicastAddress(30u)..UnicastAddress(40u))).size
        )
        // Non adjacent ranges.
        Assert.assertEquals(
            1,
            ((UnicastAddress(10u)..UnicastAddress(20u)) +
                    (UnicastAddress(21u)..UnicastAddress(40u))).size
        )
        // Overlapping ranges.
        Assert.assertEquals(
            1,
            ((UnicastAddress(10u)..UnicastAddress(20u)) +
                    (UnicastAddress(15u)..UnicastAddress(40u))).size
        )
    }

    @Test
    fun testMinus() {
        // Mid overlapping range
        var ranges = mutableListOf<Range>(
            UnicastAddress(10u)..UnicastAddress(20u),
            UnicastAddress(30u)..UnicastAddress(40u)
        )
        ranges -= (UnicastAddress(15u)..UnicastAddress(35u))
        Assert.assertEquals(2, ranges.size)
        Assert.assertEquals(ranges[0].low.toInt(), 10)
        Assert.assertEquals(ranges[0].high.toInt(), 14)
        Assert.assertEquals(ranges[1].low.toInt(), 36)
        Assert.assertEquals(ranges[1].high.toInt(), 40)

        // End overlapping range
        ranges = mutableListOf<Range>(
            UnicastAddress(10u)..UnicastAddress(20u),
            UnicastAddress(30u)..UnicastAddress(40u)
        )
        ranges -= (UnicastAddress(40u)..UnicastAddress(40u))
        Assert.assertEquals(2, ranges.size)
        Assert.assertEquals(ranges[0].low.toInt(), 10)
        Assert.assertEquals(ranges[0].high.toInt(), 20)
        Assert.assertEquals(ranges[1].low.toInt(), 30)
        Assert.assertEquals(ranges[1].high.toInt(), 39)

        // Adjacent overlapping range
        ranges = mutableListOf<Range>(
            UnicastAddress(1u)..UnicastAddress(32767u)
        )
        ranges -= (UnicastAddress(12289u)..UnicastAddress(28671u))
        ranges -= (UnicastAddress(1u)..UnicastAddress(12288u))

        Assert.assertEquals(1, ranges.size)
        Assert.assertEquals(ranges[0].low.toInt(), 28672)
        Assert.assertEquals(ranges[0].high.toInt(), 32767)
    }

    @Test
    fun testListPlus() {
        val ranges = listOf<Range>(
            UnicastAddress(1u)..UnicastAddress(1000u),
            UnicastAddress(2000u)..UnicastAddress(3000u)
        )

        val result = ranges + (UnicastAddress(4000u)..UnicastAddress(5000u))
        Assert.assertEquals(3, result.size)
        Assert.assertEquals(result[0].low.toInt(), 1)
        Assert.assertEquals(result[0].high.toInt(), 1000)
        Assert.assertEquals(result[1].low.toInt(), 2000)
        Assert.assertEquals(result[1].high.toInt(), 3000)
        Assert.assertEquals(result[2].low.toInt(), 4000)
        Assert.assertEquals(result[2].high.toInt(), 5000)

        val result2 = ranges + (UnicastAddress(1001u)..UnicastAddress(1999u))
        Assert.assertEquals(1, result2.size)
        Assert.assertEquals(result2[0].low.toInt(), 1)
        Assert.assertEquals(result2[0].high.toInt(), 3000)
    }

    @Test
    fun testListMinus() {
        val ranges = listOf<Range>(
            UnicastAddress(1u)..UnicastAddress(1000u),
            UnicastAddress(2000u)..UnicastAddress(3000u)
        )
        val otherRanges = listOf<Range>(
            UnicastAddress(500u)..UnicastAddress(800u),
            UnicastAddress(1999u)..UnicastAddress(2003u),
            UnicastAddress(2500u)..UnicastAddress(3000u),
        )

        val result = ranges - otherRanges
        Assert.assertEquals(3, result.size)
        Assert.assertEquals(result[0].low.toInt(), 1)
        Assert.assertEquals(result[0].high.toInt(), 499)
        Assert.assertEquals(result[1].low.toInt(), 801)
        Assert.assertEquals(result[1].high.toInt(), 1000)
        Assert.assertEquals(result[2].low.toInt(), 2004)
        Assert.assertEquals(result[2].high.toInt(), 2499)
    }
}