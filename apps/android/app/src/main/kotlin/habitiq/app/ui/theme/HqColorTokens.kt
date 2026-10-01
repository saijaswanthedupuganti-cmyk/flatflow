package habitiq.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

/** The single semantic color contract for every Habitiq surface. */
data class HqColorScheme(
    val brandPrimary: Color,
    val brandPrimaryHover: Color,
    val brandPrimaryPressed: Color,
    val brandPrimaryDark: Color,
    val brandPrimaryContainer: Color,
    val brandPrimarySubtle: Color,
    val onBrandPrimary: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceSubtle: Color,
    val surfaceBrand: Color,
    val surfaceDisabled: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val textOnBrand: Color,
    val borderDefault: Color,
    val borderStrong: Color,
    val borderFocus: Color,
    val borderDisabled: Color,
    val success: Color,
    val successContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val info: Color,
    val infoContainer: Color,
)

private fun statusContainer(color: Color): Color = color.copy(alpha = 0.08f).compositeOver(Color.White)

val HqLightColors = HqColorScheme(
    brandPrimary = Color(0xFF0F766E),
    brandPrimaryHover = Color(0xFF0D6B64),
    brandPrimaryPressed = Color(0xFF115E59),
    brandPrimaryDark = Color(0xFF134E4A),
    brandPrimaryContainer = Color(0xFFCCFBF1),
    brandPrimarySubtle = Color(0xFFF0FDFA),
    onBrandPrimary = Color.White,
    background = Color(0xFFF7FAF9),
    surface = Color.White,
    surfaceElevated = Color.White,
    surfaceSubtle = Color(0xFFF3F7F6),
    surfaceBrand = Color(0xFFF0FDFA),
    surfaceDisabled = Color(0xFFF3F7F6),
    textPrimary = Color(0xFF111C1B),
    textSecondary = Color(0xFF52615E),
    textTertiary = Color(0xFF6B7A77),
    textDisabled = Color(0xFFAEB9B6),
    textOnBrand = Color.White,
    borderDefault = Color(0xFFE7EFED),
    borderStrong = Color(0xFFD8E3E0),
    borderFocus = Color(0xFF0F766E),
    borderDisabled = Color(0xFFE7EFED),
    success = Color(0xFF15803D),
    successContainer = statusContainer(Color(0xFF15803D)),
    warning = Color(0xFFB45309),
    warningContainer = statusContainer(Color(0xFFB45309)),
    error = Color(0xFFB91C1C),
    errorContainer = statusContainer(Color(0xFFB91C1C)),
    info = Color(0xFF0369A1),
    infoContainer = statusContainer(Color(0xFF0369A1)),
)

// A separate dark brand system is not approved. Keep legacy dark callers on the unified light system.
val HqDarkColors: HqColorScheme = HqLightColors

val LocalHqColors = staticCompositionLocalOf { HqLightColors }
