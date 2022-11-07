@file:Suppress("LocalVariableName", "unused", "UNUSED_PARAMETER")

package no.nordicsemi.kotlin.mesh.crypto

import no.nordicsemi.kotlin.mesh.crypto.Utils.decodeHex
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex
import no.nordicsemi.kotlin.mesh.crypto.Utils.intToBigEndian
import no.nordicsemi.kotlin.mesh.crypto.Utils.xor
import org.bouncycastle.crypto.BlockCipher
import org.bouncycastle.crypto.InvalidCipherTextException
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.macs.CMac
import org.bouncycastle.crypto.modes.CCMBlockCipher
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.jcajce.provider.symmetric.AES
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

object Crypto {

    private val secureRandom = SecureRandom()
    private val blockCipher: BlockCipher = AESEngine()
    private val SALT_KEY = ByteArray(16) { 0x00 }
    private val smk2 = "smk2".encodeToByteArray()
    private val smk3 = "smk3".encodeToByteArray()
    private val smk4 = "smk4".encodeToByteArray()
    private val id64 = "id64".encodeToByteArray()
    private val id6 = "id6".encodeToByteArray()
    private val NKIK = "nkik".encodeToByteArray()
    private val NKBK = "nkbk".encodeToByteArray()
    private val ID128 = "id128".encodeToByteArray()
    private val VTAD = "vtad".encodeToByteArray()

    /**
     * Generates a 128-bit random key using a SecureRandom.
     */
    fun generateRandomKey() = secureRandom.run {
        val random = ByteArray(16) { 0x00 }
        nextBytes(random)
        random
    }

    /**
     * Creates a 16-bit virtual address for a given UUID.
     * @param uuid 128-bit Label UUID.
     */
    fun createVirtualAddress(uuid: UUID): UShort {
        val uuidHex = uuid.toString().replace("-", "").decodeHex()
        val salt = salt(VTAD)
        val hash = cmac(input = uuidHex, key = salt)
        return (0x8000 or (hash.copyOfRange(fromIndex = 14, toIndex = hash.count()).encodeHex()
            .toInt(radix = 16) and 0x3FFF)).toUShort()
    }

    /**
     * Calculates the NID, EncryptionKey, PrivacyKey, NetworkID, IdentityKey and BeaconKey for a given NetworkKey
     *
     * @param N 128-bit NetworkKey.
     * @return a Pair(first = Triple(NID, EncryptionKey, PrivacyKey), second = Triple(NetworkID, IdentityKey, BeaconKey)).
     */
    fun calculateKeyDerivatives(N: ByteArray): KeyDerivatives {
        val k2 = k2(N = N, P = byteArrayOf(0x00))
        return KeyDerivatives(
            nid = k2.first.toUByte(),
            encryptionKey = k2.second,
            privacyKey = k2.third,
            networkId = calculateNetworkId(N = N),
            identityKey = calculateIdentityKey(N = N),
            beaconKey = calculateBeaconKey(N = N)
        )
    }

    /**
     * Calculates the AID for a given ApplicationKey.
     *
     * @param N 128-bit ApplicationKey.
     * @return 8-bit Application Key Identifier.
     */
    fun calculateAid(N: ByteArray): UByte {
        val k4 = k4(N = N)
        return k4.toUByte()
    }

    /**
     * Encrypts the [data] with the EncryptionKey , Nonce and concatenates the MIC(Message Integrity Check).
     *
     * @param data                  Data to be encrypted.
     * @param key                   128-bit key.
     * @param nonce                 104-bit nonce.
     * @param micSize               Length of the MIC to be generated, in bytes.
     * @param additionalData        Additional data to be authenticated.
     * @returns Encrypted data concatenated with MIC of given size.
     */
    @kotlin.jvm.Throws(InvalidCipherTextException::class)
    fun encrypt(
        data: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        additionalData: ByteArray? = null,
        micSize: Int
    ) = calculateCCM(
        data = data,
        key = key,
        nonce = nonce,
        additionalData = additionalData,
        micSize = micSize,
        mode = true
    )

