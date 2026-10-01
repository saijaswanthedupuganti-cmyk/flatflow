package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import habitiq.app.flat.FlatViewModel
import habitiq.app.lib.currentMonthKey
import habitiq.app.lib.computeMonthNetBalances
import habitiq.app.lib.daysOverdue
import habitiq.app.lib.effectiveTaskStatus
import habitiq.app.lib.pairwisePersonalBalances
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import kotlin.math.abs

/**
 * Manage Flat -- the operational hub for the current flat (master spec sections 16-20, 60-64).
 * Tasks and Expenses are NOT separate root destinations; they're areas within this one hub, and
 * Monthly Bills lives inside Expenses so the hub keeps only two clear primary choices. Not to be confused with [FlatSettingsScreen] (flat name / join
 * mode / Discover vacancy listing), which is an admin settings screen reached from Profile.
 */
enum class ManageFlatArea { HUB, TASKS, EXPENSES }

@Composable
fun ManageFlatHub(
    viewModel: FlatViewModel,
    area: ManageFlatArea,
    onAreaChange: (ManageFlatArea) -> Unit,
    onOpenGoingAway: () -> Unit,
    onOpenTaskDetail: (String) -> Unit,
    onOpenCreateTask: () -> Unit,
    onReviewSwaps: () -> Unit,
    onOpenBills: () -> Unit,
    onOpenActivity: () -> Unit
) {
    val c = LocalHqColors.current
    when (area) {
        ManageFlatArea.TASKS -> TasksScreen(
                viewModel = viewModel,
                onBack = { onAreaChange(ManageFlatArea.HUB) },
                onOpenGoingAway = onOpenGoingAway,
                onOpenTaskDetail = onOpenTaskDetail,
                onOpenCreateTask = onOpenCreateTask,
                onReviewSwaps = onReviewSwaps,
                modifier = Modifier.fillMaxSize()
            )
        ManageFlatArea.EXPENSES -> ExpensesScreen(
            viewModel,
            onBack = { onAreaChange(ManageFlatArea.HUB) },
            onOpenBills = onOpenBills,
            modifier = Modifier.fillMaxSize()
        )
        ManageFlatArea.HUB -> ManageFlatLanding(
            viewModel = viewModel,
            onAreaChange = onAreaChange,
            onOpenTaskDetail = onOpenTaskDetail,
            onReviewSwaps = onReviewSwaps,
            onOpenActivity = onOpenActivity
        )
    }
}

