package habitiq.app.ui.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.LocalHqColors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sin

/** Minimum length of the cold-start intro. It runs longer only if startup is still resolving. */
const val IntroDurationMs = 2_600L

private val Settle = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/** Fraction of the lockup image width taken by the house mark (the rest is the wordmark). */
private const val MarkFraction = 0.275f

/** Extrusion layers drawn behind the mark while it turns, so it reads as a solid object. */
private const val DepthLayers = 7

/**
 * Cold-start intro: logo only, no copy. Concept: "Different people. One home."
 *
 *  0.0-1.4s  teal and coral glows drift together and meet behind the logo.
 *  0.3-1.5s  the house mark swings in on its vertical axis (a real 3D turn with perspective and
 *            extruded depth) and drops onto a soft ground shadow.
 *  1.1-1.9s  the wordmark wipes open beside it.
 *  after     the lockup floats with a slight tilt until startup is ready, then fades into the app.
 *
 * With system animations off, it shows the final frame and finishes as soon as the app is ready.
 */
@Composable
fun HqLoadingScreen(
    modifier: Modifier = Modifier,
    ready: Boolean = false,
    onFinished: () -> Unit = {},
) {
    val c = LocalHqColors.current
    val context = LocalContext.current
    val still = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val readyState = rememberUpdatedState(ready)
    val finish = rememberUpdatedState(onFinished)

    val merge = remember { Animatable(if (still) 1f else 0f) }
    val turn = remember { Animatable(if (still) 0f else -100f) } // degrees around Y
    val drop = remember { Animatable(if (still) 1f else 0f) }
    val wordReveal = remember { Animatable(if (still) 1f else 0f) }
    val exit = remember { Animatable(0f) }

    val idle = rememberInfiniteTransition(label = "idle")
    val float by idle.animateFloat(
        initialValue = 0f,
        targetValue = if (still) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float",
    )

    LaunchedEffect(Unit) {
        if (!still) {
            coroutineScope {
                launch { merge.animateTo(1f, tween(1400, easing = Settle)) }
                launch {
                    delay(300)
                    turn.animateTo(0f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessVeryLow))
                }
                launch {
                    delay(300)
                    drop.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow))
                }
                launch {
                    delay(1100)
                    wordReveal.animateTo(1f, tween(800, easing = Settle))
                }
                delay(IntroDurationMs - 450)
            }
        }
        snapshotFlow { readyState.value }.first { it }
        if (!still) exit.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
        finish.value()
    }

    Box(
        modifier
            .fillMaxSize()
            .background(c.canvas)
            .graphicsLayer {
                alpha = 1f - exit.value
                val s = 1f + 0.04f * exit.value
                scaleX = s
                scaleY = s
            }
            .semantics { contentDescription = "Oddroof is loading" },
    ) {
        // Different people, converging: teal drifts in from the left, coral from the right.
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.45f
            val spread = size.width * 0.38f * (1f - merge.value)
            val pulse = 1f + 0.05f * float
            val r = size.width * 0.46f * pulse
            val glow = 0.20f + 0.05f * float
            drawCircle(
                Brush.radialGradient(
                    listOf(c.brandTeal.copy(alpha = glow), c.brandTeal.copy(alpha = 0f)),
                    center = Offset(cx - spread, cy),
                    radius = r,
                ),
                radius = r,
                center = Offset(cx - spread, cy),
            )
            drawCircle(
                Brush.radialGradient(
                    listOf(c.brandCoral.copy(alpha = glow * 0.85f), c.brandCoral.copy(alpha = 0f)),
                    center = Offset(cx + spread, cy),
                    radius = r * 0.9f,
                ),
                radius = r * 0.9f,
                center = Offset(cx + spread, cy),
            )
        }

        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val lift = (1f - drop.value) * -46f - float * 6f // dp: falls in, then hovers
            val sway = float * 7f - 3.5f // degrees of gentle idle tilt once landed
            val shadowWidth = maxWidth * 0.42f
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.graphicsLayer { translationY = lift.dp.toPx() }) {
                    // Extruded depth: darker copies of the mark behind it, offset by how far it is turned.
                    val angle = turn.value + if (wordReveal.value >= 1f) sway else 0f
                    val depth = sin(Math.toRadians(angle.toDouble())).toFloat()
                    if (abs(depth) > 0.02f) {
                        for (i in DepthLayers downTo 1) {
                            Box(
                                Modifier
                                    .graphicsLayer {
                                        rotationY = angle
                                        cameraDistance = 14f * density
                                        transformOrigin = TransformOrigin(MarkFraction / 2f, 0.5f)
                                        translationX = -depth * i * 1.1f.dp.toPx()
                                        alpha = 0.10f + 0.05f * (DepthLayers - i) / DepthLayers
                                    }
                                    .clipToMark(),
                            ) { HqWordmark(height = 72.dp, tint = c.textPrimary) }
                        }
                    }
                    // The mark itself: a 3D turn around its own centre.
                    Box(
                        Modifier
                            .graphicsLayer {
                                rotationY = angle
                                rotationX = (1f - drop.value) * 18f
                                cameraDistance = 14f * density
                                transformOrigin = TransformOrigin(MarkFraction / 2f, 0.5f)
                            }
                            .clipToMark(),
                    ) { HqWordmark(height = 72.dp) }
                    // The wordmark wipes open beside the mark.
                    Box(
                        Modifier.drawWithContent {
                            val from = size.width * MarkFraction
                            val to = from + (size.width - from) * wordReveal.value
                            clipRect(left = from, right = to) { this@drawWithContent.drawContent() }
                        },
                    ) { HqWordmark(height = 72.dp) }
                }

                // Ground shadow: wide and faint while the mark is in the air, tight once it lands.
                Canvas(
                    Modifier
                        .padding(top = 26.dp)
                        .width(shadowWidth)
                        .height(18.dp),
                ) {
                    val air = (1f - drop.value).coerceIn(0f, 1f) + float * 0.25f
                    val w = size.width * (0.55f + 0.35f * air)
                    val a = (0.16f - 0.08f * air).coerceIn(0.04f, 0.18f)
                    drawOval(
                        Brush.radialGradient(
                            listOf(Color.Black.copy(alpha = a), Color.Transparent),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = w / 2f,
                        ),
                        topLeft = Offset((size.width - w) / 2f, 0f),
                        size = Size(w, size.height),
                    )
                }
            }
        }
    }
}

/** Clips the lockup to just its house mark. */
private fun Modifier.clipToMark(): Modifier = drawWithContent {
    clipRect(right = size.width * MarkFraction) { this@drawWithContent.drawContent() }
}
