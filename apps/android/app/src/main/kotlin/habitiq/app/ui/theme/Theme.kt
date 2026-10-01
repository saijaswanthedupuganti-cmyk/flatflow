package habitiq.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val MaterialTypography = hqMaterialTypography()

private fun ColorScheme.applyHq(hq: HqColorScheme): ColorScheme = copy(
    primary = hq.brandPrimary,
    onPrimary = hq.onBrandPrimary,
    primaryContainer = hq.brandPrimaryContainer,
    onPrimaryContainer = hq.brandPrimaryDark,
    secondary = hq.brandPrimary,
    onSecondary = hq.onBrandPrimary,
    secondaryContainer = hq.brandPrimarySubtle,
    onSecondaryContainer = hq.brandPrimaryDark,
    tertiary = hq.info,
    onTertiary = hq.onBrandPrimary,
    tertiaryContainer = hq.infoContainer,
    onTertiaryContainer = hq.textPrimary,
    background = hq.background,
    onBackground = hq.textPrimary,
    surface = hq.surface,
    onSurface = hq.textPrimary,
    surfaceVariant = hq.surfaceSubtle,
    onSurfaceVariant = hq.textSecondary,
    outline = hq.borderDefault,
    outlineVariant = hq.borderStrong,
    error = hq.error,
    onError = hq.onBrandPrimary,
    errorContainer = hq.errorContainer,
    onErrorContainer = hq.textPrimary,
    surfaceTint = hq.brandPrimary,
    inverseSurface = hq.textPrimary,
    inverseOnSurface = hq.surface,
    inversePrimary = hq.brandPrimaryContainer,
)

private val LightMaterialColors = lightColorScheme().applyHq(HqLightColors)
private val DarkMaterialColors = darkColorScheme().applyHq(HqDarkColors)

/**
 * Root theme wrapper. The product currently has one approved light palette. [dark] remains in
 * the signature for source compatibility while both paths intentionally resolve to that palette.
 */
@Composable
fun HabitiqTheme(dark: Boolean = false, content: @Composable () -> Unit) {
    val hqColors = HqLightColors
    val materialColors = LightMaterialColors
    CompositionLocalProvider(LocalHqColors provides hqColors) {
        MaterialTheme(colorScheme = materialColors, typography = MaterialTypography, content = content)
    }
}
