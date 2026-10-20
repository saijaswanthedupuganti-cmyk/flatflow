package habitiq.app.ui.discover

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import habitiq.app.data.SeekerProfile
import habitiq.app.lib.formatInr
import habitiq.app.discover.Compatibility
import habitiq.app.discover.ConnectionStatus
import habitiq.app.discover.SeekerFilters
import habitiq.app.discover.TrustCopy
import habitiq.app.discover.TrustPresentation
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqIconButton
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqShapes
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
    onConnect: (SeekerProfile) -> Unit,
    onMyPosts: () -> Unit = {},
    header: @Composable () -> Unit = {},
    listState: androidx.compose.foundation.lazy.LazyListState? = null,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        state = listState ?: androidx.compose.foundation.lazy.rememberLazyListState(),
        contentPadding = PaddingValues(bottom = HqSpacing.fabClearance),
        verticalArrangement = Arrangement.spacedBy(HqSpacing.lg),
    ) {
        item(key = "discover-top") {
            Column {
                header()
                habitiq.app.ui.components.HqSearchBar(
                    value = filters.cityArea,
                    onValueChange = { onFiltersChange(filters.copy(cityArea = it)) },
                    placeholder = "Search an area or landmark",
                    onFilters = onOpenFilters,
                    activeFilters = filters.activeCount,
                    modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal),
                )
                AppliedFilterRow(
                    buildList<Pair<String, () -> Unit>> {
                        if (filters.budgetMin.isNotBlank() || filters.budgetMax.isNotBlank()) {
                            add(habitiq.app.discover.DiscoverFilterLogic.budgetLabel(filters.budgetMin, filters.budgetMax) to { onFiltersChange(filters.copy(budgetMin = "", budgetMax = "")) })
                        }
                        if (filters.gender != "any") add(habitiq.app.discover.ListingFacts.suitsLabel(filters.gender) to { onFiltersChange(filters.copy(gender = "any")) })
                        filters.lifestyleTags.forEach { tag -> add(tag to { onFiltersChange(filters.copy(lifestyleTags = filters.lifestyleTags - tag)) }) }
                    }
                ) { onFiltersChange(SeekerFilters(cityArea = filters.cityArea)) }
                ResultsRow(
                    text = when {
                        isLoading -> "Searching…"
                        filters.activeCount > 0 || filters.cityArea.isNotBlank() -> "${filtered.size} of ${seekers.size} people"
                        else -> "${seekers.size} ${if (seekers.size == 1) "person" else "people"} looking"
                    },
                    onNearPlace = null,
                    onMyPosts = onMyPosts,
                )
            }
        }
        when {
            isLoading -> item { DiscoveryLoading("Finding people…") }
            loadError != null -> item { DiscoveryError("Couldn't load results.", loadError, onRetry) }
            filtered.isEmpty() -> item {
                DiscoveryEmpty(
                    title = "No flatmates found",
                    body = "Try removing one filter or a wider area.",
                    actionLabel = if (filters.activeCount > 0) "Change filters" else null,
                    onAction = if (filters.activeCount > 0) onOpenFilters else null,
                    person = true
                )
            }
            else -> items(filtered, key = { it.id }) { seeker ->
                FlatmateCard(
                    seeker = seeker,
                    signals = Compatibility.seekerSignals(seeker, filters, viewerCity).map { it.label },
                    trust = TrustCopy.forSeeker(trustConsent),
                    onView = { onOpenProfile(seeker) },
                    onConnect = { onConnect(seeker) },
                    modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal),
                )
            }
        }
    }
}

/**
 * The flatmate result, in the same card as a flat: name + budget on one line, one facts line, the areas,
 * at most three calm tags in one row (+N), one trust line. People don't get a photo slot (privacy), the
 * avatar sits beside the name instead. The whole card opens the profile, where "Send request" lives.
 * [onView] null = preview, not tappable.
 */
