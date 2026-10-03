package habitiq.app.ui.figma

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.automirrored.rounded.PlaylistAddCheck
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

enum class TaskStructure { RECURRING, GROUP, TEMP }

private fun sp(v: Float) = TextUnit(v, TextUnitType.Sp)

/** Palette from the "Task Assignment Type Selection" reference (light). Dark uses theme tokens. */
private object TypeLook {
    val ink = Color(0xFF0F1F3D)
    val muted = Color(0xFF5E6B7E)
    val page = Color(0xFFF5F9FD)
    val border = Color(0xFFE3EAF2)
    val chevronBg = Color(0xFFEFF3F8)
}

private data class TypeTone(val accent: Color, val tile: Color, val example: Color, val exampleTile: Color, val card: Color, val border: Color)

private val Teal = TypeTone(Color(0xFF11A88F), Color(0xFFD9F5EF), Color(0xFFEFF8F6), Color(0xFFDDF1EC), Color.White, TypeLook.border)
private val Blue = TypeTone(Color(0xFF2F6BEF), Color(0xFFDCE8FC), Color(0xFFEFF4FD), Color(0xFFDDE7FA), Color.White, TypeLook.border)
private val Amber = TypeTone(Color(0xFFE88A1A), Color(0xFFFFEBD3), Color(0xFFFFF5E9), Color(0xFFFCE5C9), Color(0xFFFFFDF9), Color(0xFFF6EBDD))

/**
 * "How should this task work?", matching the reference: a sparkle header icon, three white choice
 * cards with a tinted example row each, and a quiet Cancel. [exampleNames] personalises the examples
 * with real flatmates (the person first); it falls back to Sai and Rahul.
 */
