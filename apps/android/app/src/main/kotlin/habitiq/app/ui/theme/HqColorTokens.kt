package habitiq.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Primitive layer (`hq.ref.color.*`): palette C "Navy and teal" (owner decision, Oct 2026).
 * Teal is the only "press me" colour; navy is the brand surface; blue is links/info only; status
 * colours are always paired with text or an icon. Only the theme definitions below may reference
 * these; screens and components consume [HqColorScheme] through [LocalHqColors] or MaterialTheme.
 */
private object HqRef {
    val white = Color(0xFFFFFFFF)

    // Cool, navy-tinted neutrals (light theme).
    val neutral25 = Color(0xFFF5F7FA)
    val neutral50 = Color(0xFFEEF2F6)
    val neutral100 = Color(0xFFE6EBF1)
    val neutral150 = Color(0xFFE1E6EE)
    val neutral200 = Color(0xFFD5DDE7)
    val neutral300 = Color(0xFFB8C3D1)
    /** Control boundary: 3.3:1+ on white, canvas and subtle (the 94A3B8 first proposed was 2.56:1). */
    val neutral400 = Color(0xFF7A8699)
    /** Muted text: 5:1+ on every light surface. */
    val neutral500 = Color(0xFF5F6B7A)
    val neutral600 = Color(0xFF5A6578)
    val neutral700 = Color(0xFF3A4556)
    val neutral800 = Color(0xFF232E3F)
    val neutral900 = Color(0xFF141C2B)

    // Dark theme (defined for a future release; this release renders light only).
    val night950 = Color(0xFF0B1220)
    val night900 = Color(0xFF131C2C)
    val night850 = Color(0xFF182233)
    val night800 = Color(0xFF1A2538)
    val night750 = Color(0xFF212C3F)
    val night500 = Color(0xFF5B6B82)
    val night300 = Color(0xFF93A0B5)
    val night50 = Color(0xFFE5EAF3)

    val teal50 = Color(0xFFE6F4F1)
    val teal100 = Color(0xFFDCEFEB)
    val teal300 = Color(0xFF5EEAD4)
    val teal400 = Color(0xFF2DD4BF)
    val teal700 = Color(0xFF0F766E)
    val teal800 = Color(0xFF115E59)
    val teal900 = Color(0xFF134E4A)
    val teal950 = Color(0xFF0F2E2B)
    val teal990 = Color(0xFF062420)

    val navy = Color(0xFF1C2A40)
    val navyNight = Color(0xFF1A2840)
    val navyOnNight = Color(0xFFE8EEF8)

    val blue50 = Color(0xFFEEF4FF)
    val blue400 = Color(0xFF60A5FA)
    val blue600 = Color(0xFF2563EB)

    val red50 = Color(0xFFFFF0F3)
    val red300 = Color(0xFFFFB3C1)
    val red700 = Color(0xFFB4233C)
    val red800 = Color(0xFF8F1C30)
    val red950 = Color(0xFF381C26)

    val amber50 = Color(0xFFFFF5DC)
    val amber300 = Color(0xFFFFD27A)
    val amber700 = Color(0xFF8A5100)
    val amber950 = Color(0xFF352B18)

    val green50 = Color(0xFFEAF6ED)
    val green300 = Color(0xFF8FD8A7)
    val green700 = Color(0xFF21643B)
    val green950 = Color(0xFF192E22)
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

    // ---- Component roles (added for the component system). Each defaults to an existing
    // ---- semantic value, so a palette change only edits values, never call sites.
    /** Switch / checkbox / radio "on" fill. 3:1+ against the canvas. */
    val controlOn: Color = actionPrimaryBg,
    /** Mark drawn on [controlOn] (switch thumb, checkbox tick). */
    val onControlOn: Color = actionPrimaryFg,
    /** Switch track / unchecked box outline when off. */
    val controlOff: Color = borderControl,
    /** Switch thumb when off. */
    val controlThumbOff: Color = if (isDark) textPrimary else surfaceBase,
    /** Pressed state-layer ink; components apply it at a low alpha over their fill. */
    val stateLayer: Color = textPrimary,
    /** State-layer ink over strongly filled (primary / danger) buttons. */
    val stateLayerOnFill: Color = if (isDark) surfaceBase else textPrimary,
    /** Fill of the selected segment in a segmented control. */
    val segmentSelectedBg: Color = if (isDark) surfaceRaised else surfaceBase,
    /** Track of a segmented control. */
    val segmentTrack: Color = if (isDark) surfaceBase else surfaceSubtle,
    /** Snackbar surface, its text and its action. */
    val snackbarBg: Color = surfaceInverse,
    val snackbarFg: Color = textInverse,
    val snackbarAction: Color = textInverse,
    /** Sheet / dialog scrim base colour (alpha applied by the component). */
    val scrim: Color = if (isDark) Color.Black else surfaceInverse,
    /** Ghost button on photos / navy hero: label + outline. */
    val onImage: Color = Color.White,
    /** Navy brand surface: summary/hero cards, dark headers, voice overlay base. Never a press-me colour. */
    val brandInk: Color = surfaceInverse,
    val onBrandInk: Color = textInverse,
    /** Links and informational emphasis only (never an action fill). */
    val link: Color = statusInfoFg,
)

