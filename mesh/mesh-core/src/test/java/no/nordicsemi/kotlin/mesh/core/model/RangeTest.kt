package no.nordicsemi.kotlin.mesh.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RangeTest {

    @Test
    fun testOverlaps() {
        val range = UnicastAddress(1u)..UnicastAddress(10u)
        val overlappingRange = UnicastAddress(5u)..UnicastAddress(20u)
        val nonOverlappingRange = UnicastAddress(11u)..UnicastAddress(20u)
        // Overlapping ranges
        assertTrue(range.overlaps(overlappingRange))

        // Non overlapping ranges
        assertFalse(range.overlaps(nonOverlappingRange))
    }

    @Test
    fun testOverlapsRanges() {
        val range = UnicastAddress(1u)..UnicastAddress(10u)
        val overlappingRange = UnicastAddress(5u)..UnicastAddress(20u)

        // Overlapping ranges
        val overlappingRanges = listOf(overlappingRange)
        assertTrue(range.overlaps(overlappingRanges))
    }

    @Test
    fun testDistance() {
        assertEquals(
            0, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(21u)..UnicastAddress(40u)))
        )
        assertEquals(
            1, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(22u)..UnicastAddress(40u)))
        )
        assertEquals(
            9, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(30u)..UnicastAddress(40u)))
        )
        assertEquals(
            0, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(15u)..UnicastAddress(40u)))
        )
        assertEquals(
            0, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(1u)..UnicastAddress(40u)))
        )
        assertEquals(
            0, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(1u)..UnicastAddress(9u)))
        )
        assertEquals(
            1, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(1u)..UnicastAddress(8u)))
        )
        assertEquals(
            0, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(10u)..UnicastAddress(10u)))
        )
        assertEquals(
            7, (UnicastAddress(10u)..UnicastAddress(20u))
                .distance((UnicastAddress(1u)..UnicastAddress(2u)))
        )
    }

    @Test
    fun testPlus() {
        // Non overlapping ranges.
        assertEquals(
            2,
            ((UnicastAddress(10u)..UnicastAddress(20u)) +
                    (UnicastAddress(30u)..UnicastAddress(40u))).size
        )
        // Non adjacent ranges.
        assertEquals(
            1,
            ((UnicastAddress(10u)..UnicastAddress(20u)) +
                    (UnicastAddress(21u)..UnicastAddress(40u))).size
        )
        // Overlapping ranges.
        assertEquals(
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
        assertEquals(2, ranges.size)
        assertEquals(ranges[0].low.toInt(), 10)
        assertEquals(ranges[0].high.toInt(), 14)
        assertEquals(ranges[1].low.toInt(), 36)
        assertEquals(ranges[1].high.toInt(), 40)

        // End overlapping range
        ranges = mutableListOf<Range>(
            UnicastAddress(10u)..UnicastAddress(20u),
            UnicastAddress(30u)..UnicastAddress(40u)
        )
        ranges -= (UnicastAddress(40u)..UnicastAddress(40u))
        assertEquals(2, ranges.size)
        assertEquals(ranges[0].low.toInt(), 10)
        assertEquals(ranges[0].high.toInt(), 20)
        assertEquals(ranges[1].low.toInt(), 30)
        assertEquals(ranges[1].high.toInt(), 39)

        // Adjacent overlapping range
        ranges = mutableListOf<Range>(
            UnicastAddress(1u)..UnicastAddress(32767u)
        )
        ranges -= (UnicastAddress(12289u)..UnicastAddress(28671u))
        ranges -= (UnicastAddress(1u)..UnicastAddress(12288u))

        assertEquals(1, ranges.size)
        assertEquals(ranges[0].low.toInt(), 28672)
        assertEquals(ranges[0].high.toInt(), 32767)
    }

    @Test
    fun testListPlus() {
        val ranges = listOf<Range>(
            UnicastAddress(1u)..UnicastAddress(1000u),
            UnicastAddress(2000u)..UnicastAddress(3000u)
        )

        val result = ranges + (UnicastAddress(4000u)..UnicastAddress(5000u))
        assertEquals(3, result.size)
        assertEquals(result[0].low.toInt(), 1)
        assertEquals(result[0].high.toInt(), 1000)
        assertEquals(result[1].low.toInt(), 2000)
        assertEquals(result[1].high.toInt(), 3000)
        assertEquals(result[2].low.toInt(), 4000)
        assertEquals(result[2].high.toInt(), 5000)

        val result2 = ranges + (UnicastAddress(1001u)..UnicastAddress(1999u))
        assertEquals(1, result2.size)
        assertEquals(result2[0].low.toInt(), 1)
        assertEquals(result2[0].high.toInt(), 3000)
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
        assertEquals(3, result.size)
        assertEquals(result[0].low.toInt(), 1)
        assertEquals(result[0].high.toInt(), 499)
        assertEquals(result[1].low.toInt(), 801)
        assertEquals(result[1].high.toInt(), 1000)
        assertEquals(result[2].low.toInt(), 2004)
        assertEquals(result[2].high.toInt(), 2499)
    }

    @Test
    fun testListOverlaps() {
        val ranges = listOf<Range>(
            UnicastAddress(1u)..UnicastAddress(1000u),
            UnicastAddress(2000u)..UnicastAddress(3000u)
        )
        val otherRanges = listOf<Range>(
            UnicastAddress(500u)..UnicastAddress(800u),
            UnicastAddress(1999u)..UnicastAddress(2003u),
            UnicastAddress(2500u)..UnicastAddress(3000u),
        )
        assertTrue(ranges.overlaps(otherRanges))
    }
}