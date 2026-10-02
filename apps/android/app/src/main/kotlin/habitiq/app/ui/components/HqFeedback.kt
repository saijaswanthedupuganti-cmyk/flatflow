package habitiq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Reusable empty state (design doc section 31/32). Always explain what's empty, why, and what to
 * do next -- never just "No data found." [primaryLabel] is required on purpose; every empty
 * state should offer a next action.
 */
@Composable
fun HqEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryLabel: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
) {
    val c = LocalHqColors.current
    Column(
        modifier.fillMaxWidth().padding(HqSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = c.iconDefault, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(HqSpacing.lg))
        Text(title, style = HqType.titleSmall2, color = c.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(HqSpacing.xs))
        Text(message, style = HqType.bodyMedium, color = c.textSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(HqSpacing.xl))
        HqButton(text = primaryLabel, onClick = onPrimaryClick, fullWidth = false)
        if (secondaryLabel != null && onSecondaryClick != null) {
            Spacer(Modifier.height(HqSpacing.sm))
            HqTextButton(text = secondaryLabel, onClick = onSecondaryClick)
        }
    }
}

/**
 * Reusable error state for a failed list/screen load (design doc section 34). For a failed
 * single action, use an inline message or Snackbar instead -- this is for "the whole screen
 * couldn't load." Never pass a raw exception message here -- map it to plain language first.
 */
@Composable
fun HqErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
) {
    val c = LocalHqColors.current
    Column(
        modifier.fillMaxWidth().padding(HqSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = HqType.titleSmall2, color = c.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(HqSpacing.xs))
        Text(message, style = HqType.bodyMedium, color = c.textSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(HqSpacing.xl))
        HqButton(text = "Retry", onClick = onRetry, variant = HqButtonVariant.Secondary, fullWidth = false)
    }
}

/** A short inline failure message next to a single field/action (design doc section 34 "InlineError"). */
@Composable
fun HqInlineError(message: String, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    Text(message, style = HqType.bodyMedium, color = c.statusDangerFg, modifier = modifier)
}

/** A short inline confirmation/informational message that isn't an error (e.g. "Password reset email sent"). */
@Composable
fun HqInlineInfo(message: String, modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    Text(message, style = HqType.bodyMedium, color = c.textBrand, modifier = modifier)
}

/** Small inline spinner + label for a section still loading (design doc section 33 "InlineLoading"). Avoid a full-screen blocker for small refreshes. */
@Composable
fun HqInlineLoading(label: String = "Loading…", modifier: Modifier = Modifier) {
    val c = LocalHqColors.current
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = c.textBrand)
        Text(label, style = HqType.bodyMedium, color = c.textSecondary)
    }
}

/**
 * A single skeleton block -- compose several to build CardSkeleton/ListSkeleton/ScreenSkeleton
 * shapes (design doc section 33). [modifier] fully replaces the default sizing, so pass e.g.
 * `Modifier.fillMaxWidth(0.6f)` to make a shorter line.
 */
@Composable
fun HqSkeletonBlock(modifier: Modifier = Modifier.fillMaxWidth(), height: androidx.compose.ui.unit.Dp = 16.dp) {
    val c = LocalHqColors.current
    val reduce = habitiq.app.ui.theme.hqReduceMotion()
    // A soft light band sweeps across every skeleton block (static when system animations are off).
    val sweep = androidx.compose.animation.core.rememberInfiniteTransition(label = "skeleton").animateFloat(
        initialValue = -1f,
        targetValue = if (reduce) -1f else 2f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(1300, easing = androidx.compose.animation.core.LinearEasing),
        ),
        label = "skeletonSweep",
    )
    androidx.compose.foundation.layout.Box(
        modifier
            .height(height)
            .clip(RoundedCornerShape(HqRadius.small))
            .background(c.surfaceSubtle)
            .drawWithContent {
                drawContent()
                val w = size.width
                val x = sweep.value * w
                drawRect(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(androidx.compose.ui.graphics.Color.Transparent, c.surfaceBase.copy(alpha = 0.7f), androidx.compose.ui.graphics.Color.Transparent),
                        startX = x - w * 0.5f,
                        endX = x + w * 0.5f,
                    ),
                )
            }
    )
}

/** A skeleton shaped like a default HqCard -- use while a card's real content is loading. */
@Composable
fun HqCardSkeleton(modifier: Modifier = Modifier) {
    HqCard(modifier = modifier) {
        HqSkeletonBlock(height = 14.dp)
        Spacer(Modifier.height(HqSpacing.sm))
        HqSkeletonBlock(height = 14.dp, modifier = Modifier.fillMaxWidth(0.6f))
    }
}
