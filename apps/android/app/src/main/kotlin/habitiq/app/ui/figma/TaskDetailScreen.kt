package habitiq.app.ui.figma

import habitiq.app.lib.formatDueLabel
import habitiq.app.ui.components.hqTaskMotifVector
import habitiq.app.ui.components.hqTaskMotifFor
import habitiq.app.ui.components.HqBottomSheet
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqTileTone
import habitiq.app.ui.components.HqCalloutCard
import habitiq.app.ui.components.hqToneFor
import habitiq.app.ui.components.HqAvatarSize
import habitiq.app.ui.components.HqAvatar
import habitiq.app.ui.components.HqBadgeTone
import habitiq.app.ui.components.HqBadge
import habitiq.app.ui.components.HqSectionTitle
import habitiq.app.ui.components.HqPageHeader
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.data.FlatTask
import habitiq.app.flats.Member
import habitiq.app.lib.RotationEngine
import habitiq.app.lib.effectiveTaskStatus
import habitiq.app.lib.formatExpectedDue
import habitiq.app.lib.parseTaskInstant
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.theme.hqShadow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class QueueEntry(
    val member: Member,
    val position: Int,
    val state: QueueState,
    val subtitle: String,
    val reliability: Int? = null
)

enum class QueueState { NOW, NEXT, SKIPPED, QUEUED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    task: FlatTask,
    members: List<Member>,
    currentUid: String,
    onBack: () -> Unit,
    onRequestSwap: (String) -> Unit = {},
    onComplete: (() -> Unit)? = null,
    onOpenAway: (() -> Unit)? = null,
) {
    val c = LocalHqColors.current
    val queue = remember(task, members) { buildQueue(task, members) }
    val mine = task.currentAssignedUserId == currentUid
    val done = task.status == "completed"
    val overdue = effectiveTaskStatus(task) == "overdue"
    val assignee = members.find { it.uid == task.currentAssignedUserId }
    var swapOpen by remember { mutableStateOf(false) }

    FigmaScreenBackground {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState())
                .padding(horizontal = HqSpacing.screenHorizontal).padding(top = HqSpacing.sm, bottom = HqSpacing.screenEnd),
        ) {
            HqPageHeader(title = "Task details", onBack = onBack)

            // Figma task-focus: dark card with status, title, notes, due/assignee tiles and the main action.
            Column(
                Modifier.fillMaxWidth()
                    .hqShadow(habitiq.app.ui.theme.HqShadow.Floating, RoundedCornerShape(HqRadius.card))
                    .clip(RoundedCornerShape(HqRadius.card))
                    .background(c.brandInk)
                    .padding(20.dp),
            ) {
                hqTaskMotifFor(task.name)?.let {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                        Icon(hqTaskMotifVector(it), null, tint = c.onBrandInk.copy(alpha = .12f), modifier = Modifier.size(110.dp))
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    HqBadge(
                        when { done -> "Done"; overdue -> "Overdue"; else -> formatDueLabel(task.dueDate) },
                        if (done) HqBadgeTone.Success else HqBadgeTone.Warning,
                    )
                    Text(task.frequency.replaceFirstChar { it.uppercase() }, style = HqType.labelSmall, color = c.onBrandInk.copy(alpha = .8f))
                }
                Text(task.name, style = HqType.titleLarge2, color = c.onBrandInk, modifier = Modifier.padding(top = 14.dp, bottom = 6.dp))
                if (task.notes.isNotBlank()) Text(task.notes, style = HqType.bodyMedium, color = c.onBrandInk.copy(alpha = .8f))
                Row(Modifier.padding(top = 18.dp, bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FocusTile(Modifier.weight(1f), label = "Due", value = formatDueLabel(task.dueDate)) {
                        Box(Modifier.size(31.dp).clip(RoundedCornerShape(10.dp)).background(c.onBrandInk.copy(alpha = .12f)), contentAlignment = Alignment.Center) {
                            Icon(HqIcons.Clock, null, tint = c.onBrandInk, modifier = Modifier.size(17.dp))
                        }
                    }
                    FocusTile(Modifier.weight(1f), label = "Assigned to", value = if (mine) "You" else assignee?.nickname?.substringBefore(" ") ?: "Unassigned") {
                        HqAvatar(assignee?.nickname ?: "?", size = HqAvatarSize.SM, tone = hqToneFor(assignee?.nickname.orEmpty(), mine))
                    }
                }
                if (onComplete != null && mine && !done) {
                    HqButton(
                        text = "Mark done",
                        onClick = onComplete,
                        leadingIcon = HqIcons.Check,
                        haptic = true,
                    )
                } else if (!mine && !done) {
                    Text(
                        "${assignee?.nickname?.substringBefore(" ") ?: "A flatmate"} is on this task",
                        style = HqType.labelMedium, color = c.onBrandInk.copy(alpha = .8f),
                    )
                }
            }

            if (queue.isNotEmpty()) {
                HqSectionTitle("Who takes turns")
                Row(
                    Modifier.fillMaxWidth().hqShadow(habitiq.app.ui.theme.HqShadow.Floating, RoundedCornerShape(HqRadius.card))
                        .clip(RoundedCornerShape(20.dp)).background(c.surfaceBase).border(1.dp, c.borderSubtle, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    queue.take(3).forEach { entry ->
                        val label = when (entry.state) {
                            QueueState.NOW -> "Now"; QueueState.NEXT -> "Next"; QueueState.SKIPPED -> "Away"; QueueState.QUEUED -> "Then"
                        }
                        val name = entry.member.nickname.substringBefore(" ")
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                label, style = HqType.labelSmall, fontWeight = FontWeight.ExtraBold,
                                color = if (entry.state == QueueState.NOW) c.textBrand else c.textMuted,
                            )
                            HqAvatar(entry.member.nickname, size = HqAvatarSize.LG, tone = hqToneFor(entry.member.nickname, entry.member.uid == currentUid))
                            Text(if (entry.member.uid == currentUid) "You" else name, style = HqType.labelMedium, color = c.textPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (mine && !done) {
                HqSectionTitle("Can't do this turn?")
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    if (onOpenAway != null) {
                        HqCalloutCard("I'm away", "Pause my turns for a while", HqIcons.Clock, HqTileTone.Neutral, onClick = onOpenAway)
                    }
                    HqCalloutCard("Swap turn", "Ask a flatmate to take this turn", HqIcons.Users, HqTileTone.Teal, onClick = { swapOpen = true })
                }
                Row(
                    Modifier.padding(top = 14.dp).fillMaxWidth().clip(RoundedCornerShape(HqRadius.card)).background(c.selectedBg).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(HqIcons.Home, null, tint = c.textBrand, modifier = Modifier.size(HqIconSize.sm))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Keep your flat running smoothly.", style = HqType.labelMedium, color = c.textBrand, fontWeight = FontWeight.Bold)
                        Text("If you can't do this task, pause your turns or request a swap with a flatmate.", style = HqType.bodyMedium, color = c.textBrand)
                    }
                }
            }

            HqSectionTitle("History")
            Text(
                if (task.lastCompletedAt.isBlank()) "No completions yet" else "Last completed ${task.lastCompletedAt}",
                style = HqType.bodyMedium, color = c.textSecondary,
            )
        }
    }

    if (swapOpen) {
        val candidates = members.filter { it.uid != currentUid && it.status != "out_of_station" }
        var picked by remember { mutableStateOf(candidates.firstOrNull()?.uid) }
        HqBottomSheet(onDismiss = { swapOpen = false }, title = "Request a swap") {
            Text("Choose someone available for this turn.", style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(bottom = 12.dp))
            candidates.forEach { m ->
                val selected = picked == m.uid
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp))
                        .background(if (selected) c.selectedBg else c.surfaceBase)
                        .border(1.dp, if (selected) c.selectedBorder else c.borderSubtle, RoundedCornerShape(16.dp))
                        .selectable(selected = selected, role = androidx.compose.ui.semantics.Role.RadioButton, onClick = { picked = m.uid })
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically,
                ) {
                    HqAvatar(m.nickname, size = HqAvatarSize.MD, tone = hqToneFor(m.nickname, false))
                    Text(m.nickname, style = HqType.rowTitle, color = c.textPrimary, modifier = Modifier.weight(1f))
                    if (selected) Icon(HqIcons.Check, null, tint = c.selectedFg, modifier = Modifier.size(HqIconSize.sm))
                }
            }
            Row(Modifier.padding(top = 14.dp, bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.surfaceSubtle).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(HqIcons.Clock, null, tint = c.textSecondary, modifier = Modifier.size(HqIconSize.sm))
                Text("The task stays yours until a flatmate accepts. Turns change only after that.", style = HqType.bodyMedium, color = c.textSecondary)
            }
            HqButton(text = "Send swap request", enabled = picked != null, onClick = { picked?.let { onRequestSwap(it) }; swapOpen = false })
            Spacer(Modifier.height(HqSpacing.xxl))
        }
    }
}

