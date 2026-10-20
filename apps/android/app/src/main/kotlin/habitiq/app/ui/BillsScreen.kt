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
import habitiq.app.ui.theme.HqShapes
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.components.HqAmountField
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
import habitiq.app.ui.components.HqButtonSize
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqEmptyState
import habitiq.app.ui.components.HqRootAppBar
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.components.HqChipFlow
import habitiq.app.ui.components.HqInlineError
import habitiq.app.ui.components.HqDiscardChangesDialog
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.filled.CurrencyRupee
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

@Composable
fun BillsScreen(
    viewModel: FlatViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    /** When set, the screen is embedded (Manage > Expenses > Bills): no page header of its own. */
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
    val loading by viewModel.loading.collectAsStateWithLifecycleCompat()
    val loadError by viewModel.loadError.collectAsStateWithLifecycleCompat()
    val snackbar = habitiq.app.ui.components.LocalHqSnackbar.current
    val addTrigger by viewModel.showAddBillTrigger.collectAsStateWithLifecycleCompat()

    var tab by remember { mutableStateOf("Bills") }
    var showAddBill by remember { mutableStateOf(false) }
    var showSettle by remember { mutableStateOf(false) }
    var selectedBillId by remember { mutableStateOf<String?>(null) }
    var closingMonth by remember { mutableStateOf(false) }
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
    // Every bill action reports failure the same way; success shows in the list itself.
    val failed: (Boolean) -> Unit = { ok -> if (!ok) snackbar?.show(BillUpdateFailed) }

    if (showAddBill) {
        AddBillForm(
            onBack = { showAddBill = false },
            onSubmit = { d, done ->
                viewModel.createRecurringBill(d.name, d.amount, d.billingDay, d.isVariable, members.map { it.uid }, onDone = done)
            },
            onSaved = { showAddBill = false; snackbar?.show("Bill added") },
        )
        return
    }
    if (showSettle) {
        SettleUpForm(
            members = members,
            uid = currentUser?.uid.orEmpty(),
            onBack = { showSettle = false },
            onSubmit = { toUid, amt, done -> viewModel.recordManualSettlement(toUid, amt, done) },
            onSaved = { showSettle = false; snackbar?.show("Payment recorded") },
        )
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
    val activeBills = bills.filter { it.active }
    // Until the first load finishes (or if it failed), "nothing here" isn't known yet.
    val loadUnknown = loading || loadError

    // One scrolling column so an embedding header (Manage title, Expenses/Bills) scrolls with it.
    Column(
        modifier.fillMaxSize().background(c.canvas).verticalScroll(rememberScrollState())
            .padding(bottom = HqSpacing.fabClearance),
    ) {
        if (header != null) {
            Column(Modifier.padding(horizontal = HqSpacing.screenHorizontal).padding(top = HqSpacing.sm)) { header() }
            Spacer(Modifier.height(HqSpacing.md))
        } else {
            habitiq.app.ui.components.HqPageHeader(
                title = "Bills",
                subtitle = "Bills that repeat every month",
                onBack = onBack,
                modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal).padding(top = HqSpacing.sm, bottom = HqSpacing.md),
            )
        }
        // Third level of hierarchy: light chips, so it never looks like the switches above it.
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = HqSpacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
        ) {
            listOf("Bills" to "Bills", "Collections" to "This month", "Close" to "Mark month done").forEach { (key, label) ->
                habitiq.app.ui.components.HqChip(label = label, selected = tab == key, onClick = { tab = key })
            }
        }
        val listPadding = Modifier.padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.lg)
        when (tab) {
            "Bills" -> Column(listPadding, verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                when {
                    bills.isEmpty() && loadUnknown ->
                        ManageLoadPlaceholder(failed = loadError && !loading, onRetry = { viewModel.refresh() })
                    bills.isEmpty() && isAdmin -> HqEmptyState(
                        icon = Icons.Filled.Add,
                        title = "No bills yet",
                        message = "Add rent, WiFi, and other bills that repeat every month.",
                        primaryLabel = "Add bill",
                        onPrimaryClick = { showAddBill = true },
                    )
                    bills.isEmpty() -> ManageEmptyNote(
                        title = "No bills yet",
                        message = "Your flat admin adds them.",
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    )
                }
                if (activeBills.isNotEmpty()) {
                    // When bills exist, Add bill also lives in the shell's Quick add; this keeps it findable here.
                    habitiq.app.ui.components.HqSectionTitle(
                        "Upcoming",
                        action = if (isAdmin) "Add bill" else null,
                        onAction = if (isAdmin) ({ showAddBill = true }) else null,
                    )
                }
                activeBills.forEach { bill ->
                    RecurringBillCard(bill, monthInstances.find { it.templateId == bill.id }, isAdmin,
                        onOpen = { selectedBillId = bill.id },
                        onGenerate = { amt, done ->
                            viewModel.generateBill(bill.id, amt) { ok ->
                                done(ok)
                                if (!ok) snackbar?.show("Unable to start this month's bill. Check your connection and try again.")
                            }
                        },
                        onMarkPaid = { inst -> viewModel.markBillPaid(inst.id, failed) })
                }
            }
            "Collections" -> Column(listPadding, verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                if (monthInstances.isEmpty()) {
                    if (loadUnknown) {
                        ManageLoadPlaceholder(failed = loadError && !loading, onRetry = { viewModel.refresh() })
                    } else {
                        ManageEmptyNote(
                            title = "Nothing to collect yet",
                            message = "This month's bills haven't started yet.",
                            icon = Icons.Filled.CalendarMonth,
                        )
                    }
                }
                monthInstances.forEach { inst ->
                    BillInstanceRow(
                        inst, members, isAdmin, currentUser?.uid.orEmpty(),
                        onMarkPaid = { viewModel.markBillPaid(inst.id, failed) },
                        onSkip = { viewModel.skipBillInstance(inst.id, onResult = failed) },
                        onToggleCollected = { uid, collected -> viewModel.markBillCollected(inst.id, uid, collected, failed) }
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
                Column(listPadding, verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    // Figma close-month-hero.
                    Row(
                        Modifier.fillMaxWidth().clip(HqShapes.card).background(c.surfaceSubtle).padding(HqSpacing.lg),
                        horizontalArrangement = Arrangement.spacedBy(HqSpacing.md), verticalAlignment = Alignment.CenterVertically,
                    ) {
                        habitiq.app.ui.components.HqIconTile(habitiq.app.ui.components.HqIcons.Receipt, habitiq.app.ui.components.HqTileTone.Sand)
                        Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                            Text("Monthly total", style = HqType.labelMedium, color = c.textSecondary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            Text(formatInr(total), style = HqType.titleMedium2, color = c.textPrimary)
                            Text("${monthInstances.size} household ${if (monthInstances.size == 1) "bill" else "bills"}", style = HqType.bodyMedium, color = c.textSecondary)
                        }
                    }
                    if (isMonthClosed) {
                        Text("This month is marked done.", style = HqType.bodyMedium, color = c.statusSuccessFg)
                    } else {
                        habitiq.app.ui.components.HqSectionTitle("Before you mark it done")
                        if (suggestions.isNotEmpty()) {
                            Row(
                                Modifier.fillMaxWidth().clip(HqShapes.card).background(c.statusWarningBg).padding(HqSpacing.lg),
                                horizontalArrangement = Arrangement.spacedBy(HqSpacing.md),
                            ) {
                                Text("!", style = HqType.titleSmall2, color = c.statusWarningFg)
                                Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                                    Text("${suggestions.size} ${if (suggestions.size == 1) "balance is" else "balances are"} still outstanding", style = HqType.rowTitle, color = c.statusWarningFg)
                                    Text("You can mark the month done now; these balances carry forward.", style = HqType.bodyMedium, color = c.statusWarningFg)
                                }
                            }
                        }
                    }
                    ReviewRow("Paid bills", "$paid")
                    ReviewRow("Skipped bills", "$skipped")
                    ReviewRow("Outstanding balances", formatInr(outstanding), warning = outstanding > 0)
                    if (suggestions.isEmpty()) Text("Everyone is settled up for this month.", style = HqType.bodyMedium, color = c.statusSuccessFg)
                    suggestions.forEach { s ->
                        SettlementSuggestionRow(s, members, onConfirm = {
                            viewModel.recordSettlement(s) { ok ->
                                snackbar?.show(if (ok) "Payment recorded" else "Unable to record the payment. Check your connection and try again.")
                            }
                        })
                    }
                    Row(
                        Modifier.padding(top = HqSpacing.sm).fillMaxWidth().clip(HqShapes.control).background(c.surfaceSubtle).padding(HqSpacing.md),
                        horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
                    ) {
                        Icon(habitiq.app.ui.components.HqIcons.Shield, null, tint = c.textSecondary, modifier = Modifier.size(HqIconSize.sm))
                        Text("Marking a month done doesn't move money. It keeps a clear record for your flat.", style = HqType.bodyMedium, color = c.textSecondary)
                    }
                    if (isAdmin && !isMonthClosed) {
                        HqButton(text = "Mark month done", loading = closingMonth, onClick = {
                            if (!closingMonth) {
                                closingMonth = true
                                viewModel.closeMonth(month) { ok ->
                                    closingMonth = false
                                    if (!ok) snackbar?.show("Unable to mark the month done. Check your connection and try again.")
                                }
                            }
                        })
                    }
                    HqButton(text = "Settle up", onClick = { showSettle = true }, variant = HqButtonVariant.Secondary)
                    Spacer(Modifier.height(HqSpacing.sm))
                    habitiq.app.ui.components.HqSectionTitle("Recorded payments")
                    settlements.take(10).forEach { st -> SettlementHistoryRow(st, members) }
                }
            }
        }
    }
}

