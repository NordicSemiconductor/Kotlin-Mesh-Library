package no.nordicsemi.android.nrfmesh.feature.provisioners.provisioner

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.models.ProvisionerData
import no.nordicsemi.android.nrfmesh.feature.provisioners.R
import no.nordicsemi.android.nrfmesh.feature.provisioners.provisioner.ranges.AllocatedRanges
import no.nordicsemi.android.nrfmesh.feature.provisioners.provisioner.ranges.RangesScreen
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.GroupRange
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.Range
import no.nordicsemi.kotlin.mesh.core.model.minus
import no.nordicsemi.kotlin.mesh.core.model.overlaps
import no.nordicsemi.kotlin.mesh.core.model.plus
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
internal fun GroupRanges(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    provisioner: Provisioner,
    provisionerData: ProvisionerData,
    otherRanges: List<GroupRange>,
    save: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var showGroupRanges by rememberSaveable { mutableStateOf(false) }
    val ranges = remember {
        mutableStateListOf<Range>().apply { addAll(provisionerData.groupRanges) }
    }
    val overlaps by remember {
        derivedStateOf { ranges.overlaps(other = otherRanges) }
    }
    OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        AllocatedRanges(
            imageVector = Icons.Outlined.GroupWork,
            title = stringResource(id = R.string.label_group_range),
            ranges = provisionerData.groupRanges,
            otherRanges = otherRanges,
            onClick = { showGroupRanges = true }
        )
    }
    if (showGroupRanges) {
        ModalBottomSheet(
            sheetState = sheetState,
            properties = ModalBottomSheetProperties(
                shouldDismissOnBackPress = !overlaps,
                shouldDismissOnClickOutside = !overlaps
            ),
            onDismissRequest = { showGroupRanges = !showGroupRanges },
            content = {
                RangesScreen(
                    title = stringResource(id = R.string.label_group_ranges),
                    ranges = ranges,
                    otherRanges = otherRanges,
                    overlaps = overlaps,
                    addRange = { start, end ->
                        val range = GroupAddress(address = start)..GroupAddress(address = end)
                        val list = ranges.plus(other = range)
                        ranges.clear()
                        ranges.addAll(list)
                    },
                    onRangeUpdated = { start, end ->
                        val newRange = GroupAddress(address = start)..GroupAddress(address = end)
                        val list = ranges.plus(other = newRange)
                        ranges.clear()
                        ranges.addAll(list)
                    },
                    onSwiped = {
                        val list = ranges.minus(other = it)
                        ranges.clear()
                        ranges.addAll(list)
                    },
                    isValidBound = { GroupAddress.isValid(address = it) },
                    resolve = {
                        val list = ranges.minus(other = otherRanges)
                        ranges.clear()
                        ranges.addAll(list)
                    },
                    save = {
                        runCatching {
                            provisioner.remove(ranges = provisionerData.groupRanges)
                            provisioner.allocate(ranges = ranges)
                        }.onSuccess {
                            save()
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showGroupRanges = !showGroupRanges
                                }
                            }
                        }.onFailure {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = it.message ?: "Failed to allocate ranges",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                )
            }
        )
    }
}