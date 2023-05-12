@file:Suppress("LocalVariableName")

package no.nordicsemi.kotlin.mesh.crypto

import no.nordicsemi.kotlin.mesh.crypto.Crypto.createVirtualAddress
import no.nordicsemi.kotlin.mesh.crypto.Crypto.k1
import no.nordicsemi.kotlin.mesh.crypto.Crypto.k2
import no.nordicsemi.kotlin.mesh.crypto.Crypto.k3
import no.nordicsemi.kotlin.mesh.crypto.Crypto.k4
import no.nordicsemi.kotlin.mesh.crypto.Crypto.salt
import no.nordicsemi.kotlin.mesh.crypto.Utils.decodeHex
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex
import no.nordicsemi.kotlin.mesh.crypto.Utils.uint16ToUtf8
import no.nordicsemi.kotlin.mesh.crypto.Utils.utf8ToUint16
import org.junit.Assert
import org.junit.Test
import java.util.*

/**
 * Unit tests for crypto module. All test data used can be found at section 8 "Sample data" in the Bluetooth Mesh Profile specification.
 */
@Suppress("UNUSED_VARIABLE")
class CryptoTest {

    /**
     * Unit test for salt generation function
     *
     * Refer 8.1.1 "s1 SALT generation function" for test data
     */
    @Test
    fun testSalt() {
        val expected = "b73cefbd641ef2ea598c2b6efb62f79c".uppercase(Locale.US)
        val data = "test".toByteArray(Charsets.UTF_8)
        Assert.assertEquals(expected, salt(data).encodeHex())
    }

    /**
     * Unit test for k1 function
     *
     * Refer 8.1.2 "k1 function" for test data
     */
    @Test
    fun testK1() {
        val N = "3216d1509884b533248541792b877f98".uppercase(Locale.US)
        val SALT = "2ba14ffa0df84a2831938d57d276cab4".uppercase(Locale.US)
        val P = "5a09d60797eeb4478aada59db3352a0d".uppercase(Locale.US)
        val T = "c764bea25cf9738b08956ea3c712d5af".uppercase(Locale.US)
        val k1 = "f6ed15a8934afbe7d83e8dcb57fcf5d7".uppercase(Locale.US)
        Assert.assertEquals(
            k1,
            k1(N = N.decodeHex(), SALT = SALT.decodeHex(), P = P.decodeHex()).encodeHex()
        )
    }

    /**
     * Unit test for k2 function
     *
     * Refer 8.1.3 "k2 function" for test data
     */
    @Test
    fun testK2() {
        val N = "f7a2a44f8e8a8029064f173ddc1e2b00".uppercase(Locale.US)
        val P = "00".uppercase(Locale.US)
        val expectedNid = "7f".uppercase(Locale.US).toInt(radix = 16)
        val expectedEncryptionKey = "9f589181a0f50de73c8070c7a6d27f46".uppercase(Locale.US)
        val expectedPrivacyKey = "4c715bd4a64b938f99b453351653124f".uppercase(Locale.US)
        val (nid, encryptionKey, privacyKey) = k2(N = N.decodeHex(), P = P.decodeHex())
        Assert.assertEquals(expectedNid, nid)
        Assert.assertEquals(expectedEncryptionKey, encryptionKey.encodeHex())
        Assert.assertEquals(expectedPrivacyKey, privacyKey.encodeHex())
    }

    /**
     * Unit test for k3 function
     *
     * Refer 8.1.5 "k3 function" for test data
     */
    @Test
    fun testK3() {
        val N = "f7a2a44f8e8a8029064f173ddc1e2b00".uppercase(Locale.US)
        val expectedNetworkId = "ff046958233db014".uppercase(Locale.US)
        Assert.assertEquals(expectedNetworkId, k3(N = N.decodeHex()).encodeHex())
    }