@Composable
fun FlatmateCard(
    seeker: SeekerProfile,
    signals: List<String>,
    trust: TrustPresentation,
    onView: (() -> Unit)?,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalHqColors.current
    val name = seeker.displayName.ifBlank { "Oddroof user" }
    val tags = (signals + seeker.lifestyleTags.split(",", ";").map { it.trim() }).filter { it.isNotEmpty() }.distinctBy { it.lowercase() }
    DiscoverCardShell(onClick = onView, clickLabel = "View $name", modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
            habitiq.app.ui.components.HqAvatar(name = name, size = habitiq.app.ui.components.HqAvatarSize.MD, tone = habitiq.app.ui.components.hqToneFor(name, false))
            Box(Modifier.weight(1f)) {
                TitleAmountRow(
                    title = name,
                    amount = seeker.budget.takeIf { it > 0 }?.let { formatInr(it) },
                    amountUnit = seeker.budget.takeIf { it > 0 }?.let { "/mo" },
                )
            }
        }
        listOfNotNull(
            "Looking for a room",
            habitiq.app.ui.genderLabel(seeker.gender).takeIf { habitiq.app.ui.normalizeGender(seeker.gender) != "any" },
        ).joinToString(" · ").let { Text(it, style = HqType.bodyMedium, color = c.textPrimary) }
        listOf(seeker.lookingIn, seeker.city).map { it.trim() }.filter { it.isNotBlank() }.distinct().joinToString(", ")
            .takeIf { it.isNotBlank() }?.let { IconLine(habitiq.app.ui.components.HqIcons.Pin, it) }
        if (tags.isNotEmpty()) {
            Spacer(Modifier.height(HqSpacing.xs / 2))
            OneRowTags(tags)
        }
        Spacer(Modifier.height(HqSpacing.xs / 2))
        IconLine(habitiq.app.ui.components.HqIcons.Shield, trust.label, tint = c.textBrand, style = HqType.labelSmall)
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
    val name = seeker.displayName.ifBlank { "Oddroof user" }
    var menuOpen by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(c.canvas)) {
        // Report and Block live in the overflow menu so they never compete with Send request.
        HqBackAppBar(title = "Profile", onBack = onBack, actions = {
            if (!isSelf && !blocked) {
                Box {
                    HqIconButton(icon = habitiq.app.ui.components.HqIcons.More, contentDescription = "More options for $name", onClick = { menuOpen = true })
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = c.surfaceRaised) {
                        DropdownMenuItem(text = { Text("Report", style = HqType.bodyLarge, color = c.textPrimary) }, onClick = { menuOpen = false; onReport() })
                        DropdownMenuItem(text = { Text("Block", style = HqType.bodyLarge, color = c.statusDangerFg) }, onClick = { menuOpen = false; onBlock() })
                    }
                }
            }
        })
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.xl).padding(bottom = HqSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.md)
        ) {
            // Figma profile-person: centred identity block, then the key facts the person gave.
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                habitiq.app.ui.components.HqAvatar(name = name, size = habitiq.app.ui.components.HqAvatarSize.XL, tone = habitiq.app.ui.components.hqToneFor(name, false))
                Text(name, style = HqType.titleLarge2, color = c.textPrimary, modifier = Modifier.padding(top = HqSpacing.sm))
                listOf(seeker.city, seeker.lookingIn.takeIf { it.isNotBlank() }?.let { "Looking in $it" }).filterNotNull().filter { it.isNotBlank() }.distinct()
                    .joinToString(" \u00b7 ").takeIf { it.isNotBlank() }
                    ?.let { Text(it, style = HqType.bodyMedium, color = c.textSecondary) }
                TrustBadge(trust)
            }
            // Only facts the person gave; nothing is shown in place of a missing one.
            val facts = listOfNotNull(
                seeker.budget.takeIf { it > 0 }?.let { "Budget" to "Up to ${formatInr(it)}" },
                seeker.lookingIn.ifBlank { seeker.city }.takeIf { it.isNotBlank() }?.let { "Looking in" to it },
            )
            if (facts.isNotEmpty()) {
                val factShape = HqShapes.card
                Row(Modifier.fillMaxWidth().padding(top = HqSpacing.sm).height(IntrinsicSize.Min).clip(factShape).border(1.dp, c.borderSubtle, factShape)) {
                    facts.forEachIndexed { i, (label, value) ->
                        if (i > 0) Box(Modifier.width(1.dp).fillMaxHeight().background(c.borderSubtle))
                        Column(Modifier.weight(1f).padding(HqSpacing.md), verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                            Text(label, style = HqType.labelMedium, color = c.textSecondary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            Text(value, style = HqType.rowTitle, color = c.textPrimary)
                        }
                    }
                }
            }
            if (seeker.bio.isNotBlank()) {
                habitiq.app.ui.components.HqSectionTitle("About")
                Text(seeker.bio, style = HqType.bodyLarge, color = c.textSecondary)
            }
            if (seeker.lifestyleTags.isNotBlank()) {
                habitiq.app.ui.components.HqSectionTitle("Lifestyle")
                habitiq.app.ui.components.HqTagFlow(seeker.lifestyleTags.split(",").map { it.trim() }.filter { it.isNotEmpty() })
            }
            CompatibilityBlock("Compatibility with your search", signals)
            Row(
                Modifier.fillMaxWidth().clip(HqShapes.control).background(c.statusWarningBg).padding(HqSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
            ) {
                Icon(habitiq.app.ui.components.HqIcons.Shield, null, tint = c.statusWarningFg, modifier = Modifier.size(HqIconSize.sm))
                Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.xs / 2)) {
                    Text("Stay safe", style = HqType.labelMedium, color = c.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text("Personal contact details stay private until you choose to share them.", style = HqType.bodyMedium, color = c.textPrimary)
                }
            }
            if (isSelf) {
                Text("This is your post.", style = HqType.bodyMedium, color = c.textMuted)
            } else if (blocked) {
                Text("Blocked", style = HqType.titleMedium, color = c.statusDangerFg)
            } else {
                ConnectionActions(
                    status = connectionStatus,
                    incomingRequestId = incomingRequestId,
                    connectLabel = "Send request to ${name.substringBefore(" ")}",
                    onConnect = onConnect,
                    onMessage = onMessage,
                    onAccept = onAccept,
                    onDecline = onDecline
                )
            }
        }
    }
}
