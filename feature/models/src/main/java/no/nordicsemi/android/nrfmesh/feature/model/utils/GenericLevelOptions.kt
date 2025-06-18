package no.nordicsemi.android.nrfmesh.feature.model.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChangeHistory
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Flourescent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.nrfmesh.feature.models.R

enum class GenericLevelOptions(val value: Int) {
    LEVEL(0),
    DELTA(1),
    MOVE(2);

    @Composable
    fun icon(): ImageVector = when (this) {
        LEVEL -> Icons.Outlined.Flourescent
        DELTA -> Icons.Outlined.ChangeHistory
        MOVE -> Icons.Outlined.Code
    }

    @Composable
    fun description(): String = when (this) {
        LEVEL -> stringResource(R.string.label_level)
        DELTA -> stringResource(R.string.label_delta)
        MOVE -> stringResource(R.string.label_move)
    }

    companion object {

        /**
         * Returns the [GenericLevelOptions] corresponding to the given value.
         *
         * @param value The integer value representing the option.
         * @return The [GenericLevelOptions] corresponding to the value.
         * @throws IllegalArgumentException if the value does not correspond to any [GenericLevelOptions].
         */
        @Throws(IllegalArgumentException::class)
        fun from(value: Int): GenericLevelOptions {
            return GenericLevelOptions.entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Invalid value for GenericLevelOptions: $value")
        }
    }
}