package no.nordicsemi.kotlin.mesh.core.exceptions

sealed class MeshNetworkExceptions : Exception()

/**
 * Thrown when a given key index is out of range. A valid key index must range from 0 to 4095.
 */
class KeyIndexOutOfRange : MeshNetworkExceptions()

/**
 * Thrown when a given key index is already in use.
 */
class DuplicateKeyIndex : MeshNetworkExceptions()

/**
 * Thrown when a given key is in use.
 */
class KeyInUse : MeshNetworkExceptions()

/**
 * Thrown when a given group already exists.
 */
class GroupAlreadyExists : MeshNetworkExceptions()

/**
 * Thrown when a given group is in use.
 */
class GroupInUse : MeshNetworkExceptions()

/**
 * Thrown when a given scene already exists.
 */
class SceneAlreadyExists : MeshNetworkExceptions()

/**
 * Thrown when a given scene is in use.
 */
class SceneInUse : MeshNetworkExceptions()

/**
 * Thrown when an object does not belong to the current network.
 */
class DoesNotBelongToNetwork : MeshNetworkExceptions()