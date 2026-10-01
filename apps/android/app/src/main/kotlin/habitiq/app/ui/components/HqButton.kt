package habitiq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import habitiq.app.ui.theme.hqPressScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Button variants (design doc section 18). Exactly one Primary belongs on any given screen --
 * see section 19 "Button Rule". Destructive never reuses the brand-primary fill (section 18).
 */
enum class HqButtonVariant { Primary, Secondary, Tertiary, Destructive }

private data class HqButtonColors(val background: Color, val pressedBackground: Color, val content: Color, val border: Color?)

@Composable
private fun hqButtonColors(variant: HqButtonVariant): HqButtonColors {
    val c = LocalHqColors.current
    return when (variant) {
        HqButtonVariant.Primary -> HqButtonColors(c.brandPrimary, c.brandPrimaryPressed, c.onBrandPrimary, null)
        HqButtonVariant.Secondary -> HqButtonColors(c.brandPrimaryContainer, c.brandPrimaryContainer, c.brandPrimary, null)
        HqButtonVariant.Tertiary -> HqButtonColors(Color.Transparent, Color.Transparent, c.brandPrimary, null)
        HqButtonVariant.Destructive -> HqButtonColors(c.error, c.error, Color.White, null)
    }
}

/**
 * A single reusable button covering every variant + state (default/pressed/disabled/loading) in
 * the design doc. Don't create a one-off `PurpleButton`/`TaskButton`/etc. -- extend this instead
 * (section 59).
 *
 * While [loading], the button disables interaction and shows a spinner -- pass loading-specific
 * copy (e.g. "Creating…") via [text] yourself; this component doesn't rewrite your label.
 */
@Composable
fun HqButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: HqButtonVariant = HqButtonVariant.Primary,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val colors = hqButtonColors(variant)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val isEnabled = enabled && !loading

    val background = when {
        !isEnabled -> colors.background.copy(alpha = 0.4f)
        pressed -> colors.pressedBackground
        else -> colors.background
    }
    val contentColor = if (isEnabled) colors.content else colors.content.copy(alpha = 0.5f)

    Box(
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier)
            .hqPressScale(pressed && isEnabled)
            .clip(RoundedCornerShape(HqRadius.lg))
            .let { m -> if (colors.border != null) m.border(1.dp, colors.border, RoundedCornerShape(HqRadius.lg)) else m }
            .background(background)
            .defaultMinSize(minHeight = habitiq.app.ui.theme.HqTouchTarget)
            .clickable(
                enabled = isEnabled,
                interactionSource = interactionSource,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(PaddingValues(vertical = HqSpacing.md, horizontal = HqSpacing.lg)),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = contentColor,
                )
                Box(Modifier.size(HqSpacing.sm))
            } else if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = contentColor, modifier = Modifier.size(HqIconSize.sm))
                Box(Modifier.size(HqSpacing.sm))
            }
            Text(text, style = HqType.titleMedium, color = contentColor)
        }
    }
}

/**
 * Low-emphasis text-only action ("Learn more", "Skip", "Edit") -- design doc section 18.
 * [color] defaults to the brand tint; pass [LocalHqColors]'s `textSecondary` for a neutral
 * low-emphasis action (e.g. "Sign out" -- reversible, so it doesn't need the weight of a full
 * [HqButtonVariant.Destructive] button) or `error` for a destructive *text* action.
 */
@Composable
fun HqTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, color: Color? = null) {
    val c = LocalHqColors.current
    val resolved = color ?: c.brandPrimary
    Text(
        text,
        style = HqType.labelLarge,
        color = if (enabled) resolved else c.textDisabled,
        modifier = modifier
            .defaultMinSize(minHeight = habitiq.app.ui.theme.HqTouchTarget)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = HqSpacing.sm, vertical = HqSpacing.sm),
    )
}
