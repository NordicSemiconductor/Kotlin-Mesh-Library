@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.data.getUShort
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageStatus
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.ConfigStatusMessage
import no.nordicsemi.kotlin.mesh.core.messages.RemainingHeartbeatPublicationCount
import no.nordicsemi.kotlin.mesh.core.model.Feature
import no.nordicsemi.kotlin.mesh.core.model.Features
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatPublication
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatPublicationDestination
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.UnassignedAddress
import no.nordicsemi.kotlin.mesh.core.model.toUShort
import no.nordicsemi.kotlin.mesh.core.util.CountLog
import no.nordicsemi.kotlin.mesh.core.util.toRemainingPublicationCount

/**
 * This message contains the Heartbeat Publication status of an element. This is sent in response to
 * a [ConfigHeartbeatPublicationGet] or [ConfigHeartbeatPublicationSet] message.
 *
 * @param destination Destination address of the Heartbeat Publication.
 * @param countLog    Number of Heartbeat messages remaining to be sent.
 * @param periodLog   Period between publication of two consecutive periodic heartbeat transport
 *                    control messages.
 * @param ttl         TTL value used when sending Heartbeat messages.
 * @param features    Features that trigger Heartbeat messages.
 * @constructor Creates a ConfigHeartbeatPublicationStatus message.
 */
class ConfigHeartbeatPublicationStatus(
    val destination: HeartbeatPublicationDestination = UnassignedAddress,
    val countLog: CountLog = 0x00u,
    val periodLog: UByte = 0x00u,
    val ttl: UByte = 0x00u,
    val features: Array<Feature> = emptyArray(),
    val networkKeyIndex: KeyIndex = 0u,
    override val status: ConfigMessageStatus = ConfigMessageStatus.SUCCESS
) : ConfigResponse, ConfigStatusMessage {
    override val opCode: UInt = Initializer.opCode

    override val parameters: ByteArray
        get() = byteArrayOf(status.value.toByte()) +
                destination.address.toByteArray() +
                countLog.toByte() +
                periodLog.toByte() +
                ttl.toByte() +
                features.toUShort().toByteArray() +
                networkKeyIndex.toByteArray()

    val count: RemainingHeartbeatPublicationCount
        get() = countLog.toRemainingPublicationCount()

    override fun toString() = "ConfigHeartbeatPublicationStatus(destination: $destination, " +
            "countLog: $countLog, periodLog: $periodLog, ttl: $ttl, features: {${
                features.joinToString(separator = ", ") { it.toString() }
            }}, " + "networkKeyIndex: $networkKeyIndex, status: $status)"

    companion object Initializer : ConfigMessageInitializer {
        override val opCode: UInt = 0x06u

        override fun init(parameters: ByteArray?) = parameters?.takeIf {
            it.size == 10
        }?.let {
            ConfigMessageStatus.from(parameters[0].toUByte())?.let {
                ConfigHeartbeatPublicationStatus(
                    destination = MeshAddress.create(
                        parameters.getUShort(1)
                    ) as HeartbeatPublicationDestination,
                    countLog = parameters[3].toUByte(),
                    periodLog = parameters[4].toUByte(),
                    ttl = parameters[5].toUByte(),
                    features = Features(parameters.getUShort(6)).toArray(),
                    networkKeyIndex = parameters.getUShort(8),
                    status = it
                )
            }
        }

        /**
         * Creates a ConfigHeartbeatPublicationStatus message.
         *
         * @param publication Heartbeat publication settings.
         * @return ConfigHeartbeatPublicationStatus message.
         */
        fun init(publication: HeartbeatPublication?) {
            if (publication == null) {
                ConfigHeartbeatPublicationStatus()
            } else {
                ConfigHeartbeatPublicationStatus(
                    destination = publication.address,
                    countLog = publication.countLog,
                    periodLog = publication.periodLog,
                    ttl = publication.ttl,
                    features = publication.features,
                    networkKeyIndex = publication.index
                )
            }
        }

        /**
         * Creates a ConfigHeartbeatPublicationStatus message.
         *
         * @param request       ConfigHeartbeatPublicationSet message.
         * @param statusMessage Status of the message.
         * @return ConfigHeartbeatPublicationStatus message.
         */
        fun init(
            request: ConfigHeartbeatPublicationSet,
            statusMessage: ConfigMessageStatus
        ): ConfigHeartbeatPublicationStatus {
            return ConfigHeartbeatPublicationStatus(
                destination = MeshAddress.create(request.destination) as HeartbeatPublicationDestination,
                countLog = request.countLog,
                periodLog = request.periodLog,
                ttl = request.ttl,
                features = request.features,
                networkKeyIndex = request.networkKeyIndex,
                status = statusMessage
            )
        }

        /**
         * Creates a ConfigHeartbeatPublicationStatus message.
         *
         * @param request ConfigHeartbeatPublicationSet message.
         * @return ConfigHeartbeatPublicationStatus message.
         */
        fun init(request: ConfigHeartbeatPublicationSet) =
            init(request = request, statusMessage = ConfigMessageStatus.SUCCESS)

    }
}