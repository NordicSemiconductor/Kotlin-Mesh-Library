package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.coroutines.runBlocking
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class GroupTest {

    private val networkManager = MeshNetworkManager(storage = TestStorage())
    private lateinit var meshNetwork: MeshNetwork

    @Before
    fun setUp() {
        val jsonBytes =
            this.javaClass.classLoader.getResourceAsStream("cdb_json.json")?.readAllBytes()
        runBlocking {
            meshNetwork = networkManager.import(jsonBytes!!)
        }
    }

    @Test
    fun isUsed() {
        val parentGroup = meshNetwork._groups.find {
            it.address == GroupAddress(0xC023u)
        }!!
        Assert.assertTrue(parentGroup.isUsed)
    }

    @Test
    fun testIsDirectChildOf() {
        val parentGroup = meshNetwork._groups.find {
            it.address == GroupAddress(0xC023u)
        }!!
        val childGroup = meshNetwork._groups.find {
            it.address == GroupAddress(0xC024u)
        }!!
        Assert.assertTrue(childGroup.isDirectChildOf(parentGroup))
    }

    @Test
    fun testIsDirectParentOf() {
        val parentGroup = meshNetwork._groups.find {
            it.address == GroupAddress(0xC023u)
        }!!
        val childGroup = meshNetwork._groups.find {
            it.address == GroupAddress(0xC024u)
        }!!
        Assert.assertTrue(parentGroup.isDirectParentOf(childGroup))
    }

    @Test
    fun testIsChildOf() {
        val parentGroup = meshNetwork._groups.find {
            it.address == GroupAddress(0xC023u)
        }!!
        val childGroup = meshNetwork._groups.find {
            it.address == GroupAddress(0xC026u)
        }!!
        Assert.assertTrue(childGroup.isChildOf(parentGroup))
    }

    @Test
    fun testIsParentOf() {
        val parentGroup = meshNetwork._groups.find {
            it.address == GroupAddress(0xC023u)
        }!!
        val childGroup = meshNetwork._groups.find {
            it.address == GroupAddress(0xC026u)
        }!!
        Assert.assertTrue(parentGroup.isParentOf(childGroup))
    }

    @Test
    fun testSetAsChildOf() {
        val parentGroup = meshNetwork._groups.find {
            it.address == GroupAddress(0xC023u)
        }!!
        val childGroup = meshNetwork._groups.find {
            it.address == GroupAddress(0xC103u)
        }!!
        childGroup.setAsChildOf(parentGroup)
        Assert.assertTrue(childGroup.isChildOf(parentGroup))
    }

    @Test
    fun testSetAsParentOf() {
        val parentGroup = meshNetwork._groups.find {
            it.address is VirtualAddress
        }!!
        val childGroup = meshNetwork._groups.find {
            it.address == GroupAddress(0xC103u)
        }!!
        parentGroup.setAsParentOf(childGroup)
        Assert.assertTrue(parentGroup.isParentOf(childGroup))
    }

}