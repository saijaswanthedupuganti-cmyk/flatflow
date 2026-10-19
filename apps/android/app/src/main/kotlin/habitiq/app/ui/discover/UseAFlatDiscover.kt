package habitiq.app.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import habitiq.app.data.VacancyListing
import habitiq.app.discover.DiscoverFilterLogic
import habitiq.app.discover.ListingFacts
import habitiq.app.discover.VacancyFilters
import habitiq.app.lib.formatInr
import habitiq.app.ui.components.HqArt
import habitiq.app.ui.components.HqChipRow
import habitiq.app.ui.components.HqIconButton
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqIllustration
import habitiq.app.ui.components.HqRemovableChip
import habitiq.app.ui.components.HqSearchBar
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.theme.HqShapes
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

@Composable
fun UseAFlatDiscoverContent(
    vacancies: List<VacancyListing>,
    filteredVacancies: List<VacancyListing>,
    filters: VacancyFilters,
    onFiltersChange: (VacancyFilters) -> Unit,
    isLoading: Boolean,
    loadError: String?,
    onRetry: () -> Unit,
    onOpenFilters: () -> Unit,
    onOpenListing: (VacancyListing) -> Unit,
    onMyPosts: () -> Unit = {},
    onNearPlace: (() -> Unit)? = null,
    header: @Composable () -> Unit = {},
    listState: LazyListState? = null,
    savedIds: Set<String> = emptySet(),
    onToggleSave: ((VacancyListing) -> Unit)? = null,
) {
    // One scrolling list: the page header, search tools and results all move together.
    LazyColumn(
        Modifier.fillMaxSize(),
        state = listState ?: rememberLazyListState(),
        contentPadding = PaddingValues(bottom = HqSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(HqSpacing.lg),
    ) {
        item(key = "discover-top") {
            Column {
                header()
                HqSearchBar(
                    value = filters.cityArea,
                    onValueChange = { onFiltersChange(filters.copy(cityArea = it)) },
                    placeholder = "Search an area or landmark",
                    onFilters = onOpenFilters,
                    activeFilters = filters.activeCount,
                    modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal),
                )
                AppliedVacancyFilters(filters, onFiltersChange)
                ResultsRow(
                    text = when {
                        isLoading -> "Searching…"
                        filters.activeCount > 0 || filters.cityArea.isNotBlank() -> "${filteredVacancies.size} of ${vacancies.size} rooms"
                        else -> "${vacancies.size} open ${if (vacancies.size == 1) "room" else "rooms"}"
                    },
                    onNearPlace = onNearPlace,
                    onMyPosts = onMyPosts,
                )
            }
        }
        when {
            isLoading -> item { DiscoveryLoading("Searching flats…") }
            loadError != null -> item { DiscoveryError("Couldn't load results.", "Check your connection and try again.", onRetry) }
            filteredVacancies.isEmpty() -> item {
                DiscoveryEmpty(
                    title = if (vacancies.isEmpty()) "No flats found" else "No flats match",
                    body = if (vacancies.isEmpty()) {
                        "Try another area. New rooms show up when flats list them."
                    } else {
                        "Try a wider area or budget."
                    },
                    actionLabel = if (filters.activeCount > 0) "Change filters" else null,
                    onAction = if (filters.activeCount > 0) onOpenFilters else null
                )
            }
            else -> items(filteredVacancies, key = { it.flatId }) { listing ->
                VacancyListingCard(
                    listing = listing,
                    onOpen = onOpenListing,
                    saved = listing.flatId in savedIds,
                    onToggleSave = onToggleSave?.let { toggle -> { toggle(listing) } },
                    modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal),
                )
            }
        }
    }
}

/** Count on the left, quiet links on the right: one line, no section heading above the results. */
@Composable
internal fun ResultsRow(text: String, onNearPlace: (() -> Unit)?, onMyPosts: () -> Unit) {
    val c = LocalHqColors.current
    Row(
        Modifier.fillMaxWidth().padding(start = HqSpacing.screenHorizontal, end = HqSpacing.sm, top = HqSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = HqType.labelMedium, color = c.textSecondary, modifier = Modifier.weight(1f))
        if (onNearPlace != null) HqTextButton(text = "Near a place", onClick = onNearPlace, leadingIcon = HqIcons.Pin)
        HqTextButton(text = "My posts", onClick = onMyPosts)
    }
}

