package habitiq.app.ui.discover

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.text.font.FontWeight
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqListingFrame
import habitiq.app.ui.components.HqSectionTitle
import habitiq.app.ui.components.HqChipRow
import habitiq.app.ui.components.HqSearchBar
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import habitiq.app.ui.components.HqTagFlow
import habitiq.app.ui.components.HqIllustration
import habitiq.app.ui.components.HqArt
import androidx.compose.ui.graphics.Color
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
    onOpenListing: (VacancyListing) -> Unit,
    onMyPosts: () -> Unit = {},
    onNearPlace: (() -> Unit)? = null,
    header: @Composable () -> Unit = {},
    listState: androidx.compose.foundation.lazy.LazyListState? = null,
) {
    val c = LocalHqColors.current
    // One scrolling list: the page header, search tools and results all move together.
    LazyColumn(Modifier.fillMaxSize(), state = listState ?: androidx.compose.foundation.lazy.rememberLazyListState(), contentPadding = PaddingValues(bottom = HqSpacing.xxl)) {
      item(key = "discover-top") {
       Column {
        header()
        SafetyBanner()
        Spacer(Modifier.height(HqSpacing.related))

        HqSearchBar(
            value = filters.cityArea,
            onValueChange = { onFiltersChange(filters.copy(cityArea = it)) },
            placeholder = "Search area or landmark",
            onFilters = onOpenFilters,
            activeFilters = filters.activeCount,
            modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal),
        )
        if (onNearPlace != null) {
            Spacer(Modifier.height(10.dp))
            Box(Modifier.padding(horizontal = HqSpacing.screenHorizontal)) {
                habitiq.app.ui.components.HqCalloutCard(
                    title = "Find a home near a place",
                    support = "Choose work, college, or any landmark",
                    icon = HqIcons.Pin,
                    tone = habitiq.app.ui.components.HqTileTone.Teal,
                    onClick = onNearPlace,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        HqChipRow(Modifier.padding(horizontal = HqSpacing.screenHorizontal)) {
            listOf("Hyderabad", "Bangalore", "Chennai", "Delhi", "Mumbai", "Pune").forEach { city ->
                HqChip(label = city, selected = filters.cityArea.equals(city, ignoreCase = true), onClick = {
                    onFiltersChange(filters.copy(cityArea = city))
                })
            }
        }
        HqSectionTitle("Places for you", action = "My posts", onAction = onMyPosts, modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal))
        if (filters.activeCount > 0) {
            ActiveFilterChips(filters, onFiltersChange)
        }

        ResultsHeader(
            shown = filteredVacancies.size,
            total = vacancies.size,
            hasFilters = filters.activeCount > 0
        )

       }
      }
        when {
            isLoading -> item { DiscoveryLoading("Searching flats…") }
            loadError != null -> item { DiscoveryError("Couldn't load results.", "Check your connection and try again.", onRetry) }
            filteredVacancies.isEmpty() -> item {
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
            else -> items(filteredVacancies, key = { it.flatId }) { listing ->
                Box(Modifier.padding(horizontal = HqSpacing.lg).padding(top = HqSpacing.xs, bottom = 15.dp)) {
                    VacancyListingCard(listing, onOpenListing)
                }
            }
        }
    }
}

/** One-line safety reminder with its link, in the warning tokens. Wraps naturally and never overlaps. */
@Composable
private fun SafetyBanner() {
    val c = LocalHqColors.current
    val context = LocalContext.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = HqSpacing.screenHorizontal)
            .clip(RoundedCornerShape(HqRadius.control)).background(c.statusWarningBg)
            .padding(start = HqSpacing.related, end = HqSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)
    ) {
        Icon(Icons.Default.Info, null, tint = c.statusWarningFg, modifier = Modifier.size(HqIconSize.sm))
        Text(
            "Never pay rent before viewing in person.",
            style = HqType.bodyMedium, color = c.textPrimary,
            modifier = Modifier.weight(1f).padding(vertical = HqSpacing.sm),
        )
        HqTextButton(
            text = "Safety tips",
            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://habitiq.app/safety"))) }
        )
    }
}

