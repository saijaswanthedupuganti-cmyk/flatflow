package habitiq.app.lib

import habitiq.app.data.BillInstance
import habitiq.app.data.FlatExpense
import habitiq.app.data.Settlement
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

data class SuggestedSettlement(val fromUserId: String, val toUserId: String, val amount: Double)

fun currentMonthKey(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))

fun computeMonthNetBalances(
    month: String,
    expenses: List<FlatExpense>,
    billInstances: List<BillInstance>,
    settlements: List<Settlement>,
    carryForwardIn: Map<String, Double>? = null
): Map<String, Double> {
    val net = mutableMapOf<String, Double>()
    fun bump(uid: String, delta: Double) { net[uid] = (net[uid] ?: 0.0) + delta }

    carryForwardIn?.forEach { (uid, amount) ->
        if (abs(amount) >= 0.5) bump(uid, amount)
    }

    for (instance in billInstances) {
        if (instance.month != month || instance.status == "skipped") continue
        val splits = instance.splits ?: continue
        if (instance.currency != "INR") continue
        for (uid in instance.participants) {
            if (uid == instance.paidBy) continue
            val owes = splits[uid] ?: 0.0
            if (owes <= 0) continue
            bump(instance.paidBy, owes)
            bump(uid, -owes)
        }
    }

    for (expense in expenses) {
        val expMonth = expense.date.take(7)
        if (expMonth != month || expense.currency != "INR") continue
        for (uid in expense.splitAmong) {
            if (uid == expense.paidBy) continue
            val owes = expense.splits[uid] ?: 0.0
            if (owes <= 0) continue
            bump(expense.paidBy, owes)
            bump(uid, -owes)
        }
    }

    for (settlement in settlements) {
        if (settlement.currency != "INR") continue
        val settMonth = settlement.month ?: settlement.date.take(7)
        if (settMonth != month) continue
        bump(settlement.fromUserId, settlement.amount)
        bump(settlement.toUserId, -settlement.amount)
    }

    return net
}

fun suggestSettlements(netBalances: Map<String, Double>): List<SuggestedSettlement> {
    val people = netBalances.map { (uid, amt) -> uid to amt }.toMutableList()
    val debtors = people.filter { it.second < -0.5 }.sortedBy { it.second }.toMutableList()
    val creditors = people.filter { it.second > 0.5 }.sortedByDescending { it.second }.toMutableList()
    val result = mutableListOf<SuggestedSettlement>()
    var di = 0
    var ci = 0
    while (di < debtors.size && ci < creditors.size) {
        val d = debtors[di]
        val c = creditors[ci]
        val payment = min(abs(d.second), c.second)
        if (payment >= 0.5) {
            result.add(SuggestedSettlement(d.first, c.first, payment.roundToInt().toDouble()))
        }
        debtors[di] = d.first to (d.second + payment)
        creditors[ci] = c.first to (c.second - payment)
        if (abs(debtors[di].second) < 0.5) di++
        if (creditors[ci].second < 0.5) ci++
    }
    return result
}

fun pairwisePersonalBalances(
    expenses: List<FlatExpense>,
    settlements: List<Settlement>,
    currentUid: String
): Map<String, Double> {
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
    settlements.forEach { settlement ->
        if (settlement.fromUserId == currentUid) {
            calculated[settlement.toUserId] = (calculated[settlement.toUserId] ?: 0.0) + settlement.amount
        } else if (settlement.toUserId == currentUid) {
            calculated[settlement.fromUserId] = (calculated[settlement.fromUserId] ?: 0.0) - settlement.amount
        }
    }
    return calculated.filter { abs(it.value) > 0.5 }
}

fun computeEqualSplits(amount: Double, participants: List<String>): Map<String, Double> {
    if (participants.isEmpty()) return emptyMap()
    val share = amount / participants.size
    val rounded = share.toInt().toDouble()
    val result = participants.associateWith { rounded }.toMutableMap()
    val last = participants.last()
    result[last] = amount - rounded * (participants.size - 1)
    return result
}
