package habitiq.app.ui.discover

import androidx.compose.foundation.layout.Row
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import habitiq.app.ui.components.HqIllustration
import habitiq.app.ui.components.HqArt
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
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
    // Listing-shaped skeletons (photo + two text lines) so the page keeps its shape while loading.
    Column(
        Modifier.fillMaxWidth().padding(horizontal = HqSpacing.lg, vertical = HqSpacing.sm)
            .semantics { contentDescription = label },
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        repeat(2) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                habitiq.app.ui.components.HqSkeletonBlock(Modifier.fillMaxWidth(), height = 178.dp)
                habitiq.app.ui.components.HqSkeletonBlock(Modifier.fillMaxWidth(0.7f), height = 18.dp)
                habitiq.app.ui.components.HqSkeletonBlock(Modifier.fillMaxWidth(0.45f), height = 14.dp)
            }
        }
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
        HqIllustration(if (person) HqArt.IntentFlatmate else HqArt.SearchHouse, Modifier.width(96.dp))
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
        Text(title, style = HqType.titleMedium, color = c.statusDangerFg)
        Text(body, style = HqType.bodySmall, color = c.textSecondary)
        if (onRetry != null) {
            HqButton(text = "Retry", onClick = onRetry, variant = HqButtonVariant.Secondary, fullWidth = false)
        }
    }
}

/** Count, a way to clear everything, and each applied filter as a removable chip, wrapped onto as many lines as needed. */
@Composable
fun AppliedFilters(chips: List<Pair<String, () -> Unit>>, onClearAll: () -> Unit) {
    if (chips.isEmpty()) return
    val c = LocalHqColors.current
    Column(Modifier.padding(horizontal = HqSpacing.screenHorizontal)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${chips.size} applied", style = HqType.labelSmall, color = c.textSecondary, modifier = Modifier.weight(1f))
            HqTextButton(text = "Clear all", onClick = onClearAll)
        }
        habitiq.app.ui.components.HqChipFlow { chips.forEach { (label, remove) -> habitiq.app.ui.components.HqRemovableChip(label, remove) } }
    }
}
