package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import habitiq.app.data.BillInstance
import habitiq.app.data.RecurringBill
import habitiq.app.data.Settlement
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.lib.SuggestedSettlement
import habitiq.app.lib.currentMonthKey
import habitiq.app.lib.suggestSettlements
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqEmptyState
import habitiq.app.ui.components.HqRootAppBar
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

@Composable
fun BillsScreen(viewModel: FlatViewModel, onBack: (() -> Unit)? = null) {
    val c = LocalHqColors.current
    val bills by viewModel.recurringBills.collectAsStateWithLifecycleCompat()
    val instances by viewModel.billInstances.collectAsStateWithLifecycleCompat()
    val settlements by viewModel.settlements.collectAsStateWithLifecycleCompat()
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val trigger by viewModel.showBillsTrigger.collectAsStateWithLifecycleCompat()

    var tab by remember { mutableStateOf("Bills") }
    var showAddBill by remember { mutableStateOf(false) }
    var showSettle by remember { mutableStateOf(false) }
    var selectedBillId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(trigger) { if (trigger) { tab = "Bills"; viewModel.showBillsTrigger.value = false } }

    if (showAddBill) {
        AddRecurringBillScreen(viewModel, members, onBack = { showAddBill = false }, onSaved = { showAddBill = false })
        return
    }
    if (showSettle) {
        SettleUpScreen(viewModel, members, currentUser?.uid.orEmpty(), onBack = { showSettle = false })
        return
    }
    bills.find { it.id == selectedBillId }?.let { bill ->
        BillDetailScreen(
            bill = bill,
            instances = instances.filter { it.templateId == bill.id },
            members = members,
            onBack = { selectedBillId = null }
        )
        return
    }

    val month = currentMonthKey()
    val monthInstances = instances.filter { it.month == month }

    Column(Modifier.fillMaxSize().background(c.background)) {
        if (onBack != null) HqBackAppBar(title = "Monthly bills", onBack = onBack) else HqRootAppBar(title = "Monthly bills")
        Row(Modifier.padding(horizontal = HqSpacing.lg), horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            listOf("Bills", "Collections").forEach { label ->
                HqChip(label = label, selected = tab == label, onClick = { tab = label })
            }
        }
        when (tab) {
            "Bills" -> {
                if (bills.isEmpty()) {
                    if (isAdmin) {
                        HqEmptyState(
                            icon = Icons.Filled.Add,
                            title = "No recurring bills yet",
                            message = "Add rent, WiFi, and other bills that repeat every month.",
                            primaryLabel = "Add bill",
                            onPrimaryClick = { showAddBill = true },
                        )
                    } else {
                        // No action to offer a non-admin here, so HqEmptyState (which requires a
                        // primaryLabel) doesn't fit -- a plain styled message stands in instead.
                        Text(
                            "No recurring bills yet.",
                            style = HqType.bodyMedium,
                            color = c.textSecondary,
                            modifier = Modifier.padding(HqSpacing.lg),
                        )
                    }
                }
                LazyColumn(contentPadding = PaddingValues(HqSpacing.lg), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    if (bills.isNotEmpty()) item {
                        Text("Upcoming bills", style = HqType.titleMedium, color = c.textPrimary)
                    }
                    items(bills.filter { it.active }, key = { it.id }) { bill ->
                        RecurringBillCard(bill, monthInstances.find { it.templateId == bill.id }, isAdmin, members,
                            onOpen = { selectedBillId = bill.id },
                            onGenerate = { amt -> viewModel.generateBill(bill.id, amt) },
                            onMarkPaid = { inst -> viewModel.markBillPaid(inst.id) })
                    }
                    bills.firstOrNull { it.active && it.rotationQueue.isNotEmpty() }?.let { bill ->
                        item {
                            Spacer(Modifier.height(HqSpacing.sm))
                            Text("Payer rotation", style = HqType.titleMedium, color = c.textPrimary)
                            Spacer(Modifier.height(HqSpacing.sm))
                            HqCard {
                                Text(bill.name, style = HqType.titleSmall, color = c.textPrimary)
                                Text(
                                    bill.rotationQueue.mapIndexed { index, uid ->
                                        val name = members.find { it.uid == uid }?.nickname?.substringBefore(" ") ?: "Flatmate"
                                        if (index == bill.currentPayerIndex) "$name (current)" else name
                                    }.joinToString("  →  "),
                                    style = HqType.bodySmall,
                                    color = c.textSecondary
                                )
                            }
                        }
                    }
                }
            }
            "Collections" -> LazyColumn(contentPadding = PaddingValues(HqSpacing.lg), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                items(monthInstances, key = { it.id }) { inst ->
                    BillInstanceRow(
                        inst, members, isAdmin, currentUser?.uid.orEmpty(),
                        onMarkPaid = { viewModel.markBillPaid(inst.id) },
                        onSkip = { viewModel.skipBillInstance(inst.id) },
                        onToggleCollected = { uid, collected -> viewModel.markBillCollected(inst.id, uid, collected) }
                    )
                }
            }
            else -> {
                val suggestions = remember(instances, settlements, members) {
                    val nets = viewModel.computeNetBalances(month)
                    suggestSettlements(nets)
                }
                val isMonthClosed = viewModel.monthCycles.collectAsStateWithLifecycleCompat().value.any { it.month == month && it.status == "closed" }
                Column(Modifier.padding(HqSpacing.lg), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    if (isAdmin && !isMonthClosed) {
                        HqButton(text = "Close month ($month)", onClick = { viewModel.closeMonth(month) })
                    }
                    // "Closed"/"all settled" are calm/positive states -- success token, not a warning color.
                    if (isMonthClosed) Text("Month $month is closed.", style = HqType.bodyMedium, color = c.success)
                    if (suggestions.isEmpty()) Text("All settled for $month!", style = HqType.bodyMedium, color = c.success)
                    suggestions.forEach { s -> SettlementSuggestionRow(s, members, onConfirm = { viewModel.recordSettlement(s) }) }
                    Spacer(Modifier.height(HqSpacing.sm))
                    Text("Recorded settlements", style = HqType.titleSmall, color = c.textPrimary)
                    settlements.take(10).forEach { st -> SettlementHistoryRow(st, members) }
                    HqButton(text = "Manual settle up", onClick = { showSettle = true }, variant = HqButtonVariant.Secondary)
                }
            }
        }
    }
    if (isAdmin && tab == "Bills") {
        FloatingActionButton(
            onClick = { showAddBill = true },
            modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.BottomEnd).padding(HqSpacing.lg),
            containerColor = c.brandPrimary,
            contentColor = c.onBrandPrimary,
        ) { Icon(Icons.Filled.Add, "Add bill") }
    }
}

