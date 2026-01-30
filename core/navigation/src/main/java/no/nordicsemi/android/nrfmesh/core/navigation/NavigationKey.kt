package no.nordicsemi.android.nrfmesh.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class NodeKey(val nodeUuid: String) : NavKey