package no.nordicsemi.android.nrfmesh.core.data.configurator

import androidx.compose.ui.graphics.vector.ImageVector
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage

/**
 * Represents a config task.
 */
data class ConfigTask(
    val icon: ImageVector,
    val label: String,
    val message: AcknowledgedConfigMessage,
    val status: TaskStatus = TaskStatus.Idle,
)

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