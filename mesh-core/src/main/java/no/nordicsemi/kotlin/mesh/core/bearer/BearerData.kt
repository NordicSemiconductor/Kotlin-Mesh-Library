@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.bearer

interface BearerData {

    fun bearer(bearer: Bearer, data: ByteArray, type: PduType)
}

interface BearerDelegate {

    fun bearerDidOpen(bearer: Bearer)

    fun bearerDidClose(bearer: Bearer)
}