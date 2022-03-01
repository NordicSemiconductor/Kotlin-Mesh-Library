package no.nordicsemi.kotlin.mesh.core.model

import org.junit.Assert
import org.junit.Test
import java.util.*

class ExclusionListTest {

    private val elements = arrayListOf(
        Element(name = "Element 0", 0, 0, models = listOf()),
        Element(name = "Element 1", 1, 0, models = listOf()),
        Element(name = "Element 2", 2, 0, models = listOf())
    )
    private val node = Node(
        uuid = UUID.randomUUID(),
        deviceKey = byteArrayOf(),
        netKeys = listOf(),
        name = "Node",
        unicastAddress = UnicastAddress(address = 1.toUShort()),
        elements = elements,
        appKeys = listOf()
    )

    @Test
    fun testExcludeUnicast() {
        val exclusionList = ExclusionList(ivIndex = 1u)
        exclusionList.exclude(address = UnicastAddress(address = 1u))
        Assert.assertEquals(UnicastAddress(address = 1.toUShort()), exclusionList.addresses[0])
    }

    @Test
    fun testExcludeNode() {
        val exclusionList = ExclusionList(ivIndex = 1u)
        exclusionList.exclude(node = node)
        Assert.assertEquals(UnicastAddress(address = 1.toUShort()), exclusionList.addresses[0])
        Assert.assertEquals(UnicastAddress(address = 2.toUShort()), exclusionList.addresses[1])
        Assert.assertEquals(UnicastAddress(address = 3.toUShort()), exclusionList.addresses[2])
    }

    @Test
    fun testIsExcluded() {
        val exclusionList = ExclusionList(ivIndex = 1u)
        val expected = exclusionList.exclude(address = UnicastAddress(address = 1u))
        Assert.assertEquals(
            expected,
            exclusionList.isExcluded(address = UnicastAddress(address = 1u))
        )
    }
}