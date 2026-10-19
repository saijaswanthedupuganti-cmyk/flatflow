package habitiq.app.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Base spacing scale (design doc section 11). No screen should use an arbitrary dp value for padding/gaps. */
object HqSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
    val huge: Dp = 40.dp
    val xhuge: Dp = 48.dp
    val xxhuge: Dp = 64.dp

    /** Default mobile screen horizontal padding for the 360-599 phone class (design doc section 7.2). */
    val screenHorizontal: Dp = xl

    // hq.sys.space.* (design doc section 7.1)
    val inline: Dp = sm
    val related: Dp = md
    val component: Dp = lg
    val card: Dp = xl
    val group: Dp = xxl
    val section: Dp = xxxl
    val screenEnd: Dp = xxl
}

/**
 * The one radius scale (design doc section 7.3). Every component reads a functional name; nothing
 * hard-codes a corner. Nested corners are concentric: inner = outer - inset (the 16dp segmented
 * track with a 4dp inset holds a 12dp indicator).
 *
 * | Step | dp   | Name(s)           | Used by                                              |
 * |------|------|-------------------|------------------------------------------------------|
 * | xs   | 8    | small             | badges, tags, skeleton blocks, inset elements        |
 * | sm   | 12   | control           | segmented indicator, snackbar, inset option rows     |
 * | md   | 16   | button, field     | every button size, every text field, segmented track |
 * | lg   | 20   | card              | cards, groups, illustrated panels                    |
 * | xl   | 28   | sheet             | sheet top corners, dialogs                           |
 * | pill | full | pill              | chips, switch track and thumb, avatars               |
 *
 * `sm`..`xl` at the bottom are v1.x aliases kept only so older screens compile; new code uses the
 * functional names above.
 */
object HqRadius {
    /** xs (8): small status badges, inset elements. */
    val small: Dp = 8.dp
    /** sm (12): segmented indicator, snackbar, inset option rows. */
    val control: Dp = 12.dp
    /** Floating bottom navigation pill (Figma radius 22). */
    val navPill: Dp = 22.dp
    /** md (16): every button, at every size -- one corner for every button in the app. */
    val button: Dp = 16.dp
    /** lg (20): main cards, intent cards, contextual summaries. */
    val card: Dp = 20.dp
    /** xl (28): top corners of Android sheets and the dialog surface. */
    val sheet: Dp = 28.dp
    /** md (16): text fields -- deliberately identical to [button] so a field and its submit button match. */
    val field: Dp = 16.dp
    /** Avatar circles, chips, switch, segmented indicators. */
    val pill: Dp = 9999.dp

    val sm: Dp = small
    val md: Dp = control
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val full: Dp = 999.dp
}

/** Component-specific minima (design doc section 4.2 `metrics.size`). These are minima, never clipping boxes: content grows with text. */
object HqSize {
    val target: Dp = 48.dp
    /** Default (Large) button height. See HqButtonSize for Medium 48 / Small 40. */
    val button: Dp = 56.dp
    val input: Dp = 56.dp
    val row: Dp = 56.dp
    val rowTwoLine: Dp = 72.dp
    val fab: Dp = 56.dp
    val chipVisual: Dp = 32.dp
    val navigationContent: Dp = 80.dp
    val appBarContent: Dp = 56.dp
    val avatarSmall: Dp = 32.dp
    val avatarMedium: Dp = 40.dp
    val avatarLarge: Dp = 64.dp
}

/** Adaptive native grid (design doc section 7.2), measured in available window width. */
object HqLayout {
    val gutterCompact: Dp = 16.dp
    val gutterStandard: Dp = 20.dp
    val gutterMedium: Dp = 24.dp
    val gutterExpanded: Dp = 32.dp
    val formMax: Dp = 480.dp
    val detailMax: Dp = 600.dp
    val contentMax: Dp = 1200.dp
    val breakMedium: Dp = 600.dp
    val breakExpanded: Dp = 840.dp

    /** Horizontal screen gutter for an available window width. */
    fun gutterFor(width: Dp): Dp = when {
        width < 360.dp -> gutterCompact
        width < breakMedium -> gutterStandard
        width < breakExpanded -> gutterMedium
        else -> gutterExpanded
    }
}

/** Elevation scale (design doc section 15). Use sparingly -- most surfaces should sit at `none` or `subtle` (a border, not a shadow). */
object HqElevation {
    val none: Dp = 0.dp
    val subtle: Dp = 1.dp
    val low: Dp = 2.dp
    val modal: Dp = 8.dp
}

/**
 * Tinted shadow levels (design doc section 7.4), applied with `Modifier.hqShadow`. Shadows are
 * tinted with the ink colour, never pure black, and are dropped in dark mode, where depth comes
 * from a lighter surface plus a subtle border instead.
 */
enum class HqShadow(val elevation: Dp, val ambientAlpha: Float, val spotAlpha: Float) {
    /** No shadow (lists, rows, status cards). */
    None(0.dp, 0f, 0f),
    /** Cards and the selected segment: about y2 blur8 at 8%. */
    Floating(3.dp, 0.05f, 0.08f),
    /** Primary button lift, snackbar, menus. */
    Raised(6.dp, 0.07f, 0.14f),
    /** Dialogs and sheets: about y8 blur24 at 14%. */
    Overlay(12.dp, 0.08f, 0.16f),
}

/** Icon size tokens (design doc section 17). Visual size only -- pad the touch target to ~48dp separately, see HqTouchTarget. */
object HqIconSize {
    val xs: Dp = 16.dp
    val sm: Dp = 20.dp
    val md: Dp = 24.dp
    val lg: Dp = 32.dp
    val xl: Dp = 40.dp
}

/** Minimum interactive touch target (design doc section 53), independent of visual icon size. */
val HqTouchTarget: Dp = 48.dp
