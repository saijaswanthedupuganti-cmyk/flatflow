package habitiq.app.ui.theme

import android.provider.Settings
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/* -------------------------------------------------------------------------------------------------
 * Motion tokens. Every animation in a component reads one of these; nothing invents a duration,
 * curve or spring locally. Reduced motion (system "Remove animations") is read with
 * [hqReduceMotion] and always wins: no scale, no slide, at most an 80ms cross-fade.
 * -----------------------------------------------------------------------------------------------*/

/** Strong ease-out for entrances and press feedback (Emil's ease-out). */
val HqEaseOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

/** Strong ease-in-out for something already on screen that changes state. */
val HqEaseInOut = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)

const val HqPressDuration = 120
const val HqEnterDuration = 200

/**
 * The motion set. Durations: [fast] for colour/focus/selection, [base] for small enters and
 * content swaps, [slow] for screen-level travel. Springs: [pressSpring] for touch feedback,
 * [layoutSpring] for anything that moves to a new position (segmented indicator, switch thumb,
 * list placement), [sheetSpring] for sheets.
 */
object HqMotion {
    /** Colour, focus ring, selection, press release. */
    const val fast = 120
    /** Small enters, content swaps, fades. */
    const val base = 200
    /** Screen-level travel (shared axis), large layout changes. Never longer. */
    const val slow = 300
    /** Ceiling for any fade when the system asks for reduced motion (design doc section 9). */
    const val reduced = 80

    /** Standard curve: things that change while on screen (ease-out, starts fast so feedback is immediate). */
    val standard: Easing = HqEaseOut
    /** Emphasized curve: things that travel across the screen (ease-in-out with a strong middle). */
    val emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    /** Emphasized decelerate: an element arriving. */
    val emphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    /** Emphasized accelerate: an element leaving for good (short, so it never feels sluggish). */
    val emphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    /** Press feedback. Stiff and slightly under-damped: answers the finger in ~100ms. */
    const val pressStiffness = 800f
    const val pressDamping = 0.7f
    /** Layout / indicator travel. Settles without visible bounce. */
    const val layoutStiffness = 400f
    const val layoutDamping = 0.85f

    fun <T> pressSpring(visibilityThreshold: T? = null) =
        spring(dampingRatio = pressDamping, stiffness = pressStiffness, visibilityThreshold = visibilityThreshold)

    fun <T> layoutSpring(visibilityThreshold: T? = null) =
        spring(dampingRatio = layoutDamping, stiffness = layoutStiffness, visibilityThreshold = visibilityThreshold)

    /** Sheet open/settle: a touch softer than layout so a heavy surface feels heavy. */
    fun <T> sheetSpring(visibilityThreshold: T? = null) =
        spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow, visibilityThreshold = visibilityThreshold)

    fun <T> fastTween() = tween<T>(fast, easing = standard)
    fun <T> baseTween() = tween<T>(base, easing = standard)

    /** Scale a pressed button goes to. Cards and rows use [pressScaleSurface] (large surfaces move less). */
    const val pressScale = 0.97f
    const val pressScaleSurface = 0.985f
}

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

/** Press feedback only: a spring to 97% while held. Off under reduced motion. */
@Composable
fun Modifier.hqPressScale(pressed: Boolean, pressedScale: Float = HqMotion.pressScale): Modifier {
    val reduce = hqReduceMotion()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduce) pressedScale else 1f,
        animationSpec = HqMotion.pressSpring(),
        label = "pressScale",
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
 * Tappable surface with tactile feedback: the standard ripple plus a press spring to 98.5% while
 * held. Use for cards and rows (buttons already have their own press handling).
 */
fun Modifier.hqPressable(
    enabled: Boolean = true,
    role: Role? = Role.Button,
    onClick: () -> Unit,
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduce = hqReduceMotion()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduce) HqMotion.pressScaleSurface else 1f,
        animationSpec = HqMotion.pressSpring(),
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

/* -------------------------------------------------------------------------------------------------
 * Transitions. Screens adopt these; components never navigate.
 * -----------------------------------------------------------------------------------------------*/

