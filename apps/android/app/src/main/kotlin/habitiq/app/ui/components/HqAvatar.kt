package habitiq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.ui.theme.HqColorScheme
import habitiq.app.ui.theme.LocalHqColors

/** Avatar size tokens (design doc section 48). */
enum class HqAvatarSize(val diameter: Dp, val fontSize: androidx.compose.ui.unit.TextUnit) {
    XS(24.dp, 10.sp),
    SM(32.dp, 12.sp),
    MD(40.dp, 14.sp),
    LG(56.dp, 18.sp),
    XL(80.dp, 24.sp),
}

/** First-initial fallback, matching FigmaPrimitives.memberInitials's behavior. */
fun hqInitials(name: String): String =
    name.trim().split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("").ifEmpty { "?" }

/**
 * Deterministic background/text pair for a name's initials avatar (design doc section 48: "Do
 * not generate random avatar colors every render"). Picks from the semantic container/accent
 * tokens instead of inventing new literals.
 */
private fun hqAvatarPalette(c: HqColorScheme) = listOf(
    c.brandPrimaryContainer to c.brandPrimary,
    c.successContainer to c.success,
    c.warningContainer to c.warning,
    c.infoContainer to c.info,
    c.errorContainer to c.error,
)

/**
 * Initials avatar with a deterministic color derived from [name] (design doc section 48). Pass
 * [imageContent] to render a real photo instead -- the initials fallback still needs to exist
 * for members without one.
 */
@Composable
fun HqAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: HqAvatarSize = HqAvatarSize.MD,
    imageContent: (@Composable () -> Unit)? = null,
) {
    val c = LocalHqColors.current
    if (imageContent != null) {
        Box(modifier.size(size.diameter).clip(CircleShape)) { imageContent() }
        return
    }
    val palette = hqAvatarPalette(c)
    val index = if (name.isBlank()) 0 else Math.floorMod(name.trim().lowercase().hashCode(), palette.size)
    val (background, textColor) = palette[index]
    Box(
        modifier.size(size.diameter).clip(CircleShape).background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(hqInitials(name), color = textColor, fontSize = size.fontSize, fontWeight = FontWeight.SemiBold)
    }
}
