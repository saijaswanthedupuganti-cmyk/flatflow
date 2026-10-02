package habitiq.app.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.activity.compose.BackHandler
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSize
import habitiq.app.data.billStatusLabel
import habitiq.app.lib.formatInr
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
fun BillsScreen(
    viewModel: FlatViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    /** When set, the screen is embedded (Manage > Expenses > Monthly bills): no page header or FAB of its own. */
    header: (@Composable () -> Unit)? = null,
) {
    val c = LocalHqColors.current
    val bills by viewModel.recurringBills.collectAsStateWithLifecycleCompat()
    val instances by viewModel.billInstances.collectAsStateWithLifecycleCompat()
    val settlements by viewModel.settlements.collectAsStateWithLifecycleCompat()
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val trigger by viewModel.showBillsTrigger.collectAsStateWithLifecycleCompat()
    val addTrigger by viewModel.showAddBillTrigger.collectAsStateWithLifecycleCompat()

    var tab by remember { mutableStateOf("Bills") }
    var showAddBill by remember { mutableStateOf(false) }
    var showSettle by remember { mutableStateOf(false) }
    var selectedBillId by remember { mutableStateOf<String?>(null) }
    BackHandler(enabled = showAddBill || showSettle || selectedBillId != null) {
        when {
            showAddBill -> showAddBill = false
            showSettle -> showSettle = false
            else -> selectedBillId = null
        }
    }
    LaunchedEffect(trigger) { if (trigger) { tab = "Bills"; viewModel.showBillsTrigger.value = false } }
    LaunchedEffect(addTrigger, isAdmin) {
        if (addTrigger) { if (isAdmin) showAddBill = true; viewModel.showAddBillTrigger.value = false }
    }

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

    // One scrolling column so an embedding header (Manage title, Tasks/Expenses, Daily/Monthly) scrolls with it.
    Column(
        modifier.fillMaxSize().background(c.canvas).verticalScroll(rememberScrollState())
            .padding(bottom = HqSpacing.screenEnd + 72.dp),
    ) {
        if (header != null) {
            Column(Modifier.padding(horizontal = HqSpacing.screenHorizontal).padding(top = HqSpacing.sm)) { header() }
            Spacer(Modifier.height(HqSpacing.md))
        } else {
            habitiq.app.ui.components.HqPageHeader(
                title = "Monthly Bills",
                subtitle = "Recurring household bills for $month",
                onBack = onBack,
                modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal).padding(top = HqSpacing.sm, bottom = HqSpacing.md),
            )
        }
        // Third level of hierarchy: light chips, so it never looks like the switches above it.
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = HqSpacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
        ) {
            listOf("Bills" to "Bills", "Collections" to "Collections", "Close" to "Close month").forEach { (key, label) ->
                habitiq.app.ui.components.HqChip(label = label, selected = tab == key, onClick = { tab = key })
            }
        }
        if (isAdmin && tab == "Bills" && bills.isNotEmpty()) {
            Box(Modifier.padding(horizontal = HqSpacing.screenHorizontal).padding(top = HqSpacing.md)) {
                HqButton(text = "Add bill", onClick = { showAddBill = true }, variant = HqButtonVariant.Secondary, leadingIcon = habitiq.app.ui.components.HqIcons.Plus)
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
                Column(Modifier.padding(HqSpacing.lg), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    if (bills.isNotEmpty()) {
                        habitiq.app.ui.components.HqListHeading("Upcoming", right = month)
                    }
                    bills.filter { it.active }.forEach { bill ->
                        RecurringBillCard(bill, monthInstances.find { it.templateId == bill.id }, isAdmin, members,
                            onOpen = { selectedBillId = bill.id },
                            onGenerate = { amt -> viewModel.generateBill(bill.id, amt) },
                            onMarkPaid = { inst -> viewModel.markBillPaid(inst.id) })
                    }
                    bills.firstOrNull { it.active && it.rotationQueue.isNotEmpty() }?.let { bill ->
                        run {
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
            "Collections" -> Column(Modifier.padding(HqSpacing.lg), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                if (monthInstances.isEmpty()) {
                    Text("No bills generated for $month yet.", style = HqType.bodyMedium, color = c.textSecondary)
                }
                monthInstances.forEach { inst ->
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
                val total = monthInstances.sumOf { it.amount ?: 0.0 }
                val paid = monthInstances.count { it.status == "paid" }
                val skipped = monthInstances.count { it.status == "skipped" }
                val outstanding = suggestions.sumOf { it.amount }
                Column(
                    Modifier.padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(HqSpacing.sm),
                ) {
                    // Figma close-month-hero.
                    val heroShape = RoundedCornerShape(20.dp)
                    Row(
                        Modifier.fillMaxWidth().clip(heroShape).background(c.surfaceSubtle).padding(18.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically,
                    ) {
                        habitiq.app.ui.components.HqIconTile(habitiq.app.ui.components.HqIcons.Receipt, habitiq.app.ui.components.HqTileTone.Sand)
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("MONTHLY TOTAL", style = HqType.labelSmall, color = c.textMuted, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                            Text(formatInr(total), style = HqType.titleMedium2, color = c.textPrimary)
                            Text("${monthInstances.size} household ${if (monthInstances.size == 1) "bill" else "bills"}", style = HqType.bodyMedium, color = c.textSecondary)
                        }
                    }
                    if (isMonthClosed) {
                        Text("Month $month is closed.", style = HqType.bodyMedium, color = c.statusSuccessFg)
                    } else {
                        habitiq.app.ui.components.HqSectionTitle("Before you close")
                        if (suggestions.isNotEmpty()) {
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.statusWarningBg).padding(15.dp),
                                horizontalArrangement = Arrangement.spacedBy(11.dp),
                            ) {
                                Text("!", style = HqType.titleSmall2, color = c.statusWarningFg)
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text("${suggestions.size} ${if (suggestions.size == 1) "balance is" else "balances are"} still outstanding", style = HqType.rowTitle, color = c.statusWarningFg)
                                    Text("You can close the month now, but these balances will carry forward.", style = HqType.bodyMedium, color = c.statusWarningFg)
                                }
                            }
                        }
                    }
                    ReviewRow("Paid bills", "$paid")
                    ReviewRow("Skipped bills", "$skipped")
                    ReviewRow("Outstanding balances", formatInr(outstanding), warning = outstanding > 0)
                    if (suggestions.isEmpty()) Text("All settled for $month!", style = HqType.bodyMedium, color = c.statusSuccessFg)
                    suggestions.forEach { s -> SettlementSuggestionRow(s, members, onConfirm = { viewModel.recordSettlement(s) }) }
                    Row(
                        Modifier.padding(top = 10.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.surfaceSubtle).padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(habitiq.app.ui.components.HqIcons.Shield, null, tint = c.textSecondary, modifier = Modifier.size(19.dp))
                        Text("Closing a month does not process payments. It creates a clear record for your flat.", style = HqType.bodyMedium, color = c.textSecondary)
                    }
                    if (isAdmin && !isMonthClosed) {
                        HqButton(text = "Close $month", onClick = { viewModel.closeMonth(month) })
                    }
                    HqButton(text = "Manual settle up", onClick = { showSettle = true }, variant = HqButtonVariant.Secondary)
                    Spacer(Modifier.height(HqSpacing.sm))
                    habitiq.app.ui.components.HqSectionTitle("Recorded settlements")
                    settlements.take(10).forEach { st -> SettlementHistoryRow(st, members) }
                }
            }
        }
    }
    if (header == null && isAdmin && tab == "Bills") {
        FloatingActionButton(
            onClick = { showAddBill = true },
            modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.BottomEnd).padding(HqSpacing.lg),
            containerColor = c.actionPrimaryBg,
            contentColor = c.actionPrimaryFg,
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
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(c.surfaceBase).border(1.dp, c.borderSubtle, shape)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onOpen).padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(HqSpacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Figma bill-date: the billing day over a small month label.
            Column(
                Modifier.size(width = 43.dp, height = 48.dp).clip(RoundedCornerShape(11.dp)).background(c.surfaceSubtle),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
            ) {
                Text("${bill.billingDay}", style = HqType.titleSmall2, color = c.textPrimary)
                Text("DAY", style = HqType.labelSmall, color = c.textMuted)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(bill.name, style = HqType.rowTitle, color = c.textPrimary)
                Text(
                    if (bill.isVariable) "Variable amount" else "${formatInr(bill.amount ?: 0.0)} / month",
                    style = HqType.bodyMedium, color = c.textSecondary
                )
            }
            if (instance != null) {
                habitiq.app.ui.components.HqBadge(
                    billStatusLabel(instance.status),
                    if (instance.status == "paid") habitiq.app.ui.components.HqBadgeTone.Success else habitiq.app.ui.components.HqBadgeTone.Info,
                )
            }
        }
        if (instance != null) {
            instance.amount?.let { a -> Text("This month: ${formatInr(a)}", style = HqType.bodyMedium, color = c.textBrand) }
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
                Text("${billStatusLabel(inst.status)} · Paid by $payer", style = HqType.bodyMedium, color = c.textSecondary)
                // paidBy and collectorId are different people; the payer does not collect from themselves.
                if (collectorId != inst.paidBy) {
                    val collector = members.find { it.uid == collectorId }?.nickname ?: collectorId
                    Text("Collecting shares: $collector", style = HqType.bodyMedium, color = c.textSecondary)
                }
                inst.amount?.let { Text(formatInr(it), style = HqType.amountRow, color = c.textPrimary) }
            }
            Column {
                if (inst.status == "split_generated") HqTextButton(text = "Mark paid", onClick = onMarkPaid)
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
                            colors = CheckboxDefaults.colors(checkedColor = c.actionPrimaryBg, checkmarkColor = c.actionPrimaryFg, uncheckedColor = c.borderControl),
                        )
                        Text("$name ${if (collected) "paid their share" else "has not paid yet"}", style = HqType.bodyMedium, color = c.textSecondary)
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
    Column(Modifier.fillMaxSize().background(c.canvas)) {
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
                            color = if (current) c.actionPrimaryBg else c.textSecondary
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
                            "${billStatusLabel(inst.status)} · Paid by ${nameOf(inst.paidBy)}${inst.amount?.let { " · ${formatInr(it)}" } ?: ""}",
                            style = HqType.bodyMedium,
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
    val canSave = name.isNotBlank() && (isVariable || amount.toDoubleOrNull() != null)
    val save = {
        val amt = amount.toDoubleOrNull()
        viewModel.createRecurringBill(name, if (isVariable) null else amt, billingDay.toIntOrNull() ?: 1, isVariable, members.map { it.uid })
        onSaved()
    }

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        HqBackAppBar(title = "Add Recurring Bill", onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(HqSpacing.xl), verticalArrangement = Arrangement.spacedBy(HqSpacing.lg)) {
            HqTextField(value = name, onValueChange = { name = it }, label = "Bill name", placeholder = "Rent, WiFi…", leadingIcon = Icons.AutoMirrored.Filled.ReceiptLong)
            if (!isVariable) HqTextField(
                value = amount,
                onValueChange = { amount = it },
                label = "Amount (₹)",
                placeholder = "e.g. 12000",
                leadingIcon = Icons.AutoMirrored.Filled.ReceiptLong,
                keyboardType = KeyboardType.Decimal
            )
            HqTextField(
                value = billingDay,
                onValueChange = { billingDay = it },
                label = "Billing day (1-28)",
                placeholder = "e.g. 5",
                leadingIcon = Icons.Filled.CalendarMonth,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                onImeAction = if (canSave) save else null
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isVariable,
                    onCheckedChange = { isVariable = it },
                    colors = CheckboxDefaults.colors(checkedColor = c.actionPrimaryBg, checkmarkColor = c.actionPrimaryFg, uncheckedColor = c.borderControl),
                )
                Text("Variable amount (enter each month)", style = HqType.bodyMedium, color = c.textPrimary)
            }
            HqButton(text = "Save bill", onClick = save)
        }
    }
}

