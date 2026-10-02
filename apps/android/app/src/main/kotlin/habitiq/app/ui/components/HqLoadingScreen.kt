package habitiq.app.ui.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val loadingStory = listOf(
    "Different people.\nOne home.",
    "Shared living,\nmade easier.",
    "Live together.\nManage together.",
)

private val Settle = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/** Fraction of the lockup image width taken by the house mark (the rest is the wordmark). */
private const val MarkFraction = 0.275f

/**
 * Startup screen. Concept: "Different people. One home."
 *
 * 1. A teal orb and a coral orb drift in from opposite sides and merge into one warm glow that
 *    then breathes slowly (the household).
 * 2. The house mark pops in on a soft spring, then the wordmark wipes open beside it.
 * 3. Story lines rise and fade one at a time with matching progress dots, looping while the app
 *    is still starting.
 * 4. "PREPARING YOUR SPACE" progress eases toward 95% and never claims completion; the caller
 *    removes this screen when startup resolves.
 *
 * Everything animates transform/alpha only. With system animations off it renders its final frame.
 */
@Composable
fun HqLoadingScreen(modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    val context = LocalContext.current
    val still = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }

    val merge = remember { Animatable(if (still) 1f else 0f) }
    val markIn = remember { Animatable(if (still) 1f else 0f) }
    val wordReveal = remember { Animatable(if (still) 1f else 0f) }
    val storyAlpha = remember { Animatable(if (still) 1f else 0f) }
    val progressIn = remember { Animatable(if (still) 1f else 0f) }
    val progress = remember { Animatable(if (still) 0.95f else 0f) }
    val storyIndex = remember { mutableIntStateOf(0) }

    val breath = rememberInfiniteTransition(label = "breath").animateFloat(
        initialValue = 0f,
        targetValue = if (still) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathValue",
    )

    LaunchedEffect(still) {
        if (still) return@LaunchedEffect
        coroutineScope {
            launch { merge.animateTo(1f, tween(1500, easing = Settle)) }
            launch {
                delay(650)
                markIn.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow))
            }
            launch {
                delay(1050)
                wordReveal.animateTo(1f, tween(800, easing = Settle))
            }
            launch {
                delay(1700)
                while (true) {
                    storyAlpha.animateTo(1f, tween(320, easing = FastOutSlowInEasing))
                    delay(900)
                    storyAlpha.animateTo(0f, tween(260, easing = FastOutSlowInEasing))
                    storyIndex.intValue = (storyIndex.intValue + 1) % loadingStory.size
                }
            }
            launch {
                delay(1900)
                progressIn.animateTo(1f, tween(350, easing = LinearEasing))
                progress.animateTo(0.95f, tween(6000, easing = CubicBezierEasing(0.3f, 0.7f, 0.2f, 1f)))
            }
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .background(c.canvas)
            .semantics { contentDescription = "Application is loading" },
    ) {
        // Different people, converging: teal drifts in from the left, coral from the right.
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.30f
            val spread = size.width * 0.34f * (1f - merge.value)
            val pulse = 1f + 0.06f * breath.value
            val r = size.width * 0.42f * pulse
            val glow = 0.22f + 0.05f * breath.value
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

        BoxWithConstraints(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxWidth().padding(top = maxHeight * 0.24f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .graphicsLayer {
                            val s = 0.82f + 0.18f * markIn.value
                            scaleX = s
                            scaleY = s
                            alpha = markIn.value.coerceIn(0f, 1f)
                        }
                        .drawWithContent {
                            // The mark is always visible; the wordmark wipes open left-to-right.
                            val visible = MarkFraction + (1f - MarkFraction) * wordReveal.value
                            clipRect(right = size.width * visible) { this@drawWithContent.drawContent() }
                        },
                ) {
                    HqWordmark(height = 68.dp)
                }

                Box(
                    Modifier.padding(top = 52.dp).fillMaxWidth().height(84.dp).padding(horizontal = 24.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Text(
                        loadingStory[storyIndex.intValue],
                        style = HqType.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = c.textPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            alpha = storyAlpha.value
                            translationY = (1f - storyAlpha.value) * 10.dp.toPx()
                        },
                    )
                }

                Row(
                    Modifier.alpha(progressIn.value),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    loadingStory.indices.forEach { i ->
                        Box(
                            Modifier
                                .size(width = if (i == storyIndex.intValue) 18.dp else 6.dp, height = 6.dp)
                                .clip(CircleShape)
                                .background(if (i == storyIndex.intValue) c.brandTeal else c.borderSubtle),
                        )
                    }
                }
            }
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 34.dp, vertical = 44.dp)
                .alpha(progressIn.value),
            verticalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "PREPARING YOUR SPACE",
                    style = HqType.labelSmall,
                    color = c.textSecondary,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text("${(progress.value * 100).toInt()}%", style = HqType.labelSmall, color = c.textSecondary)
            }
            Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(c.borderSubtle)) {
                Box(Modifier.fillMaxWidth(progress.value).height(3.dp).clip(CircleShape).background(c.brandTeal))
            }
        }
    }
}
