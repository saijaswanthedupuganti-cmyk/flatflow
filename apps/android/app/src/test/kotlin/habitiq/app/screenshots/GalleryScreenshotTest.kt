package habitiq.app.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import habitiq.app.ui.components.HqAmountField
import habitiq.app.ui.components.HqAppBar
import habitiq.app.ui.components.HqAvatar
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonPair
import habitiq.app.ui.components.HqButtonSize
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCheckboxRow
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqChipKind
import habitiq.app.ui.components.HqDialogSurface
import habitiq.app.ui.components.HqEmptyState
import habitiq.app.ui.components.HqGroup
import habitiq.app.ui.components.HqIconButton
import habitiq.app.ui.components.HqIconButtonStyle
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqListSkeleton
import habitiq.app.ui.components.HqRadioRow
import habitiq.app.ui.components.HqRemovableChip
import habitiq.app.ui.components.HqRow
import habitiq.app.ui.components.HqRowChevron
import habitiq.app.ui.components.HqRowDivider
import habitiq.app.ui.components.HqRowValue
import habitiq.app.ui.components.HqSegmentedControl
import habitiq.app.ui.components.HqSnackbarVisual
import habitiq.app.ui.components.HqSwitch
import habitiq.app.ui.components.HqSwitchRow
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqShapes
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Component gallery: every Hq component in every state, light + (future) dark, and the text-heavy
 * controls at 200% font. Output: app/build/screenshots/gallery-*.png.
 */
