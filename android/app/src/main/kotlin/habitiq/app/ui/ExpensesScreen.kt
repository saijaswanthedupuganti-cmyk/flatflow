package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.data.FlatExpense
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.ui.figma.FigmaBackHeader
import habitiq.app.ui.figma.FigmaPrimaryButton
import habitiq.app.ui.figma.FigmaScreenBackground
import habitiq.app.ui.figma.FigmaTextField
import habitiq.app.ui.figma.TaskCategories
import habitiq.app.ui.figma.TaskCategoryLabel
import habitiq.app.ui.theme.FigmaColors
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: FlatViewModel) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycleCompat()
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val triggerAdd by viewModel.showAddExpenseTrigger.collectAsStateWithLifecycleCompat()

    var showAdd by remember { mutableStateOf(false) }
    LaunchedEffect(triggerAdd) { if (triggerAdd) { showAdd = true; viewModel.showAddExpenseTrigger.value = false } }

    val uid = currentUser?.uid.orEmpty()
    val balances = remember(expenses, uid) { computeBalances(expenses, uid) }

    if (showAdd) {
        AddExpenseScreen(
            viewModel = viewModel,
            members = members,
            onBack = { showAdd = false },
            onSaved = { showAdd = false }
        )
        return
    }

    Box(Modifier.fillMaxSize().background(FigmaColors.Background)) {
        Column(Modifier.fillMaxSize()) {
            Text("Expenses", modifier = Modifier.padding(20.dp), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FigmaColors.Ink)
            if (balances.isNotEmpty()) {
                Text("Balances", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.SemiBold, color = FigmaColors.Ink)
                balances.forEach { (otherUid, balance) ->
                    val name = members.find { it.uid == otherUid }?.nickname ?: otherUid
                    val label = if (balance > 0) "$name owes you ₹${balance.toInt()}" else "You owe $name ₹${abs(balance).toInt()}"
                    Text(label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), fontSize = 13.sp, color = FigmaColors.InkSecondary)
                }
            }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(expenses, key = { it.id }) { expense -> ExpenseRow(expense, members) }
            }
        }
        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = FigmaColors.Primary
        ) { Icon(Icons.Filled.AddCard, "Add expense") }
    }
}

@Composable
private fun AddExpenseScreen(viewModel: FlatViewModel, members: List<Member>, onBack: () -> Unit, onSaved: () -> Unit) {
    var desc by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Lifestyle") }

    FigmaScreenBackground {
        FigmaBackHeader(title = "Add Expense", onBack = onBack, subtitle = "Split equally among flatmates.")
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("DESCRIPTION", color = FigmaColors.InkSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.26.sp)
                FigmaTextField(value = desc, onValueChange = { desc = it }, placeholder = "e.g., Buy vegetables", fontSize = 16)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("CATEGORY", color = FigmaColors.InkSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.26.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TaskCategories.forEach { cat ->
                        val selected = category == cat
                        TaskCategoryLabel(
                            label = cat,
                            color = if (selected) FigmaColors.Ink else FigmaColors.InkMuted,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { category = cat }
                                .padding(horizontal = 4.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AMOUNT (INR)", color = FigmaColors.InkSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.26.sp)
                FigmaTextField(value = amount, onValueChange = { amount = it }, placeholder = "e.g., 500")
            }
            Spacer(Modifier.height(80.dp))
        }
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp)) {
            FigmaPrimaryButton(
                text = "Save Expense",
                enabled = desc.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0,
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: return@FigmaPrimaryButton
                    viewModel.addExpense(desc, amt, members.map { it.uid }, category.lowercase())
                    onSaved()
                }
            )
        }
    }
}

@Composable
private fun ExpenseRow(expense: FlatExpense, members: List<Member>) {
    val payer = members.find { it.uid == expense.paidBy }?.nickname ?: expense.paidBy
    Card(colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(expense.description, fontWeight = FontWeight.Bold, color = FigmaColors.Ink)
            Text("₹${expense.amount.toInt()} · paid by $payer", fontSize = 12.sp, color = FigmaColors.InkSecondary)
            Text(expense.date, fontSize = 11.sp, color = FigmaColors.InkMuted)
        }
    }
}

private fun computeBalances(expenses: List<FlatExpense>, currentUid: String): Map<String, Double> {
    val calculated = mutableMapOf<String, Double>()
    expenses.forEach { expense ->
        val splits = expense.splits
        if (expense.paidBy == currentUid) {
            splits.forEach { (uid, share) ->
                if (uid != currentUid) calculated[uid] = (calculated[uid] ?: 0.0) + share
            }
        } else if (expense.splitAmong.contains(currentUid)) {
            val myShare = splits[currentUid] ?: 0.0
            calculated[expense.paidBy] = (calculated[expense.paidBy] ?: 0.0) - myShare
        }
    }
    return calculated.filter { abs(it.value) > 0.5 }
}
