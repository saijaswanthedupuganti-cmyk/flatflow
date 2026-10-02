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
import kotlinx.coroutines.flow.first
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.semantics.Role

private val loadingStory = listOf(
    "Different people.\nOne home.",
    "Shared living,\nmade easier.",
    "Live together.\nManage together.",
)

/** Closing line, held while the progress bar completes. */
private const val FinalLine = "Welcome home."

/** Total length of the cold-start intro. Change this one value to lengthen or shorten it. */
const val IntroDurationMs = 10_000L

private val Settle = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/** Fraction of the lockup image width taken by the house mark (the rest is the wordmark). */
private const val MarkFraction = 0.275f

/**
 * Cold-start intro (~[IntroDurationMs]). Concept: "Different people. One home."
 *
 *  0.0-2.0s  teal and coral glows drift together; the house mark pops in; the wordmark wipes open.
 *  2.0-8.5s  three story lines, ~2.1s each, with matching progress dots.
 *  8.5-9.5s  "Welcome home." while "PREPARING YOUR SPACE" reaches 100%.
 *  9.5-10s   the whole screen fades and lifts slightly into the app, then [onFinished].
 *
 * Progress is honest: it never shows 100% until [ready]. If startup is slower than the intro, the bar
 * holds at 95% on the closing line. "Skip" ends early (as soon as [ready]). With system animations
 * off, it shows the final frame and finishes as soon as the app is ready.
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
    val markIn = remember { Animatable(if (still) 1f else 0f) }
    val wordReveal = remember { Animatable(if (still) 1f else 0f) }
    val storyAlpha = remember { Animatable(if (still) 1f else 0f) }
    val progressIn = remember { Animatable(if (still) 1f else 0f) }
    val progress = remember { Animatable(if (still) 0.95f else 0f) }
    val exit = remember { Animatable(0f) }
    val skipIn = remember { Animatable(0f) }
    val storyIndex = remember { mutableIntStateOf(if (still) loadingStory.size else 0) }
    val skipRequested = remember { mutableStateOf(false) }

    val breath = rememberInfiniteTransition(label = "breath").animateFloat(
        initialValue = 0f,
        targetValue = if (still) 0f else 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathValue",
    )

    LaunchedEffect(Unit) {
        // Ends the intro: wait for the app, complete the bar, fade out, hand over.
        suspend fun complete() {
            snapshotFlow { readyState.value }.first { it }
            if (!still) {
                progress.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
                exit.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
            }
            finish.value()
        }
        if (still) {
            complete()
            return@LaunchedEffect
        }
        coroutineScope {
            val intro = launch {
                launch { merge.animateTo(1f, tween(1600, easing = Settle)) }
                launch {
                    delay(650)
                    markIn.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow))
                }
                launch {
                    delay(1100)
                    wordReveal.animateTo(1f, tween(850, easing = Settle))
                }
                launch {
                    delay(1600)
                    skipIn.animateTo(1f, tween(300))
                }
                launch {
                    delay(2000)
                    // Three story lines: rise in, hold, fade.
                    repeat(loadingStory.size) { i ->
                        storyIndex.intValue = i
                        storyAlpha.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
                        delay(1300)
                        storyAlpha.animateTo(0f, tween(380, easing = FastOutSlowInEasing))
                    }
                    // Closing line stays up until the intro hands over.
                    storyIndex.intValue = loadingStory.size
                    storyAlpha.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
                }
                launch {
                    delay(2100)
                    progressIn.animateTo(1f, tween(400, easing = LinearEasing))
                    progress.animateTo(
                        0.95f,
                        tween((IntroDurationMs - 2100 - 400 - 900).toInt(), easing = CubicBezierEasing(0.35f, 0.1f, 0.25f, 1f)),
                    )
                }
                delay(IntroDurationMs - 900)
            }
            // Skip cancels the remaining choreography; otherwise wait for the full intro.
            val skipWatcher = launch {
                snapshotFlow { skipRequested.value }.first { it }
                intro.cancel()
            }
            intro.join()
            skipWatcher.cancel()
            if (skipRequested.value) {
                storyIndex.intValue = loadingStory.size
                storyAlpha.snapTo(1f)
                markIn.snapTo(1f)
                wordReveal.snapTo(1f)
                merge.snapTo(1f)
                progressIn.snapTo(1f)
            }
            complete()
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .background(c.canvas)
            .graphicsLayer {
                alpha = 1f - exit.value
                val s = 1f + 0.03f * exit.value
                scaleX = s
                scaleY = s
            }
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

        // Skip: small, top-right, appears after the logo lands.
        if (!still) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 8.dp, end = 12.dp)
                    .alpha(skipIn.value)
                    .size(width = 72.dp, height = 48.dp)
                    .clip(CircleShape)
                    .clickable(enabled = skipIn.value > 0.5f, role = Role.Button) { skipRequested.value = true },
                contentAlignment = Alignment.Center,
            ) {
                Text("Skip", style = HqType.labelLarge, color = c.textSecondary, fontWeight = FontWeight.SemiBold)
            }
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
                    val i = storyIndex.intValue
                    Text(
                        if (i < loadingStory.size) loadingStory[i] else FinalLine,
                        style = HqType.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (i < loadingStory.size) c.textPrimary else c.textBrand,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            alpha = storyAlpha.value
                            translationY = (1f - storyAlpha.value) * 12.dp.toPx()
                        },
                    )
                }

                Row(
                    Modifier.alpha(progressIn.value),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    loadingStory.indices.forEach { i ->
                        val active = i == storyIndex.intValue
                        val passed = i < storyIndex.intValue
                        Box(
                            Modifier
                                .size(width = if (active) 18.dp else 6.dp, height = 6.dp)
                                .clip(CircleShape)
                                .background(if (active || passed) c.brandTeal else c.borderSubtle),
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
                    if (progress.value >= 0.999f) "READY" else "PREPARING YOUR SPACE",
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
