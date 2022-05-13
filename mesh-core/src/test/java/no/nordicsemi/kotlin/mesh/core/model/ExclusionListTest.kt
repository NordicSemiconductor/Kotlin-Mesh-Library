package no.nordicsemi.kotlin.mesh.core.model

import org.junit.Assert
import org.junit.Test
import java.util.*

class ExclusionListTest {

    private val elements = arrayListOf(
        Element(location = Unknown, models = listOf()),
        Element(location = Unknown, models = listOf()),
        Element(location = Unknown, models = listOf())
    )
    private val node = Node(
        uuid = UUID.randomUUID(),
        deviceKey = byteArrayOf(),
        _primaryUnicastAddress = UnicastAddress(address = 1u),
        _elements = elements,
        _netKeys = mutableListOf(),
        _appKeys = mutableListOf()
    ).apply {
        name = "Node"
    }

    @Test
    fun testExcludeUnicast() {
        val exclusionList = ExclusionList(ivIndex = 1u)
        exclusionList.exclude(address = UnicastAddress(address = 1u))
        Assert.assertEquals(UnicastAddress(address = 1u), exclusionList._addresses[0])
    }

    @Test
    fun testExcludeNode() {
        val exclusionList = ExclusionList(ivIndex = 1u)
        exclusionList.exclude(node = node)
        Assert.assertEquals(UnicastAddress(address = 1u), exclusionList._addresses[0])
        Assert.assertEquals(UnicastAddress(address = 2u), exclusionList._addresses[1])
        Assert.assertEquals(UnicastAddress(address = 3u), exclusionList._addresses[2])
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