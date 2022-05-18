package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.coroutines.runBlocking
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

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
        Assert.assertTrue(meshNetwork._groups.any { it.address == group.address })
    }

    @Test
    fun testRemoveGroup() {
        meshNetwork.add(group)
        meshNetwork.remove(group)
        Assert.assertFalse(meshNetwork._groups.none { it.address == group.address })
    }

    @Test
    fun testNextAvailableGroup() {
        val expectedGroupAddress = GroupAddress(0xC003u)
        val provisioner = meshNetwork._provisioners.last()
        val actualGroupAddress = meshNetwork.nextAvailableGroup(provisioner)
        Assert.assertTrue(expectedGroupAddress == actualGroupAddress)
    }

    @Test
    fun testAddScene() {
        meshNetwork.add(scene)
        Assert.assertTrue(meshNetwork._scenes.any { it.number == scene.number })
    }

    @Test
    fun testRemoveScene() {
        meshNetwork.add(scene)
        meshNetwork.remove(scene)
        Assert.assertFalse(meshNetwork._scenes.none { it.number == scene.number })
    }

    @Test
    fun testNextAvailableScene() {
        val expectedSceneNumber: SceneNumber = 1u
        val provisioner = meshNetwork._provisioners.last()
        val actualSceneNumber = meshNetwork.nextAvailableScene(provisioner)
        Assert.assertTrue(expectedSceneNumber == actualSceneNumber)
    }

    @Test
    fun testMoveProvisionerFromTo() {
        meshNetwork.add(provisioner = Provisioner(UUID.randomUUID()).apply {
            this.network = meshNetwork
            this.name = "Test provisioner"
            this.allocate(UnicastAddress(0x7000u)..UnicastAddress(0x7F00u))
        })
        val provisioner = meshNetwork._provisioners.first()
        val to = meshNetwork._provisioners.size - 1
        meshNetwork.move(meshNetwork.provisioners.indexOf(provisioner), to)
        Assert.assertEquals(to, meshNetwork._provisioners.indexOf(provisioner))
    }

    @Test
    fun testMoveProvisionerTo() {
        meshNetwork.add(provisioner = Provisioner(UUID.randomUUID()).apply {
            this.network = meshNetwork
            this.name = "Test provisioner"
            this.allocate(UnicastAddress(0x7000u)..UnicastAddress(0x7F00u))
        })
        val provisioner = meshNetwork._provisioners.first()
        val to = meshNetwork._provisioners.size - 1
        meshNetwork.move(provisioner, to)
        Assert.assertEquals(to, meshNetwork._provisioners.indexOf(provisioner))
    }

    @Test
    fun testAssign() {
        val expectedAddress = UnicastAddress(255u)
        val provisioner = meshNetwork._provisioners.first()
        meshNetwork.assign(expectedAddress, provisioner)
        Assert.assertEquals(
            expectedAddress,
            meshNetwork.node(provisioner.uuid)?.primaryUnicastAddress
        )
    }

    @Test
    fun testDisableConfigurationCapabilities() {
        val provisioner = meshNetwork._provisioners.first()
        meshNetwork.disableConfigurationCapabilities(provisioner)
        Assert.assertEquals(null, meshNetwork.node(provisioner.uuid)?.primaryUnicastAddress)
    }

    @Test
    fun testIsRangeAvailableForAllocation() {
        val range = UnicastAddress(0x7000u)..UnicastAddress(0x7F00u)
        val provisioner = Provisioner(UUID.randomUUID()).apply {
            this.network = meshNetwork
            this.name = "Test provisioner"
        }
        Assert.assertEquals(true, meshNetwork.isRangeAvailableForAllocation(range, provisioner))
        meshNetwork.areRangesAvailableForAllocation(listOf(range), provisioner)
    }

    @Test
    fun testAreRangesAvailableForAllocation() {
        val range = UnicastAddress(0x7000u)..UnicastAddress(0x7F00u)
        meshNetwork.provisioners.first().allocate(range)
        val provisioner = Provisioner(UUID.randomUUID()).apply {
            this.network = meshNetwork
            this.name = "Test provisioner"
        }
        Assert.assertEquals(
            false,
            meshNetwork.areRangesAvailableForAllocation(listOf(range), provisioner)
        )
    }
}