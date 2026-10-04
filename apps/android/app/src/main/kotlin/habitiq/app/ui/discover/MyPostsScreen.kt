package habitiq.app.ui.discover

import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
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
    /** Admin, or the member who posted the vacancy. */
    canManageVacancy: Boolean = isAdmin,
    /** The person's own vacancy request while it waits for (or was declined by) the admin. */
    pendingRequest: habitiq.app.data.VacancyRequest? = null,
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
    onBack: () -> Unit,
    /** Opens the vacancy form prefilled with the request (edit while pending, or fix and resend). */
    onEditRequest: () -> Unit = onEdit,
    /** Withdraws a pending request, or clears a declined one. */
    onWithdrawRequest: () -> Unit = {},
) {
    val c = LocalHqColors.current
    var tab by remember { mutableStateOf("active") }
    Column(Modifier.fillMaxSize().background(c.canvas)) {
        habitiq.app.ui.components.HqPageHeader(
            title = "My Posts",
            subtitle = "Manage what people see in Discover",
            onBack = onBack,
            modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal).padding(top = HqSpacing.sm),
        )
        habitiq.app.ui.components.HqSegmentedControl(
            options = listOf("Active", "Past"),
            selectedIndex = if (tab == "active") 0 else 1,
            onSelect = { tab = if (it == 0) "active" else "past" },
            modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.sm),
        )
        val vacancyStatus = vacancy?.let { PostStatusLogic.fromVacancy(it.active, it.postStatus) }
        val lookingStatus = mySeeker?.let { PostStatusLogic.fromSeeker(it.active) }
        val vacancyActive = vacancyStatus == DiscoveryPostStatus.PUBLISHED
        val lookingActive = lookingStatus == DiscoveryPostStatus.PUBLISHED
        val showVacancy = vacancy != null && if (tab == "active") vacancyActive else !vacancyActive
        val showLooking = mySeeker != null && if (tab == "active") lookingActive else !lookingActive

        // Pending and declined both still need the member's attention, so they sit under Active.
        val showPending = pendingRequest != null && tab == "active"

        if (!showVacancy && !showLooking && !showPending) {
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
            item {
                Row(
                    Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp)).background(c.statusWarningBg).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    androidx.compose.material3.Icon(habitiq.app.ui.components.HqIcons.Shield, null, tint = c.statusWarningFg, modifier = Modifier.size(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Keep locations approximate", style = HqType.labelMedium, color = c.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text("Exact addresses are never shown on public posts.", style = HqType.bodyMedium, color = c.textPrimary)
                    }
                }
            }
            if (showPending && pendingRequest != null) {
                item {
                    val pending = pendingRequest.status == "pending"
                    Column(
                        Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
                            .background(if (pending) c.statusInfoBg else c.statusDangerBg).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            if (pending) "Waiting for admin approval" else "Not approved",
                            style = HqType.titleSmall2, color = c.textPrimary,
                        )
                        val v = pendingRequest.vacancy
                        Text(
                            listOf(v.area, v.city).filter { it.isNotBlank() }.joinToString(" · ") +
                                (v.rentPerHead?.let { " · ${formatInr(it)}/head" } ?: ""),
                            style = HqType.bodyMedium, color = c.textSecondary,
                        )
                        Text(
                            if (pending) "Your vacancy goes live on Discover as soon as your flat admin approves it."
                            else "Your admin didn't approve this vacancy. Talk to them, then edit it and send it again.",
                            style = HqType.bodySmall, color = c.textSecondary,
                        )
                        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                            HqButton(
                                text = if (pending) "Edit request" else "Edit and resend",
                                onClick = onEditRequest,
                                variant = if (pending) HqButtonVariant.Secondary else HqButtonVariant.Primary,
                                fullWidth = false,
                            )
                            HqTextButton(text = if (pending) "Withdraw" else "Remove", onClick = onWithdrawRequest)
                        }
                    }
                }
            }
            if (showVacancy && flat != null) {
                item {
                    PostManageCard(
                        title = flat.name,
                        subtitle = "${vacancy.area} · ${vacancy.city}".trim(' ', '·') + " · Looking for a flatmate",
                        status = vacancyStatus?.name ?: "",
                        meta = if (canManageVacancy) "$incomingCount connections" else "Ask your admin or the person who posted it to change this",
                        canManage = canManageVacancy,
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
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(c.surfaceBase).border(1.dp, c.borderSubtle, shape).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(HqSpacing.sm),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            habitiq.app.ui.components.HqIconTile(
                if (title.startsWith("Looking")) habitiq.app.ui.components.HqIcons.Profile else habitiq.app.ui.components.HqIcons.Home,
                habitiq.app.ui.components.HqTileTone.Teal, size = 46,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                habitiq.app.ui.components.HqBadge(
                    status.lowercase().replaceFirstChar { it.uppercase() },
                    if (isPublished) habitiq.app.ui.components.HqBadgeTone.Success else habitiq.app.ui.components.HqBadgeTone.Neutral,
                )
                Text(title, style = HqType.titleSmall2, color = c.textPrimary)
                Text(subtitle, style = HqType.bodyMedium, color = c.textSecondary)
                Text(meta, style = HqType.labelSmall, color = c.textMuted)
            }
        }
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