@Composable
private fun RecurringBillCard(
    bill: RecurringBill, instance: BillInstance?, isAdmin: Boolean, members: List<Member>,
    onOpen: () -> Unit,
    onGenerate: (Double?) -> Unit, onMarkPaid: (BillInstance) -> Unit
) {
    val c = LocalHqColors.current
    val icon = when {
        bill.name.contains("internet", true) || bill.name.contains("wifi", true) -> Icons.Filled.Wifi
        bill.name.contains("electric", true) || bill.name.contains("power", true) -> Icons.Filled.Bolt
        bill.name.contains("rent", true) || bill.name.contains("home", true) -> Icons.Filled.Home
        else -> Icons.AutoMirrored.Filled.ReceiptLong
    }
    HqCard(variant = HqCardVariant.Interactive, onClick = onOpen) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(c.warningContainer),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = c.warning) }
            Column(Modifier.weight(1f)) {
                Text(bill.name, style = HqType.titleSmall, color = c.textPrimary)
                Text(
                    if (bill.isVariable) "Variable · due day ${bill.billingDay}" else "₹${bill.amount?.toInt() ?: 0} / month · due day ${bill.billingDay}",
                    style = HqType.bodySmall, color = c.textSecondary
                )
            }
        }
        if (instance != null) {
            Text("This month: ${instance.status}${instance.amount?.let { a -> " · ₹${a.toInt()}" } ?: ""}", style = HqType.bodySmall, color = c.brandPrimary)
            if (instance.status == "split_generated" && instance.paidBy.isNotEmpty()) {
                HqTextButton(text = "Mark paid", onClick = { onMarkPaid(instance) })
            }
        } else if (isAdmin) {
            HqTextButton(text = "Generate this month", onClick = { onGenerate(if (bill.isVariable) null else bill.amount) })
        }
    }
}

