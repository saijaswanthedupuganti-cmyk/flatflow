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

    /** Default mobile screen horizontal padding (design doc section 12). 16dp matches the app's existing convention. */
    val screenHorizontal: Dp = lg
}

/** Semantic corner-radius scale (design doc section 13). 12dp ("md") is the app's existing standard -- see FigmaTextField/FigmaPrimaryButton. */
object HqRadius {
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val full: Dp = 999.dp
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
