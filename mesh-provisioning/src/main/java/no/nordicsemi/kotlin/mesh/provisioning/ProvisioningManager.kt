@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.provisioning

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import no.nordicsemi.kotlin.mesh.bearer.*
import no.nordicsemi.kotlin.mesh.core.exception.NoLocalProvisioner
import no.nordicsemi.kotlin.mesh.core.exception.NoUnicastRangeAllocated
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import no.nordicsemi.kotlin.mesh.logger.Logger
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastRange
import no.nordicsemi.kotlin.mesh.crypto.Algorithm
import no.nordicsemi.kotlin.mesh.crypto.Algorithm.Companion.strongest
import no.nordicsemi.kotlin.mesh.provisioning.bearer.ProvisioningBearer

/**
 * Provisioning manager is responsible for provisioning new devices to a mesh network.
 *
 * @property unprovisionedDevice          Unprovisioned device to be provisioned.
 * @property meshNetwork                  Mesh network to which the device will be provisioned.
 */
class ProvisioningManager(
    private val unprovisionedDevice: UnprovisionedDevice,
    private val meshNetwork: MeshNetwork,
    private val bearer: ProvisioningBearer
) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var authenticationMethod: AuthenticationMethod
    var logger: Logger? = null
    var unicastAddress: UnicastAddress? = null
        private set
    var suggestedUnicastAddress: UnicastAddress? = null
        private set

    init {
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
     *
     */
    @Throws(ProvisioningError::class, BearerError::class)
    fun provision(attentionTimer: UByte) = flow<ProvisioningState> {
        var networkKey: NetworkKey = meshNetwork.networkKeys.first()
        var unicastAddress: UnicastAddress
        var algorithm: Algorithm
        var publicKey: PublicKey
        var authMethod: AuthenticationMethod

        meshNetwork.localProvisioner?.let {
            require(it.allocatedUnicastRanges.isNotEmpty()) {
                logger?.e(LogCategory.PROVISIONING) { "No unicast ranges allocated" }
                throw NoUnicastRangeAllocated
            }
        } ?: run {
            logger?.e(LogCategory.PROVISIONING) { "No local provisioner" }
            throw NoLocalProvisioner
        }

        // Is there bearer open?
        require(bearer.isOpen) {
            logger?.e(LogCategory.PROVISIONING) { "Bearer closed" }
            throw BearerError.BearerClosed
        }
        // Emit the current state.
        emit(ProvisioningState.RequestingCapabilities)

        // Initialize Provisioning data.
        val provisioningData = ProvisioningData()
        val provisioningInvite = ProvisioningRequest.Invite(attentionTimer)
        logger?.w(LogCategory.PROVISIONING) { "Sending $provisioningInvite" }

        //Sends the provisioning invite
        send(provisioningInvite, provisioningData)

        val capabilities = awaitCapabilities().capabilities.apply {
            // Lets init based on the capabilities
            unicastAddress = meshNetwork.run {
                nextAvailableUnicastAddress(numberOfElements, localProvisioner!!)!!
            }
            algorithm = algorithms.strongest()
            publicKey = if (publicKeyType.isNotEmpty()) {
                PublicKey.OobPublicKey(ByteArray(16) { 0x00 })
            } else PublicKey.NoOobPublicKey
            authMethod = supportedAuthenticationMethods.first()
        }

        // We use a mutex here to wait for the user to either start or cancel the provisioning.
        val mutex = Mutex(true)
        // Emit to the user that the capabilities have been received.
        emit(
            value = ProvisioningState.CapabilitiesReceived(
                capabilities = capabilities,
                start = { address, netKey, alg, pubKey, method ->
                    unicastAddress = address
                    networkKey = netKey
                    algorithm = alg
                    publicKey = pubKey
                    authMethod = method
                    mutex.unlock()
                },
                cancel = {
                    mutex.unlock()
                }
            )
        )
        mutex.lock()

        // TODO Can the Unprovisioned Device be provisioned by this manager?

        // Is the Unicast address valid?
        require(isUnicastAddressValid(unicastAddress, capabilities.numberOfElements)) {
            logger?.e(LogCategory.PROVISIONING) { "Unicast address is not valid" }
            throw ProvisioningError.NoAddressAvailable
        }

        // Try generating Private and Public keys. This may fail if the given algorithm is not
        // supported.
        provisioningData.generateKeys(algorithm)
        // If the device's Public Key was obtained OOB, we are now ready to calculate the device's
        // Shared Secret.
        if (publicKey is PublicKey.OobPublicKey) {
            runCatching {
                provisioningData.onDevicePublicKeyReceived(
                    key = (publicKey as PublicKey.OobPublicKey).key,
                    usingOob = true
                )
            }.onFailure { throw ProvisioningError.InvalidPublicKey }
        }

        emit(ProvisioningState.Provisioning)
        provisioningData.prepare(networkKey, meshNetwork.ivIndex, unicastAddress)

        val provisioningStart = ProvisioningRequest.Start(
            algorithm,
            publicKey.method,
            authMethod,
        )
        logger?.v(LogCategory.PROVISIONING) { "Sending $provisioningStart" }
        send(provisioningStart, provisioningData)
        this@ProvisioningManager.authenticationMethod = authMethod

        val provisioningPublicKey = ProvisioningRequest.PublicKey(
            provisioningData.provisionerPublicKey
        )
        logger?.v(LogCategory.PROVISIONING) { "Sending $provisioningPublicKey" }
        send(provisioningPublicKey, provisioningData)

        if (publicKey is PublicKey.OobPublicKey) {
            provisioningData.accumulate((publicKey as PublicKey.OobPublicKey).key)
        } else {
            val provisioneePublicKey = awaitProvisioneePublicKey(provisioningData).key
            // Errata E1650 added an extra validation step to ensure the received public key is the same
            // as the provisioner's public key.
            require(provisioneePublicKey.contentEquals(provisioningData.provisionerPublicKey)) {
                logger?.e(LogCategory.PROVISIONING) { "Public keys do not match" }
                throw ProvisioningError.InvalidPublicKey
            }
            provisioningData.onDevicePublicKeyReceived(key = provisioneePublicKey, usingOob = false)
        }

        requestAuthentication(authMethod, provisioningData, mutex)?.also { action ->
            emit(ProvisioningState.AuthActionRequired(action))
            mutex.lock()
            if (action is AuthAction.DisplayNumber || action is AuthAction.DisplayAlphaNumeric) {
                awaitInputComplete()
            }
        }

        ProvisioningRequest.Confirmation(
            confirmation = provisioningData.provisionerConfirmation
        ).also { confirmation ->
            logger?.v(LogCategory.PROVISIONING) { "Sending $confirmation" }
            send(confirmation, provisioningData)
        }

        val confirmation = awaitConfirmation().confirmation
        provisioningData.onDeviceConfirmationReceived(confirmation = confirmation)

        ProvisioningRequest.Random(
            random = provisioningData.provisionerRandom
        ).also { random ->
            logger?.v(LogCategory.PROVISIONING) { "Sending $random" }
            send(random, provisioningData)
        }

        provisioningData.onDeviceRandomReceived(awaitRandom().random)

        require(provisioningData.checkIfConfirmationsMatch()) {
            logger?.e(LogCategory.PROVISIONING) { "Confirmations do not match" }
            throw ProvisioningError.ConfirmationFailed
        }

        val data = ProvisioningRequest.Data(provisioningData.encryptedProvisioningDataWithMic)
        logger?.v(LogCategory.PROVISIONING) { "Sending $data" }
        send(data, provisioningData)

        awaitComplete()
    }

    /**
     * Waits for the capabilities response from the device.
     */
    @Throws(ProvisioningError.InvalidPdu::class, ProvisioningError.NoAddressAvailable::class)
    private suspend fun awaitCapabilities() = ProvisioningResponse.from(
        pdu = awaitBearerPdu().data
    ).apply {
        require(this is ProvisioningResponse.Capabilities) {
            logger?.e(LogCategory.PROVISIONING) {
                "Provisioning failed with error: ${ProvisioningError.InvalidPdu}"
            }
            throw ProvisioningError.InvalidPdu
        }

        meshNetwork.localProvisioner?.let {
            // Calculates the unicast address automatically based ont he number of elements.
            if (unicastAddress == null) {
                val count = capabilities.numberOfElements
                unicastAddress = meshNetwork.nextAvailableUnicastAddress(count, it)?.apply {
                    suggestedUnicastAddress = this
                }
            }
        }
        require(unicastAddress != null) {
            logger?.e(LogCategory.PROVISIONING) {
                "Provisioning failed with error: ${ProvisioningError.NoAddressAvailable}"
            }
            throw ProvisioningError.NoAddressAvailable
        }
    } as ProvisioningResponse.Capabilities

    /**
     * Waits for the provisionee's public key. This is called only if the provisionee's public key
     * was not obtained via OOB.
     */
    private suspend fun awaitProvisioneePublicKey(provisioningData: ProvisioningData) =
        ProvisioningResponse.from(
            pdu = awaitBearerPdu().data
        ).apply {
            require(this is ProvisioningResponse.PublicKey) {
                throw ProvisioningError.InvalidPdu
            }
            require(key.contentEquals(provisioningData.provisionerPublicKey)) {
                throw ProvisioningError.InvalidPdu
            }

        } as ProvisioningResponse.PublicKey

    /**
     * Waits for the user to provide the authentication value.
     *
     * @param method             Authentication method.
     * @param provisioningData   Provisioning data.
     * @param mutex              Mutex to unlock when the user has provided the authentication value.
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
                    throw ProvisioningError.InvalidOobValueFormat
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
        require(this is ProvisioningResponse.InputComplete) {
            throw ProvisioningError.InvalidPdu
        }
    } as ProvisioningResponse.InputComplete

    /**
     * Waits for the confirmation response from the device.
     */
    private suspend fun awaitConfirmation() = ProvisioningResponse.from(
        pdu = awaitBearerPdu().data
    ).apply {
        require(this is ProvisioningResponse.Confirmation) {
            throw ProvisioningError.InvalidPdu
        }
    } as ProvisioningResponse.Confirmation

    /**
     * Waits for the provisioning random response from the device.
     */
    private suspend fun awaitRandom() = ProvisioningResponse.from(
        pdu = awaitBearerPdu().data
    ).apply {
        require(this is ProvisioningResponse.Random) {
            throw ProvisioningError.InvalidPdu
        }
    } as ProvisioningResponse.Random

    private suspend fun awaitComplete() = ProvisioningResponse.from(
        pdu = awaitBearerPdu().data
    ).apply {
        require(this is ProvisioningResponse.Failed) {
            throw ProvisioningError.InvalidPdu
        }
    } as ProvisioningResponse.Complete

    /**
     * Checks if the unicast address valid.
     *
     * @param unicastAddress     Unicast address to be checked.
     * @param numberOfElements   Number of elements in the node.
     * @return true if the address is valid, false otherwise.
     */
    private fun isUnicastAddressValid(unicastAddress: UnicastAddress, numberOfElements: Int) =
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

    private fun observeBearerStateChanges() {
        bearer.state.onEach {
            when (it) {
                is BearerEvent.OnBearerOpen -> {
                    bearer.open()
                }

                is BearerEvent.OnBearerClosed -> {
                    bearer.close()
                }
            }
        }.launchIn(scope)
    }


    /**
     * Awaits and returns the first Provisioning PDU received over the Bearer.
     *
     * @return First Provisioning PDU received over the Bearer.
     */
    private suspend fun awaitBearerPdu(): BearerPdu = bearer.pdus.first {
        it.type == PduType.PROVISIONING_PDU
    }
}