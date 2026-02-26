package no.nordicsemi.android.nrfmesh.feature.provisioners.provisioner

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lan
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
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.Range
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastRange
import no.nordicsemi.kotlin.mesh.core.model.minus
import no.nordicsemi.kotlin.mesh.core.model.overlaps
import no.nordicsemi.kotlin.mesh.core.model.plus
import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
internal fun UnicastRanges(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    provisioner: Provisioner,
    provisionerData: ProvisionerData,
    otherRanges: List<UnicastRange>,
    save: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    var showUnicastRanges by rememberSaveable { mutableStateOf(false) }
    val ranges = remember {
        mutableStateListOf<Range>().apply {
            addAll(provisionerData.unicastRanges)
        }
    }
    val overlaps by remember {
        derivedStateOf { ranges.overlaps(other = otherRanges) }
    }
    OutlinedCard(modifier = Modifier.padding(horizontal = 16.dp)) {
        AllocatedRanges(
            imageVector = Icons.Outlined.Lan,
            title = stringResource(id = R.string.label_unicast_range),
            ranges = provisionerData.unicastRanges,
            otherRanges = otherRanges,
            onClick = { showUnicastRanges = true }
        )
    }
    if (showUnicastRanges) {
        ModalBottomSheet(
            sheetState = sheetState,
            properties = ModalBottomSheetProperties(
                shouldDismissOnBackPress = !overlaps,
                shouldDismissOnClickOutside = !overlaps
            ),
            onDismissRequest = { showUnicastRanges = !showUnicastRanges },
            sheetGesturesEnabled = !overlaps,
            content = {
                RangesScreen(
                    title = stringResource(id = R.string.label_unicast_ranges),
                    ranges = ranges,
                    otherRanges = otherRanges,
                    overlaps = overlaps,
                    addRange = { start, end ->
                        val range = UnicastAddress(address = start)..UnicastAddress(address = end)
                        val list = ranges
                            .toList()
                            .plus(other = range)
                        ranges.clear()
                        ranges.addAll(list)
                    },
                    onRangeUpdated = { start, end ->
                        val newRange =
                            UnicastAddress(address = start)..UnicastAddress(address = end)
                        val list = ranges
                            .toList()
                            .plus(other = newRange)
                        ranges.clear()
                        ranges.addAll(list)
                    },
                    onSwiped = {
                        val list = ranges
                            .toList()
                            .minus(other = it)
                        ranges.clear()
                        ranges.addAll(list)
                    },
                    isValidBound = { UnicastAddress.isValid(address = it) },
                    resolve = {
                        val list = ranges
                            .toList()
                            .minus(other = otherRanges)
                        ranges.clear()
                        ranges.addAll(list)
                    },
                    save = {
                        runCatching {
                            provisioner.remove(ranges = provisionerData.unicastRanges)
                            provisioner.allocate(ranges = ranges)
                        }.onSuccess {
                            save()
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showUnicastRanges = !showUnicastRanges
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