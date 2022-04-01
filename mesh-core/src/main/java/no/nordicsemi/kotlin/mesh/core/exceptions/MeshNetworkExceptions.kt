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
 * Thrown when a Key, Provisioner, Node , Group or a Scene does not belong to the current network.
 */
class DoesNotBelongToNetwork : MeshNetworkExceptions()