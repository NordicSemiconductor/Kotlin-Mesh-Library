package no.nordicsemi.android.nrfmesh.feature.automation

import androidx.compose.ui.graphics.vector.ImageVector
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage

/**
 * Represents a mesh task.
 *
 * @property icon      Icon to be displayed for the task.
 * @property label     Label to be displayed for the task.
 * @property message   Task to be performed when the user clicks on the task.
 */
sealed class MeshTask(
    open val icon: ImageVector,
    open val label: String,
    open val message: MeshMessage,
    open val status: TaskStatus = TaskStatus.Idle,
)

/**
 * Represents a config task.
 */
data class ConfigTask(
    override val icon: ImageVector,
    override val label: String,
    override val message: AcknowledgedConfigMessage,
    override val status: TaskStatus = TaskStatus.Idle,
) : MeshTask(icon = icon, label = label, message = message, status = status)

/**
 * Represents an application task.
 */
data class ApplicationTask(
    override val icon: ImageVector,
    override val label: String,
    override val message: AcknowledgedMeshMessage,
    override val status: TaskStatus = TaskStatus.Idle,
) : MeshTask(icon = icon, label = label, message = message, status = status)

/**
 * Represents a task status.
 */
sealed class TaskStatus {

    /**
     * The task is idle.
     */
    object Idle : TaskStatus()

    /**
     * The task is in progress.
     */
    object InProgress : TaskStatus()

    /**
     * The task has been skipped.
     */
    object Skipped : TaskStatus()

    /**
     * The task has been completed.
     */
    object Completed : TaskStatus()

    /**
     * The task has failed.
     */
    data class Error(val error: Throwable) : TaskStatus()
}