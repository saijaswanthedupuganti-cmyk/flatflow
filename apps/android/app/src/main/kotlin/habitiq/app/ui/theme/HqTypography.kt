package habitiq.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import habitiq.app.R

/**
 * Bundled Inter (Regular 400, Medium 500, SemiBold 600, Bold 700) per design doc section 6.1.
 * Licensed under the SIL OFL 1.1; the notice ships in assets/licenses/Inter-OFL-1.1.txt.
 */
val HqFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

/**
 * DM Sans variable font (SIL OFL 1.1, assets/licenses/DMSans-OFL-1.1.txt) for headings and the primary
 * balance amount only, per the Figma Make file. Body, labels and controls stay on Inter.
 */
@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
val HqDisplayFontFamily = FontFamily(
    Font(R.font.dm_sans, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

private fun style(
    size: Int,
    line: Int,
    weight: FontWeight,
    tracking: Float = 0f,
    features: String? = null,
    family: FontFamily = HqFontFamily,
) = TextStyle(
    fontFamily = family,
    fontSize = size.sp,
    lineHeight = line.sp,
    fontWeight = weight,
    letterSpacing = tracking.em,
    fontFeatureSettings = features,
)

/**
 * `hq.sys.type.*` (design doc section 6.2). Sizes are sp so the user's font scale is honoured;
 * containers must grow with text rather than clip. No screen sets fontSize/fontWeight directly.
 *
 * Spec-named tokens come first; the v1.x names below them are migration aliases that resolve to
 * the nearest v2 token. The v2 minimum informative size is 12sp (`labelSmall`).
 */
object HqType {
    // ---- hq.sys.type.* ----
    /** Intent title or one genuinely primary block per screen. */
    val display = style(32, 40, FontWeight.Bold, -0.035f, family = HqDisplayFontFamily)
    val titleLarge2 = style(28, 36, FontWeight.Bold, -0.035f, family = HqDisplayFontFamily)
    val titleMedium2 = style(22, 28, FontWeight.Bold, -0.022f, family = HqDisplayFontFamily)
    val titleSmall2 = style(18, 24, FontWeight.Bold, -0.012f, family = HqDisplayFontFamily)
    val bodyLarge = style(16, 24, FontWeight.Normal)
    val bodyMedium = style(14, 20, FontWeight.Normal)
    /** Main buttons. */
    val buttonLabel = style(16, 24, FontWeight.SemiBold)
    /** Row titles (task names, list items): readable and a step below a section heading. */
    val rowTitle = style(16, 24, FontWeight.Medium)
    /** Filters and field labels. */
    val labelMedium = style(14, 20, FontWeight.Medium)
    /** Badges, tab labels, short metadata. Never the only explanation of a critical state. */
    val labelSmall = style(12, 16, FontWeight.Medium, 0.01f)
    /** Invite code. Selectable and copyable. */
    val code = style(16, 24, FontWeight.Medium, 0.04f).copy(fontFamily = FontFamily.Monospace)
    /** Primary balance only, always paired with a direction label. Tabular digits. */
    val amount = style(32, 40, FontWeight.Bold, -0.035f, features = "tnum", family = HqDisplayFontFamily)
    /** Aligned amounts in rows and breakdowns. */
    val amountRow = style(16, 24, FontWeight.SemiBold, features = "tnum")

    // ---- v1.x aliases ----
    val headlineLarge = titleLarge2
    val headlineMedium = titleMedium2
    val headlineSmall = titleSmall2
    val titleLarge = titleSmall2
    val titleMedium = buttonLabel
    val titleSmall = labelMedium
    val bodySmall = bodyMedium
    val labelLarge = labelMedium
    val caption = labelSmall
}

/** Maps [HqType] onto Material3's [Typography] so stock Material components stay in sync with the same scale. */
fun hqMaterialTypography(): Typography = Typography(
    displayLarge = HqType.display,
    displayMedium = HqType.display,
    displaySmall = HqType.titleLarge2,
    headlineLarge = HqType.titleLarge2,
    headlineMedium = HqType.titleMedium2,
    headlineSmall = HqType.titleSmall2,
    titleLarge = HqType.titleSmall2,
    titleMedium = HqType.buttonLabel,
    titleSmall = HqType.labelMedium,
    bodyLarge = HqType.bodyLarge,
    bodyMedium = HqType.bodyMedium,
    bodySmall = HqType.bodyMedium,
    labelLarge = HqType.buttonLabel,
    labelMedium = HqType.labelMedium,
    labelSmall = HqType.labelSmall,
)
