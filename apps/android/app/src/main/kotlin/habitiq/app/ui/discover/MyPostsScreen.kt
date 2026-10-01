package habitiq.app.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import habitiq.app.data.SeekerProfile
import habitiq.app.data.VacancyData
import habitiq.app.discover.DiscoveryPostStatus
import habitiq.app.discover.PostStatusLogic
import habitiq.app.flats.FlatInfo
import habitiq.app.lib.formatInr
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

@Composable
fun MyPostsScreen(
    isAdmin: Boolean,
    flat: FlatInfo?,
    vacancy: VacancyData?,
    mySeeker: SeekerProfile?,
    incomingCount: Int,
    onPauseVacancy: () -> Unit,
    onResumeVacancy: () -> Unit,
    onCloseVacancy: () -> Unit,
    onPauseLooking: () -> Unit,
    onResumeLooking: () -> Unit,
    onEdit: () -> Unit,
    onViewConnections: () -> Unit,
    onBack: () -> Unit
) {
    val c = LocalHqColors.current
    var tab by remember { mutableStateOf("active") }
    Column(Modifier.fillMaxSize().background(c.background)) {
        HqTextButton(text = "← Back", onClick = onBack, modifier = Modifier.padding(start = HqSpacing.sm, top = HqSpacing.sm))
        Text("My posts", style = HqType.headlineMedium, color = c.textPrimary, modifier = Modifier.padding(horizontal = HqSpacing.xl))
        Row(Modifier.padding(HqSpacing.xl), horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            HqChip(label = "Active", selected = tab == "active", onClick = { tab = "active" })
            HqChip(label = "Past", selected = tab == "past", onClick = { tab = "past" })
        }
        val vacancyStatus = vacancy?.let { PostStatusLogic.fromVacancy(it.active, it.postStatus) }
        val lookingStatus = mySeeker?.let { PostStatusLogic.fromSeeker(it.active) }
        val vacancyActive = vacancyStatus == DiscoveryPostStatus.PUBLISHED
        val lookingActive = lookingStatus == DiscoveryPostStatus.PUBLISHED
        val showVacancy = vacancy != null && if (tab == "active") vacancyActive else !vacancyActive
        val showLooking = mySeeker != null && if (tab == "active") lookingActive else !lookingActive

        if (!showVacancy && !showLooking) {
            DiscoveryEmpty(
                title = if (tab == "active") "No active posts" else "No past posts",
                body = "Create a vacancy or a looking post from +."
            )
            return
        }

        LazyColumn(
            contentPadding = PaddingValues(HqSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.md)
        ) {
            if (showVacancy && flat != null) {
                item {
                    PostManageCard(
                        title = flat.name,
                        subtitle = "${vacancy.area} · ${vacancy.city}".trim(' ', '·') + " · Looking for a flatmate",
                        status = vacancyStatus?.name ?: "",
                        meta = if (isAdmin) "$incomingCount connections" else "Ask an admin to manage this vacancy",
                        canManage = isAdmin,
                        isPublished = vacancyActive,
                        onEdit = onEdit,
                        onPause = onPauseVacancy,
                        onResume = onResumeVacancy,
                        onClose = onCloseVacancy,
                        allowClose = true,
                        onConnections = onViewConnections
                    )
                }
            }
            if (showLooking) {
                item {
                    PostManageCard(
                        title = "Looking for a flat",
                        subtitle = "${mySeeker.lookingIn.ifBlank { mySeeker.city }} · ${formatInr(mySeeker.budget)}",
                        status = lookingStatus?.name ?: "",
                        meta = "Your looking post",
                        canManage = true,
                        isPublished = lookingActive,
                        onEdit = onEdit,
                        onPause = onPauseLooking,
                        onResume = onResumeLooking,
                        onClose = onPauseLooking,
                        allowClose = false,
                        onConnections = onViewConnections
                    )
                }
            }
        }
    }
}

@Composable
private fun PostManageCard(
    title: String,
    subtitle: String,
    status: String,
    meta: String,
    canManage: Boolean,
    isPublished: Boolean,
    onEdit: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onClose: () -> Unit,
    allowClose: Boolean,
    onConnections: () -> Unit
) {
    val c = LocalHqColors.current
    HqCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            Text(title, style = HqType.titleLarge, color = c.textPrimary)
            Text(subtitle, style = HqType.bodySmall, color = c.textSecondary)
            Text(status.lowercase().replaceFirstChar { it.uppercase() }, style = HqType.labelMedium, color = c.brandPrimary)
            Text(meta, style = HqType.caption, color = c.textTertiary)
            if (canManage) {
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    HqButton(text = "Edit", onClick = onEdit, variant = HqButtonVariant.Secondary, fullWidth = false)
                    if (isPublished) HqButton(text = "Pause", onClick = onPause, variant = HqButtonVariant.Secondary, fullWidth = false)
                    else HqButton(text = "Resume", onClick = onResume, fullWidth = false)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    HqButton(text = "Connections", onClick = onConnections, variant = HqButtonVariant.Secondary, fullWidth = false)
                    if (allowClose) HqTextButton(text = "Close", onClick = onClose)
                }
            }
        }
    }
}
