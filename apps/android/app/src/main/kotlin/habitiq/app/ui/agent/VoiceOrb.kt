package habitiq.app.ui.agent

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/** What the orb is doing: listening reacts to the voice, thinking ripples gently, done rests. */
enum class OrbMood { Listening, Thinking, Done }

internal object VoiceColors {
    val orbLight = Color(0xFF8CC8FF)
    val orbMid = Color(0xFF3D7BFF)
    val orbDeep = Color(0xFF1C4FE0)
    val orbEdge = Color(0xFF173DB8)
    val orbCore = Color(0xFF7FE8FF)
    val ring = Color(0xFF5B8CFF)
    val waveNear = Color(0xFF6D9BFF)
    val waveFar = Color(0xFFA98BFF)
    val scrimTop = Color(0xFF0E1A2E)
    val scrimBottom = Color(0xFF0B1424)
    val buttonTop = Color(0xFF4C8DFF)
    val buttonBottom = Color(0xFF1F5EFF)
}

/**
 * The listening orb from the Habitiq voice reference: a glossy blue sphere with a cyan core glow,
 * two halo rings, and fine sound waves streaming out to both screen edges. Everything is drawn in
 * one Canvas with transform-free maths so it stays smooth; [level] (0..1) swells rings and waves.
 * With [animate] false it renders one still frame (system animations off, screenshots).
 */
