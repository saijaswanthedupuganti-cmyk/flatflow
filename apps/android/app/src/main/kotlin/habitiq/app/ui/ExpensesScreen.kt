package habitiq.app.ui

import habitiq.app.lib.formatDisplayDate
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import habitiq.app.data.FlatExpense
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.lib.formatInr
import habitiq.app.lib.PairContribution
import habitiq.app.lib.pairwiseContributions
import habitiq.app.lib.pairwisePersonalBalances
import habitiq.app.lib.parsePaise
import habitiq.app.ui.figma.TaskCategories
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqConfirmDialog
import habitiq.app.ui.components.HqEmptyState
import habitiq.app.ui.components.HqRootAppBar
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqFadeUp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import kotlin.math.abs

/** A settlement the person has asked to record but not yet confirmed. */
private data class PendingSettlement(val otherUid: String, val name: String, val amount: Double, val youOwe: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: FlatViewModel,
    onBack: (() -> Unit)? = null,
    onOpenBills: () -> Unit = {},
    modifier: Modifier = Modifier,
    /** Rendered at the top of the scrolling content; when set, the screen has no app bar of its own (Manage). */
    header: (@Composable () -> Unit)? = null,
) {
    val c = LocalHqColors.current
    val expenses by viewModel.expenses.collectAsStateWithLifecycleCompat()
    val settlements by viewModel.settlements.collectAsStateWithLifecycleCompat()
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val triggerAdd by viewModel.showAddExpenseTrigger.collectAsStateWithLifecycleCompat()
    val triggerBalances by viewModel.showBalancesTrigger.collectAsStateWithLifecycleCompat()

    var showAdd by remember { mutableStateOf(false) }
    var justAdded by remember { mutableStateOf<String?>(null) }
    var pending by remember { mutableStateOf<PendingSettlement?>(null) }
    val haptics = LocalHapticFeedback.current
    BackHandler(enabled = showAdd || justAdded != null) { showAdd = false; justAdded = null }
    LaunchedEffect(triggerAdd) { if (triggerAdd) { showAdd = true; viewModel.showAddExpenseTrigger.value = false } }
    // Manage's "Payment due" card lands here. The balance breakdown is always visible now, so the
    // trigger only needs consuming.
    LaunchedEffect(triggerBalances) { if (triggerBalances) viewModel.showBalancesTrigger.value = false }

    val uid = currentUser?.uid.orEmpty()
    val balances = remember(expenses, settlements, uid) { pairwisePersonalBalances(expenses, settlements, uid) }
    val iOwe = balances.filter { it.value < 0 }
    val owed = balances.filter { it.value > 0 }

    if (showAdd) {
        AddExpenseScreen(
            viewModel = viewModel,
            members = members,
            currentUid = uid,
            onBack = { showAdd = false },
            onSaved = { label ->
                showAdd = false
                justAdded = label
            }
        )
        return
    }
    justAdded?.let { label ->
        ExpenseAddedScreen(label, onAnother = { justAdded = null; showAdd = true }, onDone = { justAdded = null })
        return
    }

    pending?.let { p ->
        HqConfirmDialog(
            title = if (p.youOwe) "Record settlement?" else "Mark as received?",
            message = if (p.youOwe) {
                "You're recording that you paid ${p.name} ${formatInr(p.amount)}. Oddroof doesn't move money; this updates the balance between you."
            } else {
                "You're recording that ${p.name} paid you ${formatInr(p.amount)}. Oddroof doesn't move money; this updates the balance between you."
            },
            confirmLabel = if (p.youOwe) "Record settlement" else "Mark received",
            confirmVariant = HqButtonVariant.Primary,
            onConfirm = {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                if (p.youOwe) viewModel.recordManualSettlement(p.otherUid, p.amount)
                else viewModel.recordMarkReceived(p.otherUid, p.amount)
                pending = null
            },
            onDismiss = { pending = null },
        )
    }

    val model = remember(balances, expenses, settlements, members, uid) {
        buildExpensesModel(balances, expenses, settlements, members, uid)
    }

    Box(modifier.fillMaxSize().background(c.canvas)) {
        Column(Modifier.fillMaxSize()) {
            if (header == null) { if (onBack != null) HqBackAppBar(title = "Expenses", onBack = onBack) else HqRootAppBar(title = "Expenses") }
            ExpensesContent(
                model = model,
                onOpenBills = onOpenBills,
                onSettle = { person ->
                    val raw = balances[person.key] ?: 0.0
                    pending = PendingSettlement(person.key, person.name, abs(raw), youOwe = raw < 0)
                },
                onAddExpense = { showAdd = true },
                header = header ?: {},
            )
        }
    }
}

