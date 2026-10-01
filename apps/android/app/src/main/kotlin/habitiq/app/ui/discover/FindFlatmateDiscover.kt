package habitiq.app.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import habitiq.app.R
import habitiq.app.data.SeekerProfile
import habitiq.app.lib.formatInr
import habitiq.app.discover.Compatibility
import habitiq.app.discover.ConnectionStatus
import habitiq.app.discover.SeekerFilters
import habitiq.app.discover.TrustCopy
import habitiq.app.discover.TrustPresentation
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

@Composable
fun FindFlatmateDiscoverContent(
    seekers: List<SeekerProfile>,
    filtered: List<SeekerProfile>,
    filters: SeekerFilters,
    onFiltersChange: (SeekerFilters) -> Unit,
    isLoading: Boolean,
    loadError: String?,
    onRetry: () -> Unit,
    viewerCity: String?,
    trustConsent: Boolean,
    onOpenFilters: () -> Unit,
    onOpenProfile: (SeekerProfile) -> Unit,
    onConnect: (SeekerProfile) -> Unit
) {
    val c = LocalHqColors.current
    Column(Modifier.fillMaxSize()) {
        PeopleDiscoveryBanner()
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
                placeholder = "Search by location or lifestyle",
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
                ) { Icon(Icons.Default.FilterList, contentDescription = "Filters") }
            }
        }

        Text("Popular searches", style = HqType.labelLarge, color = c.textSecondary, modifier = Modifier.padding(horizontal = HqSpacing.lg, vertical = HqSpacing.sm))
        LazyRow(
            contentPadding = PaddingValues(horizontal = HqSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)
        ) {
            items(listOf("Male", "Female", "Work from home", "Vegetarian", "No smoking", "Social")) { tag ->
                HqChip(
                    label = tag,
                    selected = filters.lifestyleTags.any { it.equals(tag, ignoreCase = true) } || filters.gender.equals(tag, ignoreCase = true),
                    onClick = {
                        val gender = tag.lowercase()
                        if (gender == "male" || gender == "female") {
                            onFiltersChange(filters.copy(gender = gender))
                        } else {
                            val next = filters.lifestyleTags.toMutableSet()
                            if (!next.add(tag)) next.remove(tag)
                            onFiltersChange(filters.copy(lifestyleTags = next))
                        }
                    }
                )
            }
        }

        if (filters.activeCount > 0) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = HqSpacing.lg, vertical = HqSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)
            ) {
                if (filters.budgetMin.isNotBlank() || filters.budgetMax.isNotBlank()) {
                    item {
                        HqChip(
                            label = "Budget  ×",
                            selected = true,
                            onClick = { onFiltersChange(filters.copy(budgetMin = "", budgetMax = "")) }
                        )
                    }
                }
                item {
                    HqTextButton(text = "Clear all", onClick = { onFiltersChange(SeekerFilters()) })
                }
            }
        }

        when {
            isLoading -> DiscoveryLoading("Finding people…")
            loadError != null -> DiscoveryError("Couldn't load results.", loadError, onRetry)
            filtered.isEmpty() -> DiscoveryEmpty(
                title = "No flatmates found",
                body = "Try removing one filter or expanding your search.",
                actionLabel = if (filters.activeCount > 0) "Edit filters" else null,
                onAction = if (filters.activeCount > 0) onOpenFilters else null,
                person = true
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(start = HqSpacing.lg, end = HqSpacing.lg, top = HqSpacing.sm, bottom = HqSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(HqSpacing.md)
            ) {
                items(filtered, key = { it.id }) { seeker ->
                    FlatmateCard(
                        seeker = seeker,
                        signals = Compatibility.seekerSignals(seeker, filters, viewerCity).map { it.label },
                        trust = TrustCopy.forSeeker(trustConsent),
                        onView = { onOpenProfile(seeker) },
                        onConnect = { onConnect(seeker) }
                    )
                }
            }
        }
    }
}