@OptIn(ExperimentalLayoutApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w390dp-h1900dp-xxhdpi", sdk = [34])
class GalleryScreenshotTest : ScreenshotHarness() {

    @Composable
    private fun Page(content: @Composable ColumnScope.() -> Unit) {
        val c = LocalHqColors.current
        Column(
            Modifier.fillMaxSize().background(c.canvas).padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.md),
            content = content,
        )
    }

    @Composable
    private fun Label(text: String) {
        Text(text.uppercase(), style = HqType.labelSmall, color = LocalHqColors.current.textMuted, modifier = Modifier.padding(top = HqSpacing.sm))
    }

    @Composable
    private fun pressed(): MutableInteractionSource {
        val s = remember { MutableInteractionSource() }
        LaunchedEffect(Unit) { kotlinx.coroutines.delay(50); s.emit(PressInteraction.Press(Offset(10f, 10f))) }
        return s
    }

    @Composable
    private fun focused(): MutableInteractionSource {
        val s = remember { MutableInteractionSource() }
        LaunchedEffect(Unit) { kotlinx.coroutines.delay(50); s.emit(FocusInteraction.Focus()) }
        return s
    }

    // ---------------------------------------------------------------- buttons
    @Composable
    private fun Buttons() = Page {
        val c = LocalHqColors.current
        Label("Primary · Large 56 · default / pressed / focused")
        HqButton("Save expense", onClick = {})
        HqButton("Save expense", onClick = {}, interactionSource = pressed())
        HqButton("Save expense", onClick = {}, interactionSource = focused())
        Label("Primary · loading / disabled")
        HqButton("Save expense", onClick = {}, loading = true)
        HqButton("Save expense", onClick = {}, enabled = false)
        Label("Secondary (tonal) · Tertiary · Destructive")
        HqButtonPair(
            secondary = { HqButton("Previous", onClick = {}, variant = HqButtonVariant.Secondary) },
            primary = { HqButton("Next", onClick = {}, trailingIcon = Icons.Filled.ArrowForward) },
        )
        HqButtonPair(
            secondary = { HqButton("Skip for now", onClick = {}, variant = HqButtonVariant.Tertiary) },
            primary = { HqButton("Delete flat", onClick = {}, variant = HqButtonVariant.Destructive, leadingIcon = Icons.Filled.Delete) },
        )
        HqButton("Off", onClick = {}, variant = HqButtonVariant.Secondary, fullWidth = false, enabled = false)
        Label("Sizes · Medium 48 / Small 40 (48 target)")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            HqButton("Add bill", onClick = {}, fullWidth = false, size = HqButtonSize.Medium, leadingIcon = Icons.Filled.Add)
            HqButton("Add bill", onClick = {}, fullWidth = false, size = HqButtonSize.Small, variant = HqButtonVariant.Secondary, leadingIcon = Icons.Filled.Add)
            HqButton("Mark paid", onClick = {}, fullWidth = false, size = HqButtonSize.Small, variant = HqButtonVariant.Tertiary)
        }
        Label("Ghost on navy · text button · icon buttons")
        Box(Modifier.fillMaxWidth().clip(HqShapes.card).background(c.brandInk).padding(HqSpacing.lg)) {
            HqButtonPair(
                secondary = { HqButton("How it works", onClick = {}, variant = HqButtonVariant.GhostOnDark, size = HqButtonSize.Medium) },
                primary = { HqButton("Get started", onClick = {}, size = HqButtonSize.Medium) },
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            HqTextButton("See all", onClick = {})
            HqTextButton("Log out", onClick = {}, color = c.textSecondary)
            HqIconButton(Icons.Outlined.MoreVert, "More options", onClick = {})
            HqIconButton(Icons.Filled.Notifications, "Notifications", onClick = {}, style = HqIconButtonStyle.Tonal)
            HqIconButton(Icons.Filled.Add, "Add", onClick = {}, enabled = false)
        }
    }

    // ---------------------------------------------------------------- selection controls
    @Composable
    private fun Controls() = Page {
        Label("Segmented · equal width")
        HqSegmentedControl(listOf("Tasks", "Expenses"), selectedIndex = 0, onSelect = {})
        HqSegmentedControl(listOf("Day", "Week", "Month"), selectedIndex = 1, onSelect = {})
        Label("Segmented · content width (scrolls)")
        HqSegmentedControl(listOf("All", "Cleaning", "Groceries", "Bills", "Other"), selectedIndex = 2, onSelect = {}, equalWidth = false)
        Label("Switch · off / on / icon / pressed / disabled")
        Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
            HqSwitch(false, {})
            HqSwitch(true, {})
            HqSwitch(true, {}, thumbIcon = Icons.Filled.Check)
            HqSwitch(false, {}, interactionSource = pressed())
            HqSwitch(true, {}, enabled = false)
            HqSwitch(false, {}, enabled = false)
        }
        HqGroup {
            HqSwitchRow("Reminders", checked = true, onCheckedChange = {}, supporting = "A nudge the evening before a task is due")
            HqRowDivider()
            HqSwitchRow("Weekly summary", checked = false, onCheckedChange = {})
            HqRowDivider()
            HqCheckboxRow("Split equally", checked = true, onCheckedChange = {})
            HqRowDivider()
            HqCheckboxRow("Include me", checked = false, onCheckedChange = {}, supporting = "You paid for this one")
        }
        HqGroup(Modifier.selectableGroup()) {
            HqRadioRow("Every week", selected = true, onClick = {})
            HqRowDivider()
            HqRadioRow("Every month", selected = false, onClick = {})
            HqRowDivider()
            HqRadioRow("Once", selected = false, onClick = {}, enabled = false, supporting = "Not available for bills")
        }
        Label("Chips · filter / choice / disabled / input")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            HqChip("Overdue", selected = true, onClick = {})
            HqChip("Mine", onClick = {})
            HqChip("Weekly", selected = true, onClick = {}, kind = HqChipKind.Choice)
            HqChip("Monthly", onClick = {}, kind = HqChipKind.Choice)
            HqChip("Archived", onClick = {}, enabled = false)
            HqChip("Bills", onClick = {}, leadingIcon = Icons.Outlined.Receipt)
            HqRemovableChip("Hyderabad", onRemove = {})
            HqChip("Read-only")
        }
    }

    // ---------------------------------------------------------------- fields
    @Composable
    private fun Fields() = Page {
        HqTextField("", {}, label = "Task name", placeholder = "e.g. Take out the bins", helperText = "Short and specific works best.")
        HqTextField("Kitchen deep clean", {}, label = "Task name")
        HqTextFieldFocused()
        HqTextField("ab", {}, label = "Flat name", errorText = "Use at least 3 characters.")
        HqAmountField("1250.50", {}, label = "Amount")
        HqTextField("hunter22", {}, label = "Password", isPassword = true)
        HqTextField("Locked value", {}, label = "Invite code", enabled = false)
        HqTextField("Bring a spare key\nfor the plumber on Monday.", {}, label = "Note", singleLine = false, minLines = 3)
    }

    @Composable
    private fun HqTextFieldFocused() {
        val fr = remember { androidx.compose.ui.focus.FocusRequester() }
        HqTextField("Groceries", {}, label = "Focused", modifier = Modifier.focusRequester(fr))
        LaunchedEffect(Unit) { runCatching { fr.requestFocus() } }
    }

    // ---------------------------------------------------------------- surfaces
    @Composable
    private fun Surfaces() {
        val c = LocalHqColors.current
        Column(Modifier.fillMaxSize().background(c.canvas)) {
            HqAppBar("Edit task", onBack = {}, actions = { HqIconButton(Icons.Outlined.MoreVert, "More options for Edit task", onClick = {}) })
            HqAppBar("Flat 4B", onBack = {}, large = true)
            Column(Modifier.padding(horizontal = HqSpacing.screenHorizontal), verticalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
                HqCard {
                    Text("This month", style = HqType.labelMedium, color = c.textSecondary)
                    Text("₹4,820", style = HqType.amount, color = c.textPrimary)
                    Text("You're owed ₹1,200", style = HqType.bodyMedium, color = c.textBrand)
                }
                HqGroup {
                    HqRow("Electricity", supporting = "Due 5 Nov", leading = { Icon(Icons.Outlined.Receipt, null, tint = c.iconDefault) }, trailing = { HqRowValue("₹1,840") }, onClick = {})
                    HqRowDivider(startInset = HqSpacing.component + 40.dp + HqSpacing.related)
                    HqRow("Aarav", supporting = "Paid you back", leading = { HqAvatar(name = "Aarav") }, trailing = { HqRowChevron() }, onClick = {})
                    HqRowDivider(startInset = HqSpacing.component + 40.dp + HqSpacing.related)
                    HqRow("Notifications", trailing = { HqRowChevron() }, onClick = {})
                }
                HqListSkeleton(rows = 2)
                HqSnackbarVisual("Expense saved", actionLabel = "Undo", onAction = {})
            }
        }
    }

    @Composable
    private fun Overlays() {
        val c = LocalHqColors.current
        Column(Modifier.fillMaxSize().background(c.scrim.copy(alpha = 0.42f)), verticalArrangement = Arrangement.spacedBy(HqSpacing.xxl)) {
            Box(Modifier.padding(top = HqSpacing.xxxl)) {
                HqDialogSurface(
                    title = "Delete this task?",
                    message = "It disappears for everyone in the flat. This can't be undone.",
                    confirmLabel = "Delete", onConfirm = {}, onDismiss = {},
                    confirmVariant = HqButtonVariant.Destructive, dismissLabel = "Cancel", animateIn = false,
                )
            }
            Box(Modifier.padding(horizontal = HqSpacing.screenHorizontal).clip(HqShapes.card).background(c.canvas)) {
                HqEmptyState(
                    icon = Icons.Outlined.Inbox, title = "No tasks yet",
                    message = "Add the chores your flat shares and Oddroof will take turns for you.",
                    primaryLabel = "Add a task", onPrimaryClick = {}, secondaryLabel = "Learn how turns work", onSecondaryClick = {},
                )
            }
        }
    }

    @Test fun buttonsLight() = shoot("gallery-buttons-light") { Buttons() }
    @Test fun buttonsDark() = shoot("gallery-buttons-dark-future", dark = true) { Buttons() }
    @Test fun buttonsFs2() = shoot("gallery-buttons-fs2", fontScale = 2f) { Buttons() }
    @Test fun controlsLight() = shoot("gallery-controls-light") { Controls() }
    @Test fun controlsDark() = shoot("gallery-controls-dark-future", dark = true) { Controls() }
    @Test fun controlsFs2() = shoot("gallery-controls-fs2", fontScale = 2f) { Controls() }
    @Test fun fieldsLight() = shoot("gallery-fields-light") { Fields() }
    @Test fun fieldsDark() = shoot("gallery-fields-dark-future", dark = true) { Fields() }
    @Test fun fieldsFs2() = shoot("gallery-fields-fs2", fontScale = 2f) { Fields() }
    @Test fun surfacesLight() = shoot("gallery-surfaces-light") { Surfaces() }
    @Test fun surfacesDark() = shoot("gallery-surfaces-dark-future", dark = true) { Surfaces() }
    @Test fun overlaysLight() = shoot("gallery-overlays-light") { Overlays() }
    @Test fun overlaysFs2() = shoot("gallery-overlays-fs2", fontScale = 2f) { Overlays() }
}
