package habitiq.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The Habitiq icon set, ported 1:1 from the Figma Make file (`icons` map in src/App.tsx):
 * 24x24 viewport, 1.8 stroke, round caps and joins, no fill. Tint with `Icon(tint = ...)`.
 * Shapes the Figma source drew as <circle>/<rect> are written here as equivalent path data.
 */
object HqIcons {
    private fun circle(cx: Float, cy: Float, r: Float) =
        "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

    private fun icon(name: String, vararg paths: String): ImageVector {
        val b = ImageVector.Builder(name = "Hq.$name", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        paths.forEach { d ->
            b.addPath(
                pathData = addPathNodes(d),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        return b.build()
    }

    val Home by lazy { icon("Home", "M3 11.5 12 4l9 7.5", "M5.5 10v10h13V10M9.5 20v-6h5v6") }
    val Manage by lazy {
        icon("Manage", "M6 5h12a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2Z", "M8 3v4m8-4v4M4 10h16M8 14h3m-3 3h6")
    }
    val Discover by lazy { icon("Discover", circle(11f, 11f, 7f), "m20 20-4-4m-7.5-2.5 1.7-4.3 4.3-1.7-1.7 4.3-4.3 1.7Z") }
    val Profile by lazy { icon("Profile", circle(12f, 8f, 4f), "M4.5 21a7.5 7.5 0 0 1 15 0") }
    val Check by lazy { icon("Check", "m5 12 4 4L19 6") }
    val Chevron by lazy { icon("Chevron", "m9 18 6-6-6-6") }
    val Back by lazy { icon("Back", "m15 18-6-6 6-6") }
    val Clock by lazy { icon("Clock", circle(12f, 12f, 9f), "M12 7v5l3 2") }
    val Receipt by lazy { icon("Receipt", "M6 3h12v18l-3-2-3 2-3-2-3 2V3Z", "M9 8h6m-6 4h6") }
    val Users by lazy { icon("Users", circle(9f, 8f, 3f), circle(17f, 9f, 2.5f), "M3 20a6 6 0 0 1 12 0m0-5a5 5 0 0 1 6 5") }
    val Message by lazy { icon("Message", "M4 5h16v12H9l-5 4V5Z") }
    val Shield by lazy { icon("Shield", "M12 3 5 6v5c0 4.5 2.9 8.5 7 10 4.1-1.5 7-5.5 7-10V6l-7-3Z", "m9 12 2 2 4-4") }
    val Search by lazy { icon("Search", circle(11f, 11f, 7f), "m20 20-4-4") }
    val Filter by lazy { icon("Filter", "M4 6h16M7 12h10m-7 6h4") }
    val Plus by lazy { icon("Plus", "M12 5v14M5 12h14") }
    val More by lazy { icon("More", circle(5f, 12f, 1f), circle(12f, 12f, 1f), circle(19f, 12f, 1f)) }
    val Bell by lazy { icon("Bell", "M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9", "M10 21h4") }
    val Heart by lazy {
        icon("Heart", "M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.7-7.5 1.1-1.1a5.5 5.5 0 0 0 0-7.8Z")
    }
    val Pin by lazy { icon("Pin", "M20 10c0 5-8 11-8 11S4 15 4 10a8 8 0 1 1 16 0Z", circle(12f, 10f, 2.5f)) }
    val Edit by lazy { icon("Edit", "m4 16-1 5 5-1L19 9l-4-4L4 16Z", "m13 7 4 4") }
    val Arrow by lazy { icon("Arrow", "M5 12h14m-5-5 5 5-5 5") }
}
