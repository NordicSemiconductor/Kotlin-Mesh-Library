package no.nordicsemi.kotlin.mesh.crypto

import no.nordicsemi.kotlin.mesh.crypto.Utils.toByteArray

sealed class SecurityCredentials {

    data object ManagedFlooding : SecurityCredentials() {
        override val P = byteArrayOf(0x00)
    }

    data class Friendship(
        val lpnAddress: UShort,
        val lpnCounter: UShort,
        val friendAddress: UShort,
        val friendCounter: UShort,
    ): SecurityCredentials() {
        override val P =  byteArrayOf(0x01) +
                lpnAddress.toByteArray() +
                lpnCounter.toByteArray() +
                friendAddress.toByteArray() +
                friendCounter.toByteArray()
    }

    data object DirectedFlooding : SecurityCredentials() {
        override val P = byteArrayOf(0x02)
    }

    @Suppress("PropertyName")
    internal abstract val P: ByteArray
}