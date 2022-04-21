package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.coroutines.runBlocking
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.model.serialization.UUIDSerializer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

class GroupTest {

    private val networkManager = MeshNetworkManager()
    lateinit var network: MeshNetwork

    @Before
    fun setUp() = runBlocking {
        val jsonBytes =
            this.javaClass.classLoader.getResourceAsStream("cdb_json.json")?.readAllBytes()
        runBlocking {
            networkManager.importMeshNetwork(jsonBytes!!)
        }
    }

    @Test
    fun isUsed() {
        val parentGroup = networkManager.meshNetwork.groups.find {
            it.address == GroupAddress(0xC023u)
        }!!
        Assert.assertTrue(parentGroup.isUsed)
    }

    @Test
    fun testIsDirectChildOf() {
        val parentGroup = networkManager.meshNetwork.groups.find {
            it.address == GroupAddress(0xC023u)
        }!!
        val childGroup = networkManager.meshNetwork.groups.find {
            it.address == GroupAddress(0xC024u)
        }!!
        Assert.assertTrue(childGroup.isDirectChildOf(parentGroup))
    }

    @Test
    fun testIsDirectParentOf() {
        val parentGroup = networkManager.meshNetwork.groups.find {
            it.address == GroupAddress(0xC023u)
        }!!
        val childGroup = networkManager.meshNetwork.groups.find {
            it.address == GroupAddress(0xC024u)
        }!!
        Assert.assertTrue(parentGroup.isDirectParentOf(childGroup))
    }

    @Test
    fun testIsChildOf() {
        val parentGroup = networkManager.meshNetwork.groups.find {
            it.address == GroupAddress(0xC023u)
        }!!
        val childGroup = networkManager.meshNetwork.groups.find {
            it.address == GroupAddress(0xC026u)
        }!!
        Assert.assertTrue(childGroup.isChildOf(parentGroup))
    }

    @Test
    fun testIsParentOf() {
        val parentGroup = networkManager.meshNetwork.groups.find {
            it.address == GroupAddress(0xC023u)
        }!!
        val childGroup = networkManager.meshNetwork.groups.find {
            it.address == GroupAddress(0xC026u)
        }!!
        Assert.assertTrue(parentGroup.isParentOf(childGroup))
    }

    @Test
    fun testSetAsChildOf() {
        val parentGroup = networkManager.meshNetwork.groups.find {
            it.address == GroupAddress(0xC023u)
        }!!
        val childGroup = networkManager.meshNetwork.groups.find {
            it.address == GroupAddress(0xC103u)
        }!!
        childGroup.setAsChildOf(parentGroup)
        Assert.assertTrue(childGroup.isChildOf(parentGroup))
    }

    @Test
    fun testSetAsParentOf() {
        val parentGroup = networkManager.meshNetwork.groups.find {
            it.address is VirtualAddress
        }!!
        val childGroup = networkManager.meshNetwork.groups.find {
            it.address == GroupAddress(0xC103u)
        }!!
        parentGroup.setAsParentOf(childGroup)
        Assert.assertTrue(parentGroup.isParentOf(childGroup))
    }

}