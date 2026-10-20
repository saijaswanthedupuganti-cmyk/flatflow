package habitiq.app.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import habitiq.app.ui.components.HqArt
import habitiq.app.ui.components.HqIllustration
import habitiq.app.ui.theme.LocalHqColors

/** Home-themed art used when a listing has no photo, or while/if its photo fails to load. */
private val placeholderArt = listOf(HqArt.Home, HqArt.SharedHome, HqArt.Key, HqArt.ClockHouse)

/** Which art and which tint a listing gets: always the same for the same id, so it reads as "that flat". */
internal data class ListingPlaceholderStyle(val art: HqArt, val tintIndex: Int)

internal fun listingPlaceholder(id: String): ListingPlaceholderStyle {
    val h = id.hashCode()
    return ListingPlaceholderStyle(
        art = placeholderArt[Math.floorMod(h, placeholderArt.size)],
        tintIndex = Math.floorMod(h / placeholderArt.size, PLACEHOLDER_TINTS),
    )
}

private const val PLACEHOLDER_TINTS = 4

@Composable
private fun placeholderTint(index: Int): Color {
    val c = LocalHqColors.current
    return when (index) {
        0 -> c.selectedBg
        1 -> c.statusWarningBg
        2 -> c.statusInfoBg
        else -> c.surfaceSubtle
    }
}

/**
 * Fills its box with the listing's placeholder: a tinted background and one home illustration.
 * Decorative only (no semantics); the card or hero text names the flat. Put the photo on top of it,
 * so the placeholder also shows while the photo loads and if it fails.
 */
@Composable
internal fun ListingPlaceholder(id: String, modifier: Modifier = Modifier) {
    val style = listingPlaceholder(id)
    Box(modifier.fillMaxSize().background(placeholderTint(style.tintIndex)), contentAlignment = Alignment.Center) {
        HqIllustration(style.art, Modifier.fillMaxHeight(0.55f))
    }
}