/**
 * Ready-made enter/exit transitions, already resolved for density and reduced motion. Get one with
 * [rememberHqTransitions] and pass its members straight to NavHost / AnimatedContent /
 * AnimatedVisibility.
 *
 * Which one to use:
 * - [pushEnter] / [pushExit] / [popEnter] / [popExit] -- **shared axis X**. Moving deeper into a
 *   hierarchy (list -> detail, form step 1 -> 2) and back. The new screen slides 30dp and fades in
 *   while the old one slides 30dp the other way and fades out; direction tells the person where they
 *   went. Put these on the NavHost (`enterTransition = { t.pushEnter }` etc.).
 * - [fadeThroughEnter] / [fadeThroughExit] -- **fade through**. Switching between peers that have no
 *   spatial relationship: bottom-nav tabs, segmented-control content. Old content fades out quickly,
 *   new content fades in with a slight 96% -> 100% scale.
 * - [sheetEnter] / [sheetExit] -- custom in-window sheets and bottom panels (Material
 *   ModalBottomSheet already animates itself; use these only for hand-built panels).
 * - [contentSwap] -- AnimatedContent preset for a value replaced in place (empty -> list,
 *   loading -> loaded, an error message changing). Fade + 8dp rise, size animates on the layout spring.
 * - [counter] -- AnimatedContent preset for numbers ticking up or down (balances, counts).
 * - [expandEnter] / [collapseExit] -- AnimatedVisibility for a local reveal (a helper row, an
 *   inline error, "show more").
 *
 * With reduced motion every member degrades to an 80ms cross-fade (no slide, no scale, no spring).
 */
@Stable
class HqTransitions internal constructor(density: Density, val reduce: Boolean) {
    private val axisPx = with(density) { 30.dp.roundToPx() }
    private val risePx = with(density) { 8.dp.roundToPx() }

    private val reducedIn: EnterTransition = fadeIn(tween(HqMotion.reduced))
    private val reducedOut: ExitTransition = fadeOut(tween(HqMotion.reduced))

    private fun axisEnter(fromPx: Int): EnterTransition = if (reduce) reducedIn else
        slideInHorizontally(tween(HqMotion.slow, easing = HqMotion.emphasizedDecelerate)) { fromPx } +
            fadeIn(tween(HqMotion.base, delayMillis = 50, easing = HqMotion.standard))

    private fun axisExit(toPx: Int): ExitTransition = if (reduce) reducedOut else
        slideOutHorizontally(tween(HqMotion.slow, easing = HqMotion.emphasized)) { toPx } +
            fadeOut(tween(HqMotion.fast, easing = HqMotion.standard))

    /** Shared axis X, forward: the arriving screen. */
    val pushEnter: EnterTransition = axisEnter(axisPx)
    /** Shared axis X, forward: the screen being covered. */
    val pushExit: ExitTransition = axisExit(-axisPx)
    /** Shared axis X, back: the screen being revealed again. */
    val popEnter: EnterTransition = axisEnter(-axisPx)
    /** Shared axis X, back: the screen being dismissed. */
    val popExit: ExitTransition = axisExit(axisPx)

    /** Fade through (peer switch): incoming. */
    val fadeThroughEnter: EnterTransition = if (reduce) reducedIn else
        fadeIn(tween(HqMotion.base, delayMillis = 70, easing = HqMotion.standard)) +
            scaleIn(tween(HqMotion.base, delayMillis = 70, easing = HqMotion.standard), initialScale = 0.96f)
    /** Fade through (peer switch): outgoing. Quick, so the switch feels instant. */
    val fadeThroughExit: ExitTransition = if (reduce) reducedOut else fadeOut(tween(HqMotion.fast - 30, easing = HqMotion.emphasizedAccelerate))

    /** Hand-built sheet/panel rising from the bottom edge on the sheet spring. */
    val sheetEnter: EnterTransition = if (reduce) reducedIn else
        slideInVertically(HqMotion.sheetSpring(IntOffset.VisibilityThreshold)) { it } + fadeIn(tween(HqMotion.fast))
    val sheetExit: ExitTransition = if (reduce) reducedOut else
        slideOutVertically(tween(HqMotion.base, easing = HqMotion.emphasizedAccelerate)) { it } + fadeOut(tween(HqMotion.base))

