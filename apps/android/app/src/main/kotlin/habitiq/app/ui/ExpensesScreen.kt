package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import habitiq.app.data.FlatExpense
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.ui.figma.TaskCategories
import habitiq.app.lib.pairwisePersonalBalances
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqEmptyState
import habitiq.app.ui.components.HqRootAppBar
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqFadeUp
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: FlatViewModel,
    onBack: (() -> Unit)? = null,
    onOpenBills: () -> Unit = {},
    modifier: Modifier = Modifier
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
    var balancesOpen by remember { mutableStateOf(false) }
    LaunchedEffect(triggerAdd) { if (triggerAdd) { showAdd = true; viewModel.showAddExpenseTrigger.value = false } }
    // Manage Flat's "Payment due" attention card lands here with the balance breakdown
    // already open, matching section 19/30 ("no unnecessary navigation steps").
    LaunchedEffect(triggerBalances) { if (triggerBalances) { balancesOpen = true; viewModel.showBalancesTrigger.value = false } }

    val uid = currentUser?.uid.orEmpty()
    val balances = remember(expenses, settlements, uid) { pairwisePersonalBalances(expenses, settlements, uid) }
    val iOwe = balances.filter { it.value < -0.5 }
    val owed = balances.filter { it.value > 0.5 }

    if (showAdd) {
        AddExpenseScreen(
            viewModel = viewModel,
            members = members,
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

    Box(modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize()) {
            if (onBack != null) HqBackAppBar(title = "Expenses", onBack = onBack) else HqRootAppBar(title = "Expenses")
            Row(Modifier.padding(horizontal = HqSpacing.lg, vertical = HqSpacing.sm), horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                HqChip(label = "Daily splits", selected = true, onClick = {})
                HqChip(label = "Monthly bills", selected = false, onClick = onOpenBills)
            }
            val allSettled = iOwe.isEmpty() && owed.isEmpty()
            HqCard(
                modifier = Modifier.padding(horizontal = HqSpacing.lg),
                variant = HqCardVariant.Status,
                statusTint = if (allSettled) c.successContainer else null,
                onClick = { balancesOpen = !balancesOpen },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                    when {
                        iOwe.isNotEmpty() -> {
                            Text("You owe ₹${iOwe.values.sumOf { abs(it) }.toInt()}", style = HqType.titleMedium, color = c.textPrimary)
                            Text("${iOwe.size} ${if (iOwe.size == 1) "person" else "people"} · tap for details", style = HqType.bodySmall, color = c.textSecondary)
                        }
                        owed.isNotEmpty() -> {
                            Text("You're owed ₹${owed.values.sumOf { it }.toInt()}", style = HqType.titleMedium, color = c.textPrimary)
                            Text("${owed.size} ${if (owed.size == 1) "person" else "people"} · tap for details", style = HqType.bodySmall, color = c.textSecondary)
                        }
                        // Calm/positive framing per design system -- never style "all settled" like a warning.
                        else -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                            Icon(Icons.Filled.CheckCircle, null, tint = c.success)
                            Column {
                                Text("All balances settled", style = HqType.titleMedium, color = c.success)
                                Text("Great! Everyone is up to date.", style = HqType.bodySmall, color = c.textSecondary)
                            }
                        }
                    }
                }
            }
            Box(Modifier.padding(horizontal = HqSpacing.lg, vertical = HqSpacing.sm)) {
                ExpenseActionRow(
                    icon = Icons.Filled.Handshake,
                    title = "Settlements",
                    subtitle = if (balances.isEmpty()) "Everyone is settled" else "View who owes whom",
                    onClick = { balancesOpen = !balancesOpen }
                )
            }
            if (balancesOpen && balances.isNotEmpty()) {
                Column(Modifier.padding(horizontal = HqSpacing.lg, vertical = HqSpacing.sm), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    balances.forEach { (otherUid, balance) ->
                        val name = members.find { it.uid == otherUid }?.nickname ?: "Flatmate"
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (balance > 0) "$name owes you ₹${balance.toInt()}" else "You owe $name ₹${abs(balance).toInt()}",
                                style = HqType.bodySmall,
                                color = c.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            HqTextButton(
                                text = if (balance < 0) "Settle" else "Received",
                                onClick = {
                                    if (balance < 0) viewModel.recordManualSettlement(otherUid, abs(balance))
                                    else viewModel.recordMarkReceived(otherUid, balance)
                                }
                            )
                        }
                    }
                }
            }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(HqSpacing.lg), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                item {
                    Text("Recent expenses", style = HqType.titleSmall, color = c.textPrimary)
                }
                if (expenses.isEmpty()) {
                    item {
                        HqEmptyState(
                            icon = Icons.Filled.AddCard,
                            title = "No expenses yet",
                            message = "Start tracking shared spending with your flat.",
                            primaryLabel = "Add expense",
                            onPrimaryClick = { showAdd = true },
                        )
                    }
                }
                items(expenses, key = { it.id }) { expense -> ExpenseRow(expense, members) }
            }
        }
        if (expenses.isNotEmpty()) {
            FloatingActionButton(
                onClick = { showAdd = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(HqSpacing.lg),
                containerColor = c.brandPrimary,
                contentColor = c.onBrandPrimary,
            ) { Icon(Icons.Filled.AddCard, "Add expense") }
        }
    }
}

