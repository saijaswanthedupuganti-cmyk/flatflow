package habitiq.app.ui.agent

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import habitiq.app.agent.AgentCard
import habitiq.app.agent.AnswerLine
import habitiq.app.lib.formatInr
import habitiq.app.ui.components.HqIconTile
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqTileTone
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import kotlin.math.roundToLong

/**
 * Three soft teal→coral rings around a mic. They breathe slowly and swell with [level] (0..1).
 * Uses transform-only drawing. With [animate] false it's a still glow.
 */
@Composable
fun VoiceAura(level: Float, animate: Boolean, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    val voice by animateFloatAsState(if (animate) level else 0f, spring(dampingRatio = 0.6f, stiffness = 280f), label = "voice")
    val breath = if (animate) {
        rememberInfiniteTransition(label = "aura").animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "breath",
        ).value
    } else 0f
    Box(modifier.size(148.dp).semantics { contentDescription = "Listening" }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(148.dp)) {
            val base = size.minDimension / 2
            listOf(1f to .14f, .78f to .22f, .58f to .34f).forEachIndexed { i, (scale, alpha) ->
                val swell = 1f + (0.05f * breath + 0.20f * voice) * (3 - i) / 3f
                val r = base * scale * swell.coerceAtMost(1f / scale)
                drawCircle(
                    Brush.radialGradient(
                        listOf(c.brandTeal.copy(alpha = alpha), c.brandCoral.copy(alpha = alpha * .55f), Color.Transparent),
                        center = center, radius = r,
                    ),
                    radius = r,
                )
            }
        }
        Box(
            Modifier.size(60.dp).graphicsLayer { val s = 1f + 0.06f * voice; scaleX = s; scaleY = s }
                .shadow(12.dp, CircleShape, ambientColor = c.brandTeal.copy(alpha = .4f), spotColor = c.brandTeal.copy(alpha = .4f))
                .clip(CircleShape).background(c.actionPrimaryBg),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Rounded.Mic, null, tint = c.actionPrimaryFg, modifier = Modifier.size(28.dp)) }
    }
}

/** Cards rise and settle in once. Returns a 0..1 progress; it's 1 immediately when [animate] is false. */
@Composable
private fun rememberEntrance(animate: Boolean): Float {
    val p = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) { if (animate) p.animateTo(1f, spring(dampingRatio = 0.72f, stiffness = 380f)) }
    return p.value
}

private fun Modifier.entrance(p: Float) = graphicsLayer {
    alpha = p.coerceIn(0f, 1f)
    translationY = (1f - p) * 24.dp.toPx()
    val s = 0.96f + 0.04f * p
    scaleX = s; scaleY = s
}

@Composable
private fun CountUpAmount(paise: Long, animate: Boolean) {
    val c = LocalHqColors.current
    val p = remember(paise) { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(paise) { if (animate) p.animateTo(1f, tween(650, easing = FastOutSlowInEasing)) }
    Text(
        formatInr((paise * p.value).roundToLong() / 100.0),
        style = HqType.titleLarge2, color = c.textPrimary,
        modifier = Modifier.semantics { contentDescription = formatInr(paise / 100.0) },
    )
}

private fun categoryLook(category: String): Pair<ImageVector, HqTileTone> = when (category) {
    "lifestyle" -> HqIcons.Receipt to HqTileTone.Sand
    "bills" -> HqIcons.Clock to HqTileTone.Coral
    "chores" -> HqIcons.Check to HqTileTone.Teal
    else -> HqIcons.Receipt to HqTileTone.Neutral
}

@Composable
private fun CardFrame(p: Float, content: @Composable () -> Unit) {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier.entrance(p).fillMaxWidth()
            .shadow(10.dp, shape, ambientColor = Color.Black.copy(alpha = .10f), spotColor = Color.Black.copy(alpha = .10f))
            .clip(shape).background(c.surfaceRaised).border(1.dp, c.borderSubtle, shape)
            .padding(18.dp),
    ) { content() }
}

@Composable
private fun Initial(name: String, ring: Color, size: Int = 30) {
    val c = LocalHqColors.current
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(c.selectedBg).border(2.dp, ring, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(name.take(1).uppercase(), style = HqType.labelSmall, color = c.selectedFg, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AvatarStack(names: List<String>) {
    val c = LocalHqColors.current
    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
        names.take(5).forEach { Initial(it, ring = c.surfaceRaised) }
        if (names.size > 5) Initial("+${names.size - 5}", ring = c.surfaceRaised)
    }
}

/** Receipt-style card for an expense the agent understood. */
@Composable
fun ExpenseCard(card: AgentCard.Expense, animate: Boolean) {
    val c = LocalHqColors.current
    val (icon, tone) = categoryLook(card.category)
    CardFrame(rememberEntrance(animate)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HqIconTile(icon, tone, size = 44)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(card.title, style = HqType.rowTitle, color = c.textPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text("Paid by ${card.paidBy}", style = HqType.bodySmall, color = c.textSecondary)
                }
            }
            CountUpAmount(card.amountPaise, animate)
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderSubtle))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarStack(card.splitNames)
                Spacer(Modifier.width(10.dp))
                Text(
                    if (card.everyone) "Split with everyone (${card.splitNames.size})" else "Split with ${card.splitNames.joinToString(", ")}",
                    style = HqType.bodySmall, color = c.textSecondary, maxLines = 2,
                )
            }
        }
    }
}

/** Two people and an arrow: who paid whom, and how much. */
@Composable
fun PaymentCard(card: AgentCard.Payment, animate: Boolean) {
    val c = LocalHqColors.current
    CardFrame(rememberEntrance(animate)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Initial(card.from, ring = c.brandTeal, size = 44)
                    Text(card.from, style = HqType.bodySmall, color = c.textSecondary)
                }
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = c.textBrand, modifier = Modifier.size(24.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Initial(card.to, ring = c.brandCoral, size = 44)
                    Text(card.to, style = HqType.bodySmall, color = c.textSecondary)
                }
            }
            CountUpAmount(card.amountPaise, animate)
            Text("Payment", style = HqType.labelSmall, color = c.textMuted)
        }
    }
}

/** Headline and label/value lines for questions. */
@Composable
fun AnswerCard(headline: String, lines: List<AnswerLine>) {
    val c = LocalHqColors.current
    CardFrame(1f) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(headline, style = HqType.titleMedium2, color = c.textPrimary)
            lines.forEach { line ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(line.label, style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.weight(1f))
                    Text(line.value, style = HqType.rowTitle, color = c.textPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
