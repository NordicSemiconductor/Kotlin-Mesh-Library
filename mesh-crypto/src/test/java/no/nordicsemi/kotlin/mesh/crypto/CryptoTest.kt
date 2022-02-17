@file:Suppress("LocalVariableName")

package no.nordicsemi.kotlin.mesh.crypto

import no.nordicsemi.kotlin.mesh.crypto.Crypto.k1
import no.nordicsemi.kotlin.mesh.crypto.Crypto.k2
import no.nordicsemi.kotlin.mesh.crypto.Crypto.k3
import no.nordicsemi.kotlin.mesh.crypto.Crypto.k4
import no.nordicsemi.kotlin.mesh.crypto.Crypto.salt
import no.nordicsemi.kotlin.mesh.crypto.Utils.decodeHex
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex
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
        val NID = "68".toInt(16)
        val encryptionKey = "0953fa93e7caac9638f58820220a398e".uppercase(Locale.US).decodeHex()
        val privacyKey = "8b84eedec100067d670971dd2aa700cf".uppercase(Locale.US).decodeHex()
        val networkId = "3ecaff672f673370".uppercase(Locale.US).decodeHex()
        val identityKey = "84396c435ac48560b5965385253e210c".uppercase(Locale.US).decodeHex()
        val beaconKey = "5423d967da639a99cb02231a83f7d254".uppercase(Locale.US).decodeHex()
        val (first, second) = Crypto.calculateKeyDerivatives(N)
        Assert.assertTrue("NID do not match!", NID == first.first)
        Assert.assertTrue("EncryptionKeys do not match!", encryptionKey.contentEquals(first.second))
        Assert.assertTrue("PrivacyKeys do not match!", privacyKey.contentEquals(first.third))
        Assert.assertTrue("Network IDs do not match!", networkId.contentEquals(second.first))
        Assert.assertTrue("Identity Keys do not match!", identityKey.contentEquals(second.second))
        Assert.assertTrue("Beacon Keys do not match!", beaconKey.contentEquals(second.third))
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
}