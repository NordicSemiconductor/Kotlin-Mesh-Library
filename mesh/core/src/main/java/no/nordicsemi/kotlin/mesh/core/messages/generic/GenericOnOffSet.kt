package no.nordicsemi.kotlin.mesh.core.messages.generic

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.GenericMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.TransactionMessage
import no.nordicsemi.kotlin.mesh.core.messages.TransitionMessage
import no.nordicsemi.kotlin.mesh.core.model.TransitionTime

/**
 * This message is used to set the current status of a GenericOnOffServer model. Response received
 * to the message is [GenericOnOffStatus].
 *
 * @property tid              Defines a unique Transaction identifier that each message must have.
 * @property transitionTime   Defines the time interval an element will take to transition to the
 *                            target state from the present state.
 * @property delay            Message execution delay in 5 millisecond steps.
 * @property on               Defines the desired state of the Generic OnOff Server.
 */
class GenericOnOffSet(
    override var tid: UByte?,
    override val transitionTime: TransitionTime?,
    override val delay: UByte?,
    val on: Boolean
) : AcknowledgedMeshMessage, TransactionMessage, TransitionMessage {
    override val opCode = Initializer.opCode
    override val responseOpCode = GenericOnOffStatus.opCode
    override val parameters: ByteArray
        get() {
            val data = byteArrayOf(if (on) 0x01 else 0x00)
            return when (transitionTime != null && delay != null) {
                true -> data + byteArrayOf(
                    transitionTime.rawValue.toByte(),
                    delay.toByte()
                )

                else -> data
            }
        }

    /**
     * Convenience constructor to create a GenericOnOffSet message without any transition time,
     * tid or delay.
     *
     * @param on Desired state of Generic OnOff Server.
     */
    @Suppress("unused")
    constructor(on: Boolean) : this(on = on, tid = null, transitionTime = null, delay = null)

    companion object Initializer : GenericMessageInitializer {
        override val opCode = 0x8202u

        override fun init(parameters: ByteArray?) = parameters?.takeIf {
            it.size == 4 || it.size == 2
        }?.let { params ->
            GenericOnOffSet(
                on = params[0] == 0x01.toByte(),
                tid = params[1].toUByte(),
                transitionTime = if (params.size == 4)
                    TransitionTime(rawValue = params[2].toUByte())
                else null,
                delay = if (params.size == 4) params[3].toUByte() else null
            )
        }
    }
}