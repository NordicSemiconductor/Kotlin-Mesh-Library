package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.coroutines.runBlocking
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class MeshNetworkTest {

    private val networkManager = MeshNetworkManager()
    private val meshNetwork by lazy { networkManager.meshNetwork }
    private val group = Group("test", GroupAddress(0xD000u))

    @Before
    fun setUp() = runBlocking {
        val jsonBytes =
            this.javaClass.classLoader.getResourceAsStream("cdb_json.json")?.readAllBytes()
        runBlocking {
            networkManager.importMeshNetwork(jsonBytes!!)
        }
    }

    @Test
    fun testAddGroup() {
        meshNetwork.add(group)
        Assert.assertTrue(meshNetwork.groups.any { it.address == group.address })
    }

    @Test
    fun testRemoveGroup() {
        meshNetwork.add(group)
        meshNetwork.remove(group)
        Assert.assertFalse(meshNetwork.groups.none { it.address == group.address })
    }
}