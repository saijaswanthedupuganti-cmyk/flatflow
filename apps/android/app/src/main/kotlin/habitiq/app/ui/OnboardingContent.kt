package habitiq.app.ui

import androidx.compose.ui.text.font.FontWeight
import habitiq.app.ui.components.HqIconTile
import habitiq.app.ui.components.HqTileTone
import habitiq.app.ui.components.HqIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import habitiq.app.ui.components.HqArt
import habitiq.app.ui.components.HqIllustration
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * The four starting options as Figma's numbered rows: number, tinted icon tile, title and support, arrow.
 * Same four callbacks and routing as before; only the presentation and wording follow the Figma file.
 */
@Composable
fun IntentChoices(
    onFindFlat: () -> Unit,
    onFindPerson: () -> Unit,
    onCreateFlat: () -> Unit,
    onJoinFlat: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(HqSpacing.related)) {
        IntentRow("01", HqIcons.Search, HqTileTone.Coral, "I'm looking for a flat", "Find a room near work, college, or an area", onFindFlat)
        IntentRow("02", HqIcons.Users, HqTileTone.Teal, "I'm looking for a flatmate", "Find people whose way of living fits yours", onFindPerson)
        IntentRow("03", HqIcons.Manage, HqTileTone.Sand, "I want to manage my flat", "Organise tasks, expenses, bills, and members", onCreateFlat)
        IntentRow("04", HqIcons.Home, HqTileTone.Neutral, "I'm joining an existing flat", "Use an invite code from your flat admin", onJoinFlat)
        Row(Modifier.padding(top = HqSpacing.sm), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(HqIcons.Shield, null, tint = LocalHqColors.current.textMuted, modifier = Modifier.size(15.dp))
            Text("Your choice only personalises setup. It does not limit your account.", style = HqType.labelSmall, color = LocalHqColors.current.textMuted)
        }
    }
}

@Composable
private fun surfaceModifier(onClick: () -> Unit): Modifier {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(HqRadius.card)
    return Modifier.clip(shape).background(c.surfaceBase).border(1.dp, c.borderSubtle, shape)
        .clickable(role = Role.Button, onClick = onClick)
}

/** The whole tile is one target; the chevron is only a cue. No weights, so equal heights measure exactly. */
@Composable
internal fun IntentTile(title: String, support: String, art: HqArt, onClick: () -> Unit, modifier: Modifier) {
    val c = LocalHqColors.current
    Column(
        modifier.then(surfaceModifier(onClick)).fillMaxHeight().padding(HqSpacing.component),
        verticalArrangement = Arrangement.spacedBy(HqSpacing.xs),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            HqIllustration(art, Modifier.height(48.dp))
            androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, null, tint = c.iconDefault, modifier = Modifier.size(HqIconSize.md))
        }
        Text(title, style = HqType.titleSmall2, color = c.textPrimary, modifier = Modifier.padding(top = HqSpacing.xs))
        Text(support, style = HqType.bodyMedium, color = c.textSecondary)
    }
}

@Composable
private fun IntentRow(number: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tone: HqTileTone, title: String, support: String, onClick: () -> Unit) {
    val c = LocalHqColors.current
    Row(
        Modifier.fillMaxWidth().then(surfaceModifier(onClick)).padding(horizontal = HqSpacing.component, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(number, style = HqType.labelSmall, color = c.textMuted, fontWeight = FontWeight.ExtraBold)
        HqIconTile(icon, tone, size = 44)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = HqType.rowTitle, color = c.textPrimary)
            Text(support, style = HqType.bodyMedium, color = c.textSecondary)
        }
        Icon(HqIcons.Arrow, null, tint = c.iconDefault, modifier = Modifier.size(HqIconSize.md))
    }
}