@Composable
fun CreateTaskTypeScreen(onBack: () -> Unit, onSelect: (TaskStructure) -> Unit, exampleNames: List<String> = emptyList()) {
    val c = LocalHqColors.current
    val dark = c.isDark
    val ink = if (dark) c.textPrimary else TypeLook.ink
    val muted = if (dark) c.textSecondary else TypeLook.muted
    val me = exampleNames.getOrNull(0)?.substringBefore(' ')?.ifBlank { null } ?: "Sai"
    val other = exampleNames.getOrNull(1)?.substringBefore(' ')?.ifBlank { null } ?: "Rahul"

    Box(Modifier.fillMaxSize().background(if (dark) c.canvas else TypeLook.page)) {
        // Soft colour washes in the corners, as in the reference.
        if (!dark) {
            Box(Modifier.offset((-110).dp, 10.dp).size(300.dp).background(androidx.compose.ui.graphics.Brush.radialGradient(listOf(Color(0xFFDCEAFB), Color(0xFFDCEAFB).copy(alpha = 0f)))))
            Box(Modifier.align(Alignment.BottomEnd).offset(110.dp, 60.dp).size(320.dp).background(androidx.compose.ui.graphics.Brush.radialGradient(listOf(Color(0xFFD9F3EC), Color(0xFFD9F3EC).copy(alpha = 0f)))))
        }
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Box(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp)) {
                Box(
                    Modifier.size(48.dp)
                        .shadow(10.dp, CircleShape, ambientColor = Color(0xFF1B3A6B).copy(alpha = .12f), spotColor = Color(0xFF1B3A6B).copy(alpha = .12f))
                        .clip(CircleShape).background(if (dark) c.surfaceRaised else Color.White)
                        .clickable(role = Role.Button, onClickLabel = "Back", onClick = onBack)
                        .semantics { contentDescription = "Back" },
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = ink, modifier = Modifier.size(22.dp)) }
                Box(
                    Modifier.align(Alignment.TopCenter).padding(top = 2.dp).size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape).background(if (dark) c.borderSubtle else Color(0xFFD5DCE5)),
                )
            }

            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SparkleIcon()
                Text(
                    "How should this task work?",
                    style = HqType.headlineMedium.copy(fontSize = sp(26f), lineHeight = sp(32f), letterSpacing = sp(-0.3f)),
                    color = ink, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 14.dp),
                )
                Text(
                    "Choose how this task should be assigned.\nWe'll handle the schedule and reminders.",
                    style = HqType.bodyLarge.copy(lineHeight = sp(24f)), color = muted, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp, bottom = 22.dp),
                )

                TypeCard(
                    tone = Teal, icon = Icons.Rounded.Sync, title = "Recurring Duty", subtitle = "Takes turns automatically",
                    bestFor = "garbage, dishes, cleaning", exampleIcon = Icons.Rounded.CalendarMonth,
                    example = "Garbage → $me this week · $other next week",
                    ink = ink, muted = muted, onClick = { onSelect(TaskStructure.RECURRING) },
                )
                Spacer(Modifier.height(14.dp))
                TypeCard(
                    tone = Blue, icon = Icons.Rounded.Groups, title = "Group Task", subtitle = "Split the work between people",
                    bestFor = "shared cleaning, moving, big jobs", exampleIcon = Icons.AutoMirrored.Rounded.List,
                    example = "Sunday cleaning → $me: kitchen · $other: bedroom",
                    ink = ink, muted = muted, onClick = { onSelect(TaskStructure.GROUP) },
                )
                Spacer(Modifier.height(14.dp))
                TypeCard(
                    tone = Amber, icon = Icons.Rounded.Bolt, title = "One-Time Task", subtitle = "Assign it once and get it done",
                    bestFor = "groceries, repairs, quick errands", exampleIcon = Icons.Rounded.CheckCircle,
                    example = "Buy vegetables → $me · Today",
                    ink = ink, muted = muted, onClick = { onSelect(TaskStructure.TEMP) },
                )

                Row(Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Info, null, tint = muted, modifier = Modifier.size(18.dp))
                    Text("Choose one to continue", style = HqType.bodyMedium, color = muted, modifier = Modifier.padding(start = 8.dp))
                }
                Text(
                    "Cancel",
                    style = HqType.bodyLarge.copy(fontSize = sp(17f)), color = muted, fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 18.dp, bottom = 24.dp).clip(RoundedCornerShape(12.dp))
                        .clickable(role = Role.Button, onClick = onBack).padding(horizontal = 28.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun SparkleIcon() {
    val c = LocalHqColors.current
    Box(Modifier.padding(top = 6.dp).size(width = 120.dp, height = 84.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val ray = Color(0xFF8DB6F2)
            val cx = size.width / 2
            val cy = size.height / 2
            // Short dashes fanning out left and right of the tile.
            listOf(-1f, 1f).forEach { side ->
                listOf(-0.38f, 0f, 0.38f).forEach { slope ->
                    val start = Offset(cx + side * 38.dp.toPx(), cy + slope * 34.dp.toPx())
                    val end = Offset(cx + side * 48.dp.toPx(), cy + slope * 44.dp.toPx())
                    drawLine(ray, start, end, strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                }
            }
        }
        Box(
            Modifier.size(54.dp).clip(RoundedCornerShape(16.dp)).background(if (c.isDark) Blue.tile.copy(alpha = .18f) else Blue.tile),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.AutoMirrored.Rounded.PlaylistAddCheck, null, tint = Blue.accent, modifier = Modifier.size(30.dp)) }
    }
}

@Composable
private fun TypeCard(
    tone: TypeTone, icon: ImageVector, title: String, subtitle: String, bestFor: String,
    exampleIcon: ImageVector, example: String, ink: Color, muted: Color, onClick: () -> Unit,
) {
    val c = LocalHqColors.current
    val dark = c.isDark
    val shape = RoundedCornerShape(24.dp)
    Column(
        Modifier.fillMaxWidth()
            .shadow(8.dp, shape, ambientColor = Color(0xFF1B3A6B).copy(alpha = .06f), spotColor = Color(0xFF1B3A6B).copy(alpha = .06f))
            .clip(shape).background(if (dark) c.surfaceRaised else tone.card)
            .border(1.dp, if (dark) c.borderSubtle else tone.border, shape)
            .clickable(role = Role.Button, onClickLabel = "Choose $title", onClick = onClick)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(17.dp)).background(if (dark) tone.accent.copy(alpha = .16f) else tone.tile),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, null, tint = tone.accent, modifier = Modifier.size(28.dp)) }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, style = HqType.titleSmall2.copy(fontSize = sp(18f)), color = ink)
                Text(subtitle, style = HqType.bodyLarge.copy(fontSize = sp(14.5f), lineHeight = sp(20f)), color = muted, modifier = Modifier.padding(top = 2.dp))
                Text(
                    buildAnnotatedString {
                        append("Best for: ")
                        withStyle(SpanStyle(color = tone.accent)) { append(bestFor) }
                    },
                    style = HqType.bodyMedium.copy(fontSize = sp(13f), lineHeight = sp(18f)), color = muted, modifier = Modifier.padding(top = 5.dp),
                )
            }
            Box(
                Modifier.size(30.dp).clip(CircleShape).background(if (dark) c.surfaceSubtle else TypeLook.chevronBg),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = ink, modifier = Modifier.size(20.dp)) }
        }
        Row(
            Modifier.padding(top = 12.dp).fillMaxWidth().heightIn(min = 56.dp).clip(RoundedCornerShape(16.dp))
                .background(if (dark) c.surfaceSubtle else tone.example).padding(horizontal = 10.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(if (dark) tone.accent.copy(alpha = .14f) else tone.exampleTile),
                contentAlignment = Alignment.Center,
            ) { Icon(exampleIcon, null, tint = tone.accent.copy(alpha = .85f), modifier = Modifier.size(20.dp)) }
            Column(Modifier.padding(start = 12.dp)) {
                Text("Example", style = HqType.labelSmall.copy(fontSize = sp(12f)), color = muted)
                Text(example, style = HqType.bodyMedium.copy(fontSize = sp(13.5f), lineHeight = sp(18f)), color = ink, modifier = Modifier.padding(top = 1.dp))
            }
        }
    }
}
