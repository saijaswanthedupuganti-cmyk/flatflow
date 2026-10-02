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
 * Functional radius scale (design doc section 7.3). Use the functional names in new code;
 * `sm`..`xl` are v1.x aliases kept for the migration cycle.
 */
object HqRadius {
    /** Small status badges, inset elements. */
    val small: Dp = 8.dp
    /** Buttons, text fields, interactive option rows. */
    val control: Dp = 12.dp
    /** Floating bottom navigation pill (Figma radius 22). */
    val navPill: Dp = 22.dp
    /** Figma primary/secondary buttons (radius 16). */
    val button: Dp = 16.dp
    /** Main cards, intent cards, contextual summaries. */
    val card: Dp = 20.dp
    /** Top corners of Android sheets. */
    val sheet: Dp = 30.dp
    /** Figma text fields (radius 15). */
    val field: Dp = 15.dp
    /** Avatar circles, chips, segmented indicators. */
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
    val button: Dp = 52.dp
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