    /**
     * Decrypts the [data] with the EncryptionKey , Nonce and authenticates the generated MIC(Message Integrity Check).
     *
     * @param data                  Data to be decrypted.
     * @param key                   128-bit key.
     * @param nonce                 104-bit nonce.
     * @param micSize               Length of the MIC to be generated, in bytes.
     * @param additionalData        Additional data to be authenticated.
     * @returns Encrypted data concatenated with MIC of given size.
     */
    fun decrypt(
        data: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        additionalData: ByteArray? = null,
        micSize: Int
    ) = calculateCCM(
        data = data,
        key = key,
        nonce = nonce,
        additionalData = additionalData,
        micSize = micSize,
        mode = false
    )

    /**
     *  Obfuscates or De+obfuscates given data by XORing it with PECB, which is
     *  calculated by encrypting Privacy Plaintext (encrypted data (used as Privacy
     *  Random) and IV Index) using the given key.
     *  - parameters:
     *  @param data         The data to obfuscate or deobfuscate.
     *  @param random       Data used as Privacy Random.
     *  @param ivIndex      The current IV Index value.
     *  @param privacyKey   The 128-bit Privacy Key.
     *  @returns a byte array containing Obfuscated or De-obfuscated input data.
     */
    fun obfuscate(data: ByteArray, random: ByteArray, ivIndex: Int, privacyKey: ByteArray): ByteArray {
        // Privacy Random = (EncDST || EncTransportPDU || NetMIC)[0–6]
        // Privacy Plaintext = 0x0000000000 || IV Index || Privacy Random
        // PECB = e (PrivacyKey, Privacy Plaintext)
        // ObfuscatedData = (CTL || TTL || SEQ || SRC) ⊕ PECB[0–5]
        val privacyRandom = random.copyOfRange(fromIndex = 0, toIndex = 7)
        val privacyPlaintext = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00) + intToBigEndian(ivIndex) + privacyRandom
        val pecb = calculateECB(privacyPlaintext, privacyKey)
        val obfuscatedData = xor(data, pecb.copyOfRange(fromIndex = 0, toIndex = 6))
        return obfuscatedData
    }

    fun deObfuscate() {
        // TODO - identical to obfuscate (can remove)
    }

    /**
     * Calculates Electronic Code Book (ECB) for the given [data] and [key].
     *
     * @param data the input data.
     * @param key the 128-bit key.
     * @returns the encrypted data.
     */
    private fun calculateECB(data: ByteArray, key: ByteArray): ByteArray {
        try {
            val cipher = Cipher.getInstance("AES/ECB/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"))
            return cipher.doFinal(data)
        } catch (e: Exception) {
            throw InvalidCipherTextException("Error while encrypting data: ${e.message}")
        }
    }

    /**
     * Calculates the 64-bit Network ID.
     * The Network ID is derived from the network key such that each network key generates one Network ID.
     * This identifier becomes public information.
     *
     * @param N     128-bit Network key.
     * @return 64-bit Network ID.
     */
    fun calculateNetworkId(N: ByteArray): ByteArray = k3(N = N)

    /**
     * Calculates the 128-bit IdentityKey.
     * The IdentityKey is derived from the network key such that each network key generates one IdentityKey
     *
     * @param N     128-bit Network key.
     * @return 128-bit key T.
     */
    private fun calculateIdentityKey(N: ByteArray): ByteArray {
        val s1 = salt(NKIK)
        val P = ID128 + 0x01
        return k1(N = N, SALT = s1, P = P)
    }

    /**
     * Calculates the 128-bit BeaconKey
     * The BeaconKey is derived from the network key such that each network key generates one BeaconKey.
     *
     * @param N     128-bit Network key.
     * @return 128-bit key T.
     */
    private fun calculateBeaconKey(N: ByteArray): ByteArray {
        val s1 = salt(NKBK)
        val P = ID128 + 0x01
        return k1(N = N, SALT = s1, P = P)
    }

    /**
     * Calculates the salt based on a given input.
     *
     * @param input     A non-zero length octet array or ASCII encoded string.
     * @return 128-bit salt of the given input.
     */
    fun salt(input: ByteArray): ByteArray {
        return cmac(input = input, key = SALT_KEY)
    }

    /**
     * The network key material derivation function k1 is used to generate instances of IdentityKey and BeaconKey.
     * The definition of this key generation function makes use of the MAC function AES-CMAC(T) with a 128-bit key T.
     *
     * @param N         0 or more octets.
     * @param SALT      128 bits salt.
     * @param P         0 or more octets.
     * @return 128-bit key T.
     */
    internal fun k1(N: ByteArray, SALT: ByteArray, P: ByteArray): ByteArray {
        require(SALT.size == 16) { "Salt must be 128-bits." }
        val t = cmac(N, SALT)
        return cmac(P, t)
    }

    /**
     * The network key material derivation function k2 is used to generate instances of EncryptionKey, PrivacyKey,
     * and NID for use as Master and Private Low Power node communication.
     * The definition of this key generation function makes use of the MAC function AES-CMAC(T) with a 128-bit key T.
     *
     * @param N     128-bit key.
     * @param P     1 or more octets.
     * @return a Triple containing the NID, EncryptionKey and PrivacyKey.
     */
    internal fun k2(N: ByteArray, P: ByteArray): Triple<Int, ByteArray, ByteArray> {
        require(N.size == 16) {
            "N must be 128-bits."
        }
        require(P.isNotEmpty()) {
            "P must be 1 or more octets."
        }
        val s1 = salt(smk2)
        val T = cmac(N, s1)
        val T0 = byteArrayOf()
        val T1 = cmac(input = (T0 + P + byteArrayOf(0x01)), key = T)
        val T2 = cmac(input = (T1 + P + byteArrayOf(0x02)), key = T) // EncryptionKey
        val T3 = cmac(input = (T2 + P + byteArrayOf(0x03)), key = T) // PrivacyKey

        val nid = T1.last().toInt() and 0x7F
        return Triple(nid, T2, T3)
    }

    /**
     * The derivation function k3 is used to generate a public value of 64 bits derived from a private key.
     * The definition of this derivation function makes use of the MAC function AES-CMAC(T) with a 128-bit key T.
     *
     * @param N 128-bit key.
     * @return 64-bit key T.
     */
    internal fun k3(N: ByteArray): ByteArray {
        val s1 = salt(smk3)
        val T = cmac(N, s1)
        val result = cmac(input = (id64 + 0x01), key = T)
        return result.copyOfRange(8, result.count())
    }

    /**
     * The derivation function k4 is used to generate a public value of 6 bits derived from a private key.
     * The definition of this derivation function makes use of the MAC function AES-CMAC(T) with a 128-bit key T.
     *
     * @param N 128-bit key.
     * @return 128-bit key T.
     */
    internal fun k4(N: ByteArray): Int {
        val s1 = salt(smk4)
        val T = cmac(N, s1)
        val result = cmac(input = (id6 + 0x01), key = T)
        return result.last().toInt() and 0x3F
    }

    /**
     * Calculates Cipher-based Message Authentication Code (CMAC) that uses AES-128 as the block cipher function (AES-CMAC).
     * @param input Input to be authenticated.
     * @param key   128-bit key.
     * @return 128-bit message authentication code (MAC).
     */
    private fun cmac(input: ByteArray, key: ByteArray): ByteArray {
        require(key.size == 16) { "Key must be 128-bits." }
        return CMac(blockCipher).run {
            init(KeyParameter(key))
            update(input, 0, input.count())
            val output = ByteArray(macSize) { 0x00 }
            doFinal(output, 0)
            output
        }
    }

    /**
     * This method  generates the ciphertext and MIC (Message Integrity Check) and validates the ciphertext
     * RFC3610 [10] defines the AES Counter with CBC-MAC (CCM).
     *
     * @param data                  Data to be encrypted and authenticated.
     * @param key                   128-bit key.
     * @param nonce                 104-bit nonce.
     * @param additionalData        Additional data to be authenticated.
     * @param micSize               Length of the MIC to be generated, in bytes.
     * @param mode                  True to encrypt or false to decrypt
     * @returns if [mode] was set to true, returns the encrypted data with the MIC concatenated otherwise returns the decrypted data.
     */
    private fun calculateCCM(
        data: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        additionalData: ByteArray? = null,
        micSize: Int,
        mode: Boolean
    ) = CCMBlockCipher(blockCipher).run {
        val ccm = ByteArray(if (mode) data.size + micSize else data.size - micSize)
        init(mode, AEADParameters(KeyParameter(key), micSize * 8, nonce, additionalData))
        processBytes(data, 0, data.size, ccm, data.size)
        doFinal(ccm, 0)
        ccm
    }
}