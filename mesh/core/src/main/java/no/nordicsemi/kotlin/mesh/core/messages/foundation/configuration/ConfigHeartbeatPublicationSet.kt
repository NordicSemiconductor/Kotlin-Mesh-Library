@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration

import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.ConfigMessageInitializer
import no.nordicsemi.kotlin.mesh.core.messages.ConfigNetKeyMessage
import no.nordicsemi.kotlin.mesh.core.messages.RemainingHeartbeatPublicationCount
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.Feature
import no.nordicsemi.kotlin.mesh.core.model.Features
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatPublicationDestination
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.UnassignedAddress
import no.nordicsemi.kotlin.mesh.core.model.isValidKeyIndex
import no.nordicsemi.kotlin.mesh.core.model.toFeatures
import no.nordicsemi.kotlin.mesh.core.model.toUShort
import no.nordicsemi.kotlin.mesh.core.util.Utils.toByteArray
import no.nordicsemi.kotlin.mesh.core.util.Utils.toUShort
import kotlin.math.pow

typealias CountLog = UByte

/**
 * This message can be sent to set Heartbeat Publication of a given element.
 *
 * @param destination Destination address of the Heartbeat Publication.
 * @param countLog    Number of Heartbeat messages remaining to be sent.
 * @param periodLog   Period between publication of two consecutive periodic heartbeat transport
 *                    control messages.
 * @param ttl         TTL value used when sending Heartbeat messages.
 * @param features    Features that trigger Heartbeat messages.
 * @constructor Creates a ConfigHeartbeatPublicationSet message.
 */
class ConfigHeartbeatPublicationSet(
    val destination: Address = UnassignedAddress.address,
    val countLog: CountLog,
    val periodLog: UByte,
    val ttl: UByte,
    val features: Array<Feature>,
    override val networkKeyIndex: KeyIndex
) : AcknowledgedConfigMessage, ConfigNetKeyMessage {
    override val opCode = Initializer.opCode
    override val parameters: ByteArray
        get() = destination.toByteArray() +
                countLog.toByte() +
                periodLog.toByte() +
                ttl.toByte() +
                features.toUShort().toByteArray() +
                networkKeyIndex.toByteArray()

    val count: RemainingHeartbeatPublicationCount
        get() = when {
            countLog == 0x00.toUByte() -> RemainingHeartbeatPublicationCount.Disabled
            countLog == 0xFF.toUByte() -> RemainingHeartbeatPublicationCount.Indefinitely
            countLog == 0x01.toUByte() ||
                    countLog == 0x02.toUByte() ->
                RemainingHeartbeatPublicationCount.Exact(countLog.toUShort())

            countLog == 0x11.toUByte() -> RemainingHeartbeatPublicationCount.Range(
                low = 0x8001.toUShort(),
                high = 0xFFFE.toUShort()
            )

            countLog >= 0x03.toUByte() && countLog <= 0x10.toUByte() -> {
                val lowerBound = ((2.0.pow(countLog.toDouble() - 2.0)) + 1.0).toUInt().toUShort()
                val upperBound = (2.0.pow(countLog.toDouble() - 1.0)).toUInt().toUShort()
                RemainingHeartbeatPublicationCount.Range(
                    low = lowerBound,
                    high = upperBound
                )
            }

            else -> RemainingHeartbeatPublicationCount.Invalid(countLog)
        }
    val period: UShort
        get() = when {
            periodLog == 0x00.toUByte() -> 0x0000u //  Period heartbeat messages disabled.
            periodLog > 0x11.toUByte() -> 0xFFFFu //  Period heartbeat messages disabled.
            else -> (2.0.pow(periodLog.toDouble() - 1.0)).toUInt().toUShort()
        }

    override val responseOpCode: UInt = ConfigHeartbeatPublicationStatus.opCode

    val isPublicationEnabled: Boolean
        get() = destination != UnassignedAddress.address

    val enablePeriodPublication: Boolean
        get() = isPublicationEnabled && periodLog > 0x00.toUByte()

    val enablesFeatureTriggeredPublication : Boolean
        get() = isPublicationEnabled && features.toFeatures().rawValue > 0x0000u

    companion object Initializer : ConfigMessageInitializer {
        override val opCode: UInt = 0x8039u

        override fun init(payload: ByteArray) = if (payload.size == 9)
            ConfigHeartbeatPublicationSet(
                destination = payload.toUShort(offset = 0),
                countLog = payload[2].toUByte(),
                periodLog = payload[3].toUByte(),
                ttl = payload[4].toUByte(),
                features = Features(rawValue = payload.toUShort(offset = 5)).toArray(),
                networkKeyIndex = payload.toUShort(offset = 7)
            ) else null

        /**
         * Creates a ConfigurationHeartbeatPublicationSet message.
         *
         * @param countLog       Number of Heartbeat messages to be sent.
         * @param periodLog      Period for sending Heartbeat messages. This field is the interval
         *                       used for sending messages.
         * @param destination    Destination address for Heartbeat messages. Address shall be a
         *                       [HeartbeatPublicationDestination].
         * @param ttl            TTL to be used when sending Heartbeat messages.
         * @param networkKey     Network Key that will be used send Heartbeat messages.
         * @param features       Features that trigger Heartbeat messages when changed.
         * @constructor Creates a ConfigurationHeartbeatPublicationSet message.
         * @return ConfigurationHeartbeatPublicationSet message or null if the provided values are
         *         invalid.
         */
        fun init(
            countLog: UByte,
            periodLog: UByte,
            destination: Address,
            ttl: UByte,
            networkKey: NetworkKey,
            features: Features
        ): ConfigHeartbeatPublicationSet? {

            require(HeartbeatPublicationDestination.isValid(destination)) { return null }
            require(countLog <= 0x11.toUByte() || countLog == 0xFF.toUByte()) { return null }
            require(periodLog <= 0x11.toUByte()) { return null }
            require(ttl <= 0x7F.toUByte()) { return null }
            require(networkKey.index.isValidKeyIndex()) { return null }

            return ConfigHeartbeatPublicationSet(
                countLog = countLog,
                periodLog = periodLog,
                ttl = ttl,
                features = features.toArray(),
                networkKeyIndex = networkKey.index
            )
        }
    }
}