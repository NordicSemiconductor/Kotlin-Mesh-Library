@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.SecuritySerializer

/**
 * Security level describes a minimum security level of a subnet associated with this network key.
 * If all the nodes on the subnet associated with this network key have been provisioned using the
 * Secure Provisioning procedure [1], then the value of minSecurity property for the subnet is set
 * to “secure”; otherwise, the value of the minSecurity is set to “insecure”.
 */
@Serializable(with = SecuritySerializer::class)
sealed class Security {
    companion object {

        private const val INSECURE = "insecure"
        private const val SECURE = "secure"

        /**
         * Parses the security level from the security level description.
         *
         * @param                            security Security level.
         * @return                           Security level
         * @throws IllegalArgumentException  if the security level is not "insecure" or "secure"
         */
        internal fun from(security: String) = when (security) {
            INSECURE -> Insecure
            SECURE -> Secure
            else -> throw IllegalArgumentException(
                "Security level must be either $INSECURE or $SECURE!"
            )
        }

        internal fun toString(security: Security) = when (security) {
            Insecure -> INSECURE
            Secure -> SECURE
        }
    }

    override fun toString() = Companion.toString(this)
}

object Insecure : Security()
object Secure : Security()
