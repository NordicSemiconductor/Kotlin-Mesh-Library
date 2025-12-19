package no.nordicsemi.android.nrfmesh.feature.provisioners.provisioner.ranges

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.ui.TwoLineRangeListItem
import no.nordicsemi.kotlin.mesh.core.model.GroupRange
import no.nordicsemi.kotlin.mesh.core.model.Range
import no.nordicsemi.kotlin.mesh.core.model.SceneRange
import no.nordicsemi.kotlin.mesh.core.model.UnicastRange
import no.nordicsemi.kotlin.mesh.core.model.maxGroupAddress
import no.nordicsemi.kotlin.mesh.core.model.maxSceneNumber
import no.nordicsemi.kotlin.mesh.core.model.maxUnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.minGroupAddress
import no.nordicsemi.kotlin.mesh.core.model.minSceneNumber
import no.nordicsemi.kotlin.mesh.core.model.minUnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.overlap

@Composable
fun AllocatedRanges(
    imageVector: ImageVector,
    title: String,
    ranges: List<Range>,
    otherRanges: List<Range>,
    onClick: () -> Unit,
) {
    TwoLineRangeListItem(
        modifier = Modifier.clickable { onClick() },
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = imageVector,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = title,
        lineTwo = {
            val ownRangeColor = MaterialTheme.colorScheme.primary
            val otherRangeColor = Color.DarkGray
            val conflictingColor = Color.Red
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(height = 16.dp)
                    .background(color = Color.LightGray)
            ) {
                // Mark own ranges
                markRanges(color = ownRangeColor, ranges = ranges)
                // Mark other provisioners' ranges
                markRanges(color = otherRangeColor, ranges = otherRanges)
                // Mark conflicting ranges
                markRanges(
                    color = conflictingColor,
                    ranges = ranges.overlap(other = otherRanges)
                )
            }
        }
    )
}

@Composable
internal fun AllocatedRange(
    imageVector: ImageVector,
    title: String,
    range: Range,
    otherRanges: List<Range>,
    onClick: (Range) -> Unit,
) {
    TwoLineRangeListItem(
        modifier = Modifier.clickable { onClick(range) },
        leadingComposable = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = imageVector,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = title,
        lineTwo = {
            val ownRangeColor = MaterialTheme.colorScheme.primary
            val conflictingColor = Color.Red
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .height(height = 16.dp)
                    .background(color = Color.LightGray)
            ) {
                // Mark own ranges
                markRange(color = ownRangeColor, range = range)
                // Mark conflicting ranges
                markRanges(color = conflictingColor, ranges = range.overlap(other = otherRanges))
            }
        }
    )
}


private fun DrawScope.markRanges(color: Color, ranges: List<Range>) {
    ranges.forEach { range ->
        when (range) {
            is UnicastRange -> {
                mark(
                    color = color,
                    low = range.lowAddress.address.toInt(),
                    high = range.highAddress.address.toInt(),
                    lowerBound = minUnicastAddress.toInt(),
                    upperBound = maxUnicastAddress.toInt()
                )
            }

            is GroupRange -> {
                mark(
                    color = color,
                    low = range.lowAddress.address.toInt(),
                    high = range.highAddress.address.toInt(),
                    lowerBound = minGroupAddress.toInt(),
                    upperBound = maxGroupAddress.toInt()
                )
            }

            is SceneRange -> {
                mark(
                    color = color,
                    low = range.firstScene.toInt(),
                    high = range.lastScene.toInt(),
                    lowerBound = minSceneNumber.toInt(),
                    upperBound = maxSceneNumber.toInt()
                )
            }
        }
    }
}

private fun DrawScope.markRange(color: Color, range: Range) {
    when (range) {
        is UnicastRange -> {
            mark(
                color = color,
                low = range.lowAddress.address.toInt(),
                high = range.highAddress.address.toInt(),
                lowerBound = minUnicastAddress.toInt(),
                upperBound = maxUnicastAddress.toInt()
            )
        }

        is GroupRange -> {
            mark(
                color = color,
                low = range.lowAddress.address.toInt(),
                high = range.highAddress.address.toInt(),
                lowerBound = 0xC000u.toInt(),
                upperBound = 0xFEFFu.toInt()
            )
        }

        is SceneRange -> {
            mark(
                color = color,
                low = range.firstScene.toInt(),
                high = range.lastScene.toInt(),
                lowerBound = 0x0001u.toInt(),
                upperBound = 0xFFFFu.toInt()
            )
        }
    }
}

internal fun DrawScope.mark(color: Color, low: Int, high: Int, lowerBound: Int, upperBound: Int) {
    val rangeWidth = size.width * (high - low) / (upperBound - lowerBound)
    val rangeStart = size.width * (low - lowerBound) / (upperBound - lowerBound)
    drawRect(
        color = color,
        topLeft = Offset(x = rangeStart, y = 0f),
        size = Size(width = rangeWidth.inc(), height = size.height),
        style = Fill
    )
}