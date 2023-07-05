package no.nordicsemi.android.nrfmesh.core.storage

import android.content.Context
import no.nordicsemi.kotlin.mesh.core.Storage
import java.io.FileNotFoundException
import javax.inject.Inject

/**
 * Custom storage implementation using Jetpack DataStore.
 *
 * @property context    Context
 * @property fileName   Name of the file to store the network.
 * @constructor Creates the MeshNetworkStorage.
 */
class MeshNetworkStorage @Inject constructor(
    private val context: Context,
    private val fileName: String = "MeshNetwork",
) : Storage {

    override suspend fun load(): ByteArray = try {
        context.openFileInput("$fileName.json").use { stream ->
            val bytes = stream.readBytes()
            stream.close()
            bytes
        }
    } catch (e: FileNotFoundException) {
        byteArrayOf()
    }

    override suspend fun save(network: ByteArray) {
        context.openFileOutput("$fileName.json", Context.MODE_PRIVATE).use { stream ->
            stream.write(network)
            stream.close()
        }
    }
}