package no.nordicsemi.kotlin.mesh.core.exception

sealed class MeshNetworkException : Exception()

/** Thrown when a given key index is out of range. A valid key index must range from 0 to 4095. */
object KeyIndexOutOfRange : MeshNetworkException()

/** Thrown when a given key index is already in use. */
object DuplicateKeyIndex : MeshNetworkException()

/** Thrown when the length of a key is not 16-bytes */
object InvalidKeyLength : MeshNetworkException()

/** Thrown when a given key is in use. */
object KeyInUse : MeshNetworkException()

/** Thrown when a network and a node does not contain any network key. */
object NoNetworkKeysAdded : MeshNetworkException()

/** Thrown when an object cannot be removed. */
object CannotRemove : MeshNetworkException()

/** Thrown when a node already exists. */
object NodeAlreadyExists : MeshNetworkException()

/** Thrown when no provisioner is available in the mesh network. */
object NoLocalProvisioner : MeshNetworkException()

/** Thrown when no unicast address range is allocated to a provisioner. */
object ProvisionerAlreadyExists : MeshNetworkException()

/** Thrown when any allocated range of a provisioner is already allocated. */
object RangeAlreadyAllocated : MeshNetworkException()

/** Thrown when any allocated range of a new provisioner overlaps with an existing one. */
object OverlappingProvisionerRanges : MeshNetworkException()

/** Thrown when a given address does not belong to an allocated range. */
object AddressNotInAllocatedRanges : MeshNetworkException()

/** Thrown when a given address is in use by a node or it's elements. */
object AddressAlreadyInUse : MeshNetworkException()

/** Thrown when no unicast addresses available to be allocated. */
object NoAddressesAvailable : MeshNetworkException()

/** Thrown when no unicast address range is allocated to a provisioner. */
object NoUnicastRangeAllocated : MeshNetworkException()

/** Thrown when a given group already exists. */
object GroupAlreadyExists : MeshNetworkException()

/** Thrown when a given group is in use. */
object GroupInUse : MeshNetworkException()

/** Thrown when no group range is allocated to a provisioner. */
object NoGroupRangeAllocated : MeshNetworkException()

/** Thrown when a given scene already exists. */
object SceneAlreadyExists : MeshNetworkException()

/** Thrown when a given scene is in use. */
object SceneInUse : MeshNetworkException()

/** Thrown when no scene range is allocated to a provisioner. */
object NoSceneRangeAllocated : MeshNetworkException()

/** Thrown when at least one network key is not selected when exporting a partial network. */
object AtLeastOneNetworkKeyMustBeSelected : MeshNetworkException()

/** Thrown when at least one provisioner is not selected when exporting a partial network. */
object AtLeastOneProvisionerMustBeSelected : MeshNetworkException()

/** Thrown when an invalid pdu type is received*/
object InvalidPduType : MeshNetworkException()

/**
 * Security exception thrown when level of security of the network key doesn't match with the
 * security used when provisioning a node.
 */
object SecurityException : MeshNetworkException()

/**
 * Thrown when the Json deserializing encounters an error.
 *
 * @property error            Error message.
 * @property throwable        Throwable exception.
 */
data class ImportError internal constructor(
    val error: String,
    val throwable: Throwable
) : MeshNetworkException()

/** Thrown when an object does not belong to the current network. */
object DoesNotBelongToNetwork : MeshNetworkException()