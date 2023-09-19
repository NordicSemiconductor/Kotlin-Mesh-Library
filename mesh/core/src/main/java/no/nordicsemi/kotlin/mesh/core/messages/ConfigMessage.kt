@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.messages

import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.CountLog
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.ModelId
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId
import java.util.UUID
import kotlin.experimental.and
import kotlin.math.pow


/**
 * A base interface for all Configuration messages.
 *
 * Configuration messages are used to configure Nodes. They are sent between Configuration Client
 * model on the Configuration Manager and Configuration Server model on the device, which is being
 * configured. All Config messages are encrypted using target Node's Device Key.
 */
interface ConfigMessage : MeshMessage {

    companion object ConfigMessageUtils {

        /**
         * Encodes given list of Key Indexes into a Data. As each Key Index is 12 bits long, a pair
         * of them can fit 3 bytes. This method ensures that they are packed in compliance to the
         * Bluetooth Mesh specification.
         *
         * @param limit    Maximum number of Key Indexes to encode.
         * @param indexes  An array of 12-bit Key Indexes.
         * @returns Key Indexes encoded to a Data.
         */
        fun encode(limit: Int = 10000, indexes: List<KeyIndex>): ByteArray = when {
            limit == 0 || indexes.isEmpty() -> byteArrayOf()
            limit == 1 || indexes.size == 1 -> {
                // Encode a single Key Index into 2 bytes.
                indexes.first().toShort().toLittleEndian()
            }

            else -> {
                // Encode a pair of Key Indexes into 3 bytes.
                val first = indexes.first()
                val second = indexes.drop(1).first()
                val pair = (first.toInt() shl 12) or second.toInt()
                val encodedPair = pair.toShort().toLittleEndian().dropLast(1).toByteArray()
                val remainingIndexes = indexes.drop(2)
                val encodedRemaining = encode(limit - 2, remainingIndexes)
                (encodedPair + encodedRemaining)
            }
        }

        // TODO this needs to be moved to a utils module
        fun Short.toLittleEndian(): ByteArray = byteArrayOf(toByte(), (toInt() ushr 8).toByte())

        /**
         * Decodes number of Key Indexes from the given Data from the given offset. This will decode
         * as many Indexes as possible, until the end of data is reached.
         *
         * @param limit:  Maximum number of Key Indexes to decode.
         * @param data:   The data from where the indexes should be read.
         * @param offset: The offset from where to read the indexes.
         * @returns Decoded Key Indexes.
         */
        fun decode(limit: Int = 10000, data: ByteArray, offset: Int): Array<KeyIndex> = when {
            limit < 0 && data.size - offset < 2 -> emptyArray()

            limit == 1 || data.size - offset == 2 ->
                arrayOf((data[offset + 1].toInt() shl 8 or data[offset].toInt()).toUShort())

            else -> {
                val first = (data[offset + 2].toInt() shl 4 or data[offset + 1].toInt() shr 4)
                    .toUShort()
                val second = ((data[offset + 1] and 0x0F).toInt() shl 8 or data[offset].toInt())
                    .toUShort()
                arrayOf(first, second) + decode(limit - 2, data, offset + 3)
            }
        }
    }
}


/**
 * A base decoder interface for Configuration messages.
 */
interface ConfigMessageInitializer : BaseMeshMessageInitializer, HasOpCode

/**
 * THe base interface for unacknowledged Configuration messages.
 */
interface UnacknowledgedConfigMessage : ConfigMessage, UnacknowledgedMeshMessage

/**
 * The base interface for response messages.
 */
interface ConfigResponse : MeshResponse, UnacknowledgedMeshMessage

/**
 * A base class for acknowledged Configuration messages.
 *
 * Acknowledged messages will be responded with a status message.
 */
interface AcknowledgedConfigMessage : ConfigMessage, AcknowledgedMeshMessage

/**
 * Status of a Config operation.
 *
 * @property value The status value.
 */
enum class ConfigMessageStatus(val value: UByte) {

    /** Success. */
    SUCCESS(0x00.toUByte()),

    /** Invalid Address. */
    INVALID_ADDRESS(0x01.toUByte()),

    /** Invalid Model. */
    INVALID_MODEL(0x02.toUByte()),

    /** Invalid AppKey Index. */
    INVALID_APP_KEY_INDEX(0x03.toUByte()),

    /** Invalid NetKey Index. */
    INVALID_NET_KEY_INDEX(0x04.toUByte()),

    /** Insufficient Resources. */
    INSUFFICIENT_RESOURCES(0x05.toUByte()),

    /** Key Index Already Stored. */
    KEY_INDEX_ALREADY_STORED(0x06.toUByte()),

    /** Invalid Publish Parameters. */
    INVALID_PUBLISH_PARAMETERS(0x07.toUByte()),

    /** Not a Subscribe Model. */
    NOT_A_SUBSCRIBE_MODEL(0x08.toUByte()),

    /** Storage Failure. */
    STORAGE_FAILURE(0x09.toUByte()),

    /** Feature Not Supported. */
    FEATURE_NOT_SUPPORTED(0x0A.toUByte()),

