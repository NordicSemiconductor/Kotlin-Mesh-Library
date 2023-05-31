package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.coroutines.runBlocking
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.UUID

class MeshNetworkTest {

    private val networkManager = MeshNetworkManager(storage = TestStorage())
    private lateinit var meshNetwork: MeshNetwork
    private val group = Group("Test Group", GroupAddress(0xD000u))
    private val scene = Scene("Test Scene", 0x000Au)

    @Before
    fun setUp() {
        val jsonBytes =
            this.javaClass.classLoader.getResourceAsStream("cdb_json.json")?.readAllBytes()
        runBlocking {
            meshNetwork = networkManager.import(jsonBytes!!)
        }
    }

    @Test
    fun testNextAvailableUnicastAddressEmptyNetwork() {
        val meshNetwork = MeshNetwork(_name = "Test Network")
        val provisioner = Provisioner(uuid = UUID.randomUUID()).apply {
            this.network = meshNetwork
            this.name = "Test Provisioner"
            this.allocate(
                range = UnicastAddress(address = 0x0001u)..UnicastAddress(address = 0x7F00u)
            )
        }

        val address = meshNetwork.nextAvailableUnicastAddress(
            elementCount = 6,
            provisioner = provisioner
        )
        Assert.assertNotNull(address)
        Assert.assertTrue(address!! == UnicastAddress(address = 1))
    }

    @Test
    fun testNextAvailableUnicastAddressWithOffset() {
        val meshNetwork = MeshNetwork(_name = "Test Network")
        meshNetwork.apply {
            add(
                node = Node(
                    name = "Node 0",
                    unicastAddress = UnicastAddress(1),
                    elements = 9
                )
            )
            add(
                node = Node(
                    name = "Node 1",
                    unicastAddress = UnicastAddress(10),
                    elements = 9
                )
            )
            add(
                node = Node(
                    name = "Node 2",
                    unicastAddress = UnicastAddress(20),
                    elements = 9
                )
            )
            add(
                node = Node(
                    name = "Node 3",
                    unicastAddress = UnicastAddress(30),
                    elements = 9
                )
            )
        }
        val provisioner = Provisioner(
            name = "Test Provisioner",
            allocatedUnicastRanges = mutableListOf(UnicastRange(100, 200))
        ).apply {
            this.network = meshNetwork
        }

        val address = meshNetwork.nextAvailableUnicastAddress(
            elementCount = 6,
            provisioner = provisioner
        )
        Assert.assertNotNull(address)
        Assert.assertTrue(address!! == UnicastAddress(address = 100))
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
        Assert.assertEquals(true, meshNetwork._groups.none { it.address == group.address })
    }

    @Test
    fun testNextAvailableGroup() {
        val expectedGroupAddress = GroupAddress(0xC000u)
        val provisioner = meshNetwork._provisioners.last()
        val actualGroupAddress = meshNetwork.nextAvailableGroup(provisioner)
        Assert.assertEquals(expectedGroupAddress, actualGroupAddress)
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
        Assert.assertEquals(true, meshNetwork._scenes.none { it.number == scene.number })
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
        meshNetwork.moveProvisioner(meshNetwork.provisioners.indexOf(provisioner), to)
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
        provisioner.assign(expectedAddress)
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
        Assert.assertEquals(true, provisioner.isRangeAvailableForAllocation(range))
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
            provisioner.areRangesAvailableForAllocation(listOf(range))
        )
    }
}