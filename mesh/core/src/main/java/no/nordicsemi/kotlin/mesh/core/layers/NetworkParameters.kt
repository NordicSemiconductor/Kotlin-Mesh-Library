@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * A set of network parameters that can be applied to the ``MeshNetworkManager``.
 *
 * Network parameters configure the transmission and retransmission intervals, acknowledge message
 * timeout, the default Time To Live (TTL) and others.
 *
 * @property defaultTtl                         Default Time To Live (TTL) will be used for
 *                                              sending messages. If the value has not been set in
 *                                              the Provisioner's Node. By default it is set to 5,
 *                                              which is a reasonable value. The TTL shall be in
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
 *
 *
 *
 * The acknowledgement message time interval.
 * @constructor Constructs a NetworkParameters object.
 *
 */
data class NetworkParameters(
    var defaultTtl: UByte = 5u,
    var incompleteMessageTimeout: Duration = 10.toDuration(DurationUnit.SECONDS),
    var acknowledgementTimeInterval: Duration = 0.150.toDuration(DurationUnit.SECONDS),
    var transmissionTimeInterval: Duration = 0.200.toDuration(DurationUnit.SECONDS),
    var retransmissionLimit: Int = 5,
    var acknowledgementMessageTimeout: Duration = 30.toDuration(DurationUnit.SECONDS),
    var acknowledgementMessageTimeInterval: Duration = 2.toDuration(DurationUnit.SECONDS)
) {

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