    /**
     * Unit test for k4 function
     *
     * Refer 8.1.6 "k4 function" for N
     */
    @Test
    fun testK4() {
        val N = "3216d1509884b533248541792b877f98".uppercase(Locale.US)
        val expectedNetworkId = "38".uppercase(Locale.US).toInt(radix = 16)
        Assert.assertEquals(expectedNetworkId, k4(N = N.decodeHex()))
    }

    /**
     * Unit test for NetworkKeyDerivatives.
     *
     * Refer 8.2.2 "Encryption and privacy keys (Master)" for test data for NID, EncryptionKey and PrivacyKey.
     * Refer 5.2.4 for Network ID, 8.2.5 for IdentityKey and 8.2.6 BeaconKey.
     */
    @Test
    fun testNetworkKeyDerivatives() {
        val N = "7dd7364cd842ad18c17c2b820c84c3d6".decodeHex()
        val NID = "68".toInt(16).toUByte()
        val encryptionKey = "0953fa93e7caac9638f58820220a398e".uppercase(Locale.US).decodeHex()
        val privacyKey = "8b84eedec100067d670971dd2aa700cf".uppercase(Locale.US).decodeHex()
        val networkId = "3ecaff672f673370".uppercase(Locale.US).decodeHex()
        val identityKey = "84396c435ac48560b5965385253e210c".uppercase(Locale.US).decodeHex()
        val beaconKey = "5423d967da639a99cb02231a83f7d254".uppercase(Locale.US).decodeHex()
        val keyDerivatives = Crypto.calculateKeyDerivatives(N)
        Assert.assertTrue("NID do not match!", NID == keyDerivatives.nid)
        Assert.assertTrue("EncryptionKeys do not match!", encryptionKey.contentEquals(keyDerivatives.encryptionKey))
        Assert.assertTrue("PrivacyKeys do not match!", privacyKey.contentEquals(keyDerivatives.privacyKey))
        Assert.assertTrue("Network IDs do not match!", networkId.contentEquals(keyDerivatives.networkId))
        Assert.assertTrue("Identity Keys do not match!", identityKey.contentEquals(keyDerivatives.identityKey))
        Assert.assertTrue("Beacon Keys do not match!", beaconKey.contentEquals(keyDerivatives.beaconKey))
    }

    /**
     * Unit test for [Crypto.encrypt].
     *
     * Refer 8.3.1 "Message #1" for test data for NID, EncryptionKey and PrivacyKey.
     * Refer 5.2.4 for Network ID, 8.2.5 for IdentityKey and 8.2.6 BeaconKey.
     */
    @Test
    fun testEncrypt() {
        val expected = "b5e5bfdacbaf6cb7fb6bff871f035444ce83a670df".uppercase(Locale.US)
        val dst = "fffd".decodeHex()
        val transportPdu = "034b50057e400000010000".decodeHex()
        val data = dst + transportPdu
        val encryptionKey = "0953fa93e7caac9638f58820220a398e".decodeHex()
        val nonce = "00800000011201000012345678".decodeHex()
        val mic = 64 / 8
        val actual = Crypto.encrypt(
            data = data,
            key = encryptionKey,
            nonce = nonce,
            micSize = mic
        ).encodeHex()
        Assert.assertEquals(expected, actual)
    }