@Composable
private fun AddExpenseScreen(viewModel: FlatViewModel, members: List<Member>, onBack: () -> Unit, onSaved: (String) -> Unit) {
    val c = LocalHqColors.current
    var desc by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Lifestyle") }
    var equalSplit by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var selected by remember(members) { mutableStateOf(members.map { it.uid }.toSet()) }

    Column(Modifier.fillMaxSize().background(c.background).statusBarsPadding()) {
        HqBackAppBar(title = "Add Expense", onBack = onBack)
        Text(
            "Split equally among flatmates.",
            style = HqType.bodyMedium,
            color = c.textSecondary,
            modifier = Modifier.padding(horizontal = HqSpacing.lg).padding(bottom = HqSpacing.sm)
        )
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.xl)
        ) {
            HqTextField(value = desc, onValueChange = { desc = it }, label = "Description", placeholder = "e.g., Groceries", leadingIcon = Icons.AutoMirrored.Filled.ReceiptLong)
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                Text("Category", style = HqType.labelLarge, color = c.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    TaskCategories.forEach { cat ->
                        HqChip(label = cat, selected = category == cat, onClick = { category = cat })
                    }
                }
            }
            HqTextField(value = amount, onValueChange = { amount = it }, label = "Amount (₹)", placeholder = "e.g., 1,200", leadingIcon = Icons.Filled.CurrencyRupee)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                Icon(Icons.Filled.AccountBalanceWallet, null, tint = c.brandPrimary)
                Text("Paid by you", style = HqType.titleSmall, color = c.textPrimary)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                Icon(Icons.Filled.Groups, null, tint = c.brandPrimary)
                Text("Split type", style = HqType.labelLarge, color = c.textSecondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                HqChip(label = "Equal split", selected = equalSplit, onClick = { equalSplit = true })
                HqChip(label = "Custom split", selected = !equalSplit, onClick = { equalSplit = false })
            }
            Text("Uncheck anyone who should not share this.", style = HqType.bodySmall, color = c.textSecondary)
            members.forEach { member ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = member.uid in selected,
                        onCheckedChange = { on ->
                            selected = if (on) selected + member.uid else selected - member.uid
                        }
                    )
                    Text(member.nickname, style = HqType.bodyMedium, color = c.textPrimary)
                }
            }
            Spacer(Modifier.height(HqSpacing.xxhuge))
        }
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(HqSpacing.xl)) {
            saveError?.let { Text(it, color = c.error, style = HqType.bodySmall) }
            HqButton(
                text = if (saving) "Adding…" else "Add expense",
                enabled = desc.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0 && selected.isNotEmpty() && !saving,
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: return@HqButton
                    saving = true
                    saveError = null
                    viewModel.addExpense(
                        desc,
                        amt,
                        selected.toList(),
                        category.lowercase(),
                        splitType = if (equalSplit) "equal" else "custom"
                    ) { ok ->
                        saving = false
                        if (ok) onSaved("₹${amt.toInt()} for $desc") else saveError = "Couldn't add the expense. Try again."
                    }
                }
            )
        }
    }
}

@Composable
private fun ExpenseAddedScreen(label: String, onAnother: () -> Unit, onDone: () -> Unit) {
    val c = LocalHqColors.current
    HqFadeUp(modifier = Modifier.fillMaxSize()) {
    Column(
        Modifier.fillMaxSize().background(c.background).padding(HqSpacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(88.dp).clip(CircleShape).background(c.successContainer),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Filled.CheckCircle, null, tint = c.success, modifier = Modifier.size(52.dp)) }
        Spacer(Modifier.height(HqSpacing.xl))
        Text("Expense added", style = HqType.headlineMedium, color = c.textPrimary)
        Text(label, style = HqType.bodyLarge, color = c.textSecondary, modifier = Modifier.padding(vertical = HqSpacing.lg))
        HqButton(text = "View expense", onClick = onDone)
        Spacer(Modifier.height(HqSpacing.md))
        HqButton(text = "Add another", onClick = onAnother, variant = HqButtonVariant.Secondary)
    }
    }
}

@Composable
private fun ExpenseRow(expense: FlatExpense, members: List<Member>) {
    val c = LocalHqColors.current
    val payer = members.find { it.uid == expense.paidBy }?.nickname ?: expense.paidBy
    val icon = when (expense.category.lowercase()) {
        "groceries", "food" -> Icons.Filled.ShoppingCart
        "internet", "wifi" -> Icons.Filled.Wifi
        "fuel", "travel" -> Icons.Filled.LocalGasStation
        else -> Icons.AutoMirrored.Filled.ReceiptLong
    }
    HqCard(padding = HqSpacing.md) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(c.warningContainer),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = c.warning) }
            Column(Modifier.weight(1f)) {
                Text(expense.description, style = HqType.titleSmall, color = c.textPrimary)
                Text("${expense.date} · paid by $payer", style = HqType.caption, color = c.textTertiary)
            }
            Text("₹${expense.amount.toInt()}", style = HqType.titleSmall, color = c.textPrimary)
        }
    }
}

@Composable
private fun ExpenseActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val c = LocalHqColors.current
    HqCard(variant = HqCardVariant.Interactive, onClick = onClick, padding = HqSpacing.md) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(c.brandPrimaryContainer),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = c.brandPrimary) }
            Column(Modifier.weight(1f)) {
                Text(title, style = HqType.titleSmall, color = c.textPrimary)
                Text(subtitle, style = HqType.bodySmall, color = c.textSecondary)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = c.textTertiary)
        }
    }
}
