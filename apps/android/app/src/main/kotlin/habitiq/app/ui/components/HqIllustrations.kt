package habitiq.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics

/**
 * Habitiq's illustration family: small, soft, matte "architectural vignettes" - warm neutral walls,
 * teal roof and door details, one small coral accent, a contained contact shadow (design doc 19.3).
 *
 * Drawn natively so there is no network, no empty flash and one consistent style. Everything is
 * decorative: no text, no numerals, no claims. Native text always sits beside the art, never on it.
 * Colours are fixed on purpose: warm walls read on both the light and the dark selected surfaces.
 */
enum class HqArt(val aspect: Float) {
    Home(1f),
    IntentHome(1f),
    IntentFlatmate(1f),
    SharedHome(1.35f),
    Key(1f),
    Checklist(1f),
    Receipt(1f),
    SearchHouse(1.3f),
    ClockHouse(1f),
}

private object ArtColors {
    val wallLight = Color(0xFFF6EFE3)
    val wallShade = Color(0xFFE3D6C1)
    val roofDark = Color(0xFF0F766E)
    val roofLight = Color(0xFF1AA596)
    val door = Color(0xFF115E59)
    val glass = Color(0xFFBFE9E2)
    val coral = Color(0xFFFF6B5A)
    val coralDeep = Color(0xFFD94E3D)
    val leaf = Color(0xFF5BA37A)
    val leafLight = Color(0xFF86C29A)
    val shadow = Color(0x26101815)
    val skinA = Color(0xFFC98F6B)
    val skinB = Color(0xFF8D5A3C)
    val paper = Color(0xFFFFFBF3)
    val ink = Color(0xFF52605D)
    val gold = Color(0xFFE9B949)
}

/** Decorative illustration at a bounded size. Pass a size modifier; the art keeps its own aspect ratio. */
@Composable
fun HqIllustration(art: HqArt, modifier: Modifier = Modifier) {
    Canvas(modifier.aspectRatio(art.aspect).clearAndSetSemantics { }) {
        when (art) {
            HqArt.Home -> drawHome(0f, 0f, 1f)
            HqArt.IntentHome -> { drawHome(0f, 0.02f, 0.92f); drawPlusBadge() }
            HqArt.IntentFlatmate -> drawFlatmate()
            HqArt.SharedHome -> drawSharedHome()
            HqArt.Key -> drawKeyDoor()
            HqArt.Checklist -> drawChecklist()
            HqArt.Receipt -> drawReceipt()
            HqArt.SearchHouse -> drawSearchHouse()
            HqArt.ClockHouse -> drawClockHouse()
        }
    }
}

// ---- shared helpers -------------------------------------------------------------------------

/** Maps a point in a 0..1 box (offset by [ox],[oy], scaled by [s]) onto the canvas. */
private fun DrawScope.p(x: Float, y: Float, ox: Float, oy: Float, s: Float) =
    Offset((ox + x * s) * size.width, (oy + y * s) * size.height)

private fun DrawScope.poly(points: List<Offset>, brush: Brush) {
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }
    drawPath(path, brush)
}

private fun DrawScope.poly(points: List<Offset>, color: Color) = poly(points, Brush.linearGradient(listOf(color, color)))

private fun DrawScope.contactShadow(cx: Float, cy: Float, w: Float, h: Float) {
    drawOval(ArtColors.shadow, Offset((cx - w / 2) * size.width, (cy - h / 2) * size.height), Size(w * size.width, h * size.height))
}

