package habitiq.app.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqInlineLoading
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

@Composable
fun DiscoveryLoading(label: String = "Loading…") {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        HqInlineLoading(label = label)
    }
}

/**
 * [HqEmptyState] requires a primary action, but callers here (see UseAFlatDiscover.kt,
 * FindFlatmateDiscover.kt, MyPostsScreen.kt) legitimately pass no action at all -- so this stays
 * a bespoke layout built from tokens rather than swapping to that component, and uses
 * [HqTextButton] for the action when one is supplied.
 */
@Composable
fun DiscoveryEmpty(
    title: String,
    body: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    person: Boolean = false
) {
    val c = LocalHqColors.current
    Column(
        Modifier.fillMaxWidth().padding(HqSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HqSpacing.md)
    ) {
        Box(
            Modifier.size(HqSpacing.xxhuge).clip(RoundedCornerShape(HqRadius.xl)).background(c.brandPrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (person) Icons.Default.PersonSearch else Icons.Default.HomeWork,
                null,
                tint = c.brandPrimary,
                modifier = Modifier.size(HqIconSize.lg)
            )
        }
        Text(title, style = HqType.titleLarge, color = c.textPrimary)
        Text(body, style = HqType.bodyMedium, color = c.textSecondary)
        if (actionLabel != null && onAction != null) {
            HqTextButton(text = actionLabel, onClick = onAction)
        }
    }
}

/** Same nullable-action gap as [DiscoveryEmpty] -- see the note there; [HqErrorState] needs a non-null retry. */
@Composable
fun DiscoveryError(title: String, body: String, onRetry: (() -> Unit)? = null) {
    val c = LocalHqColors.current
    Column(
        Modifier.fillMaxWidth().padding(HqSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)
    ) {
        Text(title, style = HqType.titleMedium, color = c.error)
        Text(body, style = HqType.bodySmall, color = c.textSecondary)
        if (onRetry != null) {
            HqButton(text = "Retry", onClick = onRetry, variant = HqButtonVariant.Secondary, fullWidth = false)
        }
    }
}