    /** Local reveal (AnimatedVisibility): fade + small rise; height is animated by the parent's animateContentSize if needed. */
    val expandEnter: EnterTransition = if (reduce) reducedIn else
        fadeIn(tween(HqMotion.base, easing = HqMotion.standard)) + slideInVertically(tween(HqMotion.base, easing = HqMotion.standard)) { -risePx }
    val collapseExit: ExitTransition = if (reduce) reducedOut else fadeOut(tween(HqMotion.fast, easing = HqMotion.standard))

    /** AnimatedContent preset for a value replaced in place. */
    fun <S> contentSwap(): AnimatedContentTransitionScope<S>.() -> ContentTransform = {
        if (reduce) {
            (reducedIn togetherWith reducedOut).using(SizeTransform(clip = false) { _, _ -> tween(0) })
        } else {
            (fadeIn(tween(HqMotion.base, delayMillis = 40, easing = HqMotion.standard)) +
                slideInVertically(tween(HqMotion.base, easing = HqMotion.standard)) { risePx })
                .togetherWith(fadeOut(tween(HqMotion.fast - 30)))
                .using(SizeTransform(clip = false) { _, _ -> HqMotion.layoutSpring(androidx.compose.ui.unit.IntSize.VisibilityThreshold) })
        }
    }

    /** AnimatedContent preset for a number: the new value rolls in from below when it grows, from above when it shrinks. */
    fun counter(): AnimatedContentTransitionScope<Long>.() -> ContentTransform = {
        val up = targetState > initialState
        if (reduce) reducedIn togetherWith reducedOut else
            (slideInVertically(HqMotion.layoutSpring(IntOffset.VisibilityThreshold)) { h -> if (up) h / 2 else -h / 2 } + fadeIn(tween(HqMotion.fast)))
                .togetherWith(slideOutVertically(tween(HqMotion.fast)) { h -> if (up) -h / 2 else h / 2 } + fadeOut(tween(HqMotion.fast - 30)))
                .using(SizeTransform(clip = false))
    }
}

/** The app's transitions for this composition (density + reduced motion resolved once). */
@Composable
fun rememberHqTransitions(): HqTransitions {
    val density = LocalDensity.current
    val reduce = hqReduceMotion()
    return remember(density, reduce) { HqTransitions(density, reduce) }
}

/**
 * List item placement for LazyColumn/LazyRow items whose order or membership changes (a task
 * completing, a filter applied). Items glide to their new slot on the layout spring and fade in/out.
 * Use only on lists that genuinely reorder; never on a list that is just scrolling in.
 *
 * ```
 * items(tasks, key = { it.id }) { task -> TaskRow(task, Modifier.hqAnimateItem()) }
 * ```
 */
@Composable
fun LazyItemScope.hqAnimateItem(): Modifier {
    val reduce = hqReduceMotion()
    return if (reduce) {
        Modifier.animateItem(fadeInSpec = tween(HqMotion.reduced), placementSpec = null, fadeOutSpec = tween(HqMotion.reduced))
    } else {
        Modifier.animateItem(
            fadeInSpec = tween(HqMotion.base, easing = HqMotion.standard),
            placementSpec = HqMotion.layoutSpring(IntOffset.VisibilityThreshold),
            fadeOutSpec = tween(HqMotion.fast),
        )
    }
}

/* -------------------------------------------------------------------------------------------------
 * Haptics. Sparse and semantic (design doc section 9): a confirmed completion, a selection tick, a
 * toggle. Never on keystrokes, never the only signal.
 * -----------------------------------------------------------------------------------------------*/

@Stable
class HqHaptics internal constructor(private val feedback: HapticFeedback) {
    /** A confirmed, meaningful action (primary submit succeeded / was accepted). */
    fun confirm() = feedback.performHapticFeedback(HapticFeedbackType.Confirm)
    /** A discrete selection change (segmented control, picker detent). */
    fun tick() = feedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
    /** A switch changing state. */
    fun toggle(on: Boolean) = feedback.performHapticFeedback(if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
    /** A rejected action (validation failed on submit). */
    fun reject() = feedback.performHapticFeedback(HapticFeedbackType.Reject)
}

@Composable
fun rememberHqHaptics(): HqHaptics {
    val feedback = LocalHapticFeedback.current
    return remember(feedback) { HqHaptics(feedback) }
}

/** Tween for state colour changes (focus, selection, pressed tint). */
fun <T> hqColorSpec(): FiniteAnimationSpec<T> = tween(HqMotion.fast, easing = HqMotion.standard)
