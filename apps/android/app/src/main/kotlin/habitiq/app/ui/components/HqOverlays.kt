package habitiq.app.ui.components

import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import habitiq.app.ui.theme.hqShadow
import androidx.compose.ui.window.DialogProperties
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Standard bottom sheet shell (design doc section 37): handle + optional title + scrollable
 * content. Use for task/expense details, swap requests, filters, and quick actions -- not for a
 * 15-field form, which should be a full screen instead (section 38).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HqBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: ColumnScopeContent,
) {
    val c = LocalHqColors.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        // Surface raised, 28dp top radius, 20dp sides, drag handle, scrim fades in with the sheet.
        containerColor = c.surfaceRaised,
        shape = habitiq.app.ui.theme.HqShapes.sheetTop,
        scrimColor = c.scrim.copy(alpha = if (c.isDark) 0.64f else 0.42f),
        dragHandle = { HqSheetHandle() },
        modifier = modifier,
    ) {
        val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
        // Sheets are separate windows: they need their own keyboard inset and tap-outside-to-dismiss-keyboard.
        Column(
            Modifier.fillMaxWidth().imePadding()
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
                .padding(start = HqSpacing.xl, end = HqSpacing.xl, top = HqSpacing.sm, bottom = HqSpacing.xxl),
        ) {
            if (title != null) {
                Text(title, style = HqType.titleMedium2, color = c.textPrimary, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(HqSpacing.md))
            }
            content()
        }
    }
}

private typealias ColumnScopeContent = @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit

/**
 * Confirmation dialog for a destructive or critical decision only (design doc section 39). Do
 * not use for ordinary information that could be shown inline. [confirmVariant] should stay
 * [HqButtonVariant.Destructive] for destructive actions -- never the brand-primary fill.
 */
@Composable
fun HqConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmVariant: HqButtonVariant = HqButtonVariant.Destructive,
    dismissLabel: String = "Cancel",
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        HqDialogSurface(title, message, confirmLabel, onConfirm, onDismiss, confirmVariant, dismissLabel, animateIn = true)
    }
}

/**
 * The dialog card itself (28dp corners, Overlay shadow, 24dp padding, max 400dp wide): heading,
 * message, then a dismiss (Tertiary) and confirm button aligned to the end. At large font sizes the
 * buttons wrap onto their own lines instead of clipping. Internal so the component gallery can
 * render it without a window.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
internal fun HqDialogSurface(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmVariant: HqButtonVariant,
    dismissLabel: String,
    animateIn: Boolean,
) {
    val c = LocalHqColors.current
    val reduce = habitiq.app.ui.theme.hqReduceMotion()
    // Modal entrance: centred (not origin-aware), 96% -> 100% with a fade on the standard curve.
    val enter = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(if (animateIn) 0f else 1f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        enter.animateTo(1f, androidx.compose.animation.core.tween(habitiq.app.ui.theme.HqMotion.base, easing = habitiq.app.ui.theme.HqMotion.standard))
    }
    Column(
        Modifier
            .padding(horizontal = HqSpacing.xxl)
            .widthIn(max = 400.dp)
            .fillMaxWidth()
            .graphicsLayer {
                alpha = enter.value
                val s = if (reduce) 1f else 0.96f + 0.04f * enter.value
                scaleX = s; scaleY = s
            }
            .hqShadow(habitiq.app.ui.theme.HqShadow.Overlay, habitiq.app.ui.theme.HqShapes.dialog)
            .clip(habitiq.app.ui.theme.HqShapes.dialog)
            .background(c.surfaceRaised)
            .padding(start = HqSpacing.xxl, end = HqSpacing.lg, top = HqSpacing.xxl, bottom = HqSpacing.lg),
    ) {
        Text(title, style = HqType.titleSmall2, color = c.textPrimary, modifier = Modifier.padding(end = HqSpacing.sm).semantics { heading() })
        Spacer(Modifier.height(HqSpacing.sm))
        Text(message, style = HqType.bodyLarge, color = c.textSecondary, modifier = Modifier.padding(end = HqSpacing.sm))
        Spacer(Modifier.height(HqSpacing.xl))
        androidx.compose.foundation.layout.FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(HqSpacing.sm, androidx.compose.ui.Alignment.End),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(HqSpacing.xs),
        ) {
            HqButton(text = dismissLabel, onClick = onDismiss, variant = HqButtonVariant.Tertiary, fullWidth = false, size = HqButtonSize.Medium)
            HqButton(text = confirmLabel, onClick = onConfirm, variant = confirmVariant, fullWidth = false, size = HqButtonSize.Medium)
        }
    }
}

/** Drag handle shown on every sheet (and only on sheets): 36x4 pill in the control-off colour. */
@Composable
internal fun HqSheetHandle() {
    val c = LocalHqColors.current
    androidx.compose.foundation.layout.Box(
        Modifier.padding(top = HqSpacing.sm + 2.dp, bottom = HqSpacing.xs)
            .size(width = 36.dp, height = 4.dp)
            .clip(habitiq.app.ui.theme.HqShapes.pill)
            .background(c.borderControl.copy(alpha = 0.6f)),
    )
}

/**
 * Asked when someone leaves a form that has typed input (WORDS.md): keeping their work is the
 * safe, default-looking choice; discarding is the destructive one.
 */
@Composable
fun HqDiscardChangesDialog(onKeepEditing: () -> Unit, onDiscard: () -> Unit) {
    HqConfirmDialog(
        title = "Discard changes?",
        message = "What you typed won't be saved.",
        confirmLabel = "Discard",
        onConfirm = onDiscard,
        onDismiss = onKeepEditing,
        dismissLabel = "Keep editing",
    )
}