private fun DrawScope.plant(x: Float, baseY: Float, scale: Float, pot: Color) {
    val w = size.width
    val h = size.height
    val potW = 0.085f * scale
    val potH = 0.075f * scale
    // leaves behind the pot
    val leafColor = listOf(ArtColors.leaf, ArtColors.leafLight, ArtColors.leaf)
    val offsets = listOf(-0.03f, 0f, 0.03f)
    val tilts = listOf(-0.04f, 0f, 0.04f)
    for (i in 0..2) {
        val cx = (x + offsets[i] * scale) * w
        val cy = (baseY - potH - 0.06f * scale) * h
        drawOval(
            leafColor[i],
            Offset(cx - 0.032f * scale * w + tilts[i] * w, cy - 0.075f * scale * h),
            Size(0.064f * scale * w, 0.15f * scale * h),
        )
    }
    val pl = Path().apply {
        moveTo((x - potW / 2) * w, (baseY - potH) * h)
        lineTo((x + potW / 2) * w, (baseY - potH) * h)
        lineTo((x + potW * 0.38f) * w, baseY * h)
        lineTo((x - potW * 0.38f) * w, baseY * h)
        close()
    }
    drawPath(pl, pot)
}

private fun DrawScope.drawPlusBadge() {
    val c = Offset(0.82f * size.width, 0.2f * size.height)
    val r = 0.13f * size.width
    drawCircle(ArtColors.coral, r, c)
    val arm = r * 0.5f
    val stroke = Stroke(width = r * 0.3f, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(c.x - arm, c.y), Offset(c.x + arm, c.y), strokeWidth = stroke.width, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(c.x, c.y - arm), Offset(c.x, c.y + arm), strokeWidth = stroke.width, cap = StrokeCap.Round)
}

// ---- the house -------------------------------------------------------------------------------

/** A compact three-quarter house: warm walls, teal gable roof, teal door, one coral planter. */
private fun DrawScope.drawHome(ox: Float, oy: Float, s: Float, withPlants: Boolean = true) {
    fun q(x: Float, y: Float) = p(x, y, ox, oy, s)
    contactShadow(ox + 0.5f * s, oy + 0.9f * s, 0.78f * s, 0.07f * s)

    // wall: front face and shaded side face
    poly(listOf(q(0.2f, 0.46f), q(0.6f, 0.46f), q(0.6f, 0.86f), q(0.2f, 0.86f)),
        Brush.verticalGradient(listOf(ArtColors.wallLight, Color(0xFFEFE5D3)), q(0f, 0.46f).y, q(0f, 0.86f).y))
    poly(listOf(q(0.6f, 0.46f), q(0.83f, 0.4f), q(0.83f, 0.8f), q(0.6f, 0.86f)), ArtColors.wallShade)

    // roof: front gable and long right slope
    poly(listOf(q(0.14f, 0.48f), q(0.4f, 0.2f), q(0.66f, 0.48f)),
        Brush.linearGradient(listOf(ArtColors.roofDark, Color(0xFF0B5F58)), q(0.14f, 0.2f), q(0.66f, 0.48f)))
    poly(listOf(q(0.4f, 0.2f), q(0.63f, 0.14f), q(0.9f, 0.42f), q(0.66f, 0.48f)),
        Brush.linearGradient(listOf(ArtColors.roofLight, ArtColors.roofDark), q(0.4f, 0.14f), q(0.9f, 0.48f)))

    // door and windows
    val doorTop = q(0.3f, 0.62f)
    drawRoundRect(ArtColors.door, doorTop, Size(0.12f * s * size.width, 0.24f * s * size.height), CornerRadius(0.03f * s * size.width))
    drawCircle(ArtColors.gold, 0.008f * size.width * s * 1.6f, q(0.395f, 0.745f))
    drawRoundRect(ArtColors.glass, q(0.46f, 0.56f), Size(0.11f * s * size.width, 0.1f * s * size.height), CornerRadius(0.012f * s * size.width))
    poly(listOf(q(0.66f, 0.54f), q(0.76f, 0.51f), q(0.76f, 0.62f), q(0.66f, 0.65f)), ArtColors.glass)

    if (withPlants) {
        plant(0.1f * s + ox, oy + 0.88f * s, s * 1.0f, ArtColors.coral)
        plant(ox + 0.9f * s, oy + 0.88f * s, s * 0.7f, ArtColors.coralDeep)
    }
}

// ---- people ----------------------------------------------------------------------------------

