@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.provisioning

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.kotlin.mesh.bearer.*
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import no.nordicsemi.kotlin.mesh.logger.Logger
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastRange
import no.nordicsemi.kotlin.mesh.crypto.Algorithm
import no.nordicsemi.kotlin.mesh.provisioning.bearer.ProvisioningBearer

/**
 * Provisioning manager is responsible for provisioning new devices to a mesh network.
 *
 * @property unprovisionedDevice          Unprovisioned device to be provisioned.
 * @property meshNetwork                  Mesh network to which the device will be provisioned.
 * @property state                        Current state of the provisioning process.
 */
class ProvisioningManager(
    private val unprovisionedDevice: UnprovisionedDevice,
    private val meshNetwork: MeshNetwork,
    private val bearer: ProvisioningBearer
) {

    val bearerEventFlow = MutableSharedFlow<BearerEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    val bearerDataEventFlow = MutableSharedFlow<BearerPdu>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    private lateinit var authenticationMethod: AuthenticationMethod
    private lateinit var provisioningData: ProvisioningData
    var logger: Logger? = null

    private var capabilities: ProvisioningCapabilities? = null
    var unicastAddress: UnicastAddress? = null
        private set
    var suggestedUnicastAddress: UnicastAddress? = null
        private set

    var state: ProvisioningState = ProvisioningState.Ready
        private set(value) {
            MeshNetwork.onChange(oldValue = state, newValue = value) {
                when (value) {
                    is ProvisioningState.Failed -> logger?.e(LogCategory.PROVISIONING) {
                        "Provisioning failed with error: ${value.error}"
                    }
                    else -> logger?.i(LogCategory.PROVISIONING) {
                        "Provisioning state changed to: $value"
                    }
                }
                field = value
            }
        }

    init {
        // Ensures the provided bearer supports provisioning PDUs.
        require(bearer.supports(PduType.PROVISIONING_PDU)) {
            logger?.e(LogCategory.PROVISIONING) {
                "Bearer does not support provisioning pdu"
            }
            throw BearerError.PduTypeNotSupported
        }
    }

    /**
     * Initializes the provisioning process.
     *
     * @param attentionTimer Determines for how long(seconds) the device shall remain attracting the
     *                       attention of the provisioner in the form of blinking, flashing, buzzing
     *                       etc. The value 0 disables the attention timer.
     * @throws ProvisioningError in case of a provisioning error.
     */
    suspend fun identify(attentionTimer: UByte): ProvisioningCapabilities {
        // Has the provisioning been restarted?
        if (state is ProvisioningState.Failed) reset()

        // Is the Provisioner Manager in the correct state?
        require(state is ProvisioningState.Ready) {
            logger?.e(LogCategory.PROVISIONING) { "Provisioning manager is in invalid state" }
            throw ProvisioningError.InvalidState
        }

        // Is there bearer open?
        require(bearer.isOpen) {
            logger?.e(LogCategory.PROVISIONING) { "Bearer closed" }
            throw BearerError.BearerClosed
        }

        // Initialize Provisioning data.
        provisioningData = ProvisioningData()
        state = ProvisioningState.RequestingCapabilities
        val provisioningInvite = ProvisioningRequest.Invite(attentionTimer)
        logger?.w(LogCategory.PROVISIONING) { "Sending $provisioningInvite" }

        send(provisioningInvite, provisioningData)

        // Wait for the capabilities response.
        val bearerPdu = bearer.pdu.first {
            println("$it")
            it.type == PduType.PROVISIONING_PDU
        }

        // Ensure the PDU type is not [ProvisioningPduType.Failed]
        val response = ProvisioningResponse.from(bearerPdu.data)
        require(response !is ProvisioningResponse.Failed) {
            state = ProvisioningState.Failed(ProvisioningError.InvalidPdu)
            throw ProvisioningError.InvalidPdu
        }

        when {
            state == ProvisioningState.RequestingCapabilities &&
                    response is ProvisioningResponse.Capabilities -> {
                val provisioner = meshNetwork.localProvisioner
                capabilities = response.capabilities
                // Calculates the unicast address automatically based ont he number of elements.
                if (unicastAddress == null && provisioner != null) {
                    val count = response.capabilities.numberOfElements
                    unicastAddress = meshNetwork.nextAvailableUnicastAddress(count, provisioner)
                    suggestedUnicastAddress = unicastAddress
                }
                state = ProvisioningState.CapabilitiesReceived(response.capabilities)
                if (unicastAddress == null) {
                    state = ProvisioningState.Failed(ProvisioningError.NoAddressAvailable)
                    throw ProvisioningError.NoAddressAvailable
                }
            }
        }
        return capabilities!!
    }

    /**
     * Starts the provisioning of the device.
     *
     * [identify] should be called before this method.
     */
    suspend fun provision(
        unicastAddress: UnicastAddress,
        networkKey: NetworkKey = meshNetwork.networkKeys.first(),
        algorithm: Algorithm,
        publicKey: PublicKey,
        authenticationMethod: AuthenticationMethod
    ) {
        require(state is ProvisioningState.CapabilitiesReceived) {
            logger?.e(LogCategory.PROVISIONING) { "Provisioning capabilities not received" }
            throw ProvisioningError.InvalidState
        }
        capabilities = (state as ProvisioningState.CapabilitiesReceived).capabilities


        // Can the Unprovisioned Device be provisioned by this manager.


        // Is the Unicast address valid?
        require(isUnicastAddressValid(unicastAddress)) {
            logger?.e(LogCategory.PROVISIONING) { "Unicast address is not valid" }
            throw ProvisioningError.NoAddressAvailable
        }


        // Is there bearer open?
        require(bearer.isOpen) {
            BearerError.BearerClosed
        }

        // Try generating Private and Public keys. This may fail if the given algorithm is not
        // supported.
        provisioningData.generateKeys(algorithm)

        // If the device's Public Key was obtained OOB, we are now ready to calculate the device's
        // Shared Secret.


        if (publicKey is PublicKey.OobPublicKey) {
            runCatching {
                provisioningData.onDevicePublicKeyReceived(publicKey.key, true)
            }.onFailure {
                state = ProvisioningState.Failed(it)
                return
            }
        }

        state = ProvisioningState.Provisioning
        provisioningData.prepare(networkKey, meshNetwork.ivIndex, unicastAddress)

        val provisioningStart = ProvisioningRequest.Start(
            algorithm,
            publicKey.method,
            authenticationMethod,
        )
        logger?.v(LogCategory.PROVISIONING) { "Sending $provisioningStart" }
        send(provisioningStart, provisioningData)
        this.authenticationMethod = authenticationMethod

        val provisioningPublicKey = ProvisioningRequest.PublicKey(
            provisioningData.provisionerPublicKey
        )
        logger?.v(LogCategory.PROVISIONING) { "Sending $provisioningPublicKey" }
        send(provisioningPublicKey, provisioningData)

        if (publicKey is PublicKey.OobPublicKey) {
            provisioningData.accumulate(publicKey.key)
            obtainAuthValue()
        }
    }

    private fun isUnicastAddressValid(unicastAddress: UnicastAddress): Boolean {
        val provisioner = meshNetwork.localProvisioner
        val capabilities = capabilities
        return if (provisioner != null && capabilities != null) {
            val range = UnicastRange(unicastAddress, capabilities.numberOfElements)
            meshNetwork.isAddressRangeAvailable(range) && provisioner.hasAllocatedRange(range)
        } else {
            false
        }
    }

    /**
     * Resets the provisioning state.
     */
    private fun reset() {
        state = ProvisioningState.Ready
    }

    /**
     * Sends the provisioning request to the device over the Bearer specified in the
     * constructor.
     *
     * @param request Provisioning request to be sent.
     */
    private suspend fun send(request: ProvisioningRequest) {
        bearer.send(request)
    }

    /**
     * Sends the provisioning request to the device over the Bearer specified in the constructor.
     * Additionally it adds the request payload to given inputs. Inputs are required in device
     * authorization.
     *
     * @param request Provisioning request to be sent.
     * @param data    Provisioning data.
     */
    private suspend fun send(request: ProvisioningRequest, data: ProvisioningData) {
        val pdu = request.pdu
        data.accumulate(pdu.sliceArray(1 until pdu.size))
        bearer.send(pdu, PduType.PROVISIONING_PDU)
    }

    private fun obtainAuthValue() {
        val sizeInBytes = provisioningData.algorithm.length shr 3

        when (authenticationMethod) {
            AuthenticationMethod.NoOob -> {}
            AuthenticationMethod.StaticOob -> {}
            is AuthenticationMethod.OutputOob -> {}
            is AuthenticationMethod.InputOob -> {}
        }
    }

    private fun onAuthValueReceived() {

    }
}