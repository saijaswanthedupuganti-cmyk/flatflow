package habitiq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.LocalHqColors

/**
 * Semantic card variants (design doc section 24). Cards are for grouping related information --
 * not a default wrapper for every piece of content (section 2.4 "Card overload").
 */
enum class HqCardVariant {
    /** Plain grouped content, flat surface, subtle border. */
    Standard,
    /** Tappable destination -- same as Standard but clickable, no extra visual weight. */
    Interactive,
    /** Balance status / alerts / important states -- filled with a tinted container, no border. */
    Status,
}

@Composable
fun HqCard(
    modifier: Modifier = Modifier,
    variant: HqCardVariant = HqCardVariant.Standard,
    onClick: (() -> Unit)? = null,
    padding: Dp = HqSpacing.card,
    /** Status-variant only: override the tinted container color, e.g. `c.statusSuccessBg` for an "all settled" state instead of the brand-tinted default. */
    statusTint: Color? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(HqRadius.card)
    val base = modifier
        .fillMaxWidth()
        // Figma card lift: 0 7px 22px rgba(25,49,47,.035); Status cards are flat.
        .then(if (variant == HqCardVariant.Status) Modifier else Modifier.shadow(2.dp, shape, ambientColor = c.textPrimary.copy(alpha = .06f), spotColor = c.textPrimary.copy(alpha = .06f)))
        .clip(shape)
        .then(
            when (variant) {
                HqCardVariant.Status -> Modifier.background(statusTint ?: c.selectedBg)
                else -> Modifier.background(c.surfaceBase).border(1.dp, c.borderSubtle, shape)
            }
        )
    val clickable = if (onClick != null) base.clickable(onClick = onClick) else base
    Column(clickable.padding(padding), content = content)
}
