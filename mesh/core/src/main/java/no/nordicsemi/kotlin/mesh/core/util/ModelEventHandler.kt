@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.HasInitializer
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshResponse
import no.nordicsemi.kotlin.mesh.core.messages.TransactionMessage
import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber
import no.nordicsemi.kotlin.mesh.core.model.TransitionTime

typealias MessageComposer = () -> MeshMessage

/**
 * Defines a set of events that are handled by the [ModelEventHandler].
 */
sealed class ModelEvent {

    /**
     * Event used to notify when an acknowledged message has been received from the mesh network.
     *
     * @property model          Model that received the message.
     * @property request        Request that was sent.
     * @property source         Address of the Element from which the message was sent.
     * @property destination    Address to which the message was sent.
     * @property reply          Lambda function to be invoked to respond to the received message.
     */
    data class AcknowledgedMessageReceived(
        val model: Model,
        val request: AcknowledgedMeshMessage,
        val source: Address,
        val destination: MeshAddress,
        val reply: suspend (MeshResponse?) -> Unit
    ) : ModelEvent()

    /**
     * Event used to notify when an unacknowledged message has been received from the mesh network.
     *
     * @property model          Model that received the message.
     * @property message        Message that was received by the model.
     * @property source         Address of the Element from which the message was sent.
     * @property destination    Address to which the message was sent.
     */
    data class UnacknowledgedMessageReceived(
        val model: Model,
        val message: UnacknowledgedMeshMessage,
        val source: Address,
        val destination: MeshAddress
    ) : ModelEvent()

    /**
     * Event used to notify when a response to a given request was received by the mesh network.
     *
     * @property model          Model that received the message.
     * @property response       Response that was received by the model.
     * @property request        Request that was sent.
     * @property source         Address of the Element from which the message was sent.
     */
    data class ResponseReceived(
        val model: Model,
        val response: MeshResponse,
        val request: AcknowledgedMeshMessage,
        val source: Address
    ) : ModelEvent()
}

/**
 * Defines the functionality of a [Model] on the Local Node.
 *
 * This event handler is assigned to the models when setting up the
 * [MeshNetworkManager.localElements].
 *
 * The event handler must declare a map of mesh message type supported by this Model. Whenever a
 * message matching any of the declared op codes is received, and the model is bound to an
 * Application Key used to encrypt the message, one of the following events can be observed using
 * the [modelEventFlow] depending on the type of the message.
 *
 * @property messageTypes   Map of supported message types.
 * @property isSubscriptionSupported Defines the model supports subscription.
 * @property publicationMessageComposer A lambda function that returns a [MeshMessage] to be published
 * @property modelEventFlow A flow of model events.
 */
abstract class ModelEventHandler {

    abstract val messageTypes: Map<UInt, HasInitializer>

    abstract val isSubscriptionSupported: Boolean

    abstract val publicationMessageComposer: MessageComposer?

    internal val _modelEventFlow = MutableSharedFlow<ModelEvent>()
    val modelEventFlow: SharedFlow<ModelEvent>
        get() = _modelEventFlow

    internal val mutex = Mutex(locked = true)
    suspend fun publish(message: MeshMessage, manager: MeshNetworkManager) = manager.localElements
        .flatMap { element ->
            element.models
        }.firstOrNull { model ->
            model.eventHandler === this
        }?.let { model ->
            manager.publish(message, model)
        }

    suspend fun publish(manager: MeshNetworkManager) = publicationMessageComposer?.let { composer ->
        publish(message = composer(), manager = manager)
    }

    internal fun onMeshMessageReceived() {

    }
}

abstract class SceneServerModelEventHandler : ModelEventHandler() {

    abstract fun networkDidExitStoredWithSceneState()
}

abstract class StoredWithSceneModelDelegate : ModelEventHandler() {
    abstract fun store(scene: SceneNumber)

    abstract fun recall(scene: SceneNumber, transitionTime: TransitionTime?, delay: UByte)
    fun networkDidExitStoredWithSceneState(network: MeshNetwork) {
        network.localElements
            .flatMap { it.models }
            .map {
                it.eventHandler as SceneServerModelEventHandler
            }.forEach { it.networkDidExitStoredWithSceneState() }
    }
}

/**
 * The Transaction is used for Transaction Messages, for example.
 *
 * @param source        Source address of the transaction.
 * @param destination   Destination address of the transaction.
 * @param tid           Transaction ID of the message.
 * @param timestamp     Timestamp of the last transaction message sent.
 */
private data class Transaction(
    val source: Address,
    val destination: MeshAddress,
    val tid: UByte,
    val timestamp: Instant
)

class TransactionHelper {

    private var mutex = Mutex(true)

    private var lastTransactions = mutableMapOf<Address, Transaction>()

    /**
     * Checks whether the given Transaction Message was sent as a new transaction or is part of the
     * previously started transaction.
     *
     * @param message          Received message.
     * @param source           Source Unicast Address.
     * @param destination      destination address.
     * @return True, if the message starts a new transaction or false otherwise.
     */
    suspend fun isNewTransaction(
        message: TransactionMessage,
        source: Address,
        destination: MeshAddress
    ) = mutex.withLock {
        val lastTransaction = lastTransactions[source]
        (lastTransaction == null) || (lastTransaction.source != source) ||
                (lastTransaction.destination != destination) ||
                message.isNewTransaction(
                    previousTid = lastTransaction.tid,
                    timestamp = lastTransaction.timestamp
                )
    }

    /**
     * Checks whether the given Transaction Message was sent as a continuation of the previous
     * transaction.
     *
     * @param message          Received message.
     * @param source           Source Unicast Address.
     * @param destination      destination address.
     * @return True, if the message continues the last transaction; false otherwise.
     */
    suspend fun isTransactionContinuation(
        message: TransactionMessage,
        source: Address,
        destination: MeshAddress
    ) = !isNewTransaction(message, source, destination)
}
