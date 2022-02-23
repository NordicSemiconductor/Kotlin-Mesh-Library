package no.nordicsemi.kotlin.mesh.core.model

/**
 * The credentials property contains an integer of 0 or 1 that represents whether managed flooding
 * security material (0) or friendship security material (1) is used.
 *
 * @param credential 0 if master security credential is used or friendship security material is used.
 */
sealed class Credentials(val credential: Int)

/** Master security material is used for Publishing. */
object MasterSecurity : Credentials(credential = 0)

/** Friendship security material is used for Publishing. */
object FriendshipSecurity : Credentials(credential = 1)
