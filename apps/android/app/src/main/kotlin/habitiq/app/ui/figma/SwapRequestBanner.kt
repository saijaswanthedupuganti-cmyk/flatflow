package habitiq.app.ui.figma

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

@Composable
fun SwapRequestBanner(count: Int, onReview: () -> Unit) {
    val c = LocalHqColors.current
    HqCard(variant = HqCardVariant.Status, onClick = onReview, padding = HqSpacing.md) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.SwapHoriz, null, tint = c.actionPrimaryPressed, modifier = Modifier.size(HqIconSize.sm))
                Text(
                    "$count Pending Swap Request${if (count == 1) "" else "s"}",
                    color = c.actionPrimaryPressed,
                    style = HqType.labelLarge,
                )
            }
            Text(
                "Review",
                color = c.actionPrimaryPressed,
                style = HqType.labelLarge,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

@Composable
fun NextUpLabel(nextName: String) {
    val c = LocalHqColors.current
    Row {
        Text("Next up: ", color = c.textSecondary, style = HqType.labelSmall)
        Text(nextName, color = c.textPrimary, style = HqType.caption)
    }
}