@Composable
private fun ActiveFilterChips(filters: VacancyFilters, onChange: (VacancyFilters) -> Unit) {
    val chips = buildList<Pair<String, () -> Unit>> {
        if (filters.cityArea.isNotBlank()) add(filters.cityArea to { onChange(filters.copy(cityArea = "")) })
        if (filters.rentMin.isNotBlank() || filters.rentMax.isNotBlank()) add("Rent" to { onChange(filters.copy(rentMin = "", rentMax = "")) })
        if (filters.genderPreference != "any") {
            add(DiscoverFilterLogic.formatGenderPreference(filters.genderPreference) to { onChange(filters.copy(genderPreference = "any")) })
        }
        if (filters.flatType != "any") add(DiscoverFilterLogic.formatFlatType(filters.flatType) to { onChange(filters.copy(flatType = "any")) })
        if (filters.roomType != "any") {
            add((if (filters.roomType == "private") "Private room" else "Shared room") to { onChange(filters.copy(roomType = "any")) })
        }
        filters.lifestyleTags.forEach { tag -> add(tag to { onChange(filters.copy(lifestyleTags = filters.lifestyleTags - tag)) }) }
    }
    AppliedFilters(chips) { onChange(VacancyFilters()) }
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
    }
}

@Composable
fun VacancyListingCard(listing: VacancyListing, onOpen: (VacancyListing) -> Unit) {
    val c = LocalHqColors.current
    val cover = listing.photoUrls.firstOrNull()
    HqListingFrame(
        onClick = { onOpen(listing) },
        photoHeight = if (cover != null) 178 else 130,
        photo = {
            if (cover != null) {
                AsyncImage(
                    model = cover,
                    contentDescription = "Photo of ${listing.flatName}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(c.surfaceSubtle),
                    error = ColorPainter(c.surfaceSubtle),
                    fallback = ColorPainter(c.surfaceSubtle),
                )
                if (listing.photoUrls.size > 1) {
                    Text(
                        "1 / ${listing.photoUrls.size}", style = HqType.labelSmall, color = Color.White, fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).clip(RoundedCornerShape(99.dp)).background(Color(0xCC142322)).padding(horizontal = 9.dp, vertical = 5.dp),
                    )
                }
            } else {
                HqIllustration(HqArt.Home, Modifier.align(Alignment.Center).size(96.dp))
            }
        },
        body = {
            Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.related), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(listing.flatName, style = HqType.titleSmall2, color = c.textPrimary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Text("${listing.area}, ${listing.city}".trim(',', ' '), style = HqType.bodyMedium, color = c.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                listing.rentPerHead?.let { rent ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatInr(rent), style = HqType.titleSmall2, color = c.textPrimary)
                        Text("per head", style = HqType.labelSmall, color = c.textMuted)
                    }
                }
            }
            Text(
                listOfNotNull(
                    DiscoverFilterLogic.formatRoomType(listing.roomType, listing.bedsAvailable),
                    listing.flatType.takeIf { it.isNotBlank() }?.let { DiscoverFilterLogic.formatFlatType(it) },
                    DiscoverFilterLogic.formatGenderPreference(listing.preferredGender),
                ).joinToString(" \u00b7 "),
                style = HqType.bodyMedium, color = c.textSecondary,
            )
            if (listing.about.isNotBlank()) {
                Text(listing.about, style = HqType.bodyMedium, color = c.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            HqTagFlow(listing.displayTags(), maxRows = 2, onMore = { onOpen(listing) })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (listing.memberCount > 0) "Hosted by an Oddroof member \u00b7 ${listing.memberCount} in flat" else "Posted by an Oddroof member",
                    style = HqType.labelSmall, color = c.textSecondary, modifier = Modifier.weight(1f),
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(HqIcons.Shield, null, tint = c.textBrand, modifier = Modifier.size(15.dp))
                    Text("Oddroof member", style = HqType.labelSmall, color = c.textBrand, fontWeight = FontWeight.Bold)
                }
            }
            listing.health?.headline?.takeIf { it.isNotBlank() }?.let { headline ->
                Text("Flat health \u00b7 $headline", style = HqType.labelMedium, color = c.textBrand)
            }
        },
    )
}
