package habitiq.app.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import habitiq.app.data.VacancyListing
import habitiq.app.lib.formatInr
import habitiq.app.ui.components.HqBadge
import habitiq.app.ui.components.HqBadgeTone
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqListingFrame
import habitiq.app.ui.components.HqPageHeader
import habitiq.app.ui.components.HqTagFlow
import habitiq.app.ui.figma.MapLocationPicker
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Figma "Saved flats": a private shortlist kept on this device. Each card repeats the listing photo and
 * essentials with a filled heart; "Remove from saved" is the only edit.
 */
@Composable
fun SavedFlatsScreen(
    saved: List<VacancyListing>,
    onOpen: (VacancyListing) -> Unit,
    onRemove: (VacancyListing) -> Unit,
    onExplore: () -> Unit,
    onBack: () -> Unit,
) {
    val c = LocalHqColors.current
    Column(
        Modifier.fillMaxSize().background(c.canvas).verticalScroll(rememberScrollState())
            .padding(horizontal = HqSpacing.screenHorizontal).padding(top = HqSpacing.sm, bottom = HqSpacing.screenEnd),
    ) {
        HqPageHeader(title = "Saved flats", subtitle = "Keep a shortlist before you connect", onBack = onBack)
        if (saved.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(top = 48.dp, start = 12.dp, end = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(Modifier.size(72.dp).clip(RoundedCornerShape(24.dp)).background(c.warmBg), contentAlignment = Alignment.Center) {
                    Icon(HqIcons.Heart, null, tint = Color(0xFFD9584B), modifier = Modifier.size(30.dp))
                }
                Text("No saved flats yet", style = HqType.titleMedium2, color = c.textPrimary, modifier = Modifier.padding(top = 11.dp))
                Text(
                    "Tap the heart on a flat to keep it here while you compare your options.",
                    style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(bottom = 15.dp),
                )
                HqButton(text = "Explore flats", onClick = onExplore, fullWidth = false)
            }
        } else {
            Row(Modifier.fillMaxWidth().padding(bottom = 13.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${saved.size} ${if (saved.size == 1) "place" else "places"} saved", style = HqType.labelMedium, color = c.textPrimary, fontWeight = FontWeight.Bold)
                Text("Only you can see this list", style = HqType.labelSmall, color = c.textMuted)
            }
            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                saved.forEach { listing ->
                    val cover = listing.photoUrls.firstOrNull()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HqListingFrame(
                            onClick = { onOpen(listing) },
                            photoHeight = 210,
                            photo = {
                                if (cover != null) {
                                    AsyncImage(
                                        model = cover, contentDescription = "Photo of ${listing.flatName}",
                                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                                        placeholder = ColorPainter(c.surfaceSubtle), error = ColorPainter(c.surfaceSubtle), fallback = ColorPainter(c.surfaceSubtle),
                                    )
                                }
                                Box(
                                    Modifier.align(Alignment.BottomEnd).padding(13.dp).size(38.dp).clip(CircleShape).background(Color.White),
                                    contentAlignment = Alignment.Center,
                                ) { Icon(HqIcons.Heart, null, tint = Color(0xFFD9584B), modifier = Modifier.size(HqIconSize.sm)) }
                            },
                            body = {
                                HqBadge("${listing.bedsAvailable} bed available", HqBadgeTone.Success)
                                Text(listing.flatName, style = HqType.titleSmall2, color = c.textPrimary)
                                Text("${listing.area}, ${listing.city}".trim(',', ' '), style = HqType.bodyMedium, color = c.textSecondary)
                                listing.rentPerHead?.let { Text("${formatInr(it)} / month", style = HqType.titleSmall2, color = c.textPrimary) }
                                HqTagFlow(listing.displayTags(), maxRows = 1)
                            },
                        )
                        HqButton(text = "Remove from saved", onClick = { onRemove(listing) }, variant = HqButtonVariant.Secondary)
                    }
                }
            }
        }
    }
}

/**
 * Figma "Find a home near a place". The picked point is turned into an area and city, which become the
 * Discover search, because listings only carry an approximate area (never exact coordinates).
 */
@Composable
fun WorkplaceMapScreen(
    onPicked: (area: String, city: String) -> Unit,
    onBack: () -> Unit,
) {
    val c = LocalHqColors.current
    Column(
        Modifier.fillMaxSize().background(c.canvas).verticalScroll(rememberScrollState())
            .padding(horizontal = HqSpacing.screenHorizontal).padding(top = HqSpacing.sm, bottom = HqSpacing.screenEnd),
    ) {
        HqPageHeader(title = "Search near a place", subtitle = "Choose work, college, or any landmark", onBack = onBack)
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))) {
            MapLocationPicker(onLocationPicked = { onPicked(it.area, it.city) })
        }
        Row(
            Modifier.padding(top = 18.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.surfaceSubtle).padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(HqIcons.Pin, null, tint = c.textSecondary, modifier = Modifier.size(HqIconSize.sm))
            Text(
                "Oddroof shows homes in the area around the place you pick. Listings only share an approximate area, never an exact address.",
                style = HqType.bodyMedium, color = c.textSecondary,
            )
        }
    }
}
