package habitiq.app.lib

import habitiq.app.data.BillInstance
import habitiq.app.data.FlatExpense
import habitiq.app.data.Settlement
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Smallest balance, in rupees, that still counts as owed: half a paisa, so anything that rounds to
 * Rs0.01 or more is shown and settled instead of being called "all settled". Web uses the same
 * value in apps/web/lib/settlementUtils.ts. Change both together.
 */
const val BALANCE_EPSILON = 0.005

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
        if (abs(amount) >= BALANCE_EPSILON) bump(uid, amount)
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
    val debtors = people.filter { it.second < -BALANCE_EPSILON }.sortedBy { it.second }.toMutableList()
    val creditors = people.filter { it.second > BALANCE_EPSILON }.sortedByDescending { it.second }.toMutableList()
    val result = mutableListOf<SuggestedSettlement>()
    var di = 0
    var ci = 0
    while (di < debtors.size && ci < creditors.size) {
        val d = debtors[di]
        val c = creditors[ci]
        val payment = min(abs(d.second), c.second)
        if (payment >= BALANCE_EPSILON) {
            result.add(SuggestedSettlement(d.first, c.first, payment.roundToInt().toDouble()))
        }
        debtors[di] = d.first to (d.second + payment)
        creditors[ci] = c.first to (c.second - payment)
        if (abs(debtors[di].second) < BALANCE_EPSILON) di++
        if (creditors[ci].second < BALANCE_EPSILON) ci++
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
    return calculated.filter { abs(it.value) >= BALANCE_EPSILON }
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

/**
 * One line of the explanation behind a pairwise balance. [amount] is signed from the current
 * person's point of view: positive means the other person owes them, negative means they owe.
 */
data class PairContribution(
    val label: String,
    val amount: Double,
    val date: String,
    val isSettlement: Boolean,
)

/**
 * Lists exactly what [pairwisePersonalBalances] sums for one counterparty, so the breakdown a
 * person sees always reconciles with the headline balance. Keep the two in lock-step.
 */
fun pairwiseContributions(
    expenses: List<FlatExpense>,
    settlements: List<Settlement>,
    currentUid: String,
    otherUid: String,
): List<PairContribution> {
    val lines = mutableListOf<PairContribution>()
    expenses.forEach { expense ->
        if (expense.paidBy == currentUid) {
            val share = expense.splits[otherUid]
            if (otherUid != currentUid && share != null) {
                lines += PairContribution(expense.description, share, expense.date, isSettlement = false)
            }
        } else if (expense.paidBy == otherUid && expense.splitAmong.contains(currentUid)) {
            lines += PairContribution(expense.description, -(expense.splits[currentUid] ?: 0.0), expense.date, isSettlement = false)
        }
    }
    settlements.forEach { settlement ->
        if (settlement.fromUserId == currentUid && settlement.toUserId == otherUid) {
            lines += PairContribution("Settlement recorded", settlement.amount, settlement.date, isSettlement = true)
        } else if (settlement.toUserId == currentUid && settlement.fromUserId == otherUid) {
            lines += PairContribution("Settlement recorded", -settlement.amount, settlement.date, isSettlement = true)
        }
    }
    return lines.sortedByDescending { it.date }
}