internal fun buildExpensesModel(
    balances: Map<String, Double>,
    expenses: List<FlatExpense>,
    settlements: List<habitiq.app.data.Settlement>,
    members: List<Member>,
    uid: String,
): ExpensesUiModel {
    fun plural(n: Int) = "$n ${if (n == 1) "person" else "people"}"
    val owe = balances.filter { it.value < 0 }
    val owed = balances.filter { it.value > 0 }
    fun nameOf(id: String) = members.find { it.uid == id }?.nickname?.ifBlank { null } ?: "A flatmate"

    val people = (owe.entries + owed.entries).map { (otherUid, balance) ->
        PersonBalanceItem(
            key = otherUid,
            name = nameOf(otherUid),
            youOwe = balance < 0,
            amountText = formatInr(balance),
            contributions = pairwiseContributions(expenses, settlements, uid, otherUid).map { line ->
                ContributionLine(line.label, formatDisplayDate(line.date), (if (line.amount >= 0) "They owe " else "You owe ") + formatInr(line.amount))
            },
        )
    }
    return ExpensesUiModel(
        oweText = if (owe.isNotEmpty()) formatInr(owe.values.sumOf { abs(it) }) else null,
        oweSupport = if (owe.isNotEmpty()) "To ${plural(owe.size)}" else null,
        owedText = if (owed.isNotEmpty()) formatInr(owed.values.sum()) else null,
        owedSupport = if (owed.isNotEmpty()) "From ${plural(owed.size)}" else null,
        people = people,
        expenses = expenses.map { e ->
            val payer = members.find { it.uid == e.paidBy }?.nickname ?: e.paidBy
            ExpenseListItem(e.id, e.description, "${formatDisplayDate(e.date)} · paid by $payer", formatInr(e.amount))
        },
    )
}

