package habitiq.app.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import habitiq.app.data.VacancyRequest
import habitiq.app.discover.DiscoverFilterLogic
import habitiq.app.lib.formatInr
import habitiq.app.ui.components.HqBottomSheet
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Admin review of members' vacancy posts. Approve puts the room live on Discover (credited to the
 * member, who can then pause or resume it); Decline tells the member it wasn't approved.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacancyReviewSheet(
    requests: List<VacancyRequest>,
    onApprove: (VacancyRequest) -> Unit,
    onDecline: (VacancyRequest) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = LocalHqColors.current
    HqBottomSheet(onDismiss = onDismiss, title = "Vacancy to review") {
        if (requests.isEmpty()) {
            Text("Nothing waiting right now.", style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(vertical = 16.dp))
            return@HqBottomSheet
        }
        Column(Modifier.verticalScroll(rememberScrollState()).padding(top = 8.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                "Approving lists the room on Discover for your flat. The person who posted it can pause it later.",
                style = HqType.bodyMedium, color = c.textSecondary,
            )
            requests.forEach { r ->
                val v = r.vacancy
                val shape = RoundedCornerShape(18.dp)
                Column(
                    Modifier.fillMaxWidth().clip(shape).background(c.surfaceBase).border(1.dp, c.borderSubtle, shape).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("${r.requesterName} wants to list a room", style = HqType.titleSmall2, color = c.textPrimary)
                    Text(
                        listOfNotNull(
                            listOf(v.area, v.city).filter { it.isNotBlank() }.joinToString(", ").ifBlank { null },
                            v.rentPerHead?.let { "${formatInr(it)}/head" },
                            DiscoverFilterLogic.formatRoomType(v.roomType, v.bedsAvailable),
                        ).joinToString(" · "),
                        style = HqType.bodyMedium, color = c.textSecondary,
                    )
                    if (v.about.isNotBlank()) Text(v.about, style = HqType.bodyMedium, color = c.textPrimary, maxLines = 4)
                    if (v.photoUrls.isNotEmpty()) {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            v.photoUrls.take(6).forEach { url ->
                                AsyncImage(
                                    model = url, contentDescription = null, contentScale = ContentScale.Crop,
                                    placeholder = ColorPainter(c.surfaceSubtle), error = ColorPainter(c.surfaceSubtle),
                                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
                                )
                            }
                        }
                    }
                    Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        HqButton("Decline", { onDecline(r) }, variant = HqButtonVariant.Secondary, modifier = Modifier.weight(1f))
                        HqButton("Approve", { onApprove(r) }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
