package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.coroutines.runBlocking
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class MeshNetworkTest {

    private val networkManager = MeshNetworkManager()
    private val meshNetwork by lazy { networkManager.meshNetwork }
    private val group = Group("Test Group", GroupAddress(0xD000u))
    private val scene = Scene("Test Scene", 0x000Au)

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

    @Test
    fun testNextAvailableGroup() {
        val expectedGroupAddress = GroupAddress(0xC003u)
        val provisioner = meshNetwork.provisioners.last()
        val actualGroupAddress = meshNetwork.nextAvailableGroup(provisioner)
        Assert.assertTrue(expectedGroupAddress == actualGroupAddress)
    }

    @Test
    fun testAddScene() {
        meshNetwork.add(scene)
        Assert.assertTrue(meshNetwork.scenes.any { it.number == scene.number })
    }

    @Test
    fun testRemoveScene() {
        meshNetwork.add(scene)
        meshNetwork.remove(scene)
        Assert.assertFalse(meshNetwork.scenes.none { it.number == scene.number })
    }

    @Test
    fun testNextAvailableScene() {
        val expectedSceneNumber: SceneNumber = 1u
        val provisioner = meshNetwork.provisioners.last()
        val actualSceneNumber = meshNetwork.nextAvailableScene(provisioner)
        Assert.assertTrue(expectedSceneNumber == actualSceneNumber)
    }
}