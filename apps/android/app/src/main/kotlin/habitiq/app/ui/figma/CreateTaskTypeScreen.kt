package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

enum class TaskStructure { RECURRING, GROUP, TEMP }

@Composable
fun CreateTaskTypeScreen(onBack: () -> Unit, onSelect: (TaskStructure) -> Unit) {
    val c = LocalHqColors.current
    FigmaScreenBackground {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(HqSpacing.xxl))
            Text("What kind of task is this?", color = c.textPrimary, style = HqType.headlineMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(HqSpacing.sm))
            Text(
                "Select a structure for your new home task. This helps us automate the reminders and tracking for everyone.",
                color = c.textSecondary,
                style = HqType.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(HqSpacing.xxxl))
            // No categorical/qualitative token exists in the design system for these three
            // decorative accent colors -- mapped to the closest semantic tokens (info, brand,
            // warning) rather than left as raw hex. See migration report.
            TaskTypeCard(
                title = "Recurring Duty",
                description = "Rotates between members — daily, weekly, or monthly. Skips people who are out of station automatically.",
                example = "e.g. Garbage duty → Sai this week, Rahul next week",
                accent = c.brandPrimary,
                accentBg = c.brandPrimaryContainer,
                icon = Icons.Filled.Repeat,
                onClick = { onSelect(TaskStructure.RECURRING) }
            )
            Spacer(Modifier.height(HqSpacing.md))
            TaskTypeCard(
                title = "Group Task",
                description = "Split one job across people — each gets their own part. Can be one-time or repeat every week/month.",
                example = "e.g. Sunday Cleaning → Sai: kitchen · Rahul: bedroom",
                accent = c.info,
                accentBg = c.infoContainer,
                icon = Icons.Filled.Groups,
                onClick = { onSelect(TaskStructure.GROUP) }
            )
            Spacer(Modifier.height(HqSpacing.md))
            TaskTypeCard(
                title = "Temp Task",
                description = "Assign one quick job to one person. When they mark it done, it's closed — no rotation, no repeat.",
                example = "e.g. Buy vegetables today → Sai → done ✓",
                accent = c.warning,
                accentBg = c.warningContainer,
                icon = Icons.Filled.Bolt,
                onClick = { onSelect(TaskStructure.TEMP) }
            )
            Spacer(Modifier.height(HqSpacing.xxl))
        }
        Box(Modifier.fillMaxWidth().padding(HqSpacing.xl).navigationBarsPadding()) {
            HqButton(text = "Cancel", onClick = onBack)
        }
    }
}

@Composable
private fun TaskTypeCard(
    title: String,
    description: String,
    example: String,
    accent: Color,
    accentBg: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    HqCard(variant = HqCardVariant.Interactive, onClick = onClick) {
        Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.lg)) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(HqRadius.md)).background(accentBg), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(HqIconSize.md))
            }
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                Text(title, style = HqType.titleSmall, color = LocalHqColors.current.textPrimary)
                Text(description, style = HqType.bodySmall, color = LocalHqColors.current.textSecondary)
                Text(example, style = HqType.labelSmall, color = accent)
            }
        }
    }
}
