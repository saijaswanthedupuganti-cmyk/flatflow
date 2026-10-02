package habitiq.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Primitive layer (`hq.ref.color.*`) from docs/design-system/MASTER.md v2.0.0 section 4.2.
 * Only the theme definitions below may reference these; screens and components consume
 * [HqColorScheme] through [LocalHqColors] or MaterialTheme.
 */
private object HqRef {
    val neutral0 = Color(0xFFFFFFFF)
    val neutral25 = Color(0xFFF7F8F5)
    val neutral50 = Color(0xFFF2F7F4)
    val neutral100 = Color(0xFFEEF2EF)
    val neutral200 = Color(0xFFD7DFD9)
    val neutral300 = Color(0xFFC0CDC6)
    val neutral400 = Color(0xFFA3B3AE)
    val neutral500 = Color(0xFF788781)
    val neutral550 = Color(0xFF788B83)
    val neutral600 = Color(0xFF606D67)
    val neutral650 = Color(0xFF52605D)
    val neutral700 = Color(0xFF34473E)
    val neutral750 = Color(0xFF25332F)
    val neutral800 = Color(0xFF202D28)
    val neutral850 = Color(0xFF18231F)
    val neutral900 = Color(0xFF10201E)
    val neutral950 = Color(0xFF101815)

    val teal50 = Color(0xFFF0FDFA)
    val teal100 = Color(0xFFDDF5EE)
    val teal200 = Color(0xFF99F6E4)
    val teal300 = Color(0xFF5EEAD4)
    val teal400 = Color(0xFF2DD4BF)
    val teal500 = Color(0xFF14B8A6)
    val teal700 = Color(0xFF0F766E)
    val teal800 = Color(0xFF115E59)
    val teal900 = Color(0xFF134E4A)
    val teal950 = Color(0xFF153B33)

    val coral50 = Color(0xFFFFF1EE)
    val coral300 = Color(0xFFFFA89C)
    val coral500 = Color(0xFFFF6B5A)
    val coral700 = Color(0xFFA9392B)
    val coral950 = Color(0xFF3B2321)

    val red50 = Color(0xFFFFF0F3)
    val red300 = Color(0xFFFFB3C1)
    val red700 = Color(0xFFB4233C)
    val red800 = Color(0xFF8F1C30)
    val red900 = Color(0xFF741A29)
    val red950 = Color(0xFF381C26)

    val amber50 = Color(0xFFFFF5DC)
    val amber300 = Color(0xFFFFD27A)
    val amber700 = Color(0xFF8A5100)
    val amber950 = Color(0xFF352B18)

    val green50 = Color(0xFFEAF6ED)
    val green300 = Color(0xFF8FD8A7)
    val green700 = Color(0xFF21643B)
    val green950 = Color(0xFF192E22)

    val blue50 = Color(0xFFEEF4FF)
    val blue300 = Color(0xFFA8CAFF)
    val blue700 = Color(0xFF245BA8)
    val blue950 = Color(0xFF1C2A40)

    val violet300 = Color(0xFFCAB5EF)
    val violet700 = Color(0xFF7652A5)
    val ochre700 = Color(0xFF946000)

    // Figma Make file XvM5qZJzfLnSvHyp0osNjZ (light theme source of truth, src/index.css :root)
    val figBrand = Color(0xFF14B8A6)
    val figBrandDark = Color(0xFF087F73)
    val figBrandSoft = Color(0xFFE8F8F5)
    val figOnBrand = Color(0xFF062F2A)
    val figCoral = Color(0xFFFF6B5A)
    val figCoralSoft = Color(0xFFFFF0ED)
    val figInk = Color(0xFF19312F)
    val figInk2 = Color(0xFF536663)
    val figInk3 = Color(0xFF82918F)
    /** Figma ink-3 is 3.28:1 on white; used for text this darker step keeps the look and meets AA (4.5:1). */
    val figInk3Text = Color(0xFF5F6F6B)
    val figLine = Color(0xFFE3E9E7)
    val figSurface2 = Color(0xFFF5F7F6)
    val figSand = Color(0xFFF3EADC)
    /** Figma warning #BA6F29 is 3.61:1 on its soft fill; this step meets AA. */
    val figWarning = Color(0xFF9A5A1C)
    val figWarningSoft = Color(0xFFFFF5E8)
    /** Figma danger #C4524A is below AA on its soft fill; this step meets AA. */
    val figDanger = Color(0xFFB24139)
    /** Figma success #25856E is 4.12:1 on its soft fill; this step keeps the hue and meets AA. */
    val figSuccess = Color(0xFF1E7560)
}

