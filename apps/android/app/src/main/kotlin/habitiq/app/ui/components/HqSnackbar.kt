package habitiq.app.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.hqShadow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.LocalHqColors

/**
 * Consistently-styled SnackbarHost (design doc section 36). Use for a completed action, a saved
 * change, or a copied invite code -- not for critical decisions or information that must stay
 * visible (that's a dialog, see HqOverlays.kt).
 *
 * Usage: hold a `remember { SnackbarHostState() }` in the screen, call
 * `snackbarHostState.showSnackbar("Task completed")` on success, and place `HqSnackbarHost(state)`
 * in the Scaffold's `snackbarHost` slot.
 */
@Composable
fun HqSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier = modifier) { data ->
        val reduce = habitiq.app.ui.theme.hqReduceMotion()
        // Slides up 24dp on the layout spring as it appears (the host fades it out on dismiss).
        val rise = androidx.compose.runtime.remember(data) { androidx.compose.animation.core.Animatable(if (reduce) 0f else 1f) }
        androidx.compose.runtime.LaunchedEffect(data) { rise.animateTo(0f, habitiq.app.ui.theme.HqMotion.layoutSpring()) }
        HqSnackbarVisual(
            message = data.visuals.message,
            actionLabel = data.visuals.actionLabel,
            onAction = { data.performAction() },
            modifier = androidx.compose.ui.Modifier.graphicsLayer { translationY = rise.value * 24.dp.toPx() },
        )
    }
}

/**
 * The snackbar card: navy (inverse) surface, 12dp corners, Raised shadow, message on the start,
 * optional action ("Undo") on the end as a 48dp target. Announced politely, never steals focus.
 * Short duration = 4s (the system extends it when accessibility services need longer).
 */
@Composable
internal fun HqSnackbarVisual(message: String, actionLabel: String?, onAction: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    val shape = habitiq.app.ui.theme.HqShapes.control
    Row(
        modifier
            .padding(horizontal = HqSpacing.lg, vertical = HqSpacing.md)
            .fillMaxWidth()
            .hqShadow(habitiq.app.ui.theme.HqShadow.Raised, shape)
            .clip(shape)
            .background(c.snackbarBg)
            .defaultMinSize(minHeight = 52.dp)
            .padding(start = HqSpacing.lg, end = if (actionLabel != null) HqSpacing.xs else HqSpacing.lg)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(message, style = HqType.bodyMedium, color = c.snackbarFg, modifier = Modifier.weight(1f).padding(vertical = HqSpacing.md))
        if (actionLabel != null) HqTextButton(text = actionLabel, onClick = onAction, color = c.snackbarAction)
    }
}

/**
 * App-wide snackbar for "saved" feedback that must outlive the form that triggered it (the form
 * closes, the message stays). Provided once by the app shell via [LocalHqSnackbar]; null in
 * previews and screenshot tests, where callers simply skip the message.
 */
class HqSnackbarController(
    val hostState: SnackbarHostState,
    private val scope: kotlinx.coroutines.CoroutineScope,
) {
    /** Shows [message]; [onAction] runs only if the person taps [actionLabel] (e.g. "Undo"). */
    fun show(message: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
        scope.launch {
            hostState.currentSnackbarData?.dismiss()
            val result = hostState.showSnackbar(message, actionLabel = actionLabel, duration = androidx.compose.material3.SnackbarDuration.Short)
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) onAction()
        }
    }
}

val LocalHqSnackbar = androidx.compose.runtime.staticCompositionLocalOf<HqSnackbarController?> { null }