@Composable
private fun BillInstanceRow(
    inst: BillInstance, members: List<Member>, isAdmin: Boolean, currentUid: String,
    onMarkPaid: () -> Unit, onSkip: () -> Unit,
    onToggleCollected: (String, Boolean) -> Unit
) {
    val c = LocalHqColors.current
    val payer = members.find { it.uid == inst.paidBy }?.nickname ?: inst.paidBy
    val collectorId = inst.collectorId ?: inst.paidBy
    val isCollector = collectorId == currentUid
    HqCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(inst.name, style = HqType.titleSmall, color = c.textPrimary)
                Text("${inst.status} · payer: $payer", style = HqType.bodySmall, color = c.textSecondary)
                inst.amount?.let { Text("₹${it.toInt()}", style = HqType.bodyMedium, color = c.textPrimary) }
            }
            Column {
                if (inst.status == "split_generated") HqTextButton(text = "Paid", onClick = onMarkPaid)
                if (isAdmin && inst.status != "skipped") HqTextButton(text = "Skip", onClick = onSkip)
            }
        }
        if (inst.status == "split_generated" && inst.splits != null && (isCollector || isAdmin)) {
            inst.participants.filter { it != inst.paidBy }.forEach { uid ->
                val name = members.find { it.uid == uid }?.nickname ?: uid
                val collected = inst.collectedFrom?.get(uid) == true
                val canToggle = isCollector || uid == currentUid
                if (canToggle) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = collected,
                            onCheckedChange = { onToggleCollected(uid, it) },
                            colors = CheckboxDefaults.colors(checkedColor = c.brandPrimary, uncheckedColor = c.borderDefault),
                        )
                        Text("$name ${if (collected) "received" else "pending"}", style = HqType.bodySmall, color = c.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettlementSuggestionRow(s: SuggestedSettlement, members: List<Member>, onConfirm: () -> Unit) {
    val c = LocalHqColors.current
    val from = members.find { it.uid == s.fromUserId }?.nickname ?: s.fromUserId
    val to = members.find { it.uid == s.toUserId }?.nickname ?: s.toUserId
    HqCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("$from → $to: ₹${s.amount.toInt()}", style = HqType.bodyMedium, color = c.textPrimary)
            HqButton(text = "Confirm", onClick = onConfirm, fullWidth = false)
        }
    }
}

@Composable
private fun SettlementHistoryRow(st: Settlement, members: List<Member>) {
    val c = LocalHqColors.current
    val from = members.find { it.uid == st.fromUserId }?.nickname ?: st.fromUserId
    val to = members.find { it.uid == st.toUserId }?.nickname ?: st.toUserId
    Text("$from paid $to ₹${st.amount.toInt()} · ${st.date}", style = HqType.bodySmall, color = c.textSecondary)
}

