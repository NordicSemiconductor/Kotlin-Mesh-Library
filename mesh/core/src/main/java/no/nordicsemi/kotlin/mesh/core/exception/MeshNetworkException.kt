package no.nordicsemi.kotlin.mesh.core.exception

sealed class MeshNetworkException : Exception()

/** Thrown when a given key index is out of range. A valid key index must range from 0 to 4095. */
data object KeyIndexOutOfRange : MeshNetworkException()

/** Thrown when a given key index is already in use. */
data object DuplicateKeyIndex : MeshNetworkException()

/** Thrown when the length of a key is not 16-bytes */
data object InvalidKeyLength : MeshNetworkException()

/** Thrown when a given key is in use. */
data object KeyInUse : MeshNetworkException()

/** Thrown when a network and a node does not contain any network key. */
data object NoNetworkKeysAdded : MeshNetworkException()

/** Thrown when a Network property cannot be removed. */
data object CannotRemove : MeshNetworkException()

/** Thrown when a node already exists. */
data object NodeAlreadyExists : MeshNetworkException()

/** Thrown when no provisioner is available in the mesh network. */
data object NoLocalProvisioner : MeshNetworkException()

/** Thrown when no unicast address range is allocated to a provisioner. */
data object ProvisionerAlreadyExists : MeshNetworkException()

/** Thrown when any allocated range of a provisioner is already allocated. */
data object RangeAlreadyAllocated : MeshNetworkException()

/** Thrown when any allocated range of a new provisioner overlaps with an existing one. */
data object OverlappingProvisionerRanges : MeshNetworkException()

/** Thrown when a given address does not belong to an allocated range. */
data object AddressNotInAllocatedRanges : MeshNetworkException()

/** Thrown when a given address is in use by a node or it's elements. */
data object AddressAlreadyInUse : MeshNetworkException()

/** Thrown when no unicast addresses available to be allocated. */
data object NoAddressesAvailable : MeshNetworkException()

/** Thrown when no unicast address range is allocated to a provisioner. */
data object NoUnicastRangeAllocated : MeshNetworkException()

/** Thrown when a given group already exists. */
data object GroupAlreadyExists : MeshNetworkException()

/** Thrown when a given group is in use. */
data object GroupInUse : MeshNetworkException()

/** Thrown when no group range is allocated to a provisioner. */
data object NoGroupRangeAllocated : MeshNetworkException()

/** Thrown when a given scene already exists. */
data object SceneAlreadyExists : MeshNetworkException()

/** Thrown when a given scene is in use. */
data object SceneInUse : MeshNetworkException()

/** Thrown when no scene range is allocated to a provisioner. */
data object NoSceneRangeAllocated : MeshNetworkException()

/** Thrown when at least one network key is not selected when exporting a partial network. */
data object AtLeastOneNetworkKeyMustBeSelected : MeshNetworkException()

/** Thrown when at least one provisioner is not selected when exporting a partial network. */
data object AtLeastOneProvisionerMustBeSelected : MeshNetworkException()

/** Thrown when an invalid pdu type is received*/
data object InvalidPduType : MeshNetworkException()

/**
 * Security exception thrown when level of security of the network key doesn't match with the
 * security used when provisioning a node.
 */
data object SecurityException : MeshNetworkException()

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

/** Thrown when a network property does not belong to the current network. */
data object DoesNotBelongToNetwork : MeshNetworkException()