@Composable
private fun FocusTile(modifier: Modifier, label: String, value: String, leading: @Composable () -> Unit) {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier.defaultMinSize(minHeight = 54.dp).clip(shape).background(c.onBrandInk.copy(alpha = .08f)).border(1.dp, c.onBrandInk.copy(alpha = .14f), shape).padding(9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = HqType.labelSmall, color = c.onBrandInk.copy(alpha = .8f), fontWeight = FontWeight.Bold)
            Text(value, style = HqType.labelMedium, color = c.onBrandInk, fontWeight = FontWeight.Bold)
        }
    }
}

private fun buildQueue(task: FlatTask, members: List<Member>): List<QueueEntry> {
    val order = task.queueOrder.ifEmpty { members.map { it.uid } }
    val currentIndex = order.indexOf(task.currentAssignedUserId).coerceAtLeast(0)
    val nextUid = RotationEngine.getNextAssignee(task, members)
    return order.mapIndexedNotNull { index, uid ->
        val member = members.find { it.uid == uid } ?: return@mapIndexedNotNull null
        val position = index + 1
        val isOos = member.status == "out_of_station" || member.status == "inactive"
        val state = when {
            uid == task.currentAssignedUserId && !isOos -> QueueState.NOW
            uid == nextUid && uid != task.currentAssignedUserId -> QueueState.NEXT
            isOos -> QueueState.SKIPPED
            else -> QueueState.QUEUED
        }
        val subtitle = when (state) {
            // Rotation shows engine order only: no reliability or score values (design doc 3.3 / 3.5).
            QueueState.NOW -> formatLastDone(task.lastCompletedAt)
            QueueState.NEXT -> "Expected: ${formatExpectedDue(task.dueDate)}"
            QueueState.QUEUED -> ""
            QueueState.SKIPPED -> ""
        }
        QueueEntry(member, position, state, subtitle)
    }
}

private fun computeOverdueDays(task: FlatTask): Int? {
    if (effectiveTaskStatus(task) != "overdue") return null
    return runCatching {
        val due = parseTaskInstant(task.dueDate) ?: return@runCatching null
        java.time.Duration.between(due, Instant.now()).toDays().toInt().coerceAtLeast(1)
    }.getOrNull()
}

private fun formatLastDone(iso: String): String = runCatching {
    val dt = Instant.parse(iso).atZone(ZoneId.systemDefault())
    "Last done: ${dt.format(DateTimeFormatter.ofPattern("EEE dd MMM, h:mm a"))}"
}.getOrDefault("Last done: —")
