package habitiq.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Shared color tokens, pulled directly from C:\garbage\DESIGN.md (the established
 * Habitiq brand system) -- not invented here. See DESIGN.md's "Button Law": primary
 * buttons always use dark text (OnPrimary) on the violet Primary fill, never white.
 */
object HabitiqBrand {
    val Canvas = Color(0xFF0C0B0F)
    val Ink = Color(0xFFF4F3F8)
    val InkMute = Color(0xFF514E61)
    val Primary = Color(0xFF7C3AED)
    val PrimarySoft = Color(0xFFA78BFA)
    val OnPrimary = Color(0xFF0C0B0F)
    val InputBackground = Color(0xFF1A1820)
    val InputBorder = Color(0xFF2A2635)
    val Error = Color(0xFFEF4444)
    val SecondaryFill = Color(0xFFF4F3F8)
    val SecondaryText = Color(0xFF0C0B0F)
}