@Composable
private fun SettleUpScreen(viewModel: FlatViewModel, members: List<Member>, uid: String, onBack: () -> Unit) {
    val c = LocalHqColors.current
    var toUid by remember { mutableStateOf(members.firstOrNull { it.uid != uid }?.uid.orEmpty()) }
    var amount by remember { mutableStateOf("") }
    val record = {
        amount.toDoubleOrNull()?.let { viewModel.recordManualSettlement(toUid, it) }
        onBack()
    }
    Column(Modifier.fillMaxSize().background(c.canvas)) {
        HqBackAppBar(title = "Settle up", onBack = onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(HqSpacing.xl), verticalArrangement = Arrangement.spacedBy(HqSpacing.lg)) {
            Text("Pay to", style = HqType.titleSmall, color = c.textPrimary)
            members.filter { it.uid != uid }.forEach { m ->
                HqChip(label = m.nickname, selected = toUid == m.uid, onClick = { toUid = m.uid })
            }
            HqTextField(
                value = amount,
                onValueChange = { amount = it },
                label = "Amount (₹)",
                placeholder = "e.g. 1500",
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
                onImeAction = if (amount.toDoubleOrNull() != null && toUid.isNotBlank()) record else null
            )
            HqButton(text = "Record settlement", onClick = record)
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String, warning: Boolean = false) {
    val c = LocalHqColors.current
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = HqType.bodyLarge, color = c.textSecondary)
        Text(value, style = HqType.amountRow, color = if (warning) c.statusWarningFg else c.textPrimary)
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderSubtle))
}