private const val BillUpdateFailed = "Unable to update the bill. Check your connection and try again."

/** "Paid by" only once the bill is actually paid; before that, whose turn it is. */
private fun payerLine(inst: BillInstance, payerName: String): String = when {
    inst.status == "paid" -> "Paid by $payerName"
    inst.month == currentMonthKey() -> "Pays this month: $payerName"
    else -> "$payerName's turn"
}

@Composable
private fun RecurringBillCard(
    bill: RecurringBill, instance: BillInstance?, isAdmin: Boolean,
    onOpen: () -> Unit,
    onGenerate: (Double?, (Boolean) -> Unit) -> Unit, onMarkPaid: (BillInstance) -> Unit
) {
    val c = LocalHqColors.current
    val icon = when {
        bill.name.contains("internet", true) || bill.name.contains("wifi", true) -> Icons.Filled.Wifi
        bill.name.contains("electric", true) || bill.name.contains("power", true) -> Icons.Filled.Bolt
        bill.name.contains("rent", true) || bill.name.contains("home", true) -> Icons.Filled.Home
        else -> Icons.AutoMirrored.Filled.ReceiptLong
    }
    // Starting a month is a one-shot write: block repeat taps until the result comes back.
    var generating by remember(bill.id) { mutableStateOf(false) }
    var askAmount by rememberSaveable(bill.id) { mutableStateOf(false) }
    var monthAmount by rememberSaveable(bill.id) { mutableStateOf("") }
    var amountError by remember(bill.id) { mutableStateOf<String?>(null) }
    fun start(amount: Double?) {
        if (generating) return
        generating = true
        onGenerate(amount) { ok -> generating = false; if (ok) askAmount = false }
    }
    Column(
        Modifier.fillMaxWidth().clip(HqShapes.card).background(c.surfaceBase).border(1.dp, c.borderSubtle, HqShapes.card)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onOpen).padding(HqSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(HqSpacing.sm),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
            // Figma bill-date: the billing day over a small month label.
            Column(
                Modifier.size(width = 44.dp, height = 48.dp).clip(HqShapes.control).background(c.surfaceSubtle),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
            ) {
                Text("${bill.billingDay}", style = HqType.titleSmall2, color = c.textPrimary)
                Text("Day", style = HqType.labelSmall, color = c.textSecondary)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
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
            // Shares are out and it isn't paid yet; who pays is set when the month starts, so that isn't the test.
            if (instance.status == "split_generated") {
                HqTextButton(text = "Mark paid", onClick = { onMarkPaid(instance) })
            }
        } else if (isAdmin) {
            val variable = bill.isVariable || bill.amount == null
            if (variable && askAmount) {
                HqAmountField(
                    value = monthAmount,
                    onValueChange = { monthAmount = it; amountError = null },
                    label = "This month's amount",
                    placeholder = "e.g. 1,800",
                    errorText = amountError,
                    imeAction = ImeAction.Done,
                )
            }
            HqButton(
                text = "Start this month's bills",
                variant = HqButtonVariant.Tertiary,
                size = HqButtonSize.Small,
                fullWidth = false,
                loading = generating,
                onClick = {
                    when {
                        !variable -> start(bill.amount)
                        !askAmount -> askAmount = true
                        else -> {
                            val paise = lenientPaise(monthAmount)
                            if (paise == null || paise <= 0L) amountError = "Enter this month's amount, like 1,800."
                            else start(paise / 100.0)
                        }
                    }
                },
            )
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
            Column(Modifier.weight(1f)) {
                Text(inst.name, style = HqType.titleSmall, color = c.textPrimary)
                Text("${billStatusLabel(inst.status)} · ${payerLine(inst, payer)}", style = HqType.bodyMedium, color = c.textSecondary)
                // paidBy and collectorId are different people; the payer does not collect from themselves.
                if (collectorId != inst.paidBy) {
                    val collector = members.find { it.uid == collectorId }?.nickname ?: collectorId
                    Text("$collector collects everyone's share", style = HqType.bodyMedium, color = c.textSecondary)
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
                    habitiq.app.ui.components.HqCheckboxRow(
                        title = "$name ${if (collected) "paid their share" else "has not paid yet"}",
                        checked = collected,
                        onCheckedChange = { onToggleCollected(uid, it) },
                    )
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
            Text("$from → $to: ${formatInr(s.amount)}", style = HqType.bodyMedium, color = c.textPrimary, modifier = Modifier.weight(1f))
            // Secondary: the screen's one filled button is "Mark month done".
            HqButton(text = "Record payment", onClick = onConfirm, variant = HqButtonVariant.Secondary, fullWidth = false)
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
    HideShellChrome()
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
            Text("Who pays this month", style = HqType.titleMedium, color = c.textPrimary)
            if (bill.rotationQueue.isEmpty()) {
                Text("No one is set to pay this bill yet.", style = HqType.bodyMedium, color = c.textSecondary)
            } else {
                bill.rotationQueue.forEachIndexed { index, uid ->
                    val current = index == bill.currentPayerIndex
                    HqCard {
                        Text(nameOf(uid), style = HqType.titleSmall, color = c.textPrimary)
                        Text(
                            if (current) "Pays this month" else "Takes a later turn",
                            style = HqType.bodySmall,
                            color = if (current) c.actionPrimaryBg else c.textSecondary
                        )
                    }
                }
            }
            Text("History", style = HqType.titleMedium, color = c.textPrimary)
            if (instances.isEmpty()) {
                Text("No months yet.", style = HqType.bodyMedium, color = c.textSecondary)
            } else {
                instances.sortedByDescending { it.month }.forEach { inst ->
                    HqCard {
                        Text(inst.month, style = HqType.titleSmall, color = c.textPrimary)
                        Text(
                            "${billStatusLabel(inst.status)} · ${payerLine(inst, nameOf(inst.paidBy))}${inst.amount?.let { " · ${formatInr(it)}" } ?: ""}",
                            style = HqType.bodyMedium,
                            color = c.textSecondary
                        )
                    }
                }
            }
        }
    }
}

/** What the Add bill form collected; maps onto [FlatViewModel.createRecurringBill]. */
data class BillDraft(val name: String, val amount: Double?, val billingDay: Int, val isVariable: Boolean)

@Composable
fun AddBillForm(onBack: () -> Unit, onSubmit: (BillDraft, (Boolean) -> Unit) -> Unit, onSaved: () -> Unit) {
    // Full-screen form with its own submit button: hide the shell nav bar and + over it.
    HideShellChrome()
    val c = LocalHqColors.current
    var name by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var isVariable by rememberSaveable { mutableStateOf(false) }
    var billingDay by rememberSaveable { mutableStateOf("1") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var dayError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var confirmDiscard by remember { mutableStateOf(false) }
    val nameFocus = remember { FocusRequester() }
    val amountFocus = remember { FocusRequester() }
    val dayFocus = remember { FocusRequester() }
    val dirty = name.isNotBlank() || amount.isNotBlank()

    LaunchedEffect(Unit) { runCatching { nameFocus.requestFocus() } }
    fun close() { if (dirty && !saving) confirmDiscard = true else onBack() }
    BackHandler(enabled = dirty || saving) { if (!saving) confirmDiscard = true }

    fun submit() {
        if (saving) return
        val paise = lenientPaise(amount)
        val day = billingDay.trim().toIntOrNull()
        nameError = if (name.isBlank()) "Enter the bill name, like \"Rent\"." else null
        amountError = when {
            isVariable -> null
            amount.isBlank() -> "Enter the amount, or turn on \"Amount changes every month\"."
            paise == null || paise <= 0L -> "Enter the amount as a number above ₹0, like 12,000."
            else -> null
        }
        dayError = if (day == null || day !in 1..28) "Enter a day from 1 to 28." else null
        when {
            nameError != null -> { runCatching { nameFocus.requestFocus() }; return }
            amountError != null -> { runCatching { amountFocus.requestFocus() }; return }
            dayError != null -> { runCatching { dayFocus.requestFocus() }; return }
        }
        saving = true
        saveError = null
        onSubmit(BillDraft(name.trim(), if (isVariable) null else (paise ?: 0L) / 100.0, day ?: 1, isVariable)) { ok ->
            saving = false
            if (ok) onSaved() else saveError = "Unable to save the bill. Check your connection and try again."
        }
    }

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        HqBackAppBar(title = "Add bill", onBack = { close() })
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.group),
        ) {
            HqTextField(
                value = name, onValueChange = { name = it; nameError = null },
                label = "Bill name", placeholder = "e.g. Rent, WiFi",
                leadingIcon = Icons.AutoMirrored.Filled.ReceiptLong, errorText = nameError,
                modifier = Modifier.focusRequester(nameFocus),
            )
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                if (!isVariable) {
                    HqTextField(
                        value = amount, onValueChange = { amount = it; amountError = null },
                        label = "Amount (₹)", placeholder = "e.g. 12,000",
                        leadingIcon = Icons.Filled.CurrencyRupee, keyboardType = KeyboardType.Decimal,
                        errorText = amountError, modifier = Modifier.focusRequester(amountFocus),
                    )
                }
                habitiq.app.ui.components.HqSwitchRow(
                    title = "Amount changes every month",
                    checked = isVariable,
                    onCheckedChange = { isVariable = it; amountError = null },
                    supporting = if (isVariable) "You'll enter the amount when you start each month's bills." else null,
                )
            }
            HqTextField(
                value = billingDay, onValueChange = { billingDay = it; dayError = null },
                label = "Due on day", placeholder = "e.g. 5",
                helperText = "A day from 1 to 28, so it works every month.",
                leadingIcon = Icons.Filled.CalendarMonth, keyboardType = KeyboardType.Number,
                errorText = dayError, imeAction = ImeAction.Done,
                modifier = Modifier.focusRequester(dayFocus),
            )
            Spacer(Modifier.height(HqSpacing.lg))
        }
        Column(
            Modifier.fillMaxWidth().background(c.canvas).navigationBarsPadding().imePadding()
                .padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.sm),
        ) {
            saveError?.let { HqInlineError(it) }
            HqButton(text = "Add bill", loading = saving, onClick = { submit() })
        }
    }
    if (confirmDiscard) {
        HqDiscardChangesDialog(onKeepEditing = { confirmDiscard = false }, onDiscard = { confirmDiscard = false; onBack() })
    }
}

