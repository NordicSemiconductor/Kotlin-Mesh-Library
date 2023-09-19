@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageStatus
import no.nordicsemi.kotlin.mesh.core.messages.ConfigResponse
import no.nordicsemi.kotlin.mesh.core.messages.ConfigStatusMessage
import no.nordicsemi.kotlin.mesh.core.messages.RemainingHeartbeatPublicationCount
import no.nordicsemi.kotlin.mesh.core.messages.RemainingHeartbeatPublicationCount.Companion.toRemainingPublicationCount
import no.nordicsemi.kotlin.mesh.core.model.Feature
import no.nordicsemi.kotlin.mesh.core.model.Features
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatPublication
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatPublicationDestination
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.UnassignedAddress
import no.nordicsemi.kotlin.mesh.core.model.toUShort
import no.nordicsemi.kotlin.mesh.core.util.Utils.toByteArray
import no.nordicsemi.kotlin.mesh.core.util.Utils.toUShort

data class ConfigHeartbeatPublicationStatus(
    val destination: HeartbeatPublicationDestination = UnassignedAddress,
    val countLog: CountLog = 0x00.toUByte(),
    val periodLog: UByte = 0x00.toUByte(),
    val ttl: UByte = 0x00.toUByte(),
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

    companion object Initializer : ConfigMessageInitializer {
        override val opCode: UInt = 0x803Cu

        override fun init(payload: ByteArray): ConfigHeartbeatPublicationStatus? {
            require(payload.size == 10) { return null }
            require(payload[0].toUByte() == ConfigMessageStatus.SUCCESS.value) { return null }
            return ConfigHeartbeatPublicationStatus(
                destination = MeshAddress.create(
                    payload.toUShort(1)
                ) as HeartbeatPublicationDestination,
                countLog = payload[3].toUByte(),
                periodLog = payload[4].toUByte(),
                ttl = payload[5].toUByte(),
                features = Features(payload.toUShort(6)).toArray(),
                networkKeyIndex = payload.toUShort(8)
            )
        }

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

        fun init(request: ConfigHeartbeatPublicationSet) =
            init(request = request, statusMessage = ConfigMessageStatus.SUCCESS)

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConfigHeartbeatPublicationStatus

        if (destination != other.destination) return false
        if (countLog != other.countLog) return false
        if (periodLog != other.periodLog) return false
        if (ttl != other.ttl) return false
        if (!features.contentEquals(other.features)) return false
        if (networkKeyIndex != other.networkKeyIndex) return false
        if (status != other.status) return false
        if (opCode != other.opCode) return false
        if (count != other.count) return false

        return true
    }

    override fun hashCode(): Int {
        var result = destination.hashCode()
        result = 31 * result + countLog.hashCode()
        result = 31 * result + periodLog.hashCode()
        result = 31 * result + ttl.hashCode()
        result = 31 * result + features.contentHashCode()
        result = 31 * result + networkKeyIndex.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + opCode.hashCode()
        result = 31 * result + count.hashCode()
        return result
    }
}