    /**
     * Unit test for [Crypto.decrypt].
     *
     * Refer 8.3.1 "Message #1" for test data for NID, EncryptionKey and PrivacyKey.
     * Refer 5.2.4 for Network ID, 8.2.5 for IdentityKey and 8.2.6 BeaconKey.
     */
    @Test
    fun testDecrypt() {
        val dst = "fffd"
        val transportPdu = "034b50057e400000010000"
        val expected = (dst + transportPdu).uppercase(Locale.US)
        val encryptedTransportPdu = "b5e5bfdacbaf6cb7fb6bff871f035444ce83a670df".decodeHex()
        val encryptionKey = "0953fa93e7caac9638f58820220a398e".decodeHex()
        val nonce = "00800000011201000012345678".decodeHex()
        val mic = 64 / 8
        val actual = Crypto.decrypt(
            data = encryptedTransportPdu,
            key = encryptionKey,
            nonce = nonce,
            micSize = mic
        ).encodeHex()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun testVirtualAddress() {
        val uuid = UUID.fromString("0073e7e4-d8b9-440f-af84-15df4c56c0e1")
        val expected = "B529".toUInt(radix = 16).toUShort()
        val actual = createVirtualAddress(uuid = uuid)
        Assert.assertEquals(expected, actual)
    }

    /**
     * Unit test for [Crypto.authenticate].
     *
     * Refer 8.4.3 "Secure Network beacon" for test data for Beacon Key and Beacon PDU.
     */
    @Test
    fun testAuthenticate() {
        val beaconPDU = "01003ecaff672f673370123456788ea261582f364f6f".decodeHex()
        val beaconKey = "5423d967da639a99cb02231a83f7d254".decodeHex()
        val expected = true
        val actual = Crypto.authenticate(beaconPDU, beaconKey)
        Assert.assertEquals(expected, actual)
    }

    /**
     * Unit test for [Crypto.decodeAndAuthenticate].
     *
     * Refer 8.4.6.1 "Mesh Private Beacon - IV update in Progress" for test data for Beacon Key and Beacon PDU.
     */
    @Test
    fun testDecodeAndAuthenticate() {
        val beaconPDU = "02435f18f85cf78a3121f58478a561e488e7cbf3174f022a514741".decodeHex()
        val beaconKey = "6be76842460b2d3a5850d4698409f1bb".decodeHex()
        val expected = Pair(0x02.toByte(), "1010abcd".decodeHex())
        val actual = Crypto.decodeAndAuthenticate(beaconPDU, beaconKey)
        val isEqual = expected.first == actual?.first && expected.second.contentEquals(actual.second)
        Assert.assertEquals(true, isEqual)
    }

    /**
     * Unit test for [Crypto.obfuscate].
     *
     * Refer 8.3.16 "Message #16" for test data for Data to Obfuscate/Deobfuscate, Privacy Key
     * Privacy Random, IV Index and the final expected result.
     */
    @Test
    fun testObfuscate() {
        val data = "e80e5da5af0e".decodeHex()
        val random = "6b9be7f5a642f2f98680e61c3a8b47f228".decodeHex()
        val ivIndex = 0x12345678.toUInt()
        val privacyKey = "8b84eedec100067d670971dd2aa700cf".decodeHex()
        val expected = "0b0000061201".decodeHex()
        val actual = Crypto.obfuscate(data, random, ivIndex, privacyKey)
        Assert.assertTrue(expected.contentEquals(actual))
    }


    /**
     * Unit test for [Crypto.calculateECB].
     *
     * Refer 8.3.17 "Message #17" for test data for Privacy Plain Text, Privacy Key and PECB.
     */
    @Test
    fun testCalculateECB() {
        val privacyPlainText = "0000000000123456782a80d381b91f82".decodeHex()
        val privacyKey = "8b84eedec100067d670971dd2aa700cf".decodeHex()
        val expectedPECB = "b8bd2c18096e".decodeHex()
        val actualPECB = Crypto.calculateECB(privacyPlainText, privacyKey).copyOfRange(0, 6)
        Assert.assertTrue(expectedPECB.contentEquals(actualPECB))
    }

    @Test
    fun testUint16ToUTF8() {
        val expected = "0010C280C480".decodeHex()
        val actual = "0000001000800100".decodeHex().uint16ToUtf8()
        Assert.assertTrue(expected.contentEquals(actual))
    }

    @Test
    fun testUTF8ToUint16() {
        val expected = "0000001000800100".decodeHex()
        val actual = "0010C280C480".decodeHex().utf8ToUint16()
        Assert.assertTrue(expected.contentEquals(actual))
    }
}