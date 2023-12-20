@file:OptIn(ExperimentalStdlibApi::class, ExperimentalStdlibApi::class)

package no.nordicsemi.android.kotlin.mesh.bearer

import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.bearer.ProxyProtocolHandler
import no.nordicsemi.kotlin.mesh.crypto.Utils.decodeHex
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex
import org.junit.Test

import org.junit.Assert.*

class ProxyProtocolHandlerTest {

    @Test
    fun segment() {
        val expectedMessage =
            "430339C31CD3EFB53FD664443882AA4E4E4DDED7BE6063D16E5EA84CBF09" +
                    "8337205C8D0854AE88FC98873FE58B699FD4638924C3D21824C0CCD64722" +
                    "C38F1535C73D6FC8"
        val message =
            "0339c31cd3efb53fd664443882aa4e4e4dded7be6063d16e5ea84cbf0937205c8d0854ae88fc98873fe58b699fd4638924c3d21824c0ccd647228f1535c73d6fc8".uppercase()
        val handler = ProxyProtocolHandler()
        val actualMessage =
            handler.segment(data = message.decodeHex(), type = PduType.PROVISIONING_PDU, mtu = 30)
                .joinToString("") { it.encodeHex() }
        assertEquals(expectedMessage, actualMessage)
    }

    @Test
    fun reassemble() {
        val expectedMessage =
            "0339c31cd3efb53fd664443882aa4e4e4dded7be6063d16e5ea84cbf0937205c8d0854ae88fc98873fe58b699fd4638924c3d21824c0ccd647228f1535c73d6fc8".uppercase()
        val messages = listOf(
            "430339C31CD3EFB53FD664443882AA4E4E4DDED7BE6063D16E5EA84CBF09".decodeHex(),
            "8337205C8D0854AE88FC98873FE58B699FD4638924C3D21824C0CCD64722".decodeHex(),
            "C38F1535C73D6FC8".decodeHex()
        )
        val handler = ProxyProtocolHandler()
        var actualMessage = ""
        messages.forEach { message ->
            handler.reassemble(message)?.let {
                actualMessage += it.data.encodeHex()
            }
        }
        assertEquals(expectedMessage, actualMessage)
    }
}