@Composable
fun VoiceOrbStage(level: Float, orbSize: Dp, mood: OrbMood, animate: Boolean, modifier: Modifier = Modifier) {
    val voice by animateFloatAsState(if (animate && mood == OrbMood.Listening) level else 0f, spring(dampingRatio = 0.55f, stiffness = 220f), label = "voice")
    val waveStrength by animateFloatAsState(
        when (mood) { OrbMood.Listening -> 1f; OrbMood.Thinking -> 0.35f; OrbMood.Done -> 0f },
        tween(500, easing = FastOutSlowInEasing), label = "waves",
    )
    val phase: Float
    val breath: Float
    if (animate) {
        val t = rememberInfiniteTransition(label = "orb")
        phase = t.animateFloat(0f, (2 * PI).toFloat(), infiniteRepeatable(tween(2600, easing = LinearEasing)), label = "phase").value
        breath = t.animateFloat(0f, 1f, infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breath").value
    } else {
        phase = 0.9f
        breath = 0.5f
    }
    Canvas(modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val r = orbSize.toPx() / 2
        drawHalo(center, r, breath, voice)
        if (waveStrength > 0.01f) {
            drawWaves(center, r, side = -1f, phase, breath, voice, waveStrength)
            drawWaves(center, r, side = 1f, phase, breath, voice, waveStrength)
        }
        drawOrb(center, r)
        drawGlyph(center, r, mood, phase, voice)
    }
}

private fun DrawScope.drawHalo(c: Offset, r: Float, breath: Float, voice: Float) {
    drawCircle(
        Brush.radialGradient(
            listOf(VoiceColors.ring.copy(alpha = .62f + .2f * voice), VoiceColors.ring.copy(alpha = .22f), Color.Transparent),
            center = c, radius = r * 1.75f,
        ),
        radius = r * 1.75f, center = c,
    )
    val r1 = r * 1.17f * (1f + .03f * breath + .06f * voice)
    val r2 = r * 1.38f * (1f + .04f * breath + .09f * voice)
    drawCircle(VoiceColors.ring.copy(alpha = .55f), r1, c, style = Stroke(1.2.dp.toPx()))
    drawCircle(VoiceColors.ring.copy(alpha = .22f), r2, c, style = Stroke(1.dp.toPx()))
}

private fun DrawScope.drawWaves(c: Offset, r: Float, side: Float, phase: Float, breath: Float, voice: Float, strength: Float) {
    val start = c.x + side * r * 0.92f
    val end = if (side < 0) 10.dp.toPx() else size.width - 10.dp.toPx()
    val amp = (24.dp.toPx() + 16.dp.toPx() * voice + 4.dp.toPx() * breath) * strength
    val brush = Brush.horizontalGradient(
        listOf(VoiceColors.waveNear.copy(alpha = .85f * strength), VoiceColors.waveFar.copy(alpha = .55f * strength)),
        startX = start, endX = end,
    )
    val strands = 12
    val steps = 64
    for (i in 0 until strands) {
        val path = Path()
        val spread = 0.35f + 0.65f * kotlin.math.abs(kotlin.math.cos(i * 0.45f))
        for (s in 0..steps) {
            val t = s / steps.toFloat()
            val x = start + (end - start) * t
            // Bulges just outside the orb and tapers to a fine point at the screen edge.
            val envelope = sin(PI.toFloat() * t) * (1f - .35f * t)
            val y = c.y + amp * envelope * spread * sin(2f * PI.toFloat() * 1.25f * t + phase * side * -1f + i * 0.52f)
            if (s == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, brush, style = Stroke(width = 0.9.dp.toPx()))
    }
}

private fun DrawScope.drawOrb(c: Offset, r: Float) {
    drawCircle(
        Brush.radialGradient(
            0f to VoiceColors.orbLight, 0.38f to VoiceColors.orbMid, 0.72f to VoiceColors.orbDeep, 1f to VoiceColors.orbEdge,
            center = Offset(c.x - r * .35f, c.y - r * .45f), radius = r * 1.75f,
        ),
        radius = r, center = c,
    )
    // Cyan light pooling at the bottom of the glass.
    drawCircle(
        Brush.radialGradient(listOf(VoiceColors.orbCore.copy(alpha = .85f), Color.Transparent), center = Offset(c.x, c.y + r * .78f), radius = r * .72f),
        radius = r, center = c,
    )
    // Soft sheen on top.
    drawCircle(
        Brush.radialGradient(listOf(Color.White.copy(alpha = .30f), Color.Transparent), center = Offset(c.x - r * .2f, c.y - r * .62f), radius = r * .75f),
        radius = r, center = c,
    )
    drawCircle(Color.White.copy(alpha = .32f), r - .75.dp.toPx(), c, style = Stroke(1.5.dp.toPx()))
}

private val BAR_SHAPE = floatArrayOf(0.5f, 0.78f, 1f, 0.78f, 0.5f)

/** Five rounded white bars: the waveform mark used on the nav button and the orb. */
internal fun DrawScope.drawWaveGlyph(c: Offset, height: Float, heights: FloatArray = BAR_SHAPE, color: Color = Color.White, barRatio: Float = 0.13f) {
    val barW = height * barRatio
    val gap = height * 0.13f
    val total = heights.size * barW + (heights.size - 1) * gap
    var x = c.x - total / 2
    for (h in heights) {
        val bh = height * h
        drawRoundRect(color, Offset(x, c.y - bh / 2), Size(barW, bh), CornerRadius(barW / 2))
        x += barW + gap
    }
}

private fun DrawScope.drawGlyph(c: Offset, r: Float, mood: OrbMood, phase: Float, voice: Float) {
    val heights = FloatArray(BAR_SHAPE.size) { i ->
        val base = BAR_SHAPE[i]
        when (mood) {
            OrbMood.Listening -> base * (0.86f + 0.14f * sin(phase * 2f + i * 1.1f)) * (0.9f + 0.2f * voice)
            OrbMood.Thinking -> base * (0.55f + 0.45f * ((sin(phase * 3f - i * 0.9f) + 1f) / 2f))
            OrbMood.Done -> base
        }.coerceIn(0.12f, 1f)
    }
    drawWaveGlyph(c, r * 0.76f, heights)
}

/** The five-bar voice glyph, for places outside this file (the nav button). */
object VoiceGlyph {
    fun DrawScope.draw(center: Offset, height: Float) = drawWaveGlyph(center, height * 0.72f, barRatio = 0.17f)
}