@Composable
private fun AddExpenseScreen(viewModel: FlatViewModel, members: List<Member>, currentUid: String, onBack: () -> Unit, onSaved: (String) -> Unit) {
    val c = LocalHqColors.current
    var desc by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Lifestyle") }
    var equalSplit by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var selected by remember(members) { mutableStateOf(members.map { it.uid }.toSet()) }
    var customAmounts by remember { mutableStateOf(mapOf<String, String>()) }
    var paidBy by remember { mutableStateOf(currentUid) }

    val totalPaise = parsePaise(amount) ?: 0L
    val assignedPaise = selected.sumOf { parsePaise(customAmounts[it].orEmpty()) ?: 0L }
    val remainingPaise = totalPaise - assignedPaise
    val customValid = equalSplit || (totalPaise > 0 && remainingPaise == 0L)
    val canSubmit = desc.isNotBlank() && totalPaise > 0 && selected.isNotEmpty() && customValid
    fun submit() {
        val amt = totalPaise / 100.0
        saving = true
        saveError = null
        val custom = if (equalSplit) null else selected.associateWith { (parsePaise(customAmounts[it].orEmpty()) ?: 0L) / 100.0 }
        viewModel.addExpense(
            desc,
            amt,
            selected.toList(),
            category.lowercase(),
            splitType = if (equalSplit) "equal" else "custom",
            customSplits = custom,
            paidBy = paidBy,
        ) { ok ->
            saving = false
            if (ok) onSaved("${formatInr(amt)} for $desc") else saveError = "Couldn't add the expense. Try again."
        }
    }
    // The keyboard's Done key submits only when the form is complete; otherwise it just closes the keyboard.
    val imeSubmit: (() -> Unit)? = if (canSubmit && !saving) ({ submit() }) else null

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        HqBackAppBar(title = "Add expense", onBack = onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.group)
        ) {
            HqTextField(value = desc, onValueChange = { desc = it }, label = "What was it for?", placeholder = "e.g., Groceries", leadingIcon = Icons.AutoMirrored.Filled.ReceiptLong)
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                Text("Category", style = HqType.labelMedium, color = c.textSecondary)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    TaskCategories.forEach { cat ->
                        HqChip(label = cat, selected = category == cat, onClick = { category = cat })
                    }
                }
            }
            HqTextField(
                value = amount, onValueChange = { amount = it }, label = "Amount (₹)", placeholder = "e.g., 1,200",
                leadingIcon = Icons.Filled.CurrencyRupee, keyboardType = KeyboardType.Decimal,
                imeAction = if (equalSplit) ImeAction.Done else ImeAction.Next,
                onImeAction = if (equalSplit) imeSubmit else null,
            )
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    Icon(Icons.Filled.AccountBalanceWallet, null, tint = c.iconDefault)
                    Text("Paid by", style = HqType.labelMedium, color = c.textSecondary)
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    members.forEach { member ->
                        HqChip(
                            label = if (member.uid == currentUid) "You" else member.nickname.substringBefore(" "),
                            selected = paidBy == member.uid,
                            onClick = { paidBy = member.uid },
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    Icon(Icons.Filled.Groups, null, tint = c.iconDefault)
                    Text("Split", style = HqType.labelMedium, color = c.textSecondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    HqChip(label = "Equal split", selected = equalSplit, onClick = { equalSplit = true })
                    HqChip(label = "Custom split", selected = !equalSplit, onClick = { equalSplit = false })
                }
                Text(
                    if (equalSplit) "Shared equally between everyone checked below." else "Enter what each checked person owes.",
                    style = HqType.bodyMedium, color = c.textSecondary,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                val lastShareUid = members.lastOrNull { it.uid in selected }?.uid
                members.forEach { member ->
                    val on = member.uid in selected
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = HqSize.target)
                            .toggleable(value = on, role = Role.Checkbox) { checked ->
                                selected = if (checked) selected + member.uid else selected - member.uid
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
                    ) {
                        Checkbox(checked = on, onCheckedChange = null)
                        Text(member.nickname, style = HqType.bodyLarge, color = c.textPrimary)
                    }
                    if (!equalSplit && on) {
                        HqTextField(
                            value = customAmounts[member.uid].orEmpty(),
                            onValueChange = { customAmounts = customAmounts + (member.uid to it) },
                            label = "${member.nickname}'s share (₹)",
                            keyboardType = KeyboardType.Decimal,
                            imeAction = if (member.uid == lastShareUid) ImeAction.Done else ImeAction.Next,
                            onImeAction = if (member.uid == lastShareUid) imeSubmit else null,
                            modifier = Modifier.padding(start = HqSpacing.xxxl, bottom = HqSpacing.sm),
                        )
                    }
                }
            }
            if (!equalSplit) {
                // Name the actual gap so the person can fix it; never a generic "invalid".
                val message = when {
                    totalPaise <= 0L -> "Enter the total amount first."
                    remainingPaise == 0L -> "Shares add up to ${formatInr(totalPaise / 100.0)}."
                    remainingPaise > 0 -> "${formatInr(remainingPaise / 100.0)} still to assign."
                    else -> "Shares are ${formatInr(-remainingPaise / 100.0)} over the total."
                }
                Text(
                    message,
                    style = HqType.bodyLarge,
                    color = if (remainingPaise == 0L && totalPaise > 0) c.statusSuccessFg else c.statusWarningFg,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            Spacer(Modifier.height(HqSpacing.xxhuge))
        }
        Column(Modifier.fillMaxWidth().background(c.surfaceBase).navigationBarsPadding().padding(HqSpacing.screenHorizontal)) {
            saveError?.let {
                Text(it, color = c.statusDangerFg, style = HqType.bodyMedium, modifier = Modifier.padding(bottom = HqSpacing.sm))
            }
            HqButton(
                text = if (saving) "Adding…" else "Add expense",
                loading = saving,
                enabled = canSubmit,
                onClick = { submit() }
            )
        }
    }
}

@Composable
private fun ExpenseAddedScreen(label: String, onAnother: () -> Unit, onDone: () -> Unit) {
    val c = LocalHqColors.current
    HqFadeUp(modifier = Modifier.fillMaxSize()) {
    Column(
        Modifier.fillMaxSize().background(c.canvas).padding(HqSpacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(88.dp).clip(CircleShape).background(c.statusSuccessBg),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.CheckCircle, null, tint = c.statusSuccessFg, modifier = Modifier.size(52.dp)) }
        Spacer(Modifier.height(HqSpacing.xl))
        Text("Expense added", style = HqType.headlineMedium, color = c.textPrimary)
        Text(label, style = HqType.bodyLarge, color = c.textSecondary, modifier = Modifier.padding(vertical = HqSpacing.lg))
        HqButton(text = "View expense", onClick = onDone)
        Spacer(Modifier.height(HqSpacing.md))
        HqButton(text = "Add another", onClick = onAnother, variant = HqButtonVariant.Secondary)
    }
    }
}
