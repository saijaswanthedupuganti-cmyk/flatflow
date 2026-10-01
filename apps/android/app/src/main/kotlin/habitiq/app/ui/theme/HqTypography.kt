package habitiq.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Named type scale (design doc section 9/10). No screen should set `fontSize`/`fontWeight`
 * directly -- use one of these instead.
 *
 * The brand font is Inter (see PRODUCT.md / the design doc), but no Inter .ttf is bundled in
 * this app yet and no third-party font-loading dependency has been approved, so [HqFontFamily]
 * is the platform default (Roboto) for now. It's isolated to this one line specifically so
 * swapping in Inter later (e.g. via the Google Fonts downloadable-fonts provider) is a one-line
 * change, not a find-and-replace across every screen.
 */
val HqFontFamily = FontFamily.Default

object HqType {
    val display = TextStyle(fontFamily = HqFontFamily, fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
    val headlineLarge = TextStyle(fontFamily = HqFontFamily, fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold)
    val headlineMedium = TextStyle(fontFamily = HqFontFamily, fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold)
    val headlineSmall = TextStyle(fontFamily = HqFontFamily, fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold)
    val titleLarge = TextStyle(fontFamily = HqFontFamily, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)
    val titleMedium = TextStyle(fontFamily = HqFontFamily, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium)
    val titleSmall = TextStyle(fontFamily = HqFontFamily, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
    val bodyLarge = TextStyle(fontFamily = HqFontFamily, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal)
    val bodyMedium = TextStyle(fontFamily = HqFontFamily, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal)
    val bodySmall = TextStyle(fontFamily = HqFontFamily, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal)
    val labelLarge = TextStyle(fontFamily = HqFontFamily, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
    val labelMedium = TextStyle(fontFamily = HqFontFamily, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
    val labelSmall = TextStyle(fontFamily = HqFontFamily, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
    val caption = TextStyle(fontFamily = HqFontFamily, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Normal)
}

/** Maps [HqType] onto Material3's [Typography] so stock Material components (and MaterialTheme.typography.*) stay in sync with the same scale. */
fun hqMaterialTypography(): Typography = Typography(
    displayLarge = HqType.display,
    headlineLarge = HqType.headlineLarge,
    headlineMedium = HqType.headlineMedium,
    headlineSmall = HqType.headlineSmall,
    titleLarge = HqType.titleLarge,
    titleMedium = HqType.titleMedium,
    titleSmall = HqType.titleSmall,
    bodyLarge = HqType.bodyLarge,
    bodyMedium = HqType.bodyMedium,
    bodySmall = HqType.bodySmall,
    labelLarge = HqType.labelLarge,
    labelMedium = HqType.labelMedium,
    labelSmall = HqType.labelSmall,
)