/** Applied filters as ONE horizontally scrolling row of removable chips under the search field. */
@Composable
private fun AppliedVacancyFilters(filters: VacancyFilters, onChange: (VacancyFilters) -> Unit) {
    val chips = buildList<Pair<String, () -> Unit>> {
        if (filters.rentMin.isNotBlank() || filters.rentMax.isNotBlank()) {
            add(DiscoverFilterLogic.budgetLabel(filters.rentMin, filters.rentMax) to { onChange(filters.copy(rentMin = "", rentMax = "")) })
        }
        if (filters.roomType != "any") add((ListingFacts.roomTypeLabel(filters.roomType) ?: filters.roomType) to { onChange(filters.copy(roomType = "any")) })
        if (filters.genderPreference != "any") add(ListingFacts.suitsLabel(filters.genderPreference) to { onChange(filters.copy(genderPreference = "any")) })
        if (filters.available != "any") {
            add(("Available " + (DiscoverFilterLogic.availableOptions.firstOrNull { it.first == filters.available }?.second?.lowercase() ?: "")) to { onChange(filters.copy(available = "any")) })
        }
        if (filters.furnishing != "any") add((ListingFacts.furnishingLabel(filters.furnishing) ?: filters.furnishing) to { onChange(filters.copy(furnishing = "any")) })
        if (filters.flatType != "any") add(DiscoverFilterLogic.formatFlatType(filters.flatType) to { onChange(filters.copy(flatType = "any")) })
        filters.lifestyleTags.forEach { tag -> add(tag to { onChange(filters.copy(lifestyleTags = filters.lifestyleTags - tag)) }) }
    }
    AppliedFilterRow(chips) { onChange(VacancyFilters(cityArea = filters.cityArea)) }
}

@Composable
internal fun AppliedFilterRow(chips: List<Pair<String, () -> Unit>>, onClearAll: () -> Unit) {
    if (chips.isEmpty()) return
    Spacer(Modifier.height(HqSpacing.xs))
    HqChipRow(Modifier.padding(start = HqSpacing.screenHorizontal)) {
        chips.forEach { (label, remove) -> HqRemovableChip(label, remove) }
        if (chips.size > 1) HqTextButton(text = "Clear all", onClick = onClearAll)
        Spacer(Modifier.size(HqSpacing.md))
    }
}

/**
 * The listing card (also the wizard's live preview): 16:9 photo with rounded top, title + rent on one
 * line, one facts line, the area, at most three calm tags in one row (+N), one trust line. The whole
 * card opens the listing; Save is its own 48dp target on the photo. [onOpen] null = preview, not tappable.
 */
@Composable
fun VacancyListingCard(
    listing: VacancyListing,
    onOpen: ((VacancyListing) -> Unit)?,
    modifier: Modifier = Modifier,
    saved: Boolean = false,
    onToggleSave: (() -> Unit)? = null,
) {
    val c = LocalHqColors.current
    val cover = listing.photoUrls.firstOrNull()
    DiscoverCardShell(
        onClick = onOpen?.let { open -> { open(listing) } },
        clickLabel = "View ${listing.flatName}",
        modifier = modifier,
        media = {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(c.selectedBg)) {
                if (cover != null) {
                    AsyncImage(
                        model = cover,
                        contentDescription = null, // The card text already names the flat.
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(c.surfaceSubtle),
                        error = ColorPainter(c.surfaceSubtle),
                        fallback = ColorPainter(c.surfaceSubtle),
                    )
                } else {
                    HqIllustration(HqArt.Home, Modifier.align(Alignment.Center).fillMaxSize(0.5f))
                }
                if (listing.photoUrls.size > 1) {
                    Text(
                        "${listing.photoUrls.size} photos", style = HqType.labelSmall, color = c.onBrandInk,
                        modifier = Modifier.align(Alignment.BottomStart).padding(HqSpacing.md)
                            .clip(HqShapes.pill).background(c.brandInk.copy(alpha = 0.72f))
                            .padding(horizontal = HqSpacing.sm, vertical = HqSpacing.xs / 2),
                    )
                }
                if (onToggleSave != null) {
                    Box(Modifier.align(Alignment.TopEnd).padding(HqSpacing.xs).clip(HqShapes.pill).background(c.surfaceBase.copy(alpha = 0.92f))) {
                        HqIconButton(
                            icon = if (saved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = if (saved) "Remove ${listing.flatName} from saved" else "Save ${listing.flatName}",
                            onClick = onToggleSave,
                            tint = if (saved) c.statusDangerFg else c.iconDefault,
                        )
                    }
                }
            }
        },
    ) {
        TitleAmountRow(
            title = listing.flatName.ifBlank { "Room in a flat" },
            amount = listing.rentPerHead?.let { formatInr(it) },
            amountUnit = listing.rentPerHead?.let { "/mo" },
        )
        ListingFacts.factsLine(listing).takeIf { it.isNotBlank() }?.let {
            Text(it, style = HqType.bodyMedium, color = c.textPrimary)
        }
        ListingFacts.areaLine(listing).takeIf { it.isNotBlank() }?.let { IconLine(HqIcons.Pin, it) }
        val tags = ListingFacts.cardTags(listing)
        if (tags.isNotEmpty()) {
            Spacer(Modifier.height(HqSpacing.xs / 2))
            OneRowTags(tags)
        }
        Spacer(Modifier.height(HqSpacing.xs / 2))
        IconLine(
            HqIcons.Shield,
            if (listing.memberCount > 0) "Oddroof member · ${listing.memberCount} live here" else "Listed by an Oddroof member",
            tint = c.textBrand,
            style = HqType.labelSmall,
        )
    }
}
