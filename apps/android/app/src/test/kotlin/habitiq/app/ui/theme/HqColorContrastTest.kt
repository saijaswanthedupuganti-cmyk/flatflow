package habitiq.app.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/**
 * Design doc section 5.3 / 17.3: informative text targets 4.5:1 on its actual background and
 * essential control boundaries and indicators target 3:1. Both themes are checked for every
 * semantic pairing a component actually uses.
 */
class HqColorContrastTest {

    private fun channel(v: Float): Double =
        if (v <= 0.03928f) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)

    private fun luminance(c: Color): Double =
        0.2126 * channel(c.red) + 0.7152 * channel(c.green) + 0.0722 * channel(c.blue)

    private fun ratio(a: Color, b: Color): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    private val themes = mapOf("light" to HqLightColors, "dark" to HqDarkColors)

    private fun assertText(theme: String, name: String, fg: Color, bg: Color) {
        val r = ratio(fg, bg)
        assertTrue("$theme $name text contrast ${"%.2f".format(r)} < 4.5", r >= 4.5)
    }

    private fun assertNonText(theme: String, name: String, fg: Color, bg: Color) {
        val r = ratio(fg, bg)
        assertTrue("$theme $name non-text contrast ${"%.2f".format(r)} < 3.0", r >= 3.0)
    }

    @Test
    fun textPairsMeetAA() {
        themes.forEach { (t, c) ->
            for ((surfaceName, surface) in listOf("canvas" to c.canvas, "base" to c.surfaceBase, "raised" to c.surfaceRaised, "subtle" to c.surfaceSubtle)) {
                assertText(t, "primary on $surfaceName", c.textPrimary, surface)
                assertText(t, "secondary on $surfaceName", c.textSecondary, surface)
                assertText(t, "muted on $surfaceName", c.textMuted, surface)
                assertText(t, "brand on $surfaceName", c.textBrand, surface)
            }
            assertText(t, "primary button", c.actionPrimaryFg, c.actionPrimaryBg)
            assertText(t, "primary button hover", c.actionPrimaryFg, c.actionPrimaryHover)
            assertText(t, "primary button pressed", c.actionPrimaryFg, c.actionPrimaryPressed)
            assertText(t, "secondary button", c.actionSecondaryFg, c.actionSecondaryBg)
            assertText(t, "secondary button pressed", c.actionSecondaryFg, c.actionSecondaryPressed)
            assertText(t, "tertiary on canvas", c.actionTertiaryFg, c.canvas)
            assertText(t, "danger button", c.actionDangerFg, c.actionDangerBg)
            assertText(t, "selected", c.selectedFg, c.selectedBg)
            assertText(t, "warm note", c.warmFg, c.warmBg)
            assertText(t, "inverse", c.textInverse, c.surfaceInverse)
            assertText(t, "disabled", c.disabledFg, c.disabledBg)
            assertText(t, "info", c.statusInfoFg, c.statusInfoBg)
            assertText(t, "success", c.statusSuccessFg, c.statusSuccessBg)
            assertText(t, "warning", c.statusWarningFg, c.statusWarningBg)
            assertText(t, "danger", c.statusDangerFg, c.statusDangerBg)
        }
    }

    @Test
    fun errorTextReadableOnEverySurface() {
        themes.forEach { (t, c) ->
            for ((name, surface) in listOf("canvas" to c.canvas, "base" to c.surfaceBase, "raised" to c.surfaceRaised)) {
                assertText(t, "danger text on $name", c.statusDangerFg, surface)
            }
        }
    }

    @Test
    fun controlBoundariesAndIndicatorsMeetNonTextContrast() {
        themes.forEach { (t, c) ->
            assertNonText(t, "control border on base", c.borderControl, c.surfaceBase)
            assertNonText(t, "control border on canvas", c.borderControl, c.canvas)
            assertNonText(t, "focus on base", c.focus, c.surfaceBase)
            assertNonText(t, "focus on canvas", c.focus, c.canvas)
            assertNonText(t, "selected border on selected bg", c.selectedBorder, c.surfaceBase)
        }
    }

    @Test
    fun brandAnchorsAreNotApprovedForText() {
        // Spec section 5.3: white on the raw anchors fails and must never be used for text.
        assertTrue(ratio(Color.White, Color(0xFF14B8A6)) < 4.5)
        assertTrue(ratio(Color.White, Color(0xFFFF6B5A)) < 4.5)
        themes.values.forEach { c ->
            assertEquals(Color(0xFF14B8A6), c.brandTeal)
            assertEquals(Color(0xFFFF6B5A), c.brandCoral)
        }
    }

    @Test
    fun specReferenceRatiosHold() {
        // Spot-check against the calculated table in section 5.3.
        assertEquals(5.47, ratio(Color.White, Color(0xFF0F766E)), 0.02)
        assertEquals(11.38, ratio(Color(0xFF10201E), Color(0xFF5EEAD4)), 0.02)
        assertEquals(3.76, ratio(Color(0xFF788781), Color.White), 0.02)
    }
}
