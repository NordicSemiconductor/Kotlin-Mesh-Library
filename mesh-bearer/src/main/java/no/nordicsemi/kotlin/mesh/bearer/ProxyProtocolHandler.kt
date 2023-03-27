@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.bearer

import kotlin.experimental.and

private enum class SAR(val rawValue: UByte) {
    COMPLETE_MESSAGE(rawValue = 0b0u),
    FIRST_SEGMENT(rawValue = 0b1u),
    CONTINUATION(rawValue = 0b10u),
    LAST_SEGMENT(rawValue = 0b11u);

    val value: UByte
        get() = (rawValue.toUInt() shl 6).toUByte()

}

/**
 * PDU type identifies the type of the message.
 */
private fun ByteArray.from(): PduType? {
    if (isEmpty())
        return null
    return when ((first() and 0b00111111).toUByte()) {
        0.toUByte() -> PduType.NETWORK_PDU
        1.toUByte() -> PduType.MESH_BEACON
        2.toUByte() -> PduType.PROXY_CONFIGURATION
        3.toUByte() -> PduType.PROVISIONING_PDU
        else -> null
    }
}

class ProxyProtocolHandler {

    private lateinit var buffer: ByteArray
    private lateinit var bufferTypes: PduType

    /**
     * Segments the given data with given message type to 1+ messages where all but the last one are
     * of the MTU size and the last one is MTU size or smaller. This method implements the Proxy
     * Protocol from Bluetooth Mesh specification.
     *
     * @param data  Data to be segmented.
     * @param type  Type of the message.
     * @param mtu   Maximum size of the message.
     * @return Array of segmented messages.
     */
    fun segment(data: ByteArray, type: PduType, mtu: Int) =
        if (data.size <= mtu - 1) {
            var singlePacket = byteArrayOf((SAR.COMPLETE_MESSAGE.value or type.value).toByte())
            singlePacket += data
            arrayOf(singlePacket)
        } else {
            var packets: Array<ByteArray> = emptyArray()
            for (i in data.indices step mtu - 1) {
                val sar = when {
                    i == 0 -> SAR.FIRST_SEGMENT
                    i + mtu - 1 >= data.size -> SAR.LAST_SEGMENT
                    else -> SAR.CONTINUATION
                }
                var singlePacket = byteArrayOf((sar.value or type.value).toByte())
                singlePacket += data.sliceArray(i until minOf(data.size, i + mtu - 1))
                packets += singlePacket
            }
            packets
        }
}