package habitiq.app.ui.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import habitiq.app.ui.components.HqBottomSheet
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqConfirmDialog
import habitiq.app.ui.components.HqInlineError
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

val ReportReasons = listOf(
    "Fake listing",
    "Harassment",
    "Scam / payment request",
    "Fake identity",
    "Inappropriate content",
    "Other"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionRequestSheet(
    toName: String,
    contextLine: String,
    sending: Boolean,
    error: String?,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val c = LocalHqColors.current
    var message by remember {
        mutableStateOf("Hi $toName, this looks like a good fit. Are you still looking?")
    }
    HqBottomSheet(onDismiss = onDismiss, title = "Send connection request") {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(72.dp).clip(CircleShape).background(c.infoContainer),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.AutoMirrored.Filled.Send, null, tint = c.info) }
        }
        Spacer(Modifier.height(HqSpacing.md))
        Text("To: $toName", style = HqType.bodyMedium, color = c.textSecondary)
        Text(contextLine, style = HqType.bodySmall, color = c.textTertiary)
        Spacer(Modifier.height(HqSpacing.sm))
        Text(
            "Phone numbers stay hidden until you both accept. Chat stays in Habitiq.",
            style = HqType.bodySmall,
            color = c.textSecondary
        )
        Spacer(Modifier.height(HqSpacing.md))
        HqTextField(
            value = message,
            onValueChange = { message = it.take(400) },
            label = "Why are you interested?",
            leadingIcon = Icons.AutoMirrored.Filled.Send
        )
        if (error != null) {
            Spacer(Modifier.height(HqSpacing.sm))
            HqInlineError(error)
        }
        Spacer(Modifier.height(HqSpacing.lg))
        HqButton(
            text = if (sending) "Sending…" else "Send request",
            onClick = { onSend(message.trim()) },
            enabled = !sending && message.isNotBlank(),
            loading = sending
        )
    }
}

/**
 * Kept as a themed dialog rather than [HqConfirmDialog] -- that component's `message` slot is a
 * plain String and can't host the selectable reason chips this needs. Submit uses the Destructive
 * variant per the button-color rule for Report actions.
 */
@Composable
fun ReportSheet(
    title: String,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val c = LocalHqColors.current
    var selected by remember { mutableStateOf(ReportReasons.first()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surface,
        title = { Text(title, style = HqType.titleLarge, color = c.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                Text(
                    "A report is reviewed. It does not automatically change anyone’s trust tag.",
                    style = HqType.bodySmall,
                    color = c.textSecondary
                )
                ReportReasons.forEach { reason ->
                    HqChip(label = reason, selected = selected == reason, onClick = { selected = reason })
                }
            }
        },
        confirmButton = {
            HqButton(
                text = "Submit report",
                onClick = { onSubmit(selected); onDismiss() },
                variant = HqButtonVariant.Destructive,
                fullWidth = false
            )
        },
        dismissButton = { HqTextButton(text = "Cancel", onClick = onDismiss) }
    )
}

@Composable
fun BlockConfirmation(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    HqConfirmDialog(
        title = "Block $name?",
        message = "You won’t be able to message or connect with this person.",
        confirmLabel = "Block",
        onConfirm = { onConfirm(); onDismiss() },
        onDismiss = onDismiss,
        confirmVariant = HqButtonVariant.Destructive
    )
}

@Composable
fun TrustConsentDialog(
    onAllow: () -> Unit,
    onNotNow: () -> Unit
) {
    HqConfirmDialog(
        title = "How Habitiq uses your activity",
        message = "We can use chore completion, expense settlement and dispute history to show a qualitative Habitiq trust tag — never a percentage. You can stay Unrated.",
        confirmLabel = "Allow",
        onConfirm = onAllow,
        onDismiss = onNotNow,
        confirmVariant = HqButtonVariant.Primary,
        dismissLabel = "Not now"
    )
}
