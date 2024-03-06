@file:Suppress("MemberVisibilityCanBePrivate", "unused", "PropertyName")

package no.nordicsemi.kotlin.mesh.core.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.layers.MessageHandle
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
        val reply: suspend (MeshResponse) -> Unit
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
 * Application Key used to encrypt the message. Upon receiving a message, the [handle] with
 * the [ModelEvent] will be invoked.
 *
 *
 * @property messageTypes                 Map of supported message types.
 * @property isSubscriptionSupported      Defines the model supports subscription.
 * @property publicationMessageComposer   Lambda function that returns a [MeshMessage] to be
 *                                        published.
 */
abstract class ModelEventHandler {

    abstract val meshNetwork: MeshNetwork

    abstract val messageTypes: Map<UInt, HasInitializer>

    abstract val isSubscriptionSupported: Boolean

    abstract val publicationMessageComposer: MessageComposer?

    internal val mutex = Mutex(locked = true)

    /**
     * Publishes a single message given as a parameter using the Publish information set in the
     * underlying model.
     *
     * @param message Message to be published.
     * @param manager Mesh network manager.
     * @return a nullable [MessageHandle] that can be used to cancel the message.
     */
    suspend fun publish(message: MeshMessage, manager: MeshNetworkManager) = manager.localElements
        .flatMap { it.models }
        .firstOrNull { it.eventHandler === this }
        ?.let { manager.publish(message, it) }

    /**
     * Publishes a single message created by Model;s message composer using the Publish information
     * set in the underlying model.
     *
     * @param manager Mesh network manager.
     * @return a nullable [MessageHandle] that can be used to cancel the message.
     */
    suspend fun publish(manager: MeshNetworkManager) = publicationMessageComposer?.let { composer ->
        publish(message = composer(), manager = manager)
    }

    /**
     * Invoked when a model event is published.
     *
     * @param event Model event.
     * @throws MeshResponse Exception if the message is not supported by the model.
     */
    abstract fun handle(event: ModelEvent)
}

/**
 * This event handler should be used when defining Scene Server model. In addition to handling
 * messages, the Scene Server delegate should also clear the current whenever
 * [StoredWithSceneModelDelegate.store] and
 * [StoredWithSceneModelDelegate.recall] are called.
 *
 * Whenever the state changes due toa ny other reason than receiving a Scene Recall message, the
 * delegate should call [StoredWithSceneModelDelegate.networkDidExitStoredWithSceneState] to clear
 * the current scene.
 */
abstract class SceneServerModelEventHandler : ModelEventHandler() {

    /**
     * This method should be called whenever the State of the model changes for any reason other
     * than receiving Scene Recall message.
     *
     * The invocation of this method should eb consumed by the Scene Server model, which should
     * clear the Current Scene.
     */
    abstract fun networkDidExitStoredWithSceneState()
}

abstract class StoredWithSceneModelDelegate : ModelEventHandler() {

    /**
     * This method should store the current States of the Model and associate them with the given
     * Scene number.
     *
     * @param scene Scene number.
     */
    abstract fun store(scene: SceneNumber)

    /**
     * This method should recall the States of the Model that were stored with the given Scene
     * number.
     *
     * @param scene          Scene number.
     * @param transitionTime Identifies that an element will take to transition to the target state
     *                       from the present state.
     * @param delay          Message execution delay in 5 millisecond steps.
     */
    abstract fun recall(scene: SceneNumber, transitionTime: TransitionTime?, delay: UByte)

    /**
     * This method should be called whenever the State of a local model changes due to a different
     * action than recalling a scene.
     *
     * This method will invalidate the current scene state in Scene Server model.
     *
     * @param network Mesh network to which the model belongs to.
     */
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
