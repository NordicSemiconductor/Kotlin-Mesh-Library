@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.provisioning

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import no.nordicsemi.kotlin.mesh.bearer.BearerError
import no.nordicsemi.kotlin.mesh.bearer.BearerEvent
import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.bearer.ReassembledPdu
import no.nordicsemi.kotlin.mesh.bearer.provisioning.MeshProvisioningBearer
import no.nordicsemi.kotlin.mesh.core.exception.MeshNetworkException
import no.nordicsemi.kotlin.mesh.core.exception.NoLocalProvisioner
import no.nordicsemi.kotlin.mesh.core.exception.NoUnicastRangeAllocated
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastRange
import no.nordicsemi.kotlin.mesh.crypto.Algorithm.Companion.strongest
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import no.nordicsemi.kotlin.mesh.logger.Logger
import no.nordicsemi.kotlin.mesh.provisioning.bearer.send

/**
 * Provisioning manager is responsible for provisioning new devices to a mesh network.
 *
 * @property unprovisionedDevice          Unprovisioned device to be provisioned.
 * @property meshNetwork                  Mesh network to which the device will be provisioned.
 * @property bearer                       Bearer used to send provisioning PDUs.
 */
class ProvisioningManager(
    private val unprovisionedDevice: UnprovisionedDevice,
    private val meshNetwork: MeshNetwork,
    private val bearer: MeshProvisioningBearer
) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    lateinit var configuration: ProvisioningConfiguration
    var logger: Logger? = null

    var suggestedUnicastAddress: UnicastAddress? = null
        private set

    init {
        // Ensures that the mesh network has at least one provisioner added and a unicast address
        // range is allocated.
        meshNetwork.localProvisioner?.let {
            require(it.allocatedUnicastRanges.isNotEmpty()) {
                logger?.e(LogCategory.PROVISIONING) { "No unicast ranges allocated" }
                throw NoUnicastRangeAllocated
            }
        } ?: run {
            logger?.e(LogCategory.PROVISIONING) { "No local provisioner" }
            throw NoLocalProvisioner
        }

        // Ensures the provided bearer supports provisioning PDUs.
        require(bearer.supports(PduType.PROVISIONING_PDU)) {
            logger?.e(LogCategory.PROVISIONING) {
                "Bearer does not support provisioning pdu"
            }
            throw BearerError.PduTypeNotSupported
        }
        observeBearerStateChanges()
    }

    /**
     * Starts the provisioning process with the given attention timer.
     *
     * @param attentionTimer Attention timer value in seconds.
     * @return Flow of provisioning states that could be used to observe and continue/cancel the
     *         provisioning process.
     */
    @Throws(MeshNetworkException::class, ProvisioningError::class, BearerError::class)
    fun provision(attentionTimer: UByte) = flow {
        // Is there bearer open?
        require(bearer.isOpen) {
            logger?.e(LogCategory.PROVISIONING) { "Bearer closed" }
            throw BearerError.BearerClosed
        }
        // Emit the current state.
        emit(ProvisioningState.RequestingCapabilities)

        // Initialize Provisioning data.
        val provisioningData = ProvisioningData()

        // Sends the provisioning invite and awaits for the capabilities.
        val capabilities = awaitCapabilities(
            invite = ProvisioningRequest.Invite(attentionTimer),
            provisioningData = provisioningData
        ).apply {
            // Lets init based on the capabilities
            configuration.unicastAddress = meshNetwork.run {
                nextAvailableUnicastAddress(
                    elementCount = numberOfElements,
                    provisioner = localProvisioner!!
                )!!
            }
            configuration.algorithm = algorithms.strongest()
            configuration.publicKey = if (publicKeyType.isNotEmpty()) {
                PublicKey.OobPublicKey(ByteArray(16) { 0x00 })
            } else PublicKey.NoOobPublicKey
            configuration.authMethod = supportedAuthenticationMethods.first()
        }

        // We use a mutex here to wait for the user to either start or cancel the provisioning.
        val mutex = Mutex(true)
        // Emit to the user that the capabilities have been received.
        emit(
            value = ProvisioningState.CapabilitiesReceived(
                capabilities = capabilities,
                configuration = configuration,
                start = { configuration ->
                    this@ProvisioningManager.configuration = configuration
                    mutex.unlock()
                },
                cancel = { mutex.unlock() }
            )
        )
        mutex.lock()

        // TODO Can the Unprovisioned Device be provisioned by this manager?

        require(configuration.unicastAddress != null) {
            throw NoAddressAvailable
        }
        // Is the Unicast address valid?
        require(
            isUnicastAddressValid(
                configuration.unicastAddress!!,
                capabilities.numberOfElements
            )
        ) {
            logger?.e(LogCategory.PROVISIONING) { "Unicast address is not valid" }
            throw InvalidAddress
        }

        // Try generating Private and Public keys. This may fail if the given algorithm is not
        // supported.
        provisioningData.generateKeys(configuration.algorithm)

        emit(ProvisioningState.Provisioning)
        provisioningData.prepare(
            configuration.networkKey,
            meshNetwork.ivIndex,
            configuration.unicastAddress!!
        )

        ProvisioningRequest.Start(configuration).also { start ->
            logger?.v(LogCategory.PROVISIONING) { "Sending $start" }
            send(start).also { provisioningData.accumulate(it) }
        }

        // If the device's Public Key was obtained OOB, we are now ready to calculate the device's
        // Shared Secret, if not we need send the provisioner public key and wait for the device's
        // Public Key.
        val key = when (configuration.publicKey) {
            is PublicKey.OobPublicKey -> (configuration.publicKey as PublicKey.OobPublicKey).key
            else -> {
                awaitProvisioneePublicKey(
                    request = ProvisioningRequest.PublicKey(provisioningData.provisionerPublicKey),
                    provisioningData = provisioningData
                )
            }
        }
        provisioningData.apply {
            onDevicePublicKeyReceived(key, configuration.publicKey is PublicKey.OobPublicKey)
            accumulate(key)
        }

        requestAuthentication(configuration.authMethod, provisioningData, mutex)?.also { action ->
            emit(ProvisioningState.AuthActionRequired(action))
            mutex.lock()
            if (action is AuthAction.DisplayNumber || action is AuthAction.DisplayAlphaNumeric) {
                awaitInputComplete()
            }
        }

        val confirmation = awaitConfirmation(
            request = ProvisioningRequest.Confirmation(
                confirmation = provisioningData.provisionerConfirmation
            )
        )
        provisioningData.onDeviceConfirmationReceived(confirmation = confirmation)

        val random = awaitRandom(
            request = ProvisioningRequest.Random(
                random = provisioningData.provisionerRandom
            )
        )
        provisioningData.onDeviceRandomReceived(random = random)

        require(provisioningData.checkIfConfirmationsMatch()) {
            logger?.e(LogCategory.PROVISIONING) { "Confirmations do not match" }
            throw ConfirmationFailed
        }

        val data = ProvisioningRequest.Data(provisioningData.encryptedProvisioningDataWithMic)
        logger?.v(LogCategory.PROVISIONING) { "Sending $data" }
        send(data)

        awaitComplete().also {
            val node = Node(
                uuid = unprovisionedDevice.uuid,
                deviceKey = provisioningData.deviceKey,
                unicastAddress = configuration.unicastAddress!!,
                elementCount = capabilities.numberOfElements,
                assignedNetworkKey = configuration.networkKey,
                security = provisioningData.security
            )
            meshNetwork.add(node)
        }
    }

    /**
     * Waits for the capabilities response from the device.
     *
     * @param invite           Provisioning invite to send.
     * @param provisioningData The provisioning data to accumulate the received PDUs.
     */
    private suspend fun awaitCapabilities(
        invite: ProvisioningRequest.Invite,
        provisioningData: ProvisioningData
    ): ProvisioningCapabilities {
        logger?.v(LogCategory.PROVISIONING) { "Sending $invite" }
        send(invite).also { provisioningData.accumulate(it) }
        val response = ProvisioningResponse.from(
            pdu = awaitBearerPdu().data
        ).apply {
            logger?.v(LogCategory.PROVISIONING) { "Received $this" }
            require(this is ProvisioningResponse.Capabilities) {
                logger?.e(LogCategory.PROVISIONING) {
                    "Provisioning failed with error: $InvalidPdu"
                }
                throw InvalidPdu
            }
            provisioningData.accumulate(pdu.sliceArray(1 until pdu.size))
            configuration = ProvisioningConfiguration(meshNetwork, capabilities)
            meshNetwork.localProvisioner?.let {
                // Calculates the unicast address automatically based ont he number of elements.
                if (configuration.unicastAddress == null) {
                    val count = capabilities.numberOfElements
                    configuration.unicastAddress =
                        meshNetwork.nextAvailableUnicastAddress(
                            elementCount = count,
                            provisioner = it
                        )?.apply {
                            suggestedUnicastAddress = this
                        }
                }
            }
            require(configuration.unicastAddress != null) {
                logger?.e(LogCategory.PROVISIONING) {
                    "Provisioning failed with error: $NoAddressAvailable"
                }
                throw NoAddressAvailable
            }
            suggestedUnicastAddress = configuration.unicastAddress
        } as ProvisioningResponse.Capabilities
        return response.capabilities
    }

    /**
     * Waits for the device's Public Key.
     *
     * @param request                Provisioner's Public Key.
     * @param provisioningData   Provisioning data to accumulate the received PDUs.
     */
    private suspend fun awaitProvisioneePublicKey(
        request: ProvisioningRequest.PublicKey,
        provisioningData: ProvisioningData
    ): ByteArray {
        logger?.v(LogCategory.PROVISIONING) { "Sending $request" }
        send(request).also { provisioningData.accumulate(it) }
        val response = ProvisioningResponse.from(pdu = awaitBearerPdu().data).also { response ->
            logger?.v(LogCategory.PROVISIONING) { "Received $response" }
            if (response is ProvisioningResponse.Failed) {
                logger?.e(LogCategory.PROVISIONING) {
                    "Provisioning failed with error: ${response.error}"
                }
                throw RemoteError(response.error)
            }
            require(response is ProvisioningResponse.PublicKey) {
                throw InvalidPdu
            }
            // Errata E1650 added an extra validation step to ensure the received public key is
            // the same as the provisioner's public key.
            require(!response.key.contentEquals(request.publicKey)) {
                throw InvalidPublicKey
            }
        } as ProvisioningResponse.PublicKey
        return response.key
    }

    /**
     * Waits for the user to provide the authentication value.
     *
     * @param method            Authentication method.
     * @param provisioningData  Provisioning data.
     * @param mutex             Mutex to unlock when the user has provided the authentication value.
     */
    private fun requestAuthentication(
        method: AuthenticationMethod,
        provisioningData: ProvisioningData,
        mutex: Mutex
    ): AuthAction? {
        val sizeInBytes = provisioningData.algorithm.length shr 3
        return when (method) {
            AuthenticationMethod.NoOob -> {
                provisioningData.onAuthValueReceived(ByteArray(sizeInBytes) { 0x00 })
                mutex.unlock()
                null
            }

            AuthenticationMethod.StaticOob -> AuthAction.ProvideStaticKey {
                require(it.size == sizeInBytes) {
                    throw InvalidOobValueFormat
                }
                provisioningData.onAuthValueReceived(it)
                mutex.unlock()
            }

            is AuthenticationMethod.OutputOob -> {
                when (method.action) {
                    OutputAction.OUTPUT_ALPHANUMERIC ->
                        AuthAction.ProvideAlphaNumeric(method.length) {
                            provisioningData.onAuthValueReceived(it.encodeToByteArray())
                            mutex.unlock()
                        }
                    // BLINK,BEEP,VIBRATE,OUTPUT_NUMERIC
                    else -> AuthAction.ProvideNumeric(method.length, method.action) {
                        val authValue = ByteArray(sizeInBytes - it.toInt()) + it.toByte()
                        provisioningData.onAuthValueReceived(authValue)
                        mutex.unlock()
                    }
                }
            }

            is AuthenticationMethod.InputOob -> {
                when (method.action) {
                    InputAction.INPUT_ALPHANUMERIC -> AuthAction.DisplayAlphaNumeric(
                        text = AuthenticationMethod.randomAlphaNumeric(
                            length = method.length.toInt()
                        )
                    )
                    // PUSH, TWIST, INPUT_NUMERIC
                    else -> AuthAction.DisplayNumber(
                        number = AuthenticationMethod.randomInt(
                            length = method.length.toInt()
                        ).toUInt(),
                        action = method.action
                    )
                }
            }
        }
    }

    /**
     * Waits for the input complete response from the device.
     */
    private suspend fun awaitInputComplete() = ProvisioningResponse.from(
        pdu = awaitBearerPdu().data
    ).apply {
        logger?.v(LogCategory.PROVISIONING) { "Received $this" }
        if (this is ProvisioningResponse.Failed) {
            logger?.e(LogCategory.PROVISIONING) {
                "Provisioning failed with error: ${error.debugDescription}"
            }
            throw RemoteError(error)
        }
    } as ProvisioningResponse.InputComplete

    /**
     * Waits for the confirmation response from the device.
     *
     * @param request  Confirmation value to send to the device.
     */
    private suspend fun awaitConfirmation(
        request: ProvisioningRequest.Confirmation,
    ): ByteArray {
        logger?.v(LogCategory.PROVISIONING) { "Sending $request" }
        send(request)
        val response = ProvisioningResponse.from(
            pdu = awaitBearerPdu().data
        ).apply {
            logger?.v(LogCategory.PROVISIONING) { "Received $this" }
            if (this is ProvisioningResponse.Failed) {
                logger?.e(LogCategory.PROVISIONING) {
                    "Provisioning failed with error: $error"
                }
                throw RemoteError(error)
            }
        } as ProvisioningResponse.Confirmation
        return response.confirmation
    }

    private suspend fun awaitRandom(request: ProvisioningRequest.Random): ByteArray {
        logger?.v(LogCategory.PROVISIONING) { "Sending $request" }
        send(request)
        return (ProvisioningResponse.from(
            pdu = awaitBearerPdu().data
        ).apply {
            logger?.v(LogCategory.PROVISIONING) { "Received $this" }
            if (this is ProvisioningResponse.Failed) {
                logger?.e(LogCategory.PROVISIONING) {
                    "Provisioning failed with error: $error"
                }
                throw RemoteError(error)
            }
        } as ProvisioningResponse.Random).random
    }

    /**
     * Waits for the provisioning random response from the device.
     */
    private suspend fun awaitRandom() = ProvisioningResponse.from(
        pdu = awaitBearerPdu().data
    ).apply {
        logger?.v(LogCategory.PROVISIONING) { "Received $this" }
        if (this is ProvisioningResponse.Failed) {
            logger?.e(LogCategory.PROVISIONING) {
                "Provisioning failed with error: $error"
            }
            throw RemoteError(error)
        }
    } as ProvisioningResponse.Random

    private suspend fun awaitComplete() = ProvisioningResponse.from(
        pdu = awaitBearerPdu().data
    ).apply {
        logger?.v(LogCategory.PROVISIONING) { "Received $this" }
        if (this is ProvisioningResponse.Failed) {
            logger?.e(LogCategory.PROVISIONING) {
                "Provisioning failed with error: $error"
            }
            throw RemoteError(error)
        }
    } as ProvisioningResponse.Complete

    private fun isPublicKeyValid(provisionerPublicKey: ByteArray, devicePublicKey: ByteArray) {
        require(!provisionerPublicKey.contentEquals(devicePublicKey)) {
            throw InvalidPublicKey
        }
    }

    /**
     * Checks if the unicast address valid.
     *
     * @param unicastAddress     Unicast address to be checked.
     * @param numberOfElements   Number of elements in the node.
     * @return true if the address is valid, false otherwise.
     */
    fun isUnicastAddressValid(unicastAddress: UnicastAddress, numberOfElements: Int) =
        meshNetwork.localProvisioner?.let { provisioner ->
            val range = UnicastRange(unicastAddress, numberOfElements)
            meshNetwork.isAddressRangeAvailable(range) && provisioner.hasAllocatedRange(range)
        } ?: false

    /**
     * Sends the provisioning request to the device over the Bearer specified in the
     * constructor.
     *
     * @param request Provisioning request to be sent.
     */
    private suspend fun send(request: ProvisioningRequest): ByteArray {
        bearer.send(request)
        return request.pdu.let { it.sliceArray(1 until it.size) }
    }


    private fun observeBearerStateChanges() {
        bearer.state.onEach {
            when (it) {
                is BearerEvent.OnBearerOpen -> bearer.open()
                is BearerEvent.OnBearerClosed -> bearer.close()
            }
        }.launchIn(scope)
    }


    /**
     * Awaits and returns the first Provisioning PDU received over the Bearer.
     *
     * @return First Provisioning PDU received over the Bearer.
     */
    private suspend fun awaitBearerPdu(): ReassembledPdu = bearer.pdus.first {
        it.type == PduType.PROVISIONING_PDU
    }
}