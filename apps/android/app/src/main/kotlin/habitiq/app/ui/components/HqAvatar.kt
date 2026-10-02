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
import habitiq.app.ui.theme.HqFontFamily
import habitiq.app.ui.theme.LocalHqColors

/** Avatar size tokens (design doc section 48). */
enum class HqAvatarSize(val diameter: Dp, val fontSize: androidx.compose.ui.unit.TextUnit) {
    XS(24.dp, 10.sp),
    SM(32.dp, 12.sp),
    MD(40.dp, 14.sp),
    LG(56.dp, 18.sp),
    PROFILE(64.dp, 22.sp),
    XL(80.dp, 24.sp),
}

private val HqSand = androidx.compose.ui.graphics.Color(0xFFF3EADC)
private val HqSandInk = androidx.compose.ui.graphics.Color(0xFF7D613B)
private val HqCoralInk = androidx.compose.ui.graphics.Color(0xFFB84C40)

/** Figma avatar tones: teal (default), sand and coral initials on a soft fill. */
enum class HqAvatarTone { Teal, Sand, Coral }

/** First-initial fallback, matching FigmaPrimitives.memberInitials's behavior. */
fun hqInitials(name: String): String =
    name.trim().split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("").ifEmpty { "?" }

/**
 * Initials avatar in the shared selected-tint pair (design doc section 10.12). Pass
 * [imageContent] to render a real photo instead -- the initials fallback still needs to exist
 * for members without one.
 */
@Composable
fun HqAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: HqAvatarSize = HqAvatarSize.MD,
    tone: HqAvatarTone = HqAvatarTone.Teal,
    imageContent: (@Composable () -> Unit)? = null,
) {
    val c = LocalHqColors.current
    if (imageContent != null) {
        Box(modifier.size(size.diameter).clip(CircleShape)) { imageContent() }
        return
    }
    val (bg, fg) = when (tone) {
        HqAvatarTone.Teal -> c.selectedBg to c.selectedFg
        HqAvatarTone.Sand -> HqSand to HqSandInk
        HqAvatarTone.Coral -> c.warmBg to HqCoralInk
    }
    Box(
        modifier.size(size.diameter).clip(CircleShape).background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(hqInitials(name), color = fg, fontSize = size.fontSize, fontWeight = FontWeight.Bold, fontFamily = HqFontFamily)
    }
}
