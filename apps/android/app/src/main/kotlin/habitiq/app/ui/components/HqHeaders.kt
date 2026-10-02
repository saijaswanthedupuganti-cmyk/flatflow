package habitiq.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Figma `Header`: optional back button, a large DM Sans title, a muted subtitle and a trailing action.
 * Screens place this in the body instead of a top bar.
 */
@Composable
fun HqPageHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    /** When true (default) a compact pinned bar with this title appears once the header scrolls away. */
    pinOnScroll: Boolean = true,
    action: @Composable RowScope.() -> Unit = {},
) {
    val c = LocalHqColors.current
    val host = LocalHqTopBar.current
    val entry = if (pinOnScroll) rememberRegisteredTopBarEntry() else null
    if (entry != null) {
        entry.title = title
        entry.onBack = onBack
        entry.actions = action
    }
    val threshold = headerPinThresholdPx()
    Row(
        modifier.fillMaxWidth().hqTrackHeader(entry, host, threshold).padding(bottom = HqSpacing.xxl),
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(HqSize.target).offset(x = (-7).dp)) {
                Icon(HqIcons.Back, contentDescription = "Back", tint = c.iconDefault, modifier = Modifier.size(HqIconSize.md))
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = HqType.titleLarge2, color = c.textPrimary)
            if (subtitle != null) {
                Text(subtitle, style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(top = 6.dp))
            }
        }
        action()
    }
}

/** Figma `SectionTitle`: DM Sans heading with an optional text action on the right. */
@Composable
fun HqSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val c = LocalHqColors.current
    Row(
        modifier.fillMaxWidth().padding(top = HqSpacing.section - 2.dp, bottom = HqSpacing.md + 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = HqType.titleSmall2, color = c.textPrimary, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) HqTextButton(action, onAction)
    }
}
