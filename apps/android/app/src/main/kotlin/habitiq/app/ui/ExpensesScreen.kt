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
import habitiq.app.ui.components.HqChipFlow
import habitiq.app.ui.components.HqDiscardChangesDialog
import habitiq.app.ui.components.HqInlineError
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.heading
import kotlinx.coroutines.launch
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
    val monthly by viewModel.expenseScopeMonthly.collectAsStateWithLifecycleCompat()

    var showAdd by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<PendingSettlement?>(null) }
    var recording by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val snackbar = habitiq.app.ui.components.LocalHqSnackbar.current
    BackHandler(enabled = showAdd) { showAdd = false }
    LaunchedEffect(triggerAdd) { if (triggerAdd) { showAdd = true; viewModel.showAddExpenseTrigger.value = false } }
    // Manage's "Payment due" card lands here. The balance breakdown is always visible now, so the
    // trigger only needs consuming.
    LaunchedEffect(triggerBalances) { if (triggerBalances) viewModel.showBalancesTrigger.value = false }

    val uid = currentUser?.uid.orEmpty()
    val balances = remember(expenses, settlements, uid) { pairwisePersonalBalances(expenses, settlements, uid) }
    val iOwe = balances.filter { it.value < 0 }
    val owed = balances.filter { it.value > 0 }

    if (showAdd) {
        AddExpenseForm(
            members = members,
            currentUid = uid,
            onBack = { showAdd = false },
            onSubmit = { d, done ->
                viewModel.addExpense(
                    d.description, d.amount, d.splitAmong, d.category,
                    splitType = if (d.customSplits == null) "equal" else "custom",
                    customSplits = d.customSplits,
                    paidBy = d.paidBy,
                    onDone = done,
                )
            },
            onSaved = {
                showAdd = false
                snackbar?.show("Expense added")
            }
        )
        return
    }

    pending?.let { p ->
        HqConfirmDialog(
            title = if (p.youOwe) "Settle up with ${p.name}?" else "Mark as received?",
            message = (if (p.youOwe) {
                "You're recording that you paid ${p.name} ${formatInr(p.amount)}."
            } else {
                "You're recording that ${p.name} paid you ${formatInr(p.amount)}."
            }) + " $SettleNote",
            confirmLabel = if (p.youOwe) "Record payment" else "Mark received",
            confirmVariant = HqButtonVariant.Primary,
            onConfirm = {
                if (recording) return@HqConfirmDialog
                recording = true
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                val done: (Boolean) -> Unit = { ok ->
                    recording = false
                    pending = null
                    snackbar?.show(if (ok) "Payment recorded" else "Unable to record the payment. Check your connection and try again.")
                }
                if (p.youOwe) viewModel.recordManualSettlement(p.otherUid, p.amount, done)
                else viewModel.recordMarkReceived(p.otherUid, p.amount, done)
            },
            onDismiss = { if (!recording) pending = null },
        )
    }

    val model = remember(balances, expenses, settlements, members, uid) {
        buildExpensesModel(balances, expenses, settlements, members, uid)
    }

    if (monthly) {
        // Same screen, other scope: the header and the Daily/Monthly switch stay exactly where they were.
        BillsScreen(
            viewModel,
            onBack = if (header == null) onBack else null,
            modifier = modifier,
            header = {
                if (header != null) header()
                ExpenseScopeTabs(monthly = true, onSelect = { viewModel.expenseScopeMonthly.value = it })
            },
        )
        return
    }

    Box(modifier.fillMaxSize().background(c.canvas)) {
        Column(Modifier.fillMaxSize()) {
            if (header == null) { if (onBack != null) HqBackAppBar(title = "Expenses", onBack = onBack) else HqRootAppBar(title = "Expenses") }
            ExpensesContent(
                model = model,
                onOpenBills = { viewModel.expenseScopeMonthly.value = true },
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

/** Shared wording for every "record a payment" confirmation: Oddroof keeps the record, not the money. */
internal const val SettleNote = "Oddroof records this; it doesn't move money."

/** Forgiving money input: accepts "1,200", "1200", " 1200 " and "₹1,200". Null when it isn't a number. */
internal fun lenientPaise(text: String): Long? = parsePaise(text.replace("₹", "").replace(" ", ""))

/** What the Add expense form collected; maps 1:1 onto [FlatViewModel.addExpense]. */
data class ExpenseDraft(
    val description: String,
    val amount: Double,
    val splitAmong: List<String>,
    val category: String,
    val customSplits: Map<String, Double>?,
    val paidBy: String,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddExpenseForm(
    members: List<Member>,
    currentUid: String,
    onBack: () -> Unit,
    onSubmit: (ExpenseDraft, (Boolean) -> Unit) -> Unit,
    onSaved: () -> Unit,
    autoFocus: Boolean = true,
) {
    val c = LocalHqColors.current
    val scope = rememberCoroutineScope()
    var amount by rememberSaveable { mutableStateOf("") }
    var desc by rememberSaveable { mutableStateOf("") }
    var paidBy by rememberSaveable { mutableStateOf(currentUid) }
    var equalSplit by rememberSaveable { mutableStateOf(true) }
    // Everyone is in the split until the person changes it (null), so late-loading members are included.
    var picked by remember { mutableStateOf<Set<String>?>(null) }
    var customAmounts by remember { mutableStateOf(mapOf<String, String>()) }
    var category by rememberSaveable { mutableStateOf("Other") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var descError by remember { mutableStateOf<String?>(null) }
    var splitError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var confirmDiscard by remember { mutableStateOf(false) }
    val amountFocus = remember { FocusRequester() }
    val descFocus = remember { FocusRequester() }
    val splitBring = remember { BringIntoViewRequester() }

    val selected = picked ?: members.map { it.uid }.toSet()
    val totalPaise = lenientPaise(amount) ?: 0L
    val assignedPaise = selected.sumOf { lenientPaise(customAmounts[it].orEmpty()) ?: 0L }
    val remainingPaise = totalPaise - assignedPaise
    val dirty = amount.isNotBlank() || desc.isNotBlank() || customAmounts.values.any { it.isNotBlank() }

    LaunchedEffect(Unit) { if (autoFocus) runCatching { amountFocus.requestFocus() } }
    fun close() { if (dirty && !saving) confirmDiscard = true else onBack() }
    BackHandler(enabled = dirty || saving) { if (!saving) confirmDiscard = true }

    fun submit() {
        if (saving) return
        amountError = when {
            amount.isBlank() -> "Enter the amount, like 1,200."
            lenientPaise(amount) == null -> "Enter the amount as a number, like 1,200."
            totalPaise <= 0L -> "Enter an amount above ₹0."
            else -> null
        }
        descError = if (desc.isBlank()) "Enter what it was for, like \"Groceries\"." else null
        splitError = when {
            selected.isEmpty() -> "Choose at least one person to split with."
            !equalSplit && totalPaise > 0 && remainingPaise > 0 -> "${formatInr(remainingPaise / 100.0)} still to assign."
            !equalSplit && totalPaise > 0 && remainingPaise < 0 -> "Shares are ${formatInr(-remainingPaise / 100.0)} over the total."
            else -> null
        }
        when {
            amountError != null -> { runCatching { amountFocus.requestFocus() }; return }
            descError != null -> { runCatching { descFocus.requestFocus() }; return }
            splitError != null -> { scope.launch { splitBring.bringIntoView() }; return }
        }
        val split = members.map { it.uid }.filter { it in selected }
        val custom = if (equalSplit) null else split.associateWith { (lenientPaise(customAmounts[it].orEmpty()) ?: 0L) / 100.0 }
        saving = true
        saveError = null
        onSubmit(ExpenseDraft(desc.trim(), totalPaise / 100.0, split, category.lowercase(), custom, paidBy)) { ok ->
            saving = false
            if (ok) onSaved() else saveError = "Unable to add the expense. Check your connection and try again."
        }
    }

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        HqBackAppBar(title = "Add expense", onBack = { close() })
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.group)
        ) {
            HqTextField(
                value = amount,
                onValueChange = { amount = it; amountError = null },
                label = "Amount (₹)", placeholder = "e.g. 1,200",
                leadingIcon = Icons.Filled.CurrencyRupee, keyboardType = KeyboardType.Decimal,
                errorText = amountError,
                modifier = Modifier.focusRequester(amountFocus),
            )
            HqTextField(
                value = desc,
                onValueChange = { desc = it; descError = null },
                label = "What was it for?", placeholder = "e.g. Groceries",
                leadingIcon = Icons.AutoMirrored.Filled.ReceiptLong,
                errorText = descError,
                imeAction = ImeAction.Done,
                modifier = Modifier.focusRequester(descFocus),
            )
            FormGroup("Paid by") {
                HqChipFlow {
                    members.forEach { member ->
                        HqChip(
                            label = if (member.uid == currentUid) "You" else member.nickname.substringBefore(" ").ifBlank { "Flatmate" },
                            selected = paidBy == member.uid,
                            onClick = { paidBy = member.uid },
                        )
                    }
                }
            }
            FormGroup("Split", Modifier.bringIntoViewRequester(splitBring)) {
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    HqChip(label = "Split equally", selected = equalSplit, onClick = { equalSplit = true; splitError = null })
                    HqChip(label = "Custom amounts", selected = !equalSplit, onClick = { equalSplit = false; splitError = null })
                }
                Text(
                    if (equalSplit) "Shared equally between everyone checked below." else "Enter what each checked person owes.",
                    style = HqType.bodyMedium, color = c.textSecondary,
                )
                val lastShareUid = members.lastOrNull { it.uid in selected }?.uid
                members.forEach { member ->
                    val on = member.uid in selected
                    habitiq.app.ui.components.HqCheckboxRow(
                        title = if (member.uid == currentUid) "You" else member.nickname.substringBefore(" ").ifBlank { "Flatmate" },
                        checked = on,
                        onCheckedChange = { checked ->
                            picked = if (checked) selected + member.uid else selected - member.uid
                            splitError = null
                        },
                    )
                    if (!equalSplit && on) {
                        HqTextField(
                            value = customAmounts[member.uid].orEmpty(),
                            onValueChange = { customAmounts = customAmounts + (member.uid to it); splitError = null },
                            label = if (member.uid == currentUid) "Your share (₹)" else "${member.nickname.substringBefore(" ").ifBlank { "Flatmate" }}'s share (₹)",
                            keyboardType = KeyboardType.Decimal,
                            imeAction = if (member.uid == lastShareUid) ImeAction.Done else ImeAction.Next,
                            modifier = Modifier.padding(start = HqSpacing.xxxl, bottom = HqSpacing.sm),
                        )
                    }
                }
                if (!equalSplit && splitError == null) {
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
                splitError?.let { HqInlineError(it) }
            }
            FormGroup("Category (optional)") {
                HqChipFlow {
                    TaskCategories.forEach { cat ->
                        HqChip(label = cat, selected = category == cat, onClick = { category = cat })
                    }
                }
            }
            Spacer(Modifier.height(HqSpacing.lg))
        }
        Column(
            Modifier.fillMaxWidth().background(c.canvas).navigationBarsPadding().imePadding()
                .padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.sm),
        ) {
            saveError?.let { HqInlineError(it) }
            HqButton(text = "Add expense", loading = saving, onClick = { submit() })
        }
    }

    if (confirmDiscard) {
        HqDiscardChangesDialog(onKeepEditing = { confirmDiscard = false }, onDiscard = { confirmDiscard = false; onBack() })
    }
}

/** A short label directly above the controls it names (chunking + proximity). */
@Composable
internal fun FormGroup(label: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = LocalHqColors.current
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
        Text(label, style = HqType.labelMedium, color = c.textPrimary, modifier = Modifier.semantics { heading() })
        content()
    }
}