/**
 * Semantic layer (`hq.sys.color.*`). Both themes expose exactly the same keys.
 *
 * The v1.x alias properties were removed once every consumer read the v2 names (design doc 16.2).
 */
data class HqColorScheme(
    val isDark: Boolean,

    val canvas: Color,
    val surfaceBase: Color,
    val surfaceRaised: Color,
    val surfaceSubtle: Color,
    val surfaceInverse: Color,

    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textInverse: Color,
    val textBrand: Color,
    val iconDefault: Color,

    val borderSubtle: Color,
    val borderControl: Color,
    val focus: Color,

    val actionPrimaryBg: Color,
    val actionPrimaryFg: Color,
    val actionPrimaryHover: Color,
    val actionPrimaryPressed: Color,
    val actionSecondaryBg: Color,
    val actionSecondaryFg: Color,
    val actionSecondaryHover: Color,
    val actionSecondaryPressed: Color,
    val actionTertiaryFg: Color,
    val actionTertiaryHover: Color,
    val actionTertiaryPressed: Color,
    val actionDangerBg: Color,
    val actionDangerFg: Color,
    val actionDangerHover: Color,
    val actionDangerPressed: Color,

    val disabledBg: Color,
    val disabledFg: Color,
    val disabledBorder: Color,

    val selectedBg: Color,
    val selectedFg: Color,
    val selectedBorder: Color,

    val warmBg: Color,
    val warmFg: Color,
    val brandTeal: Color,
    val brandCoral: Color,

    val statusInfoBg: Color,
    val statusInfoFg: Color,
    val statusSuccessBg: Color,
    val statusSuccessFg: Color,
    val statusWarningBg: Color,
    val statusWarningFg: Color,
    val statusDangerBg: Color,
    val statusDangerFg: Color,

    val chart1: Color,
    val chart2: Color,
    val chart3: Color,
    val chart4: Color,
) {
}

