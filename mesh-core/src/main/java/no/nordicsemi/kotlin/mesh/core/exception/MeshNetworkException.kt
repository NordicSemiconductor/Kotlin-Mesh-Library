package no.nordicsemi.kotlin.mesh.core.exception

sealed class MeshNetworkException : Exception()

/** Thrown when a given key index is out of range. A valid key index must range from 0 to 4095. */
class KeyIndexOutOfRange : MeshNetworkException()

/** Thrown when a given key index is already in use. */
class DuplicateKeyIndex : MeshNetworkException()

/** Thrown when a given key is in use. */
class KeyInUse : MeshNetworkException()

/** Thrown when a node does not contain a network key. */
class NoNetworkKey : MeshNetworkException()

/** Thrown when an object cannot be removed. */
class CannotRemove : MeshNetworkException()

/** Thrown when a node already exists. */
class NodeAlreadyExists : MeshNetworkException()

/** Thrown when no unicast address range is allocated to a provisioner. */
class ProvisionerAlreadyExists : MeshNetworkException()

/** Thrown when any allocated range of a new provisioner overlaps with an existing one. */
class OverlappingProvisionerRanges : MeshNetworkException()

/** Thrown when a given address does not belong to an allocated range. */
class AddressNotInAllocatedRanges : MeshNetworkException()

/** Thrown when a given address is in use by a node or it's elements. */
class AddressAlreadyInUse : MeshNetworkException()

/** Thrown when no unicast addresses available to be allocated. */
class NoAddressesAvailable : MeshNetworkException()

/** Thrown when no unicast address range is allocated to a provisioner. */
class NoUnicastRangeAllocated : MeshNetworkException()

/** Thrown when a given group already exists. */
class GroupAlreadyExists : MeshNetworkException()

/** Thrown when a given group is in use. */
class GroupInUse : MeshNetworkException()

/** Thrown when no group range is allocated to a provisioner. */
class NoGroupRangeAllocated : MeshNetworkException()

/** Thrown when a given scene already exists. */
class SceneAlreadyExists : MeshNetworkException()

/** Thrown when a given scene is in use. */
class SceneInUse : MeshNetworkException()

/** Thrown when no scene range is allocated to a provisioner. */
class NoSceneRangeAllocated : MeshNetworkException()

/** Thrown when an object does not belong to the current network. */
class DoesNotBelongToNetwork : MeshNetworkException()

/** Thrown when at least one network key is not selected when exporting a partial network. */
class AtLeastOneNetworkKeyMustBeSelected : MeshNetworkException()

/** Thrown when at least one provisioner is not selected when exporting a partial network. */
class AtLeastOneProvisionerMustBeSelected : MeshNetworkException()

/** Thrown when the Json deserializing encounters an error. */
class ImportError internal constructor(
    val error: String,
    val throwable: Throwable
) : MeshNetworkException()
