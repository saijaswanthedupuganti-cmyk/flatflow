package habitiq.app.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import habitiq.app.data.VacancyListing
import habitiq.app.lib.formatInr
import habitiq.app.ui.components.HqBadge
import habitiq.app.ui.components.HqBadgeTone
import habitiq.app.ui.components.HqEmptyState
import habitiq.app.ui.components.HqIconButton
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqListingFrame
import habitiq.app.ui.components.HqPageHeader
import habitiq.app.ui.components.HqTagFlow
import habitiq.app.ui.figma.MapLocationPicker
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqShapes
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Figma "Saved flats": a private shortlist kept on this device. Each card repeats the listing photo and
 * essentials; the filled heart on the photo removes it from the list. Shows loading until the listings
 * have arrived, so "No saved flats yet" never flashes before the real list.
 */
@Composable
fun SavedFlatsScreen(
    saved: List<VacancyListing>,
    onOpen: (VacancyListing) -> Unit,
    onRemove: (VacancyListing) -> Unit,
    onExplore: () -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean = false,
    loadError: String? = null,
    onRetry: (() -> Unit)? = null,
) {
    val c = LocalHqColors.current
    LazyColumn(
        Modifier.fillMaxSize().background(c.canvas),
        contentPadding = PaddingValues(
            start = HqSpacing.screenHorizontal, end = HqSpacing.screenHorizontal,
            top = HqSpacing.sm, bottom = HqSpacing.fabClearance,
        ),
        verticalArrangement = Arrangement.spacedBy(HqSpacing.lg),
    ) {
        item(key = "header") {
            HqPageHeader(title = "Saved flats", subtitle = "Your shortlist before you send a request", onBack = onBack)
        }
        when {
            isLoading -> item(key = "loading") { DiscoveryLoading("Loading saved flats…") }
            loadError != null && saved.isEmpty() -> item(key = "error") {
                DiscoveryError("Couldn't load your saved flats.", "Check your connection and try again.", onRetry)
            }
            saved.isEmpty() -> item(key = "empty") {
                HqEmptyState(
                    icon = HqIcons.Heart,
                    title = "No saved flats yet",
                    message = "Tap the heart on a flat to keep it here while you compare your options.",
                    primaryLabel = "Explore flats",
                    onPrimaryClick = onExplore,
                )
            }
            else -> {
                item(key = "count") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${saved.size} ${if (saved.size == 1) "place" else "places"} saved", style = HqType.labelMedium, color = c.textPrimary, fontWeight = FontWeight.Bold)
                        Text("Only you can see this list", style = HqType.labelSmall, color = c.textMuted)
                    }
                }
                items(saved, key = { it.flatId }) { listing -> SavedFlatCard(listing, onOpen, onRemove) }
            }
        }
    }
}

@Composable
private fun SavedFlatCard(listing: VacancyListing, onOpen: (VacancyListing) -> Unit, onRemove: (VacancyListing) -> Unit) {
    val c = LocalHqColors.current
    val cover = listing.photoUrls.firstOrNull()
    HqListingFrame(
        onClick = { onOpen(listing) },
        photoHeight = 210,
        photo = {
            ListingPlaceholder(listing.flatId)
            if (cover != null) {
                AsyncImage(
                    model = cover, contentDescription = "Photo of ${listing.flatName}",
                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                )
            }
            // The heart is the toggle: a 48dp target on a light disc so it reads on any photo.
            Box(Modifier.align(Alignment.TopEnd).padding(HqSpacing.xs).clip(HqShapes.pill).background(c.surfaceBase.copy(alpha = 0.92f))) {
                HqIconButton(
                    icon = Icons.Filled.Favorite,
                    contentDescription = "Remove ${listing.flatName.ifBlank { "this flat" }} from saved",
                    onClick = { onRemove(listing) },
                    tint = c.statusDangerFg,
                )
            }
        },
        body = {
            HqBadge("${listing.bedsAvailable} bed available", HqBadgeTone.Success)
            Text(listing.flatName, style = HqType.titleSmall2, color = c.textPrimary)
            Text("${listing.area}, ${listing.city}".trim(',', ' '), style = HqType.bodyMedium, color = c.textSecondary)
            listing.rentPerHead?.let { Text("${formatInr(it)} / month", style = HqType.titleSmall2, color = c.textPrimary) }
            HqTagFlow(listing.displayTags(), maxRows = 1)
        },
    )
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
            .padding(horizontal = HqSpacing.screenHorizontal).padding(top = HqSpacing.sm, bottom = HqSpacing.fabClearance),
    ) {
        HqPageHeader(title = "Search near a place", subtitle = "Choose work, college, or any landmark", onBack = onBack)
        Box(Modifier.fillMaxWidth().clip(HqShapes.card)) {
            MapLocationPicker(onLocationPicked = { onPicked(it.area, it.city) })
        }
        Row(
            Modifier.padding(top = HqSpacing.lg).fillMaxWidth().clip(HqShapes.control).background(c.surfaceSubtle).padding(HqSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
        ) {
            Icon(HqIcons.Pin, null, tint = c.textSecondary, modifier = Modifier.size(HqIconSize.sm))
            Text(
                "Oddroof shows homes in the area around the place you pick. Listings only share an approximate area, never an exact address.",
                style = HqType.bodyMedium, color = c.textSecondary,
            )
        }
    }
}