val HqLightColors = HqColorScheme(
    isDark = false,
    canvas = HqRef.neutral0,
    surfaceBase = HqRef.neutral0,
    surfaceRaised = HqRef.neutral0,
    surfaceSubtle = HqRef.figSurface2,
    surfaceInverse = HqRef.figInk,
    textPrimary = HqRef.figInk,
    textSecondary = HqRef.figInk2,
    textMuted = HqRef.figInk3Text,
    textInverse = HqRef.neutral0,
    textBrand = HqRef.figBrandDark,
    iconDefault = HqRef.figInk2,
    borderSubtle = HqRef.figLine,
    borderControl = HqRef.figInk3,
    focus = HqRef.figBrandDark,
    actionPrimaryBg = HqRef.figBrand,
    actionPrimaryFg = HqRef.figOnBrand,
    actionPrimaryHover = HqRef.teal400,
    actionPrimaryPressed = HqRef.teal300,
    actionSecondaryBg = HqRef.neutral0,
    actionSecondaryFg = HqRef.figInk,
    actionSecondaryHover = HqRef.figSurface2,
    actionSecondaryPressed = HqRef.figLine,
    actionTertiaryFg = HqRef.figBrandDark,
    actionTertiaryHover = HqRef.figBrandSoft,
    actionTertiaryPressed = HqRef.figBrandSoft,
    actionDangerBg = HqRef.figDanger,
    actionDangerFg = HqRef.neutral0,
    actionDangerHover = HqRef.red700,
    actionDangerPressed = HqRef.red800,
    disabledBg = Color(0xFFE9EEEC),
    disabledFg = HqRef.figInk3Text,
    disabledBorder = HqRef.figLine,
    selectedBg = HqRef.figBrandSoft,
    selectedFg = HqRef.teal700,
    selectedBorder = HqRef.figBrandDark,
    warmBg = HqRef.figCoralSoft,
    warmFg = HqRef.coral700,
    brandTeal = HqRef.figBrand,
    brandCoral = HqRef.figCoral,
    statusInfoBg = HqRef.blue50,
    statusInfoFg = HqRef.blue700,
    statusSuccessBg = HqRef.figBrandSoft,
    statusSuccessFg = HqRef.figSuccess,
    statusWarningBg = HqRef.figWarningSoft,
    statusWarningFg = HqRef.figWarning,
    statusDangerBg = HqRef.figCoralSoft,
    statusDangerFg = HqRef.figDanger,
    chart1 = HqRef.figBrand,
    chart2 = HqRef.blue700,
    chart3 = HqRef.figCoral,
    chart4 = HqRef.figWarning,
)

val HqDarkColors = HqColorScheme(
    isDark = true,
    canvas = HqRef.neutral950,
    surfaceBase = HqRef.neutral850,
    surfaceRaised = HqRef.neutral800,
    surfaceSubtle = HqRef.neutral750,
    surfaceInverse = HqRef.neutral50,
    textPrimary = HqRef.neutral50,
    textSecondary = HqRef.neutral300,
    textMuted = HqRef.neutral400,
    textInverse = HqRef.neutral850,
    textBrand = HqRef.teal300,
    iconDefault = HqRef.neutral300,
    borderSubtle = HqRef.neutral700,
    borderControl = HqRef.neutral550,
    focus = HqRef.teal300,
    actionPrimaryBg = HqRef.teal300,
    actionPrimaryFg = HqRef.neutral900,
    actionPrimaryHover = HqRef.teal200,
    actionPrimaryPressed = HqRef.teal400,
    actionSecondaryBg = HqRef.neutral850,
    actionSecondaryFg = HqRef.teal300,
    actionSecondaryHover = HqRef.teal950,
    actionSecondaryPressed = HqRef.neutral750,
    actionTertiaryFg = HqRef.teal300,
    actionTertiaryHover = HqRef.teal950,
    actionTertiaryPressed = HqRef.neutral750,
    actionDangerBg = HqRef.red300,
    actionDangerFg = HqRef.red950,
    actionDangerHover = HqRef.red50,
    actionDangerPressed = HqRef.red300,
    disabledBg = HqRef.neutral750,
    disabledFg = HqRef.neutral400,
    disabledBorder = HqRef.neutral700,
    selectedBg = HqRef.teal950,
    selectedFg = HqRef.teal200,
    selectedBorder = HqRef.teal300,
    warmBg = HqRef.coral950,
    warmFg = HqRef.coral300,
    brandTeal = HqRef.teal500,
    brandCoral = HqRef.coral500,
    statusInfoBg = HqRef.blue950,
    statusInfoFg = HqRef.blue300,
    statusSuccessBg = HqRef.green950,
    statusSuccessFg = HqRef.green300,
    statusWarningBg = HqRef.amber950,
    statusWarningFg = HqRef.amber300,
    statusDangerBg = HqRef.red950,
    statusDangerFg = HqRef.red300,
    chart1 = HqRef.teal300,
    chart2 = HqRef.blue300,
    chart3 = HqRef.violet300,
    chart4 = HqRef.amber300,
)

val LocalHqColors = staticCompositionLocalOf { HqLightColors }
