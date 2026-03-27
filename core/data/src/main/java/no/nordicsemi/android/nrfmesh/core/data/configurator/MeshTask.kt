package no.nordicsemi.android.nrfmesh.core.data.configurator

import androidx.compose.ui.graphics.vector.ImageVector
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.TransactionMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.Model

/**
 * Represents a mesh messaging task.
 */
sealed class MeshTask(
    open val icon: ImageVector,
    open val label: String,
    open val element: Element?,
    open val model: Model?,
    open val applicationKey: ApplicationKey? = null,
    open val message: MeshMessage,
    open val status: TaskStatus = TaskStatus.Idle,
)

/**
 * Represents a config message task.
 */
data class ConfigTask(
    override val icon: ImageVector,
    override val label: String,
    override val message: AcknowledgedConfigMessage,
    override val status: TaskStatus = TaskStatus.Idle,
) : MeshTask(
    icon = icon,
    label = label,
    element = null,
    model = null,
    message = message,
    status = status,
)

/**
 * Represents an application message task.
 */
data class AppTask(
    override val icon: ImageVector,
    override val label: String,
    override val element: Element,
    override val model: Model,
    override val message: TransactionMessage,
    override val applicationKey: ApplicationKey,
    override val status: TaskStatus = TaskStatus.Idle,
) : MeshTask(
    icon = icon,
    label = label,
    element = element,
    model = model,
    applicationKey = applicationKey,
    message = message,
    status = status
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
    data class Error(val error: String) : TaskStatus()
}