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
import habitiq.app.ui.theme.HqShapes
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.theme.hqShadow

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
    loading: Boolean = false,
    loadFailed: Boolean = false,
    onRetry: () -> Unit = {},
    header: @Composable () -> Unit = {},
) {
    val c = LocalHqColors.current
    val settled = model.oweText == null && model.owedText == null
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = HqSpacing.screenHorizontal)
            .padding(top = HqSpacing.sm, bottom = HqSpacing.fabClearance),
    ) {
        header()
        // In-place switch: Bills replaces this content below the same header (no navigation). It sits
        // directly under the header in both scopes so it never jumps when switching.
        ExpenseScopeTabs(monthly = false, onSelect = { monthly -> if (monthly) onOpenBills() })
        Spacer(Modifier.height(HqSpacing.md))

        // Balance hero: navy brand summary card, label, the amount, then who.
        Column(
            Modifier.fillMaxWidth()
                .hqShadow(habitiq.app.ui.theme.HqShadow.Floating, HqShapes.card)
                .clip(HqShapes.card)
                .background(c.brandInk)
                .padding(HqSpacing.card),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.xs),
        ) {
            Text("Your balance", style = HqType.labelMedium, color = c.onBrandInk.copy(alpha = .8f), fontWeight = FontWeight.SemiBold)
            if (settled) {
                Text("All settled", style = HqType.titleMedium2, color = c.onBrandInk, modifier = Modifier.padding(top = HqSpacing.xs))
                Text("Nobody owes anything right now.", style = HqType.bodyMedium, color = c.onBrandInk.copy(alpha = .8f))
            } else {
                model.oweText?.let {
                    Text("You owe $it", style = HqType.titleMedium2.copy(fontFeatureSettings = "tnum"), color = c.onBrandInk, modifier = Modifier.padding(top = HqSpacing.xs))
                    model.oweSupport?.let { s -> Text(s, style = HqType.bodyMedium, color = c.onBrandInk.copy(alpha = .8f)) }
                }
                model.owedText?.let {
                    Text("You're owed $it", style = HqType.titleMedium2.copy(fontFeatureSettings = "tnum"), color = c.onBrandInk, modifier = Modifier.padding(top = HqSpacing.xs))
                    model.owedSupport?.let { s -> Text(s, style = HqType.bodyMedium, color = c.onBrandInk.copy(alpha = .8f)) }
                }
            }
        }

        if (model.people.isNotEmpty()) {
            HqSectionTitle("Breakdown")
            model.people.forEach { person -> PersonBalance(person) { onSettle(person) } }
            Row(
                Modifier.padding(top = HqSpacing.lg).fillMaxWidth().clip(HqShapes.control).background(c.surfaceSubtle).padding(HqSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
            ) {
                Icon(HqIcons.Shield, null, tint = c.textSecondary, modifier = Modifier.size(HqIconSize.sm))
                Text("Oddroof records payments; it doesn't move money.", style = HqType.bodyMedium, color = c.textSecondary)
            }
        }

        HqListHeading("Recent")
        if (model.expenses.isEmpty() && (loading || loadFailed)) {
            ManageLoadPlaceholder(failed = loadFailed && !loading, onRetry = onRetry)
        } else if (model.expenses.isEmpty()) {
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
    Column(Modifier.fillMaxWidth().padding(vertical = HqSpacing.xs)) {
        Row(Modifier.padding(vertical = HqSpacing.sm), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
            HqAvatar(person.name, size = HqAvatarSize.MD, tone = hqToneFor(person.name, false))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
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
            HqTextButton(text = if (person.youOwe) "Settle up" else "Mark received", onClick = onSettle)
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
        Modifier.fillMaxWidth().padding(vertical = HqSpacing.lg, horizontal = HqSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HqIconTile(
            null, listOf(HqTileTone.Teal, HqTileTone.Sand, HqTileTone.Coral)[index % 3], size = 44,
            glyph = item.title.firstOrNull()?.uppercase() ?: "R",
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
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

/** Daily splits / Monthly bills switch shared by both Expenses scopes so it never moves or restyles. */
@Composable
fun ExpenseScopeTabs(monthly: Boolean, onSelect: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    habitiq.app.ui.components.HqScopeTabs(
        options = listOf("Expenses" to null, "Bills" to null),
        selectedIndex = if (monthly) 1 else 0,
        onSelect = { onSelect(it == 1) },
        modifier = modifier,
    )
}