private fun DrawScope.person(cx: Float, baseY: Float, s: Float, skin: Color, top: Color) {
    val w = size.width
    val h = size.height
    val bodyW = 0.2f * s
    val bodyH = 0.24f * s
    // shoulders / torso
    val torso = Path().apply {
        moveTo((cx - bodyW / 2) * w, baseY * h)
        quadraticTo((cx - bodyW / 2) * w, (baseY - bodyH) * h, cx * w, (baseY - bodyH) * h)
        quadraticTo((cx + bodyW / 2) * w, (baseY - bodyH) * h, (cx + bodyW / 2) * w, baseY * h)
        close()
    }
    drawPath(torso, top)
    drawCircle(skin, 0.075f * s * w, Offset(cx * w, (baseY - bodyH - 0.07f * s) * h))
    // simple hair cap
    drawArc(
        Color(0xFF2C2A29),
        startAngle = 180f, sweepAngle = 180f, useCenter = true,
        topLeft = Offset((cx - 0.075f * s) * w, (baseY - bodyH - 0.145f * s) * h),
        size = Size(0.15f * s * w, 0.1f * s * h),
    )
}

private fun DrawScope.drawFlatmate() {
    // small home on the right, people in front on the left
    drawHome(0.34f, 0.12f, 0.66f, withPlants = false)
    person(0.24f, 0.88f, 1f, ArtColors.skinA, ArtColors.roofDark)
    person(0.46f, 0.88f, 0.92f, ArtColors.skinB, ArtColors.coral)
}

private fun DrawScope.drawSharedHome() {
    contactShadow(0.5f, 0.9f, 0.9f, 0.07f)
    drawHome(0.4f, 0.1f, 0.56f, withPlants = false)
    person(0.16f, 0.88f, 1.05f, ArtColors.skinA, ArtColors.roofDark)
    person(0.34f, 0.88f, 0.98f, ArtColors.skinB, ArtColors.coral)
    person(0.5f, 0.88f, 1f, Color(0xFFB57A57), Color(0xFF7BA4B5))
    plant(0.94f, 0.88f, 0.8f, ArtColors.coral)
}

// ---- utility objects --------------------------------------------------------------------------

private fun DrawScope.drawKeyDoor() {
    contactShadow(0.5f, 0.9f, 0.7f, 0.07f)
    val w = size.width
    val h = size.height
    // door with arch
    val door = Path().apply {
        moveTo(0.18f * w, 0.86f * h)
        lineTo(0.18f * w, 0.36f * h)
        quadraticTo(0.18f * w, 0.12f * h, 0.4f * w, 0.12f * h)
        quadraticTo(0.62f * w, 0.12f * h, 0.62f * w, 0.36f * h)
        lineTo(0.62f * w, 0.86f * h)
        close()
    }
    drawPath(door, Brush.verticalGradient(listOf(ArtColors.roofLight, ArtColors.door)))
    drawCircle(ArtColors.gold, 0.022f * w, Offset(0.54f * w, 0.55f * h))
    // key in coral
    val kc = Offset(0.64f * w, 0.7f * h)
    drawCircle(ArtColors.coral, 0.12f * w, kc)
    drawCircle(ArtColors.wallLight, 0.045f * w, kc)
    drawLine(ArtColors.coral, kc, Offset(0.9f * w, 0.7f * h), strokeWidth = 0.05f * w, cap = StrokeCap.Round)
    drawLine(ArtColors.coral, Offset(0.82f * w, 0.7f * h), Offset(0.82f * w, 0.78f * h), strokeWidth = 0.04f * w, cap = StrokeCap.Round)
    drawLine(ArtColors.coral, Offset(0.9f * w, 0.7f * h), Offset(0.9f * w, 0.8f * h), strokeWidth = 0.04f * w, cap = StrokeCap.Round)
}

