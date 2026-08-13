package habitiq.app.ui.figma

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.ui.theme.FigmaColors

enum class TaskStructure { RECURRING, GROUP, TEMP }

@Composable
fun CreateTaskTypeScreen(onBack: () -> Unit, onSelect: (TaskStructure) -> Unit) {
    FigmaScreenBackground {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Text("What kind of task is this?", color = FigmaColors.Ink, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "Select a structure for your new home task. This helps us automate the reminders and tracking for everyone.",
                color = FigmaColors.InkSecondary,
                fontSize = 15.sp,
                lineHeight = 22.5.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            TaskTypeCard(
                title = "Recurring Duty",
                description = "Rotates between members — daily, weekly, or monthly. Skips people who are out of station automatically.",
                example = "e.g. Garbage duty → Sai this week, Rahul next week",
                accent = Color(0xFF2563EB),
                accentBg = Color(0xFFDBEAFE),
                icon = Icons.Filled.Repeat,
                onClick = { onSelect(TaskStructure.RECURRING) }
            )
            Spacer(Modifier.height(12.dp))
            TaskTypeCard(
                title = "Group Task",
                description = "Split one job across people — each gets their own part. Can be one-time or repeat every week/month.",
                example = "e.g. Sunday Cleaning → Sai: kitchen · Rahul: bedroom",
                accent = Color(0xFF0D9488),
                accentBg = Color(0xFFCCFBF1),
                icon = Icons.Filled.Groups,
                onClick = { onSelect(TaskStructure.GROUP) }
            )
            Spacer(Modifier.height(12.dp))
            TaskTypeCard(
                title = "Temp Task",
                description = "Assign one quick job to one person. When they mark it done, it's closed — no rotation, no repeat.",
                example = "e.g. Buy vegetables today → Sai → done ✓",
                accent = Color(0xFFD97706),
                accentBg = Color(0xFFFEF3C7),
                icon = Icons.Filled.Bolt,
                onClick = { onSelect(TaskStructure.TEMP) }
            )
            Spacer(Modifier.height(24.dp))
        }
        Box(Modifier.fillMaxWidth().padding(20.dp).navigationBarsPadding()) {
            FigmaPrimaryButton(text = "Cancel", onClick = onBack)
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
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FigmaColors.Surface)
            .border(1.dp, FigmaColors.SurfaceBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(accentBg), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = FigmaColors.Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(description, color = FigmaColors.InkSecondary, fontSize = 12.sp, lineHeight = 18.sp)
            Text(example, color = accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
