package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.data.BillInstance
import habitiq.app.data.RecurringBill
import habitiq.app.data.Settlement
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.lib.SuggestedSettlement
import habitiq.app.lib.currentMonthKey
import habitiq.app.lib.suggestSettlements
import habitiq.app.ui.figma.FigmaBackHeader
import habitiq.app.ui.figma.FigmaPrimaryButton
import habitiq.app.ui.figma.FigmaScreenBackground
import habitiq.app.ui.figma.FigmaTextField
import habitiq.app.ui.theme.FigmaColors
import kotlin.math.abs

@Composable
fun BillsScreen(viewModel: FlatViewModel) {
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
    LaunchedEffect(trigger) { if (trigger) { tab = "Settle"; viewModel.showBillsTrigger.value = false } }

    if (showAddBill) {
        AddRecurringBillScreen(viewModel, members, onBack = { showAddBill = false }, onSaved = { showAddBill = false })
        return
    }
    if (showSettle) {
        SettleUpScreen(viewModel, members, currentUser?.uid.orEmpty(), onBack = { showSettle = false })
        return
    }

    val month = currentMonthKey()
    val monthInstances = instances.filter { it.month == month }

    Column(Modifier.fillMaxSize().background(FigmaColors.Background)) {
        Text("Bills & Settlements", modifier = Modifier.padding(20.dp), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FigmaColors.Ink)
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Bills", "Instances", "Settle").forEach { label ->
                FilterChip(selected = tab == label, onClick = { tab = label }, label = { Text(label) })
            }
        }
        when (tab) {
            "Bills" -> {
                if (bills.isEmpty()) {
                    Text("No recurring bills yet.${if (isAdmin) " Tap + to add rent, WiFi, etc." else ""}",
                        modifier = Modifier.padding(16.dp), color = FigmaColors.InkSecondary, fontSize = 14.sp)
                }
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(bills.filter { it.active }, key = { it.id }) { bill ->
                        RecurringBillCard(bill, monthInstances.find { it.templateId == bill.id }, isAdmin, members,
                            onGenerate = { amt -> viewModel.generateBill(bill.id, amt) },
                            onMarkPaid = { inst -> viewModel.markBillPaid(inst.id) })
                    }
                }
            }
            "Instances" -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isAdmin && !isMonthClosed) {
                        FigmaPrimaryButton("Close month ($month)", onClick = { viewModel.closeMonth(month) })
                    }
                    if (isMonthClosed) Text("Month $month is closed.", color = FigmaColors.Success)
                    if (suggestions.isEmpty()) Text("All settled for $month!", color = FigmaColors.Primary)
                    suggestions.forEach { s -> SettlementSuggestionRow(s, members, onConfirm = { viewModel.recordSettlement(s) }) }
                    Spacer(Modifier.height(8.dp))
                    Text("Recorded settlements", fontWeight = FontWeight.SemiBold, color = FigmaColors.Ink)
                    settlements.take(10).forEach { st -> SettlementHistoryRow(st, members) }
                    FigmaPrimaryButton("Manual settle up", onClick = { showSettle = true })
                }
            }
        }
    }
    if (isAdmin && tab == "Bills") {
        FloatingActionButton(
            onClick = { showAddBill = true },
            modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.BottomEnd).padding(16.dp),
            containerColor = FigmaColors.Primary
        ) { Icon(Icons.Filled.Add, "Add bill") }
    }
}

@Composable
private fun RecurringBillCard(
    bill: RecurringBill, instance: BillInstance?, isAdmin: Boolean, members: List<Member>,
    onGenerate: (Double?) -> Unit, onMarkPaid: (BillInstance) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface)) {
        Column(Modifier.padding(12.dp)) {
            Text(bill.name, fontWeight = FontWeight.Bold)
            Text(
                if (bill.isVariable) "Variable · day ${bill.billingDay}" else "₹${bill.amount?.toInt() ?: 0} · day ${bill.billingDay}",
                fontSize = 13.sp, color = FigmaColors.InkSecondary
            )
            if (instance != null) {
                Text("This month: ${instance.status}${instance.amount?.let { a -> " · ₹${a.toInt()}" } ?: ""}", fontSize = 12.sp, color = FigmaColors.Primary)
                if (instance.status == "split_generated" && instance.paidBy.isNotEmpty()) {
                    TextButton(onClick = { onMarkPaid(instance) }) { Text("Mark paid") }
                }
            } else if (isAdmin) {
                TextButton(onClick = { onGenerate(if (bill.isVariable) null else bill.amount) }) { Text("Generate this month") }
            }
        }
    }
}