private fun DrawScope.drawChecklist() {
    contactShadow(0.5f, 0.9f, 0.62f, 0.07f)
    val w = size.width
    val h = size.height
    drawRoundRect(ArtColors.paper, Offset(0.2f * w, 0.1f * h), Size(0.6f * w, 0.78f * h), CornerRadius(0.06f * w))
    drawRoundRect(ArtColors.roofDark, Offset(0.36f * w, 0.05f * h), Size(0.28f * w, 0.1f * h), CornerRadius(0.03f * w))
    for (i in 0..2) {
        val y = (0.3f + i * 0.2f) * h
        drawCircle(if (i < 2) ArtColors.roofLight else ArtColors.wallShade, 0.045f * w, Offset(0.32f * w, y))
        if (i < 2) {
            drawLine(Color.White, Offset(0.3f * w, y), Offset(0.315f * w, y + 0.02f * h), strokeWidth = 0.014f * w, cap = StrokeCap.Round)
            drawLine(Color.White, Offset(0.315f * w, y + 0.02f * h), Offset(0.345f * w, y - 0.025f * h), strokeWidth = 0.014f * w, cap = StrokeCap.Round)
        }
        drawLine(ArtColors.wallShade, Offset(0.42f * w, y), Offset(0.7f * w, y), strokeWidth = 0.03f * w, cap = StrokeCap.Round)
    }
    drawCircle(ArtColors.coral, 0.04f * w, Offset(0.74f * w, 0.84f * h))
}

private fun DrawScope.drawReceipt() {
    contactShadow(0.5f, 0.9f, 0.7f, 0.07f)
    val w = size.width
    val h = size.height
    val r = Path().apply {
        moveTo(0.16f * w, 0.1f * h)
        lineTo(0.62f * w, 0.1f * h)
        lineTo(0.62f * w, 0.82f * h)
        var x = 0.62f
        var up = false
        while (x > 0.16f) { x -= 0.0575f; lineTo(x * w, (if (up) 0.82f else 0.88f) * h); up = !up }
        lineTo(0.16f * w, 0.82f * h)
        close()
    }
    drawPath(r, ArtColors.paper)
    for (i in 0..3) {
        val y = (0.24f + i * 0.14f) * h
        drawLine(ArtColors.wallShade, Offset(0.24f * w, y), Offset((if (i == 3) 0.4f else 0.54f) * w, y), strokeWidth = 0.03f * w, cap = StrokeCap.Round)
    }
    drawLine(ArtColors.roofLight, Offset(0.24f * w, 0.74f * h), Offset(0.54f * w, 0.74f * h), strokeWidth = 0.04f * w, cap = StrokeCap.Round)
    plant(0.76f, 0.88f, 1.1f, ArtColors.coral)
}

private fun DrawScope.drawSearchHouse() {
    drawHome(0.0f, 0.04f, 0.78f, withPlants = false)
    val w = size.width
    val h = size.height
    val c = Offset(0.74f * w, 0.5f * h)
    drawCircle(ArtColors.glass.copy(alpha = 0.55f), 0.17f * h, c)
    drawCircle(ArtColors.coral, 0.17f * h, c, style = Stroke(width = 0.045f * h))
    drawLine(ArtColors.coral, Offset(c.x + 0.12f * h, c.y + 0.12f * h), Offset(c.x + 0.27f * h, c.y + 0.27f * h), strokeWidth = 0.06f * h, cap = StrokeCap.Round)
}

private fun DrawScope.drawClockHouse() {
    drawHome(0.0f, 0.04f, 0.86f, withPlants = false)
    val w = size.width
    val h = size.height
    val c = Offset(0.78f * w, 0.26f * h)
    drawCircle(ArtColors.paper, 0.14f * w, c)
    drawCircle(ArtColors.ink, 0.14f * w, c, style = Stroke(width = 0.025f * w))
    drawLine(ArtColors.ink, c, Offset(c.x, c.y - 0.085f * w), strokeWidth = 0.022f * w, cap = StrokeCap.Round)
    drawLine(ArtColors.ink, c, Offset(c.x + 0.06f * w, c.y + 0.02f * w), strokeWidth = 0.022f * w, cap = StrokeCap.Round)
}
