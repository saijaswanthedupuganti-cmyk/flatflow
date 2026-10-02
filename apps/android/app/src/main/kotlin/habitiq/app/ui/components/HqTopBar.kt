package habitiq.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHeroBleed
import habitiq.app.ui.theme.LocalHqColors

/**
 * Pinned compact top bar (Android "large title collapses into a small bar" pattern).
 *
 * Screens keep their large in-body header ([HqPageHeader] or the Home hero). When that header scrolls
 * out of view, [HqTopBarHost] slides in a 56dp bar with the same title, back button and actions, so
 * navigation and actions are always reachable and the status bar always has a solid backdrop.
 *
 * Column-scrolled screens get this automatically through [HqPageHeader]. Lazy lists (whose header
 * item leaves composition when scrolled away) and the Home hero register with [HqPinnedTopBar].
 */
@Stable
class HqTopBarEntry {
    var title by mutableStateOf<String?>(null)
    var showLogo by mutableStateOf(false)
    var onBack by mutableStateOf<(() -> Unit)?>(null)
    var actions by mutableStateOf<(@Composable RowScope.() -> Unit)?>(null)
    val pinned: MutableState<Boolean> = mutableStateOf(false)
}

@Stable
class HqTopBarState {
    internal val entries = mutableStateListOf<HqTopBarEntry>()
    internal var hostTop by mutableFloatStateOf(0f)
}

val LocalHqTopBar = staticCompositionLocalOf<HqTopBarState?> { null }

/** Wrap the app's screen area once; it draws the active screen's compact bar over the content. */
@Composable
fun HqTopBarHost(content: @Composable () -> Unit) {
    val state = remember { HqTopBarState() }
    val bleed = LocalHeroBleed.current.value
    val c = LocalHqColors.current
    val view = LocalView.current
    Box(Modifier.fillMaxSize().onGloballyPositioned { state.hostTop = it.positionInWindow().y }) {
        CompositionLocalProvider(LocalHqTopBar provides state) { content() }

        val entry = state.entries.lastOrNull()
        val visible = entry != null && entry.pinned.value

        // Over a photo hero the status icons are light; once the solid bar covers it they must turn dark.
        LaunchedEffect(visible, bleed) {
            if (!bleed) return@LaunchedEffect
            var ctx = view.context
            while (ctx is android.content.ContextWrapper && ctx !is android.app.Activity) ctx = ctx.baseContext
            val window = (ctx as? android.app.Activity)?.window ?: return@LaunchedEffect
            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = visible
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(180)) + slideInVertically(tween(220)) { -it / 2 },
            exit = fadeOut(tween(140)) + slideOutVertically(tween(160)) { -it / 2 },
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            val shown = entry ?: return@AnimatedVisibility
            HqCompactBar(shown, statusInset = bleed)
        }
    }
}

@Composable
private fun BoxScope.HqCompactBar(entry: HqTopBarEntry, statusInset: Boolean) {
    val c = LocalHqColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(6.dp, ambientColor = c.textPrimary.copy(alpha = .10f), spotColor = c.textPrimary.copy(alpha = .10f))
            .background(c.canvas)
            .then(if (statusInset) Modifier.statusBarsPadding() else Modifier)
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = HqSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val back = entry.onBack
        if (back != null) {
            IconButton(onClick = back, modifier = Modifier.size(HqSize.target)) {
                Icon(HqIcons.Back, contentDescription = "Back", tint = c.iconDefault, modifier = Modifier.size(HqIconSize.md))
            }
        } else {
            Spacer(Modifier.width(HqSpacing.md))
        }
        Box(Modifier.weight(1f)) {
            when {
                entry.showLogo -> HqWordmark(height = 24.dp)
                entry.title != null -> Text(
                    entry.title.orEmpty(),
                    style = HqType.titleSmall2,
                    color = c.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        entry.actions?.invoke(this)
    }
}

/** Registers [entry] with the host while composed. Latest registration wins (the screen on top). */
@Composable
internal fun rememberRegisteredTopBarEntry(): HqTopBarEntry? {
    val host = LocalHqTopBar.current ?: return null
    val entry = remember { HqTopBarEntry() }
    DisposableEffect(host, entry) {
        host.entries.add(entry)
        onDispose { host.entries.remove(entry) }
    }
    return entry
}

/** Modifier for a large in-body header: marks the bar pinned once the header's bottom passes under it. */
internal fun Modifier.hqTrackHeader(entry: HqTopBarEntry?, host: HqTopBarState?, thresholdPx: Float): Modifier =
    if (entry == null || host == null) this else onGloballyPositioned { coords ->
        val bottom = coords.positionInWindow().y + coords.size.height
        entry.pinned.value = bottom < host.hostTop + thresholdPx
    }

/**
 * Explicit registration for screens whose header lives in a lazy list or is a custom hero.
 * Pass [pinned] from the screen's own scroll state.
 */
@Composable
fun HqPinnedTopBar(
    pinned: Boolean,
    title: String? = null,
    showLogo: Boolean = false,
    onBack: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val entry = rememberRegisteredTopBarEntry() ?: return
    entry.title = title
    entry.showLogo = showLogo
    entry.onBack = onBack
    entry.actions = actions
    entry.pinned.value = pinned
}

/** Header-height threshold in px used by [HqPageHeader]: pin once the title row is mostly hidden. */
@Composable
internal fun headerPinThresholdPx(): Float = with(LocalDensity.current) { 56.dp.toPx() }
