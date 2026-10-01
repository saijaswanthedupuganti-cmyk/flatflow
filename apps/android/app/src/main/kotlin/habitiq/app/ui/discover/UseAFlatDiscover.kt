package habitiq.app.ui.discover

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import habitiq.app.R
import habitiq.app.data.VacancyListing
import habitiq.app.lib.formatInr
import habitiq.app.discover.DiscoverFilterLogic
import habitiq.app.discover.VacancyFilters
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
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
    onOpenListing: (VacancyListing) -> Unit
) {
    val c = LocalHqColors.current
    Column(Modifier.fillMaxSize()) {
        SafetyBanner()
        Spacer(Modifier.height(HqSpacing.sm))

        FlatDiscoveryBanner()
        Spacer(Modifier.height(HqSpacing.md))

        Row(
            Modifier.fillMaxWidth().padding(horizontal = HqSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)
        ) {
            HqTextField(
                value = filters.cityArea,
                onValueChange = { onFiltersChange(filters.copy(cityArea = it)) },
                label = "Where",
                placeholder = "Search city, area or landmark",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.weight(1f)
            )
            BadgedBox(
                badge = {
                    if (filters.activeCount > 0) {
                        Badge(containerColor = c.brandPrimary) {
                            Text(filters.activeCount.toString(), color = c.onBrandPrimary)
                        }
                    }
                }
            ) {
                FilledTonalIconButton(
                    onClick = onOpenFilters,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = c.brandPrimaryContainer,
                        contentColor = c.brandPrimary
                    )
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filters")
                }
            }
        }

        Text("Popular cities", style = HqType.labelLarge, color = c.textSecondary, modifier = Modifier.padding(horizontal = HqSpacing.lg, vertical = HqSpacing.sm))
        LazyRow(
            contentPadding = PaddingValues(horizontal = HqSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)
        ) {
            items(listOf("Hyderabad", "Bangalore", "Chennai", "Delhi", "Mumbai", "Pune")) { city ->
                HqChip(label = city, selected = filters.cityArea.equals(city, ignoreCase = true), onClick = {
                    onFiltersChange(filters.copy(cityArea = city))
                })
            }
        }
        if (filters.activeCount > 0) {
            ActiveFilterChips(filters, onFiltersChange)
        }

        ResultsHeader(
            shown = filteredVacancies.size,
            total = vacancies.size,
            hasFilters = filters.activeCount > 0
        )

        when {
            isLoading -> {
                DiscoveryLoading("Searching flats…")
            }
            loadError != null -> {
                DiscoveryError("Couldn't load results.", "Check your connection and try again.", onRetry)
            }
            filteredVacancies.isEmpty() -> {
                DiscoveryEmpty(
                    title = if (vacancies.isEmpty()) "No flats found" else "No flats match these filters",
                    body = if (vacancies.isEmpty()) {
                        "Try expanding your area later — listings appear when admins publish a vacancy."
                    } else {
                        "Try expanding your area or adjusting your budget."
                    },
                    actionLabel = if (filters.activeCount > 0) "Change filters" else null,
                    onAction = if (filters.activeCount > 0) onOpenFilters else null
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(start = HqSpacing.lg, end = HqSpacing.lg, top = HqSpacing.xs, bottom = HqSpacing.xxl),
                    verticalArrangement = Arrangement.spacedBy(HqSpacing.md)
                ) {
                    items(filteredVacancies, key = { it.flatId }) { listing ->
                        VacancyListingCard(listing, onOpenListing)
                    }
                }
            }
        }
    }
}

/** No Hq banner/alert component exists yet -- kept as a custom Surface, restyled with the warning tokens. */
@Composable
private fun SafetyBanner() {
    val c = LocalHqColors.current
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = HqSpacing.lg),
        color = c.warningContainer,
        shape = RoundedCornerShape(HqRadius.lg),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            Modifier.padding(horizontal = HqSpacing.md, vertical = HqSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)
        ) {
            Icon(Icons.Default.Info, null, tint = c.warning, modifier = Modifier.size(HqIconSize.sm))
            Column(Modifier.weight(1f)) {
                Text(
                    "Never pay rent before viewing in person.",
                    style = HqType.labelMedium,
                    color = c.textPrimary
                )
                HqTextButton(
                    text = "Safety tips",
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://habitiq.app/safety")))
                    }
                )
            }
        }
    }
}

@Composable
private fun ActiveFilterChips(filters: VacancyFilters, onChange: (VacancyFilters) -> Unit) {
    val chips = buildList {
        if (filters.rentMin.isNotBlank() || filters.rentMax.isNotBlank()) {
            add("Rent" to { onChange(filters.copy(rentMin = "", rentMax = "")) })
        }
        if (filters.genderPreference != "any") {
            add(DiscoverFilterLogic.formatGenderPreference(filters.genderPreference) to {
                onChange(filters.copy(genderPreference = "any"))
            })
        }
        if (filters.flatType != "any") {
            add(DiscoverFilterLogic.formatFlatType(filters.flatType) to { onChange(filters.copy(flatType = "any")) })
        }
        if (filters.roomType != "any") {
            val label = if (filters.roomType == "private") "Private room" else "Shared room"
            add(label to { onChange(filters.copy(roomType = "any")) })
        }
        filters.lifestyleTags.forEach { tag ->
            add(tag to { onChange(filters.copy(lifestyleTags = filters.lifestyleTags - tag)) })
        }
    }
    if (chips.isEmpty()) return

    LazyRow(
        contentPadding = PaddingValues(horizontal = HqSpacing.lg, vertical = HqSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)
    ) {
        items(chips) { (label, clear) ->
            // HqChip has no trailing-icon slot, so the clear affordance is folded into the label.
            HqChip(label = "$label  ×", selected = true, onClick = clear)
        }
        item {
            HqTextButton(text = "Clear all", onClick = { onChange(VacancyFilters()) })
        }
    }
}

