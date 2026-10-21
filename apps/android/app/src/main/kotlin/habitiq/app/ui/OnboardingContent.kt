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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.defaultMinSize
import habitiq.app.ui.theme.HqSize
import androidx.compose.ui.unit.dp
import habitiq.app.ui.components.HqArt
import habitiq.app.ui.components.HqIllustration
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/** WORDS.md: the one sentence that says what Oddroof is. Shown on Welcome, Create account and the first choice. */
const val OddroofValueSentence = "Oddroof keeps your flat in order. Split expenses, share chores and bills, and find a flat or a flatmate."

const val StartChoicesTitle = "What brings you here?"
const val StartChoicesSubtitle = "You can do the rest anytime."

/**
 * Three situations instead of four features (user test 18 Oct: four options read as a menu, not a question).
 * "I live in a shared flat" opens its two answers in place: Join (most people are invited) and Create.
 * Used by the first-choice screen and by Home when the person has no flat, so both read exactly the same.
 * [onFindPerson] stays for callers; browsing people now lives in Discover.
 */
@Composable
fun IntentChoices(
    onFindFlat: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onFindPerson: () -> Unit,
    onCreateFlat: () -> Unit,
    onJoinFlat: () -> Unit,
    modifier: Modifier = Modifier,
    onListRoom: () -> Unit = onCreateFlat,
) {
    var livesInFlat by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(HqSpacing.related)) {
        IntentRow(HqIcons.Home, HqTileTone.Teal, "I live in a shared flat", "Split expenses and share tasks with your flatmates", { livesInFlat = !livesInFlat }, expanded = livesInFlat)
        if (livesInFlat) {
            Column(Modifier.padding(start = HqSpacing.xxl), verticalArrangement = Arrangement.spacedBy(HqSpacing.related)) {
                IntentRow(HqIcons.Users, HqTileTone.Sand, "Join with an invite code", "Your flatmate already set up the flat", onJoinFlat)
                IntentRow(HqIcons.Plus, HqTileTone.Teal, "Create a new flat", "You're the first one here; invite the rest", onCreateFlat)
            }
        }
        IntentRow(HqIcons.Search, HqTileTone.Coral, "I'm looking for a room", "Browse flats near work or college", onFindFlat)
        IntentRow(HqIcons.Discover, HqTileTone.Neutral, "I have a room to fill", "Set up your flat, then list the room", onListRoom)
    }
}

/** Title, subtitle and the three choices as one block, so every place that offers them uses one copy set. */
@Composable
fun StartChooser(
    onFindFlat: () -> Unit,
    onFindPerson: () -> Unit,
    onCreateFlat: () -> Unit,
    onJoinFlat: () -> Unit,
    modifier: Modifier = Modifier,
    onListRoom: () -> Unit = onCreateFlat,
) {
    val c = LocalHqColors.current
    Column(modifier.fillMaxWidth()) {
        Text(StartChoicesTitle, style = HqType.headlineMedium, color = c.textPrimary, modifier = Modifier.semantics { heading() })
        Text(
            StartChoicesSubtitle, style = HqType.bodyMedium, color = c.textSecondary,
            modifier = Modifier.padding(top = HqSpacing.sm, bottom = HqSpacing.xl),
        )
        IntentChoices(onFindFlat = onFindFlat, onFindPerson = onFindPerson, onCreateFlat = onCreateFlat, onJoinFlat = onJoinFlat, onListRoom = onListRoom)
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
private fun IntentRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector, tone: HqTileTone, title: String, support: String,
    onClick: () -> Unit, expanded: Boolean? = null,
) {
    val c = LocalHqColors.current
    Row(
        Modifier.fillMaxWidth().defaultMinSize(minHeight = HqSize.target).then(surfaceModifier(onClick))
            .then(if (expanded != null) Modifier.semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" } else Modifier)
            .padding(horizontal = HqSpacing.component, vertical = HqSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.md),
    ) {
        HqIconTile(icon, tone, size = 44)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
            Text(title, style = HqType.rowTitle, color = c.textPrimary, fontWeight = FontWeight.SemiBold)
            Text(support, style = HqType.bodyMedium, color = c.textSecondary)
        }
        Icon(
            HqIcons.Arrow, null, tint = c.iconDefault,
            modifier = Modifier.size(HqIconSize.md).graphicsLayer { rotationZ = if (expanded == true) 90f else 0f },
        )
    }
}