@Composable
private fun BillInstanceRow(
    inst: BillInstance, members: List<Member>, isAdmin: Boolean, currentUid: String,
    onMarkPaid: () -> Unit, onSkip: () -> Unit,
    onToggleCollected: (String, Boolean) -> Unit
) {
    val payer = members.find { it.uid == inst.paidBy }?.nickname ?: inst.paidBy
    val collectorId = inst.collectorId ?: inst.paidBy
    val isCollector = collectorId == currentUid
    Card(colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface)) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(inst.name, fontWeight = FontWeight.SemiBold)
                    Text("${inst.status} · payer: $payer", fontSize = 12.sp, color = FigmaColors.InkSecondary)
                    inst.amount?.let { Text("₹${it.toInt()}", fontSize = 14.sp) }
                }
                Column {
                    if (inst.status == "split_generated") TextButton(onClick = onMarkPaid) { Text("Paid") }
                    if (isAdmin && inst.status != "skipped") TextButton(onClick = onSkip) { Text("Skip") }
                }
            }
            if (inst.status == "split_generated" && inst.splits != null && (isCollector || isAdmin)) {
                inst.participants.filter { it != inst.paidBy }.forEach { uid ->
                    val name = members.find { it.uid == uid }?.nickname ?: uid
                    val collected = inst.collectedFrom?.get(uid) == true
                    val canToggle = isCollector || uid == currentUid
                    if (canToggle) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = collected, onCheckedChange = { onToggleCollected(uid, it) })
                            Text("$name ${if (collected) "received" else "pending"}", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettlementSuggestionRow(s: SuggestedSettlement, members: List<Member>, onConfirm: () -> Unit) {
    val from = members.find { it.uid == s.fromUserId }?.nickname ?: s.fromUserId
    val to = members.find { it.uid == s.toUserId }?.nickname ?: s.toUserId
    Card(colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface)) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("$from → $to: ₹${s.amount.toInt()}", fontSize = 14.sp)
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = FigmaColors.Primary)) { Text("Confirm") }
        }
    }
}

@Composable
private fun SettlementHistoryRow(st: Settlement, members: List<Member>) {
    val from = members.find { it.uid == st.fromUserId }?.nickname ?: st.fromUserId
    val to = members.find { it.uid == st.toUserId }?.nickname ?: st.toUserId
    Text("$from paid $to ₹${st.amount.toInt()} · ${st.date}", fontSize = 12.sp, color = FigmaColors.InkSecondary)
}

@Composable
private fun AddRecurringBillScreen(viewModel: FlatViewModel, members: List<Member>, onBack: () -> Unit, onSaved: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var billingDay by remember { mutableStateOf("1") }
    var isVariable by remember { mutableStateOf(false) }

    FigmaScreenBackground {
        FigmaBackHeader("Add Recurring Bill", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FigmaTextField(name, { name = it }, "Bill name (Rent, WiFi…)")
            if (!isVariable) FigmaTextField(amount, { amount = it }, "Amount (₹)")
            FigmaTextField(billingDay, { billingDay = it }, "Billing day (1-28)")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isVariable, onCheckedChange = { isVariable = it })
                Text("Variable amount (enter each month)")
            }
            FigmaPrimaryButton("Save bill", onClick = {
                val amt = amount.toDoubleOrNull()
                viewModel.createRecurringBill(name, if (isVariable) null else amt, billingDay.toIntOrNull() ?: 1, isVariable, members.map { it.uid })
                onSaved()
            })
        }
    }
}

@Composable
private fun SettleUpScreen(viewModel: FlatViewModel, members: List<Member>, uid: String, onBack: () -> Unit) {
    var toUid by remember { mutableStateOf(members.firstOrNull { it.uid != uid }?.uid.orEmpty()) }
    var amount by remember { mutableStateOf("") }
    FigmaScreenBackground {
        FigmaBackHeader("Settle up", onBack)
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Pay to", fontWeight = FontWeight.SemiBold)
            members.filter { it.uid != uid }.forEach { m ->
                FilterChip(selected = toUid == m.uid, onClick = { toUid = m.uid }, label = { Text(m.nickname) })
            }
            FigmaTextField(amount, { amount = it }, "Amount (₹)")
            FigmaPrimaryButton("Record settlement", onClick = {
                amount.toDoubleOrNull()?.let { viewModel.recordManualSettlement(toUid, it) }
                onBack()
            })
        }
    }
}
