package habitiq.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val MaterialTypography = hqMaterialTypography()

/**
 * Compose/Material role mapping from design doc section 15.1. Every role a stock Material
 * component can read is set deliberately so no default purple or dynamic color leaks in.
 */
private fun ColorScheme.applyHq(hq: HqColorScheme): ColorScheme = copy(
    primary = hq.actionPrimaryBg,
    onPrimary = hq.actionPrimaryFg,
    primaryContainer = hq.selectedBg,
    onPrimaryContainer = hq.selectedFg,
    inversePrimary = hq.actionPrimaryBg,
    secondary = hq.actionPrimaryBg,
    onSecondary = hq.actionPrimaryFg,
    secondaryContainer = hq.selectedBg,
    onSecondaryContainer = hq.selectedFg,
    tertiary = hq.statusInfoFg,
    onTertiary = hq.statusInfoBg,
    tertiaryContainer = hq.statusInfoBg,
    onTertiaryContainer = hq.statusInfoFg,
    background = hq.canvas,
    onBackground = hq.textPrimary,
    surface = hq.surfaceBase,
    onSurface = hq.textPrimary,
    surfaceVariant = hq.surfaceSubtle,
    onSurfaceVariant = hq.textSecondary,
    surfaceTint = Color.Transparent,
    inverseSurface = hq.surfaceInverse,
    inverseOnSurface = hq.textInverse,
    error = hq.actionDangerBg,
    onError = hq.actionDangerFg,
    errorContainer = hq.statusDangerBg,
    onErrorContainer = hq.statusDangerFg,
    outline = hq.borderControl,
    outlineVariant = hq.borderSubtle,
    scrim = Color.Black,
    surfaceBright = hq.surfaceRaised,
    surfaceDim = hq.canvas,
    surfaceContainerLowest = hq.surfaceBase,
    surfaceContainerLow = hq.surfaceBase,
    surfaceContainer = hq.surfaceBase,
    surfaceContainerHigh = hq.surfaceRaised,
    surfaceContainerHighest = hq.surfaceSubtle,
)

private val LightMaterialColors = lightColorScheme().applyHq(HqLightColors)
private val DarkMaterialColors = darkColorScheme().applyHq(HqDarkColors)

/**
 * Root theme wrapper. Light-first launch: the Figma Make file is light only, so the default ignores the OS
 * dark setting until the dark pass ships; then restore `isSystemInDarkTheme()` as the default.
 * There is intentionally no in-app appearance setting (design doc section 1.2).
 * Wallpaper-derived dynamic color is off to preserve the approved contrast pairings.
 */
@Composable
fun HabitiqTheme(dark: Boolean = false, content: @Composable () -> Unit) {
    val hqColors = if (dark) HqDarkColors else HqLightColors
    val materialColors = if (dark) DarkMaterialColors else LightMaterialColors
    CompositionLocalProvider(LocalHqColors provides hqColors) {
        MaterialTheme(colorScheme = materialColors, typography = MaterialTypography, content = content)
    }
}

/**
 * Set to true by a screen whose hero artwork extends under the status bar (Home). The root then skips
 * its status-bar inset and that screen applies the inset itself.
 */
val LocalHeroBleed = androidx.compose.runtime.staticCompositionLocalOf<androidx.compose.runtime.MutableState<Boolean>> {
    androidx.compose.runtime.mutableStateOf(false)
}
