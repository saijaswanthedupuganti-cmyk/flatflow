package habitiq.app.ui.components

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
import androidx.compose.ui.graphics.Color
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
        // Figma `.sheet`: white, 30dp top radius, 22dp side padding, scrim rgba(9,28,26,.42).
        containerColor = c.surfaceRaised,
        shape = RoundedCornerShape(topStart = HqRadius.sheet, topEnd = HqRadius.sheet),
        scrimColor = if (c.isDark) Color.Black.copy(alpha = 0.64f) else Color(0xFF091C1A).copy(alpha = 0.42f),
        modifier = modifier,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = HqSpacing.md)) {
            if (title != null) {
                Text(title, style = HqType.titleMedium2, color = c.textPrimary)
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
    val c = LocalHqColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surfaceRaised,
        shape = RoundedCornerShape(HqRadius.card),
        title = { Text(title, style = HqType.titleSmall2, color = c.textPrimary) },
        text = { Text(message, style = HqType.bodyLarge, color = c.textSecondary) },
        confirmButton = { HqButton(text = confirmLabel, onClick = onConfirm, variant = confirmVariant, fullWidth = false) },
        dismissButton = { HqTextButton(text = dismissLabel, onClick = onDismiss) },
        properties = DialogProperties(),
    )
}
