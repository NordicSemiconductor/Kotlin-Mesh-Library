@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core.layers

import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * A set of network parameters that can be applied to the ``MeshNetworkManager``.
 *
 * Network parameters configure the transmission and retransmission intervals, acknowledge message
 * timeout, the default Time To Live (TTL) and others.
 *
 * @property defaultTtl                         Default Time To Live (TTL) will be used for sending
 *                                              messages. If the value has not been set in the
 *                                              Provisioner's Node. By default it is set to 5, which
 *                                              is a reasonable value. The TTL shall be in
 *                                              range 2...127.
 *
 * @property incompleteMessageTimeout           Timeout after which an incomplete segmented message
 *                                              will be abandoned. The timer is restarted each time
 *                                              a segment of this message is received.The incomplete
 *                                              timeout should be set to at least 10 seconds.
 *
 * @property acknowledgementTimeInterval        The amount of time after which the lower transport
 *                                              layer sends a Segment Acknowledgment message after
 *                                              receiving a segment of a multi-segment message where
 *                                              the destination is a Unicast Address of the
 *                                              Provisioner's Element. The acknowledgment timer
 *                                              shall be set to a minimum of 150 + 50 * TTL in
 *                                              milliseconds. The TTL dependent part is added
 *                                              automatically, and this value shall specify only the
 *                                              constant part.
 *
 * @property transmissionTimeInterval           The time within which a Segment Acknowledgment
 *                                              message is expected to be received after a segment
 *                                              of a segmented message has been sent. When the timer
 *                                              is fired, the non-acknowledged segments are
 *                                              repeated, at most ``retransmissionLimit`` times.
 *
 *                                              The transmission timer shall be set to a minimum of
 *                                              200 + 50 * TTL milliseconds. The TTL dependent part
 *                                              is added automatically, and this value shall specify
 *                                              only the constant part.
 *
 *                                              If the bearer is using GATT, it is recommended to
 *                                              set the transmission interval longer than the
 *                                              connection interval, so that the acknowledgment had
 *                                              a chance to be received.
 *
 * @property retransmissionLimit                Number of times a non-acknowledged segment of a
 *                                              segmented message will be retransmitted before the
 *                                              message will be cancelled.
 *
 *                                              The limit may be decreased with increasing of
 *                                              @see transmissionTimeInterval as the target Node has
 *                                              more time to reply with the Segment
 *
 * @property acknowledgementMessageTimeout      If the Element does not receive a response within a
 *                                              period of time known as the acknowledged message
 *                                              timeout, then the Element may consider the message
 *                                              has not been delivered, without sending any
 *                                              additional messages.
 *
 *                                              @TODO fix documentation for message timeout
 *
 *                                              The acknowledged message timeout should be set to a
 *                                              minimum of 30 seconds.
 *
 * @property acknowledgementMessageTimeInterval The base time after which the acknowledged message
 *                                              will be repeated.
 *
 *                                              The repeat timer will be set to the
 *                                              base time + 50 * TTL milliseconds + 50 * segment
 *                                              count. The TTL and segment count dependent parts are
 *                                              added automatically, and this value shall specify
 *                                              only the constant part.
 * @constructor Constructs a NetworkParameters object.
 */