@Composable
fun SettleUpForm(
    members: List<Member>,
    uid: String,
    onBack: () -> Unit,
    onSubmit: (String, Double, (Boolean) -> Unit) -> Unit,
    onSaved: () -> Unit,
) {
    HideShellChrome()
    val c = LocalHqColors.current
    val others = members.filter { it.uid != uid }
    var toUid by rememberSaveable { mutableStateOf<String?>(null) }
    var amount by rememberSaveable { mutableStateOf("") }
    var personError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var confirmDiscard by remember { mutableStateOf(false) }
    val amountFocus = remember { FocusRequester() }
    val selectedTo = toUid ?: others.firstOrNull()?.uid
    val dirty = amount.isNotBlank()

    fun close() { if (dirty && !saving) confirmDiscard = true else onBack() }
    BackHandler(enabled = dirty || saving) { if (!saving) confirmDiscard = true }

    fun submit() {
        if (saving) return
        val paise = lenientPaise(amount)
        personError = if (selectedTo == null) "Choose who you paid." else null
        amountError = when {
            amount.isBlank() -> "Enter the amount you paid, like 1,500."
            paise == null || paise <= 0L -> "Enter the amount as a number above ₹0, like 1,500."
            else -> null
        }
        if (amountError != null) { runCatching { amountFocus.requestFocus() }; return }
        if (personError != null || selectedTo == null) return
        saving = true
        saveError = null
        onSubmit(selectedTo, (paise ?: 0L) / 100.0) { ok ->
            saving = false
            if (ok) onSaved() else saveError = "Unable to record the payment. Check your connection and try again."
        }
    }

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        HqBackAppBar(title = "Settle up", onBack = { close() })
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.group),
        ) {
            Text(SettleNote, style = HqType.bodyMedium, color = c.textSecondary)
            FormGroup("Paid to") {
                HqChipFlow {
                    others.forEach { m ->
                        HqChip(label = m.nickname.ifBlank { "Flatmate" }, selected = selectedTo == m.uid, onClick = { toUid = m.uid; personError = null })
                    }
                }
                personError?.let { HqInlineError(it) }
            }
            HqTextField(
                value = amount, onValueChange = { amount = it; amountError = null },
                label = "Amount (₹)", placeholder = "e.g. 1,500",
                leadingIcon = Icons.Filled.CurrencyRupee, keyboardType = KeyboardType.Decimal,
                errorText = amountError, imeAction = ImeAction.Done,
                modifier = Modifier.focusRequester(amountFocus),
            )
            Spacer(Modifier.height(HqSpacing.lg))
        }
        Column(
            Modifier.fillMaxWidth().background(c.canvas).navigationBarsPadding().imePadding()
                .padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.sm),
        ) {
            saveError?.let { HqInlineError(it) }
            HqButton(text = "Record payment", loading = saving, onClick = { submit() })
        }
    }
    if (confirmDiscard) {
        HqDiscardChangesDialog(onKeepEditing = { confirmDiscard = false }, onDiscard = { confirmDiscard = false; onBack() })
    }
}

@Composable
private fun ReviewRow(label: String, value: String, warning: Boolean = false) {
    val c = LocalHqColors.current
    Row(Modifier.fillMaxWidth().padding(vertical = HqSpacing.md), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = HqType.bodyLarge, color = c.textSecondary)
        Text(value, style = HqType.amountRow, color = if (warning) c.statusWarningFg else c.textPrimary)
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderSubtle))
}
