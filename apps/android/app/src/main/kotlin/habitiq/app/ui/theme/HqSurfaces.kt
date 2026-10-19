package habitiq.app.ui.theme

import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Ready-made shapes for the radius scale, so components never build a corner from a raw number. */
object HqShapes {
    val small = RoundedCornerShape(HqRadius.small)
    val control = RoundedCornerShape(HqRadius.control)
    // Buttons are full pills (current Android and iOS convention); fields and cards keep their radii.
    val button = RoundedCornerShape(50)
    val field = RoundedCornerShape(HqRadius.field)
    val card = RoundedCornerShape(HqRadius.card)
    val dialog = RoundedCornerShape(HqRadius.sheet)
    val sheetTop = RoundedCornerShape(topStart = HqRadius.sheet, topEnd = HqRadius.sheet)
    val pill = RoundedCornerShape(50)
}

/**
 * Tinted elevation (design doc section 7.4). Light theme: a soft shadow tinted with the ink colour.
 * Dark theme: no shadow (it would be invisible and muddy); depth comes from the raised surface colour.
 */
@Composable
fun Modifier.hqShadow(level: HqShadow, shape: Shape): Modifier {
    val c = LocalHqColors.current
    if (level == HqShadow.None || c.isDark) return this
    val tint = c.textPrimary
    return shadow(level.elevation, shape, clip = false, ambientColor = tint.copy(alpha = level.ambientAlpha), spotColor = tint.copy(alpha = level.spotAlpha))
}

/** Same as [hqShadow] but tinted with a specific colour (the primary button lifts with a brand-tinted shadow). */
@Composable
fun Modifier.hqShadow(level: HqShadow, shape: Shape, tint: Color): Modifier {
    val c = LocalHqColors.current
    if (level == HqShadow.None || c.isDark) return this
    return shadow(level.elevation, shape, clip = false, ambientColor = tint.copy(alpha = level.ambientAlpha * 2f), spotColor = tint.copy(alpha = level.spotAlpha * 2f))
}

/**
 * Keyboard / switch-access focus indicator: a 2dp ring in the focus colour drawn 3dp outside the
 * control (no layout shift, never hides the control's own border). Drawn only while [focused].
 */
fun Modifier.hqFocusRing(focused: Boolean, color: Color, shape: Shape): Modifier =
    if (!focused) this else this.drawWithContent {
        drawContent()
        val gap = 3.dp.toPx()
        val stroke = 2.dp.toPx()
        val outline = shape.createOutline(Size(size.width + gap * 2, size.height + gap * 2), layoutDirection, this)
        translate(-gap, -gap) { drawOutline(outline, color, style = Stroke(stroke)) }
    }
