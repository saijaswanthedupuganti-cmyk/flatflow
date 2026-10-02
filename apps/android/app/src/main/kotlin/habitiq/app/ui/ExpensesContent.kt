package habitiq.app.ui

import androidx.compose.foundation.layout.Box
import habitiq.app.ui.components.hqToneFor
import habitiq.app.ui.components.HqAvatarSize
import habitiq.app.ui.components.HqAvatar
import habitiq.app.ui.components.HqListHeading
import habitiq.app.ui.components.HqSectionTitle
import habitiq.app.ui.components.HqTileTone
import habitiq.app.ui.components.HqIconTile
import habitiq.app.ui.components.HqIcons
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import habitiq.app.ui.components.HqArt
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqGroup
import habitiq.app.ui.components.HqIllustration
import habitiq.app.ui.components.HqRowDivider
import habitiq.app.ui.components.HqSectionHeader
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

data class ContributionLine(val label: String, val date: String, val text: String)

/** One person you owe or who owes you. Amounts are pre-formatted so currency and value never separate. */
data class PersonBalanceItem(
    val key: String,
    val name: String,
    val youOwe: Boolean,
    val amountText: String,
    val contributions: List<ContributionLine>,
)

data class ExpenseListItem(val id: String, val title: String, val support: String, val amountText: String)

data class ExpensesUiModel(
    val oweText: String?,
    val oweSupport: String?,
    val owedText: String?,
    val owedSupport: String?,
    val people: List<PersonBalanceItem>,
    val expenses: List<ExpenseListItem>,
)

/**
 * Expenses, following the Figma Make Expenses and Balances screens: a dark balance hero, the Daily/Monthly
 * segmented control, a per-person breakdown and recent expense rows. [header] renders at the top of the
 * scrolling column (Manage puts its title and switch there).
 */
@Composable
fun ExpensesContent(
    model: ExpensesUiModel,
    onOpenBills: () -> Unit,
    onSettle: (PersonBalanceItem) -> Unit,
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
) {
    val c = LocalHqColors.current
    val settled = model.oweText == null && model.owedText == null
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = HqSpacing.screenHorizontal)
            .padding(top = HqSpacing.sm, bottom = HqSpacing.screenEnd),
    ) {
        header()

        // Figma balance hero: dark teal gradient card, tracked caps label, the amount, then who.
        Column(
            Modifier.fillMaxWidth()
                .shadow(15.dp, RoundedCornerShape(24.dp), ambientColor = c.textPrimary.copy(alpha = .17f), spotColor = c.textPrimary.copy(alpha = .17f))
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF173A36), Color(0xFF102B28))))
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text("YOUR BALANCE", style = HqType.labelSmall, color = Color(0xFF8DD8CF), fontWeight = FontWeight.ExtraBold)
            if (settled) {
                Text("All settled", style = HqType.titleMedium2, color = Color.White, modifier = Modifier.padding(top = 4.dp))
                Text("Nobody owes anything right now.", style = HqType.bodyMedium, color = Color(0xFFACBCBA))
            } else {
                model.oweText?.let {
                    Text("You owe $it", style = HqType.titleMedium2, color = Color.White, modifier = Modifier.padding(top = 4.dp))
                    model.oweSupport?.let { s -> Text(s, style = HqType.bodyMedium, color = Color(0xFFACBCBA)) }
                }
                model.owedText?.let {
                    Text("You are owed $it", style = HqType.titleMedium2, color = Color.White, modifier = Modifier.padding(top = 4.dp))
                    model.owedSupport?.let { s -> Text(s, style = HqType.bodyMedium, color = Color(0xFFACBCBA)) }
                }
            }
        }

        Spacer(Modifier.size(18.dp))
        // Daily splits is this screen; Monthly bills opens its own screen, so selection stays on Daily.
        habitiq.app.ui.components.HqScopeTabs(
            options = listOf("Daily splits" to null, "Monthly bills" to null),
            selectedIndex = 0,
            onSelect = { if (it == 1) onOpenBills() },
        )

        if (model.people.isNotEmpty()) {
            HqSectionTitle("Breakdown")
            model.people.forEach { person -> PersonBalance(person) { onSettle(person) } }
            Row(
                Modifier.padding(top = 18.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.surfaceSubtle).padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(HqIcons.Shield, null, tint = c.textSecondary, modifier = Modifier.size(HqIconSize.sm))
                Text("Oddroof records settlements but does not process payments.", style = HqType.bodyMedium, color = c.textSecondary)
            }
        }

        HqListHeading("Recent")
        if (model.expenses.isEmpty()) {
            EmptyExpenses(onAddExpense)
        } else {
            model.expenses.forEachIndexed { index, item -> ExpenseRow(item, index) }
        }
    }
}

@Composable
private fun PersonBalance(person: PersonBalanceItem, onSettle: () -> Unit) {
    val c = LocalHqColors.current
    var expanded by rememberSaveable(person.key) { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HqAvatar(person.name, size = HqAvatarSize.MD, tone = hqToneFor(person.name, false))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(person.name, style = HqType.rowTitle, color = c.textPrimary)
                Text(if (person.youOwe) "You owe" else "Owes you", style = HqType.bodyMedium, color = c.textSecondary)
            }
            Text(person.amountText, style = HqType.amountRow, color = c.textPrimary)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (person.contributions.isNotEmpty()) {
                HqTextButton(text = if (expanded) "Hide details" else "Details", onClick = { expanded = !expanded })
            }
            Spacer(Modifier.weight(1f))
            // A text action keeps the rows light; the confirmation dialog carries the weight of the decision.
            HqTextButton(text = if (person.youOwe) "Record a settlement" else "Mark received", onClick = onSettle)
        }
        if (expanded) {
            Column(Modifier.padding(top = HqSpacing.xs, bottom = HqSpacing.related), verticalArrangement = Arrangement.spacedBy(HqSpacing.related)) {
                person.contributions.forEach { line ->
                    Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.related), verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(line.label, style = HqType.bodyMedium, color = c.textPrimary)
                            Text(line.date, style = HqType.labelSmall, color = c.textSecondary)
                        }
                        Text(line.text, style = HqType.bodyMedium, color = c.textSecondary)
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderSubtle))
    }
}

/** Figma expense row: letter tile in a rotating tone, title and support, amount on the right. */
@Composable
private fun ExpenseRow(item: ExpenseListItem, index: Int) {
    val c = LocalHqColors.current
    Row(
        Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HqIconTile(
            null, listOf(HqTileTone.Teal, HqTileTone.Sand, HqTileTone.Coral)[index % 3], size = 44,
            glyph = item.title.firstOrNull()?.uppercase() ?: "R",
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.title, style = HqType.rowTitle, color = c.textPrimary)
            Text(item.support, style = HqType.bodyMedium, color = c.textSecondary)
        }
        Text(item.amountText, style = HqType.amountRow, color = c.textPrimary)
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderSubtle))
}

@Composable
private fun EmptyExpenses(onAddExpense: () -> Unit) {
    val c = LocalHqColors.current
    Column(
        Modifier.fillMaxWidth().padding(vertical = HqSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HqSpacing.related),
    ) {
        HqIllustration(HqArt.Receipt, Modifier.width(80.dp))
        Text("No expenses yet", style = HqType.titleSmall2, color = c.textPrimary)
        Text("Start tracking shared spending with your flat.", style = HqType.bodyMedium, color = c.textSecondary)
        HqButton(text = "Add expense", onClick = onAddExpense, fullWidth = false)
    }
}