val HqLightColors = HqColorScheme(
    isDark = false,
    canvas = HqRef.neutral25,
    surfaceBase = HqRef.white,
    surfaceRaised = HqRef.white,
    surfaceSubtle = HqRef.neutral50,
    surfaceInverse = HqRef.navy,
    textPrimary = HqRef.neutral900,
    textSecondary = HqRef.neutral600,
    textMuted = HqRef.neutral500,
    textInverse = HqRef.white,
    textBrand = HqRef.teal700,
    iconDefault = HqRef.neutral600,
    borderSubtle = HqRef.neutral150,
    borderControl = HqRef.neutral400,
    focus = HqRef.teal700,
    actionPrimaryBg = HqRef.teal700,
    actionPrimaryFg = HqRef.white,
    actionPrimaryHover = HqRef.teal800,
    actionPrimaryPressed = HqRef.teal800,
    actionSecondaryBg = HqRef.teal50,
    actionSecondaryFg = HqRef.teal700,
    actionSecondaryHover = HqRef.teal100,
    actionSecondaryPressed = HqRef.teal100,
    actionTertiaryFg = HqRef.teal700,
    actionTertiaryHover = HqRef.teal50,
    actionTertiaryPressed = HqRef.teal50,
    actionDangerBg = HqRef.red700,
    actionDangerFg = HqRef.white,
    actionDangerHover = HqRef.red800,
    actionDangerPressed = HqRef.red800,
    disabledBg = HqRef.neutral100,
    disabledFg = HqRef.neutral600,
    disabledBorder = HqRef.neutral150,
    selectedBg = HqRef.teal50,
    selectedFg = HqRef.teal700,
    selectedBorder = HqRef.teal700,
    // Coral is retired from UI roles: "warm" notes read as danger-soft (overdue uses danger).
    warmBg = HqRef.red50,
    warmFg = HqRef.red700,
    brandTeal = HqRef.teal700,
    brandCoral = HqRef.red700,
    statusInfoBg = HqRef.blue50,
    statusInfoFg = HqRef.blue600,
    statusSuccessBg = HqRef.green50,
    statusSuccessFg = HqRef.green700,
    statusWarningBg = HqRef.amber50,
    statusWarningFg = HqRef.amber700,
    statusDangerBg = HqRef.red50,
    statusDangerFg = HqRef.red700,
    chart1 = HqRef.teal700,
    chart2 = HqRef.blue600,
    chart3 = HqRef.navy,
    chart4 = HqRef.amber700,
    snackbarAction = HqRef.teal300,
    brandInk = HqRef.navy,
    onBrandInk = HqRef.white,
    link = HqRef.blue600,
)

/** Palette C dark. Defined for a future release; HabitiqTheme renders light only in this release. */
val HqDarkColors = HqColorScheme(
    isDark = true,
    canvas = HqRef.night950,
    surfaceBase = HqRef.night900,
    surfaceRaised = HqRef.night800,
    surfaceSubtle = HqRef.night850,
    surfaceInverse = HqRef.night50,
    textPrimary = HqRef.night50,
    textSecondary = HqRef.night300,
    textMuted = HqRef.night300,
    textInverse = HqRef.night900,
    textBrand = HqRef.teal300,
    iconDefault = HqRef.night300,
    borderSubtle = HqRef.night750,
    borderControl = HqRef.night500,
    focus = HqRef.teal400,
    actionPrimaryBg = HqRef.teal400,
    actionPrimaryFg = HqRef.teal990,
    actionPrimaryHover = HqRef.teal300,
    actionPrimaryPressed = HqRef.teal300,
    actionSecondaryBg = HqRef.teal950,
    actionSecondaryFg = HqRef.teal300,
    actionSecondaryHover = HqRef.teal900,
    actionSecondaryPressed = HqRef.teal900,
    actionTertiaryFg = HqRef.teal300,
    actionTertiaryHover = HqRef.teal950,
    actionTertiaryPressed = HqRef.teal950,
    actionDangerBg = HqRef.red300,
    actionDangerFg = HqRef.red950,
    actionDangerHover = HqRef.red50,
    actionDangerPressed = HqRef.red300,
    disabledBg = HqRef.night750,
    disabledFg = HqRef.night300,
    disabledBorder = HqRef.night750,
    selectedBg = HqRef.teal950,
    selectedFg = HqRef.teal300,
    selectedBorder = HqRef.teal400,
    warmBg = HqRef.red950,
    warmFg = HqRef.red300,
    brandTeal = HqRef.teal400,
    brandCoral = HqRef.red300,
    statusInfoBg = HqRef.navyNight,
    statusInfoFg = HqRef.blue400,
    statusSuccessBg = HqRef.green950,
    statusSuccessFg = HqRef.green300,
    statusWarningBg = HqRef.amber950,
    statusWarningFg = HqRef.amber300,
    statusDangerBg = HqRef.red950,
    statusDangerFg = HqRef.red300,
    chart1 = HqRef.teal400,
    chart2 = HqRef.blue400,
    chart3 = HqRef.navyOnNight,
    chart4 = HqRef.amber300,
    snackbarAction = HqRef.teal700,
    brandInk = HqRef.navyNight,
    onBrandInk = HqRef.navyOnNight,
    link = HqRef.blue400,
)

val LocalHqColors = staticCompositionLocalOf { HqLightColors }