// TODO Clarify API and documentation.
data class NetworkParameters(
    private var _defaultTtl: UByte = 5u,
    private var _sarDiscardTimeout: UByte = 0b001u,                                  // (n+1)*5 sec = 10 seconds.
    private var _sarAcknowledgementDelayIncrement: UByte = 0b001u,                   // (n+1.5) = 2.5
    private var _sarReceiverSegmentIntervalStep: UByte = 0b0101u,                    // (n+1)*10 ms = 20 ms
    private var _sarSegmentsThreshold: UByte = 0b00011u,                             // 3
    private var _sarAcknowledgementRetransmissionCount: UByte = 0b00u,               // 0
    private var _sarSegmentIntervalStep: UByte = 0b0101u,                            // (n+1)*10 ms = 60 ms
    private var _sarUnicastRetransmissionsCount: UByte = 0b0010u,                    // 3
    private var _sarUnicastRetransmissionsWithoutProgressCount: UByte = 0b0010u,     // 3
    private var _sarUnicastRetransmissionsIntervalStep: UByte = 0b0111u,             // (n+1)*25 ms = 200 ms
    private var _sarUnicastRetransmissionsIntervalIncrement: UByte = 0b0001u,         // (n+1)*25 ms = 50 ms
    private var _sarMulticastRetransmissionsCount: UByte = 0b0010u,                  // 3
    private var _sarMulticastRetransmissionsIntervalStep: UByte = 0b1001u,           // (n+1)*25 ms = 250 ms
    private var _acknowledgementMessageTimeout: Duration = 30.toDuration(DurationUnit.SECONDS),
    private var _acknowledgementMessageInterval: Duration = 2.toDuration(DurationUnit.SECONDS)
) {

    var defaultTtl: UByte
        get() = _defaultTtl
        private set(value) {
            _defaultTtl = max(2, min(value.toInt(), 127)).toUByte()
        }

    var discardTimeout: Duration
        get() = ((_sarDiscardTimeout + 1u).toInt() * 5).toDuration(DurationUnit.SECONDS)
        set(value) {
            _sarDiscardTimeout =
                (min(5, value.toInt(DurationUnit.SECONDS) / 5).toUByte() - 1u).toUByte()
        }

    var sarDiscardTimeout: UByte
        get() = _sarDiscardTimeout
        set(value) {
            _sarDiscardTimeout =
                min(value.toInt(), 0b1111).toUByte() // valid range 0..15
        }

    fun setAcknowledgementTimerInterval(
        segmentReceptionInterval: Duration,
        acknowledgementDelayIncrement: Double
    ) {
        val min1 = min(
            a = 0.16,
            b = (segmentReceptionInterval.toDouble(DurationUnit.MILLISECONDS)) * 100
        )
        _sarReceiverSegmentIntervalStep = ((max(a = 0.01, b = min1) * 100) - 1.0).toInt().toUByte()

        val min2 = min(8.5, acknowledgementDelayIncrement)

        _sarAcknowledgementDelayIncrement = max(0.0, max(1.5, min2) - 1.5).toInt().toUByte()
    }

    var sarAcknowledgementDelayIncrement: UByte
        get() = _sarAcknowledgementDelayIncrement
        set(value) {
            _sarAcknowledgementDelayIncrement = min(value.toInt(), 0b111).toUByte()
        }

    var sarReceiverSegmentIntervalStep: UByte
        get() = _sarReceiverSegmentIntervalStep
        set(value) {
            _sarReceiverSegmentIntervalStep = min(value.toInt(), 0b1111).toUByte()
        }

    var acknowledgementDelayIncrement: Double
        get() = _sarAcknowledgementDelayIncrement.toDouble() + 1.5
        set(value) {
            val min1 = min(8.5, acknowledgementDelayIncrement)
            _sarAcknowledgementDelayIncrement = max(0.0, max(1.5, min1) - 1.5).toInt().toUByte()
        }

    var segmentReceptionInterval: Duration
        get() = ((_sarReceiverSegmentIntervalStep + 1u).toInt() * 10).toDuration(DurationUnit.MILLISECONDS)
        set(value) {
            val min1 = min(
                a = 0.16,
                b = (value.toDouble(DurationUnit.MILLISECONDS)) * 100
            )
            _sarReceiverSegmentIntervalStep =
                ((max(a = 0.01, b = min1) * 100) - 1.0).toInt().toUByte()
        }

    internal fun acknowledgementTimerInterval(segN: UByte): Duration {
        val min = min(segN.toDouble() + 0.5, acknowledgementDelayIncrement)
        val duration = min * segmentReceptionInterval.toDouble(DurationUnit.SECONDS)
        return duration.toDuration(DurationUnit.SECONDS)
    }

    internal val completeAcknowledgementTimerInterval: Duration
        get() = (acknowledgementDelayIncrement * (segmentReceptionInterval
            .toDouble(unit = DurationUnit.SECONDS)))
            .toDuration(DurationUnit.SECONDS)


    fun retransmitSegmentAcknowledgementMessages(count: UByte, threshold: UByte) {
        _sarSegmentsThreshold = threshold
        _sarAcknowledgementRetransmissionCount = count
    }

    var sarSegmentsThreshold: UByte
        get() = _sarSegmentsThreshold
        set(value) {
            _sarSegmentsThreshold = min(value.toInt(), 0b11111).toUByte()
        }

    var sarAcknowledgementRetransmissionCount: UByte
        get() = _sarAcknowledgementRetransmissionCount
        set(value) {
            _sarAcknowledgementRetransmissionCount = min(value.toInt(), 0b11).toUByte()
        }

    var sarSegmentIntervalStep: UByte
        get() = _sarSegmentIntervalStep
        set(value) {
            _sarSegmentIntervalStep = min(value.toInt(), 0b1111).toUByte()
        }

    var segmentTransmissionInterval: Duration
        get() = ((_sarSegmentIntervalStep + 1u).toInt() * 0.01).toDuration(DurationUnit.SECONDS)
        set(value) {
            val max = max(value.toDouble(DurationUnit.MILLISECONDS), 0.01)
            val min = min(0.16, max)
            _sarSegmentIntervalStep = ((min * 100).toInt() - 1).toUByte()
        }

    var sarUnicastRetransmissionsCount: UByte
        get() = _sarUnicastRetransmissionsCount
        set(value) {
            _sarUnicastRetransmissionsCount = min(value.toInt(), 0b1111).toUByte()
        }

    var sarUnicastRetransmissionsWithoutProgressCount: UByte
        get() = _sarUnicastRetransmissionsWithoutProgressCount
        set(value) {
            _sarUnicastRetransmissionsWithoutProgressCount = min(value.toInt(), 0b1111).toUByte()
        }

    var sarUnicastRetransmissionsIntervalStep: UByte
        get() = _sarUnicastRetransmissionsIntervalStep
        set(value) {
            _sarUnicastRetransmissionsIntervalStep = min(value.toInt(), 0b1111).toUByte()
        }

    var unicastRetransmissionsIntervalStep: Duration
        get() = ((_sarUnicastRetransmissionsIntervalStep + 1u).toInt() * 0.025).toDuration(
            DurationUnit.SECONDS
        )
        set(value) {
            val max = max(value.toDouble(DurationUnit.MILLISECONDS), 0.025)
            val min = min(max, 0.4)
            _sarUnicastRetransmissionsIntervalStep = ((min * 40).toInt() - 1).toUByte()
        }

    var sarUnicastRetransmissionsIntervalIncrement: UByte
        get() = _sarUnicastRetransmissionsIntervalIncrement
        set(value) {
            _sarUnicastRetransmissionsIntervalIncrement = min(value.toInt(), 0b1111).toUByte()
        }

    var unicastRetransmissionsIntervalIncrement: Duration
        get() = ((_sarUnicastRetransmissionsIntervalIncrement + 1u).toInt() * 0.025).toDuration(
            DurationUnit.SECONDS
        )
        set(value) {
            val max = max(value.toDouble(DurationUnit.MILLISECONDS), 0.025)
            val min = min(0.4, max)
            _sarUnicastRetransmissionsIntervalIncrement = ((min * 40).toInt() - 1).toUByte()
        }

    internal fun unicastRetransmissionsInterval(ttl: UByte) = if (ttl == 0.toUByte())
        unicastRetransmissionsIntervalStep
    else unicastRetransmissionsIntervalStep +
            (unicastRetransmissionsIntervalIncrement * (ttl.toDouble() - 1))

    var sarMulticastRetransmissionsCount: UByte
        get() = _sarMulticastRetransmissionsCount
        set(value) {
            _sarMulticastRetransmissionsCount = min(value.toInt(), 0b1111).toUByte()
        }

    var sarMulticastRetransmissionsIntervalStep: UByte
        get() = _sarMulticastRetransmissionsIntervalStep
        set(value) {
            _sarMulticastRetransmissionsIntervalStep = min(value.toInt(), 0b1111).toUByte()
        }

    var multicastRetransmissionsInterval : Duration
        get() = ((_sarMulticastRetransmissionsIntervalStep + 1u).toInt() * 0.025).toDuration(
            DurationUnit.SECONDS
        )
        set(value) {
            val max = max(value.toDouble(DurationUnit.MILLISECONDS), 0.025)
            val min = min(max, 0.4)
            _sarMulticastRetransmissionsIntervalStep = ((min * 40).toInt() - 1).toUByte()
        }

    var acknowledgementMessageTimeout: Duration
        get() = _acknowledgementMessageTimeout
        set(value) {
            _acknowledgementMessageTimeout = max(30.0, value.toDouble(DurationUnit.SECONDS))
                .toDuration(DurationUnit.SECONDS)
        }

    var acknowledgementMessageTimeInterval: Duration
        get() = _acknowledgementMessageInterval
        set(value) {
            _acknowledgementMessageInterval = max(2.0, value.toDouble(DurationUnit.SECONDS))
                .toDuration(DurationUnit.SECONDS)
        }

    internal fun acknowledgementMessageInterval(ttl: UByte, segmentCount: Int) =
        _acknowledgementMessageInterval +
                ((ttl.toDouble() * 0.050) + (segmentCount.toDouble() * 0.050))
                    .toDuration(DurationUnit.SECONDS)

    /**
     * According to Bluetooth Mesh Profile 1.0.1, section 3.10.5, if the IV Index of the mesh
     * network increased by more than 42 since the last connection (which can take at least 48
     * weeks), the Node should be re-provisioned. However, as this library can be used to provision
     * other Nodes, it should not be blocked from sending messages to the network only because the
     * phone wasn't connected to the network for that time. This flag can disable this check,
     * effectively allowing such connection.
     *
     * The same can be achieved by clearing the app data (uninstalling and reinstalling the app) and
     * importing the mesh network. With no "previous" IV Index, the library will accept any IV Index
     * received in the Secure Network beacon upon connection to the GATT Proxy Node.
     */
    var allowIvIndexRecoveryOver42 = false

    /**
     * IV Update Test Mode enables efficient testing of the IV Update procedure. The IV Update test
     * mode removes the 96-hour limit; all other behavior of the device are unchanged.
     *
     * @see [Bluetooth Mesh Profile 1.0.1, section 3.10.5.1.](https://www.bluetooth.com/specifications/mesh-specifications/)
     */
    var ivUpdateTestMode = false
}