@Composable
fun FlatmateCard(
    seeker: SeekerProfile,
    signals: List<String>,
    trust: TrustPresentation,
    onView: () -> Unit,
    onConnect: () -> Unit
) {
    val c = LocalHqColors.current
    val name = seeker.displayName.ifBlank { "Habitiq user" }
    HqCard(onClick = onView) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(HqIconSize.xl + HqSpacing.xs).clip(CircleShape).background(c.brandPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString(""),
                    style = HqType.titleLarge,
                    color = c.brandPrimary
                )
            }
            Spacer(Modifier.width(HqSpacing.md))
            Column(Modifier.weight(1f)) {
                Text(name, style = HqType.titleLarge, color = c.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    listOf(seeker.lookingIn, seeker.city).filter { it.isNotBlank() }.distinct().joinToString(" · ").ifBlank { "Location on request" },
                    style = HqType.labelMedium,
                    color = c.textSecondary
                )
            }
        }
        Spacer(Modifier.height(HqSpacing.sm))
        TrustBadge(trust)
        if (seeker.budget > 0) {
            Spacer(Modifier.height(HqSpacing.xs))
            Text("Budget: ${formatInr(seeker.budget)}", style = HqType.labelLarge, color = c.brandPrimary)
        }
        Spacer(Modifier.height(HqSpacing.sm))
        CompatibilityBlock("Why you may fit", signals.take(3))
        if (seeker.bio.isNotBlank()) {
            Spacer(Modifier.height(HqSpacing.sm))
            Text(seeker.bio, style = HqType.bodySmall, color = c.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(HqSpacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            HqButton(text = "View profile", onClick = onView, variant = HqButtonVariant.Secondary, modifier = Modifier.weight(1f))
            HqButton(text = "Connect", onClick = onConnect, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun FlatmateProfileScreen(
    seeker: SeekerProfile,
    trust: TrustPresentation,
    signals: List<String>,
    connectionStatus: ConnectionStatus,
    incomingRequestId: String?,
    blocked: Boolean,
    isSelf: Boolean,
    onBack: () -> Unit,
    onConnect: () -> Unit,
    onMessage: () -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onReport: () -> Unit,
    onBlock: () -> Unit
) {
    val c = LocalHqColors.current
    val name = seeker.displayName.ifBlank { "Habitiq user" }
    Column(Modifier.fillMaxSize().background(c.background)) {
        HqBackAppBar(title = "Profile", onBack = onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.xl).padding(bottom = HqSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.md)
        ) {
            Box(
                Modifier.size(92.dp).clip(CircleShape).background(c.brandPrimaryContainer).align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString(""),
                    style = HqType.headlineLarge,
                    color = c.brandPrimary
                )
            }
            Text(name, style = HqType.headlineMedium, color = c.textPrimary)
            Text(
                listOf(seeker.lookingIn, seeker.city).filter { it.isNotBlank() }.distinct().joinToString(" · "),
                style = HqType.bodyMedium,
                color = c.textSecondary
            )
            TrustBadge(trust)
            if (seeker.budget > 0) Text("Budget ${formatInr(seeker.budget)}", style = HqType.titleMedium, color = c.brandPrimary)
            if (seeker.bio.isNotBlank()) {
                Text("About", style = HqType.titleMedium, color = c.textPrimary)
                Text(seeker.bio, style = HqType.bodyMedium, color = c.textSecondary)
            }
            if (seeker.lifestyleTags.isNotBlank()) {
                Text("Living preferences", style = HqType.titleMedium, color = c.textPrimary)
                Text(seeker.lifestyleTags, style = HqType.bodySmall, color = c.textSecondary)
            }
            Text("Looking for", style = HqType.titleMedium, color = c.textPrimary)
            Text(
                "${seeker.lookingIn.ifBlank { seeker.city }.ifBlank { "Open to areas" }} · ${if (seeker.budget > 0) formatInr(seeker.budget) else "Budget not listed"}",
                style = HqType.bodyMedium,
                color = c.textPrimary
            )
            CompatibilityBlock("Compatibility with your search", signals)
            Text(
                "Company, college and hometown are not shown — those fields are not on this profile yet.",
                style = HqType.caption,
                color = c.textTertiary
            )
            if (isSelf) {
                Text("This is your looking post.", style = HqType.bodyMedium, color = c.textTertiary)
            } else if (blocked) {
                Text("Blocked", style = HqType.titleMedium, color = c.error)
            } else {
                ConnectionActions(
                    status = connectionStatus,
                    incomingRequestId = incomingRequestId,
                    connectLabel = "Connect",
                    onConnect = onConnect,
                    onMessage = onMessage,
                    onAccept = onAccept,
                    onDecline = onDecline
                )
                Row {
                    HqTextButton(text = "Report", onClick = onReport)
                    HqTextButton(text = "Block", onClick = onBlock)
                }
            }
        }
    }
}

@Composable
private fun PeopleDiscoveryBanner() {
    val c = LocalHqColors.current
    Box(
        Modifier.fillMaxWidth().padding(horizontal = HqSpacing.lg).height(124.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(habitiq.app.ui.theme.HqRadius.lg))
    ) {
        Image(
            painter = painterResource(R.drawable.onboard_join),
            contentDescription = "A group of welcoming flatmates",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        )
        Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(c.textPrimary.copy(alpha = 0.78f), c.textPrimary.copy(alpha = 0.05f)))))
        Column(Modifier.align(Alignment.CenterStart).padding(HqSpacing.lg)) {
            Text("Find the right people", style = HqType.titleLarge, color = c.onBrandPrimary)
            Text("Live with people who match your lifestyle.", style = HqType.bodySmall, color = c.onBrandPrimary.copy(alpha = 0.9f))
        }
    }
}