    /** Cannot Update. */
    CANNOT_UPDATE(0x0B.toUByte()),

    /** Cannot Remove. */
    CANNOT_REMOVE(0x0C.toUByte()),

    /** Cannot Bind. */
    CANNOT_BIND(0x0D.toUByte()),

    /** Temporarily Unable to Change State. */
    TEMPORARILY_UNABLE_TO_CHANGE_STATE(0x0E.toUByte()),

    /** Cannot Set. */
    CANNOT_SET(0x0F.toUByte()),

    /** Unspecified Error. */
    UNSPECIFIED_ERROR(0x10.toUByte()),

    /** Invalid Binding. */
    INVALID_BINDING(0x11.toUByte());

    companion object {

        /**
         * Returns the ConfigMessageStatus for the given value.
         *
         * @param value Value of the status.
         * @return ConfigMessageStatus
         */
        fun from(value: UByte): ConfigMessageStatus? = values().find { it.value == value }
    }
}

/**
 * A base interface for config status messages.
 *
 * @property status Status of the Config operation.
 */
interface ConfigStatusMessage : ConfigMessage, StatusMessage {
    val status: ConfigMessageStatus

    override val isSuccess: Boolean
        get() = status == ConfigMessageStatus.SUCCESS

    override val message: String
        get() = "$status"
}

/**
 * A base interface for Network Key configuration messages.
 *
 * @property networkKeyIndex The Network Key Index.
 */
interface ConfigNetKeyMessage : ConfigMessage {
    val networkKeyIndex: KeyIndex

    fun encodeNetKeyIndex(): ByteArray = encodeNetKeyIndex(networkKeyIndex)

    fun decodeNetKeyIndex(data: ByteArray, offset: Int): KeyIndex = decodeNetKeyIndex(data, offset)

    companion object {

        /**
         * Encodes the Network Key Index into a 2 octet byte array
         */
        fun encodeNetKeyIndex(networkKeyIndex: KeyIndex): ByteArray =
            ConfigMessage.encode(indexes = listOf(networkKeyIndex))

        /**
         * Decodes the Network Key Index from the given dat at the given offset.
         *
         * @param data       Data from where the indexes should be read.
         * @param offset     Offset from where to read the indexes.
         * @return Decoded Key Indexes.
         */
        fun decodeNetKeyIndex(data: ByteArray, offset: Int): KeyIndex =
            ConfigMessage.decode(limit = 1, data = data, offset = offset).first()
    }
}

/**
 * A base interface for Application Key configuration messages.
 *
 * @property applicationKeyIndex The Application Key Index.
 */
interface ConfigAppKeyMessage : ConfigMessage {
    var applicationKeyIndex: KeyIndex
}

/**
 *  A combined base interface for both Network and Application Key configuration messages.
 */
interface ConfigNetAndAppKeyMessage : ConfigNetKeyMessage, ConfigAppKeyMessage

/**
 * A base interface for a configuration messages sent to an element.
 *
 * @property elementAddress Unicast Address of the Model's parent Element.
 */
interface ConfigElementMessage : ConfigMessage {
    val elementAddress: UnicastAddress
}

/**
 * A base interface for a configuration messages sent to a Model.
 *
 * @property modelIdentifier 16-bit Model identifier.
 * @property modelId         32-bit Model identifier.
 */
interface ConfigModelMessage : ConfigElementMessage {
    val modelIdentifier: UShort
    val modelId: ModelId
}

/**
 * A base interface for a configuration messages sent to a Model including a Vendor specific model.
 *
 * @property companyIdentifier Company identifier, as defined in Assigned Numbers, or `nil`, if the
 *                             Model is defined in Bluetooth Mesh Model Specification.
 *
 * @seeAlso https://www.bluetooth.com/specifications/assigned-numbers/company-identifiers/
 */
interface ConfigAnyModelMessage : ConfigModelMessage {
    val companyIdentifier: UShort?
}

/**
 * A base interface for a configuration messages sent to a Vendor Model.
 *
 * @property companyIdentifier The Company identified, as defined in Assigned Numbers.
 * @seeAlso: https://www.bluetooth.com/specifications/assigned-numbers/company-identifiers/
 *
 */
interface ConfigVendorModelMessage : ConfigModelMessage {
    val companyIdentifier: UShort
    override val modelId: VendorModelId
        get() = VendorModelId(modelIdentifier, companyIdentifier)
}

/**
 *  A base protocol for config messages with an Address property.
 *
 *  @property address The Address.
 */
interface ConfigAddressMessage : ConfigMessage {
    val address: Address
}

/**
 * A base protocol for config messages with Virtual Label property.
 *
 * @property virtualLabel Value of the 128-bt Virtual Label UUID.
 */
interface ConfigVirtualLabelMessage : ConfigMessage {
    val virtualLabel: UUID
}

/**
 * A base protocol for config messages with list of Application Keys.
 *
 * @property applicationKeyIndexes Application Key Indexes bound to the Model.
 */
interface ConfigModelAppList : ConfigStatusMessage, ConfigModelMessage {
    val applicationKeyIndexes: List<KeyIndex>
}

