package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.data.FlatExpense
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
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
    var desc by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    LaunchedEffect(triggerAdd) { if (triggerAdd) { showAdd = true; viewModel.showAddExpenseTrigger.value = false } }

    val uid = currentUser?.uid.orEmpty()
    val balances = remember(expenses, uid) { computeBalances(expenses, uid) }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (INR)") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (desc.isNotBlank() && amt != null && amt > 0) {
                        viewModel.addExpense(desc, amt, members.map { it.uid })
                        desc = ""
                        amount = ""
                        showAdd = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }

    Box(Modifier.fillMaxSize().background(FigmaColors.Background)) {
    Column(Modifier.fillMaxSize()) {
        Text("Expenses", modifier = Modifier.padding(20.dp), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FigmaColors.Ink)
        if (balances.isNotEmpty()) {
            Text("Balances", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.SemiBold)
            balances.forEach { (otherUid, balance) ->
                val name = members.find { it.uid == otherUid }?.nickname ?: otherUid
                val label = if (balance > 0) "$name owes you ₹${balance.toInt()}" else "You owe $name ₹${abs(balance).toInt()}"
                Text(label, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), fontSize = 13.sp)
            }
        }
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(expenses, key = { it.id }) { expense ->
                ExpenseRow(expense, members)
            }
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
private fun ExpenseRow(expense: FlatExpense, members: List<Member>) {
    val payer = members.find { it.uid == expense.paidBy }?.nickname ?: expense.paidBy
    Card(colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface)) {
        Column(Modifier.padding(12.dp)) {
            Text(expense.description, fontWeight = FontWeight.Bold)
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