@Composable
private fun BillDetailScreen(
    bill: RecurringBill,
    instances: List<BillInstance>,
    members: List<Member>,
    onBack: () -> Unit
) {
    val c = LocalHqColors.current
    fun nameOf(uid: String) = members.find { it.uid == uid }?.nickname ?: uid
    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding()) {
        HqBackAppBar(title = bill.name, onBack = onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(HqSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.lg)
        ) {
            HqCard {
                Text(
                    if (bill.isVariable) "Variable amount" else "₹${bill.amount?.toInt() ?: 0}",
                    style = HqType.headlineSmall,
                    color = c.textPrimary
                )
                Text("Due on day ${bill.billingDay}", style = HqType.bodyMedium, color = c.textSecondary)
            }
            Text("Payer rotation", style = HqType.titleMedium, color = c.textPrimary)
            if (bill.rotationQueue.isEmpty()) {
                Text("No rotation set for this bill.", style = HqType.bodyMedium, color = c.textSecondary)
            } else {
                bill.rotationQueue.forEachIndexed { index, uid ->
                    val current = index == bill.currentPayerIndex
                    HqCard {
                        Text(nameOf(uid), style = HqType.titleSmall, color = c.textPrimary)
                        Text(
                            if (current) "Pays this round" else "In the rotation",
                            style = HqType.bodySmall,
                            color = if (current) c.brandPrimary else c.textSecondary
                        )
                    }
                }
            }
            Text("History", style = HqType.titleMedium, color = c.textPrimary)
            if (instances.isEmpty()) {
                Text("No months generated yet.", style = HqType.bodyMedium, color = c.textSecondary)
            } else {
                instances.sortedByDescending { it.month }.forEach { inst ->
                    HqCard {
                        Text(inst.month, style = HqType.titleSmall, color = c.textPrimary)
                        Text(
                            "${inst.status} · paid by ${nameOf(inst.paidBy)}${inst.amount?.let { " · ₹${it.toInt()}" } ?: ""}",
                            style = HqType.bodySmall,
                            color = c.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddRecurringBillScreen(viewModel: FlatViewModel, members: List<Member>, onBack: () -> Unit, onSaved: () -> Unit) {
    val c = LocalHqColors.current
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var billingDay by remember { mutableStateOf("1") }
    var isVariable by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding()) {
        HqBackAppBar(title = "Add Recurring Bill", onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(HqSpacing.xl), verticalArrangement = Arrangement.spacedBy(HqSpacing.lg)) {
            HqTextField(value = name, onValueChange = { name = it }, label = "Bill name", placeholder = "Rent, WiFi…", leadingIcon = Icons.AutoMirrored.Filled.ReceiptLong)
            if (!isVariable) HqTextField(value = amount, onValueChange = { amount = it }, label = "Amount (₹)", leadingIcon = Icons.AutoMirrored.Filled.ReceiptLong)
            HqTextField(value = billingDay, onValueChange = { billingDay = it }, label = "Billing day (1-28)", leadingIcon = Icons.Filled.CalendarMonth)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isVariable,
                    onCheckedChange = { isVariable = it },
                    colors = CheckboxDefaults.colors(checkedColor = c.brandPrimary, uncheckedColor = c.borderDefault),
                )
                Text("Variable amount (enter each month)", style = HqType.bodyMedium, color = c.textPrimary)
            }
            HqButton(text = "Save bill", onClick = {
                val amt = amount.toDoubleOrNull()
                viewModel.createRecurringBill(name, if (isVariable) null else amt, billingDay.toIntOrNull() ?: 1, isVariable, members.map { it.uid })
                onSaved()
            })
        }
    }
}

@Composable
private fun SettleUpScreen(viewModel: FlatViewModel, members: List<Member>, uid: String, onBack: () -> Unit) {
    val c = LocalHqColors.current
    var toUid by remember { mutableStateOf(members.firstOrNull { it.uid != uid }?.uid.orEmpty()) }
    var amount by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding()) {
        HqBackAppBar(title = "Settle up", onBack = onBack)
        Column(Modifier.padding(HqSpacing.xl), verticalArrangement = Arrangement.spacedBy(HqSpacing.lg)) {
            Text("Pay to", style = HqType.titleSmall, color = c.textPrimary)
            members.filter { it.uid != uid }.forEach { m ->
                HqChip(label = m.nickname, selected = toUid == m.uid, onClick = { toUid = m.uid })
            }
            HqTextField(value = amount, onValueChange = { amount = it }, label = "Amount (₹)")
            HqButton(text = "Record settlement", onClick = {
                amount.toDoubleOrNull()?.let { viewModel.recordManualSettlement(toUid, it) }
                onBack()
            })
        }
    }
}