@Composable
private fun ResultsHeader(shown: Int, total: Int, hasFilters: Boolean) {
    val c = LocalHqColors.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = HqSpacing.xl, vertical = HqSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (hasFilters) "$shown of $total flats" else "$total open ${if (total == 1) "room" else "rooms"}",
            style = HqType.labelLarge,
            color = c.textPrimary
        )
        Text("Find flat", style = HqType.caption, color = c.textTertiary)
    }
}

@Composable
fun VacancyListingCard(listing: VacancyListing, onOpen: (VacancyListing) -> Unit) {
    val c = LocalHqColors.current
    HqCard(onClick = { onOpen(listing) }, padding = 0.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            Box(Modifier.fillMaxWidth().height(148.dp).clip(RoundedCornerShape(topStart = HqRadius.lg, topEnd = HqRadius.lg))) {
                val cover = listing.photoUrls.firstOrNull()
                if (cover != null) {
                    AsyncImage(
                        model = cover,
                        contentDescription = "Photo of ${listing.flatName}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(c.surfaceSubtle),
                        error = ColorPainter(c.surfaceSubtle),
                        fallback = ColorPainter(c.surfaceSubtle)
                    )
                    if (listing.photoUrls.size > 1) {
                        Text(
                            "1/${listing.photoUrls.size}",
                            style = HqType.labelSmall,
                            color = c.textPrimary,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(HqSpacing.sm)
                                .background(c.surface.copy(alpha = 0.92f), RoundedCornerShape(HqRadius.full))
                                .padding(horizontal = HqSpacing.sm, vertical = HqSpacing.xs)
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(R.drawable.onboard_create),
                        contentDescription = "Illustration placeholder; this listing has no room photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    )
                }
            }
            Column(Modifier.padding(horizontal = HqSpacing.md).padding(bottom = HqSpacing.md), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        listing.flatName,
                        style = HqType.titleLarge,
                        color = c.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                        Icon(Icons.Default.LocationOn, null, tint = c.textSecondary, modifier = Modifier.size(HqIconSize.xs))
                        Text(
                            "${listing.area}, ${listing.city}".trim(',', ' '),
                            style = HqType.labelMedium,
                            color = c.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                listing.rentPerHead?.let { rent ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            formatInr(rent),
                            style = HqType.titleLarge,
                            color = c.brandPrimary
                        )
                        Text("/head", style = HqType.caption, color = c.textTertiary)
                    }
                }
            }

            if (listing.about.isNotBlank()) {
                Text(
                    listing.about,
                    style = HqType.bodySmall,
                    color = c.textSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val tags = listing.displayTags()
            if (tags.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                    items(tags) { tag ->
                        HqChip(label = tag, selected = true)
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HqChip(label = DiscoverFilterLogic.formatRoomType(listing.roomType, listing.bedsAvailable))
                if (listing.flatType.isNotBlank()) {
                    HqChip(label = DiscoverFilterLogic.formatFlatType(listing.flatType))
                }
                HqChip(label = DiscoverFilterLogic.formatGenderPreference(listing.preferredGender))
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                    Icon(Icons.Default.HomeWork, null, tint = c.info, modifier = Modifier.size(HqIconSize.xs))
                    val memberLabel = if (listing.memberCount > 0) {
                        "Habitiq member · ${listing.memberCount} in flat"
                    } else {
                        "Posted by Habitiq member"
                    }
                    Text(memberLabel, style = HqType.caption, color = c.textTertiary)
                }
            }
            listing.health?.headline?.takeIf { it.isNotBlank() }?.let { headline ->
                Text("Flat health · $headline", style = HqType.labelLarge, color = c.brandPrimary)
            }

            HqButton(text = "View flat", onClick = { onOpen(listing) })
            }
        }
    }
}

@Composable
private fun FlatDiscoveryBanner() {
    val c = LocalHqColors.current
    Box(
        Modifier.fillMaxWidth().padding(horizontal = HqSpacing.lg).height(132.dp)
            .clip(RoundedCornerShape(HqRadius.lg))
    ) {
        Image(
            painter = painterResource(R.drawable.onboard_join),
            contentDescription = "Flatmates welcoming someone home",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(c.textPrimary.copy(alpha = 0.78f), c.textPrimary.copy(alpha = 0.08f)))))
        Column(Modifier.align(Alignment.CenterStart).padding(HqSpacing.lg)) {
            Text("Verified homes", style = HqType.titleLarge, color = c.onBrandPrimary)
            Text("Real people. Better living.", style = HqType.bodyMedium, color = c.onBrandPrimary.copy(alpha = 0.9f))
        }
    }
}