@Composable
private fun ManageFlatLanding(
    viewModel: FlatViewModel,
    onAreaChange: (ManageFlatArea) -> Unit,
    onOpenTaskDetail: (String) -> Unit,
    onReviewSwaps: () -> Unit,
    onOpenActivity: () -> Unit
) {
    val c = LocalHqColors.current
    val flatInfo by viewModel.flatInfo.collectAsStateWithLifecycleCompat()
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val tasks by viewModel.tasks.collectAsStateWithLifecycleCompat()
    val expenses by viewModel.expenses.collectAsStateWithLifecycleCompat()
    val settlements by viewModel.settlements.collectAsStateWithLifecycleCompat()
    val billInstances by viewModel.billInstances.collectAsStateWithLifecycleCompat()
    val swapRequests by viewModel.swapRequests.collectAsStateWithLifecycleCompat()
    val activity by viewModel.activity.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val uid = currentUser?.uid.orEmpty()

    val myTasks = tasks.filter { it.currentAssignedUserId == uid }
    val mine = myTasks.count { effectiveTaskStatus(it) != "completed" }
    val myOverdueTasks = myTasks.filter { effectiveTaskStatus(it) == "overdue" }
        .sortedByDescending { daysOverdue(it.dueDate) }
    val overdue = myOverdueTasks.size

    val monthNets = remember(expenses, billInstances, settlements, uid) {
        computeMonthNetBalances(currentMonthKey(), expenses, billInstances, settlements)
    }
    val myNet = monthNets[uid] ?: 0.0
    val people = remember(expenses, settlements, uid) { pairwisePersonalBalances(expenses, settlements, uid) }
    val owePeople = people.count { it.value < -0.5 }
    val totalIOwe = people.values.filter { it < -0.5 }.sumOf { abs(it) }

    val myPendingSwaps = swapRequests.filter { it.status == "pending" && it.toUserId == uid }
    val allMyPendingSwaps = swapRequests.filter {
        it.status == "pending" && (it.toUserId == uid || it.fromUserId == uid)
    }

    val hasAttention = myOverdueTasks.isNotEmpty() || totalIOwe > 0.5 || allMyPendingSwaps.isNotEmpty()

    Column(
        Modifier.fillMaxSize().background(c.background).verticalScroll(rememberScrollState()).padding(HqSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(HqSpacing.md)
    ) {
        FlatContextHeader(name = flatInfo?.name.orEmpty(), memberCount = members.size.takeIf { it > 0 } ?: flatInfo?.memberCount ?: 0)

        Spacer(Modifier.height(HqSpacing.sm))
        Text("MANAGE YOUR FLAT", style = HqType.labelMedium, color = c.textTertiary)

        HubCard(
            icon = Icons.Filled.Checklist,
            iconTint = c.brandPrimary,
            iconBackground = c.brandPrimaryContainer,
            title = "Tasks",
            subtitle = when {
                overdue > 0 -> "$mine active · $overdue overdue"
                mine == 1 -> "1 active task assigned to you"
                else -> "$mine active tasks assigned to you"
            },
            onClick = { onAreaChange(ManageFlatArea.TASKS) }
        )
        HubCard(
            icon = Icons.Filled.Payments,
            iconTint = c.warning,
            iconBackground = c.warningContainer,
            title = "Expenses",
            subtitle = when {
                myNet < -0.5 -> "You owe ₹${abs(myNet).toInt()}" + if (owePeople > 0) " to $owePeople ${if (owePeople == 1) "person" else "people"}" else ""
                myNet > 0.5 -> "You're owed ₹${myNet.toInt()}"
                else -> "All balances settled"
            },
            onClick = { onAreaChange(ManageFlatArea.EXPENSES) }
        )

        Spacer(Modifier.height(HqSpacing.sm))
        Text("NEEDS YOUR ATTENTION", style = HqType.labelMedium, color = c.textTertiary)

        if (!hasAttention) {
            HqCard(variant = HqCardVariant.Status, statusTint = c.successContainer) {
                Text("You're all caught up.", style = HqType.bodyMedium, color = c.success)
            }
        } else {
            myOverdueTasks.firstOrNull()?.let { task ->
                val days = daysOverdue(task.dueDate)
                AttentionCard(
                    icon = Icons.Filled.Warning,
                    tint = c.errorContainer,
                    iconTint = c.error,
                    title = "Overdue task",
                    body = task.name,
                    meta = "${days}d overdue · assigned to you" + if (overdue > 1) " · ${overdue - 1} more overdue" else "",
                    ctaLabel = "View task",
                    onClick = { onOpenTaskDetail(task.taskId) }
                )
            }
            if (totalIOwe > 0.5) {
                AttentionCard(
                    icon = Icons.Filled.Payments,
                    tint = c.warningContainer,
                    iconTint = c.warning,
                    title = "Payment due",
                    body = "₹${totalIOwe.toInt()}",
                    meta = "to $owePeople ${if (owePeople == 1) "person" else "people"}",
                    ctaLabel = "Pay now",
                    onClick = {
                        viewModel.showBalancesTrigger.value = true
                        onAreaChange(ManageFlatArea.EXPENSES)
                    }
                )
            }
            if (allMyPendingSwaps.isNotEmpty()) {
                AttentionCard(
                    icon = Icons.Filled.SwapHoriz,
                    tint = c.infoContainer,
                    iconTint = c.info,
                    title = if (allMyPendingSwaps.size == 1) "Swap request" else "Swap requests",
                    body = "${allMyPendingSwaps.size} ${if (allMyPendingSwaps.size == 1) "request" else "requests"}",
                    meta = if (myPendingSwaps.isNotEmpty()) "${myPendingSwaps.size} needs your response" else "waiting on a flatmate",
                    ctaLabel = "Review",
                    onClick = onReviewSwaps
                )
            }
        }

        if (activity.isNotEmpty()) {
            Spacer(Modifier.height(HqSpacing.sm))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("RECENT ACTIVITY", style = HqType.labelMedium, color = c.textTertiary)
                TextButton(onClick = onOpenActivity) {
                    Text("View all", style = HqType.labelLarge, color = c.brandPrimary)
                }
            }
            activity.take(3).forEach { entry ->
                HqCard(padding = HqSpacing.md) {
                    Text(entry.details, style = HqType.bodyMedium, color = c.textPrimary)
                    Text(
                        "${entry.action} · ${entry.timestamp.take(19).replace('T', ' ')}",
                        style = HqType.caption,
                        color = c.textTertiary
                    )
                }
            }
        }

        Spacer(Modifier.height(HqSpacing.xxl))
    }
}

@Composable
private fun FlatContextHeader(name: String, memberCount: Int) {
    val c = LocalHqColors.current
    Column {
        Text("Manage", style = HqType.headlineLarge, color = c.textPrimary)
        Text(
            "Keep your household running smoothly.",
            style = HqType.bodyMedium,
            color = c.textSecondary
        )
        if (name.isNotBlank()) {
            Text(
                "$name · ${if (memberCount == 1) "1 member" else "$memberCount members"}",
                style = HqType.caption,
                color = c.textTertiary,
                modifier = Modifier.padding(top = HqSpacing.xs)
            )
        }
    }
}

@Composable
private fun AttentionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    body: String,
    meta: String,
    ctaLabel: String,
    onClick: () -> Unit
) {
    val c = LocalHqColors.current
    HqCard(variant = HqCardVariant.Status, statusTint = tint) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(HqIconSize.md))
            Column(Modifier.weight(1f)) {
                Text(title.uppercase(), style = HqType.labelSmall, color = c.textSecondary)
                Text(body, style = HqType.titleLarge, color = c.textPrimary)
                Text(meta, style = HqType.bodySmall, color = c.textSecondary)
            }
        }
        Spacer(Modifier.height(HqSpacing.sm))
        HqButton(text = ctaLabel, onClick = onClick, variant = HqButtonVariant.Secondary, fullWidth = false)
    }
}

@Composable
private fun HubCard(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val c = LocalHqColors.current
    HqCard(variant = HqCardVariant.Interactive, onClick = onClick, padding = HqSpacing.xl) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.lg)
        ) {
            androidx.compose.foundation.layout.Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(HqIconSize.md))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = HqType.titleLarge, color = c.textPrimary)
                Text(subtitle, style = HqType.bodySmall, color = c.textSecondary)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = c.textTertiary, modifier = Modifier.size(HqIconSize.md))
        }
    }
}
