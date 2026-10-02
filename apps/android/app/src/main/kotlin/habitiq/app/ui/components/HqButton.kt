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
import androidx.compose.ui.draw.shadow
import habitiq.app.ui.theme.hqPressScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Button variants (design doc section 18). Exactly one Primary belongs on any given screen --
 * see section 19 "Button Rule". Destructive never reuses the brand-primary fill (section 18).
 */
enum class HqButtonVariant { Primary, Secondary, Tertiary, Destructive }

private data class HqButtonColors(
    val background: Color,
    val pressedBackground: Color,
    val content: Color,
    val border: Color?,
    val disabledBackground: Color,
    val disabledContent: Color,
    val disabledBorder: Color?,
)

@Composable
private fun hqButtonColors(variant: HqButtonVariant): HqButtonColors {
    val c = LocalHqColors.current
    return when (variant) {
        HqButtonVariant.Primary -> HqButtonColors(c.actionPrimaryBg, c.actionPrimaryPressed, c.actionPrimaryFg, null, c.disabledBg, c.disabledFg, null)
        HqButtonVariant.Secondary -> HqButtonColors(c.actionSecondaryBg, c.actionSecondaryPressed, c.actionSecondaryFg, c.borderSubtle, c.disabledBg, c.disabledFg, c.disabledBorder)
        HqButtonVariant.Tertiary -> HqButtonColors(Color.Transparent, c.actionTertiaryPressed, c.actionTertiaryFg, null, Color.Transparent, c.disabledFg, null)
        HqButtonVariant.Destructive -> HqButtonColors(c.actionDangerBg, c.actionDangerPressed, c.actionDangerFg, null, c.disabledBg, c.disabledFg, null)
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
    trailingIcon: ImageVector? = null,
) {
    val colors = hqButtonColors(variant)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val isEnabled = enabled && !loading

    // A loading button keeps its enabled colours (only interaction is blocked) so the label stays readable.
    val background = when {
        !enabled -> colors.disabledBackground
        pressed && !loading -> colors.pressedBackground
        else -> colors.background
    }
    val contentColor = if (enabled) colors.content else colors.disabledContent
    val borderColor = if (enabled) colors.border else colors.disabledBorder
    val shape = RoundedCornerShape(HqRadius.button)

    Box(
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier)
            .hqPressScale(pressed && isEnabled)
            .let { m ->
                // Figma primary button: soft teal lift (0 8px 20px rgba(20,184,166,.2)).
                if (variant == HqButtonVariant.Primary && isEnabled) {
                    m.shadow(8.dp, shape, ambientColor = colors.background.copy(alpha = .2f), spotColor = colors.background.copy(alpha = .2f))
                } else m
            }
            .clip(shape)
            .background(background)
            .let { m -> if (borderColor != null) m.border(1.dp, borderColor, shape) else m }
            .defaultMinSize(minHeight = HqSize.button, minWidth = HqSize.target)
            .clickable(
                enabled = isEnabled,
                interactionSource = interactionSource,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(PaddingValues(vertical = HqSpacing.md, horizontal = HqSpacing.xl)),
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
            Text(text, style = HqType.buttonLabel, color = contentColor)
            if (trailingIcon != null && !loading) {
                Box(Modifier.size(HqSpacing.sm))
                Icon(trailingIcon, contentDescription = null, tint = contentColor, modifier = Modifier.size(HqIconSize.sm))
            }
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
    val resolved = color ?: c.actionTertiaryFg
    // The label is centred inside the full 48dp target, so it lines up with neighbouring text.
    Box(
        modifier
            .defaultMinSize(minHeight = HqSize.target, minWidth = HqSize.target)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = HqSpacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = HqType.labelMedium, color = if (enabled) resolved else c.disabledFg)
    }
}
