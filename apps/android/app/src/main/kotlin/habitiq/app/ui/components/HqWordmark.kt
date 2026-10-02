package habitiq.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import habitiq.app.R
import habitiq.app.ui.theme.LocalHqColors

/**
 * The approved oddroof lockup (house mark + wordmark) from the Figma Make file. The asset is a
 * single-colour transparent PNG, so dark mode tints it to the primary text colour rather than
 * swapping artwork. Never stretch: only the height is set and the aspect ratio is preserved.
 */
@Composable
fun HqWordmark(modifier: Modifier = Modifier, height: Dp = 36.dp, tint: androidx.compose.ui.graphics.Color? = null) {
    val c = LocalHqColors.current
    Image(
        painter = painterResource(R.drawable.oddroof_logo),
        contentDescription = "Oddroof",
        modifier = modifier.height(height),
        colorFilter = when {
            tint != null -> ColorFilter.tint(tint)
            isSystemInDarkTheme() -> ColorFilter.tint(c.textPrimary)
            else -> null
        },
    )
}
