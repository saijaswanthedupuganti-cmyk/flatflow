package habitiq.app.ui.theme

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView

/** Strong ease-out for entrances and press feedback. */
val HqEaseOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

/** Strong ease-in-out for something already on screen that changes state. */
val HqEaseInOut = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)

const val HqPressDuration = 140
const val HqEnterDuration = 200

@Composable
fun hqReduceMotion(): Boolean {
    val view = LocalView.current
    return remember(view) {
        val scale = runCatching {
            Settings.Global.getFloat(view.context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }.getOrDefault(1f)
        scale == 0f
    }
}

/** Press feedback only. Scale stays near 1 so a frequent tap does not bounce. */
@Composable
fun Modifier.hqPressScale(pressed: Boolean): Modifier {
    val reduce = hqReduceMotion()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduce) 0.98f else 1f,
        animationSpec = tween(durationMillis = HqPressDuration, easing = HqEaseOut),
        label = "pressScale"
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** First paint of a screen. Opacity always; a short rise only when motion is allowed. */
@Composable
fun HqFadeUp(index: Int = 0, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val reduce = hqReduceMotion()
    var shown by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(reduce) {
        if (reduce) {
            shown = true
        } else {
            delay(index * 40L)
            shown = true
        }
    }
    AnimatedVisibility(
        visible = shown,
        modifier = modifier,
        enter = fadeIn(tween(HqEnterDuration, easing = HqEaseOut)) +
            slideInVertically(tween(HqEnterDuration, easing = HqEaseOut)) { if (reduce) 0 else it / 8 }
    ) {
        content()
    }
}


/**
 * Tappable surface with tactile feedback: the standard ripple plus a quick spring squash to 98%
 * while pressed. Use for cards and rows (buttons already have their own press handling).
 */
fun Modifier.hqPressable(
    enabled: Boolean = true,
    role: androidx.compose.ui.semantics.Role? = androidx.compose.ui.semantics.Role.Button,
    onClick: () -> Unit,
): Modifier = composed {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduce = hqReduceMotion()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduce) 0.98f else 1f,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "pressable",
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(
            interactionSource = interaction,
            indication = androidx.compose.material3.ripple(),
            enabled = enabled,
            role = role,
            onClick = onClick,
        )
}
