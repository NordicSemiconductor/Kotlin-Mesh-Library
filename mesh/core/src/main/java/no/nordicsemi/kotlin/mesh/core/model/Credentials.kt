@file:Suppress("unused", "SERIALIZER_TYPE_INCOMPATIBLE")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.CredentialsSerializer

/**
 * The credentials property contains an integer of 0 or 1 that represents whether managed flooding
 * security material (0) or friendship security material (1) is used.
 *
 * @property credential 0 if master security credential is used or friendship security material
 *                      is used.
 */
@Serializable(with = CredentialsSerializer::class)
sealed class Credentials(val credential: Int) {
    internal companion object {
        internal fun from(credential: Int) = when (credential) {
            MASTER_SECURITY -> MasterSecurity
            FRIENDSHIP_SECURITY -> FriendshipSecurity
            else -> throw IllegalArgumentException(
                "Credentials values supported are $MASTER_SECURITY and $FRIENDSHIP_SECURITY!"
            )
        }
    }
}

/** Master security material is used for Publishing. */
@Serializable(with = CredentialsSerializer::class)
data object MasterSecurity : Credentials(credential = MASTER_SECURITY)

/** Friendship security material is used for Publishing. */
@Serializable(with = CredentialsSerializer::class)
data object FriendshipSecurity : Credentials(credential = FRIENDSHIP_SECURITY)

private const val MASTER_SECURITY = 0
private const val FRIENDSHIP_SECURITY = 1