/**
 * A base protocol for config messages with list of Model subscription addresses.
 *
 * @property addresses A list of Addresses.
 */
interface ConfigModelSubscriptionList : ConfigStatusMessage, ConfigModelMessage {
    val addresses: List<Address>
}

/**
 * This enum represents number of periodic Heartbeat messages remaining to be sent.
 */
sealed class RemainingHeartbeatPublicationCount {
    /**
     * Periodic Heartbeat messages are not published.
     */
    data object Disabled : RemainingHeartbeatPublicationCount()

    /**
     * Periodic Heartbeat messages are not published.
     */
    data object Indefinitely : RemainingHeartbeatPublicationCount()

    /**
     * Periodic Heartbeat messages are not published.
     *
     * @property value
     *
     */
    data class Exact(val value: UShort) : RemainingHeartbeatPublicationCount()

    /**
     * Remaining count of periodic Heartbeat messages represented as range. Exact count is only
     * available when the count goes down to 2 and 1; otherwise a range is returned.
     *
     * @property low   Short range value.
     * @property high  High range value.
     * @constructor Constructs a range of remaining count of periodic Heartbeat messages.
     */
    data class Range(val low: UShort, val high: UShort) : RemainingHeartbeatPublicationCount()

    /**
     * Periodic Heartbeat messages are not published.
     *
     * @property countLog Count log values sent.
     */
    data class Invalid(val countLog: UByte) : RemainingHeartbeatPublicationCount()

    companion object {

        /**
         * Converts the given CountLog value to a RemainingHeartbeatPublicationCount.
         *
         * @return RemainingHeartbeatPublicationCount
         */
        internal fun CountLog.toRemainingPublicationCount(): RemainingHeartbeatPublicationCount {
            return when {
                this == 0x00.toUByte() -> Disabled
                this == 0xFF.toUByte() -> Indefinitely
                this == 0x01.toUByte() || this == 0x02.toUByte() -> Exact(this.toUShort())
                this == 0x11.toUByte() -> Range(low = 0x8001.toUShort(), high = 0xFFFE.toUShort())
                this >= 0x03.toUByte() && this <= 0x10.toUByte() -> Range(
                    low = ((2.0.pow(this.toDouble() - 2.0)) + 1.0).toUInt().toUShort(),
                    high = (2.0.pow(this.toDouble() - 1.0)).toUInt().toUShort()
                )

                else -> Invalid(this)
            }
        }
    }
}

/**
 *This enum represents remaining period for processing Heartbeat messages, in seconds.
 */
sealed class RemainingHeartbeatSubscriptionPeriod {

    /**
     * Heartbeat messages are not processed.
     */
    data object Disabled : RemainingHeartbeatSubscriptionPeriod()

    /**
     * Exact remaining period for processing Heartbeat messages, in seconds. Exact period is only
     * available when the count goes down to 1 or when is maximum; otherwise a range is returned.
     *
     * @property value Exact period value.
     */
    data class Exact(val value: UShort) : RemainingHeartbeatSubscriptionPeriod()

    /**
     * Remaining period for processing Heartbeat messages as range, in seconds.
     *
     * @property range Remaining period range.
     */
    data class Range(val range: ClosedRange<UShort>) : RemainingHeartbeatSubscriptionPeriod()

    /**
     * Unsupported PeriodLog value sent.
     *
     * @property periodLog PeriodLog value sent.
     */
    data class Invalid(val periodLog: UByte) : RemainingHeartbeatSubscriptionPeriod()
}


/**
 * This enum represents the number of Heartbeat messages received.
 */
sealed class HeartbeatSubscriptionCount {
    /**
     * Number of Heartbeat messages received. Exact count is only available when there was none, or
     * only one Heartbeat message received.
     *
     * @property value Exact count value.
     */
    data class Exact(val value: UShort) : HeartbeatSubscriptionCount()

    /**
     * Number of Heartbeat messages received as range.
     *
     * @property range Range of values.
     */
    data class Range(val range: ClosedRange<UShort>) : HeartbeatSubscriptionCount()

    /**
     * More than 0xFFFE messages have been received.
     */
    data object ReallyALot : HeartbeatSubscriptionCount()

    /**
     * Unsupported CountLog value sent.
     *
     * @property countLog CountLog value sent.
     */
    data class Invalid(val countLog: UByte) : HeartbeatSubscriptionCount()
}

/**
 * The Random Update Interval Steps state determines the cadence of updates to the Random field in
 * the Mesh Private beacon.
 *
 * The Random Update Interval Steps are defined in units of 10 seconds, with an approximate maximum
 * value of 42 minutes.
 *
 * The default value of this state shall be ``RandomUpdateIntervalSteps/interval(n:)`` with value
 * n = 60 (0x3C) (i.e., 10 minutes).
 */
sealed class RandomUpdateIntervalSteps {
    /**
     * Random field is updated for every Mesh Private beacon.
     */
    data object EveryTime : RandomUpdateIntervalSteps()

    /**
     * Random field is updated at an interval (in 10 seconds steps).
     */
    data class Interval(val n: UByte) : RandomUpdateIntervalSteps()
}







