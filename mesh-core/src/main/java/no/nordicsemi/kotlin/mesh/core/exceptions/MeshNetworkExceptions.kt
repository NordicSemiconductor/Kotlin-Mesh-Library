package no.nordicsemi.kotlin.mesh.core.exceptions

sealed class MeshNetworkExceptions : Exception()

/** Thrown when a given key index is out of range. A valid key index must range from 0 to 4095. */
class KeyIndexOutOfRange : MeshNetworkExceptions()

/** Thrown when a given key index is already in use. */
class DuplicateKeyIndex : MeshNetworkExceptions()

/** Thrown when a given key is in use. */
class KeyInUse : MeshNetworkExceptions()

/** Thrown when a node does not contain a network key. */
class NoNetworkKey : MeshNetworkExceptions()

/** Thrown when an object cannot be removed. */
class CannotRemove : MeshNetworkExceptions()

/** Thrown when a node already exists. */
class NodeAlreadyExists : MeshNetworkExceptions()

/** Thrown when no unicast address range is allocated to a provisioner. */
class ProvisionerAlreadyExists : MeshNetworkExceptions()

/** Thrown when any allocated range of a new provisioner overlaps with an existing one. */
class OverlappingProvisionerRanges : MeshNetworkExceptions()

/** Thrown when a given address does not belong to an allocated range. */
class AddressNotInAllocatedRanges : MeshNetworkExceptions()

/** Thrown when a given address is in use by a node or it's elements. */
class AddressAlreadyInUse : MeshNetworkExceptions()

/** Thrown when no unicast addresses available to be allocated. */
class NoAddressesAvailable : MeshNetworkExceptions()

/** Thrown when no unicast address range is allocated to a provisioner. */
class NoUnicastRangeAllocated : MeshNetworkExceptions()

/** Thrown when a given group already exists. */
class GroupAlreadyExists : MeshNetworkExceptions()

/** Thrown when a given group is in use. */
class GroupInUse : MeshNetworkExceptions()

/** Thrown when no group range is allocated to a provisioner. */
class NoGroupRangeAllocated : MeshNetworkExceptions()

/** Thrown when a given scene already exists. */
class SceneAlreadyExists : MeshNetworkExceptions()

/** Thrown when a given scene is in use. */
class SceneInUse : MeshNetworkExceptions()

/** Thrown when no scene range is allocated to a provisioner. */
class NoSceneRangeAllocated : MeshNetworkExceptions()

/** Thrown when an object does not belong to the current network. */
class DoesNotBelongToNetwork : MeshNetworkExceptions()
