package habitiq.app.ui.discover

import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.components.HqTextButton
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import habitiq.app.discover.TrustPresentation
import habitiq.app.discover.TrustTier
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

@Composable
fun TrustBadge(trust: TrustPresentation, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    var showWhy by remember { mutableStateOf(false) }
    // Every tier reads neutral: a trust tag is information, not an endorsement (design doc 5.4).
    val tint = when (trust.tier) {
        TrustTier.HABITIQ_MEMBER, TrustTier.UNRATED, TrustTier.NEW_TO_HABITIQ -> c.textSecondary
    }
    Row(
        modifier.defaultMinSize(minHeight = HqSize.target).clickable(role = Role.Button) { showWhy = true }.padding(vertical = HqSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs)
    ) {
        Icon(Icons.Default.Info, null, tint = tint, modifier = Modifier.size(HqIconSize.xs))
        Text(trust.label, style = HqType.labelSmall, color = tint)
        Text("Why?", style = HqType.labelSmall, color = c.textBrand)
    }
    // No Hq dialog fits a single-button informational dialog (HqConfirmDialog always renders a
    // confirm + dismiss pair for a destructive/critical decision) -- kept as a themed AlertDialog.
    if (showWhy) {
        AlertDialog(
            onDismissRequest = { showWhy = false },
            containerColor = c.surfaceRaised,
            title = { Text(trust.label, style = HqType.titleSmall2, color = c.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    Text(trust.explanation, style = HqType.bodyLarge, color = c.textPrimary)
                    Text(
                        "This is not a score. A report does not automatically change this tag.",
                        style = HqType.bodyMedium,
                        color = c.textSecondary
                    )
                }
            },
            confirmButton = {
                HqTextButton(text = "Close", onClick = { showWhy = false })
            }
        )
    }
}

@Composable
fun CompatibilityBlock(title: String, signals: List<String>) {
    if (signals.isEmpty()) return
    val c = LocalHqColors.current
    Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
        Text(title, style = HqType.labelMedium, color = c.textPrimary)
        // Shared preferences are information, not filters, so they are read-only tags that wrap.
        habitiq.app.ui.components.HqTagFlow(signals)
    }
}
