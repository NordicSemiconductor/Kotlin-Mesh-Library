package no.nordicsemi.kotlin.mesh.core.model

import java.util.*

/**
 * Wrapper class representing a standard UUID formatted with dashes.
 *
 * @property meshUuid UUID imported from json with/without dashes.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
internal class MeshUuid(private val meshUuid: String) {
    val uuid: UUID = UUID.fromString(format())

    /**
     * Formats a UUID string to a standard UUID format.
     */
    private fun format(): String = meshUuid.takeIf {
        HEX_UUID_PATTERN.matches(it)
    }?.apply {
        StringBuilder(this).apply {
            insert(8, "-")
            insert(13, "-")
            insert(18, "-")
            insert(23, "-")
        }.toString().uppercase()
    } ?: meshUuid

    /**
     * Drops the dashes in the UUID
     * @return a UUID string without dashes.
     */
    fun encode(): String = uuid.toString()
        .filter { !it.isLetterOrDigit() }
        .uppercase()

    companion object {
        val HEX_UUID_PATTERN = Regex("[0-9a-fA-F]{32}")
    }
}
