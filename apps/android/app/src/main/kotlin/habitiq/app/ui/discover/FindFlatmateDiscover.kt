package habitiq.app.ui.discover

import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
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
    onConnect: (SeekerProfile) -> Unit,
    onMyPosts: () -> Unit = {},
) {
    val c = LocalHqColors.current
    Column(Modifier.fillMaxSize()) {
        habitiq.app.ui.components.HqSearchBar(
            value = filters.cityArea,
            onValueChange = { onFiltersChange(filters.copy(cityArea = it)) },
            placeholder = "Search people or area",
            onFilters = onOpenFilters,
            activeFilters = filters.activeCount,
            modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal),
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(14.dp))
        habitiq.app.ui.components.HqChipRow(Modifier.padding(horizontal = HqSpacing.screenHorizontal)) {
            listOf("Male", "Female", "Work from home", "Vegetarian", "No smoking", "Social").forEach { tag ->
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
        habitiq.app.ui.components.HqSectionTitle("People looking nearby", action = "My posts", onAction = onMyPosts, modifier = Modifier.padding(horizontal = HqSpacing.screenHorizontal))
        AppliedFilters(
            buildList<Pair<String, () -> Unit>> {
                if (filters.cityArea.isNotBlank()) add(filters.cityArea to { onFiltersChange(filters.copy(cityArea = "")) })
                if (filters.budgetMin.isNotBlank() || filters.budgetMax.isNotBlank()) add("Budget" to { onFiltersChange(filters.copy(budgetMin = "", budgetMax = "")) })
                if (filters.gender != "any") add(filters.gender.replaceFirstChar { it.uppercase() } to { onFiltersChange(filters.copy(gender = "any")) })
                filters.lifestyleTags.forEach { tag -> add(tag to { onFiltersChange(filters.copy(lifestyleTags = filters.lifestyleTags - tag)) }) }
            }
        ) { onFiltersChange(SeekerFilters()) }

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
    val name = seeker.displayName.ifBlank { "Oddroof user" }
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
    Column(
        Modifier.fillMaxWidth()
            .androidx_shadow(shape, c.textPrimary)
            .clip(shape).background(c.surfaceBase).border(1.dp, c.borderSubtle, shape)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onView)
            .padding(top = 15.dp, start = 15.dp, end = 15.dp, bottom = HqSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(HqSpacing.xs),
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            habitiq.app.ui.components.HqAvatar(name = name, size = habitiq.app.ui.components.HqAvatarSize.LG, tone = habitiq.app.ui.components.hqToneFor(name, false))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(name, style = HqType.titleSmall2.copy(fontSize = HqType.rowTitle.fontSize), color = c.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    listOf(
                        listOf(seeker.lookingIn, seeker.city).filter { it.isNotBlank() }.distinct().joinToString(", ").takeIf { it.isNotBlank() }?.let { "Looking in $it" },
                        seeker.budget.takeIf { it > 0 }?.let { "Budget ${formatInr(it)}" },
                    ).filterNotNull().joinToString(" \u00b7 ").ifBlank { "Location on request" },
                    style = HqType.bodyMedium, color = c.textSecondary,
                )
            }
            Icon(habitiq.app.ui.components.HqIcons.Chevron, null, tint = c.iconDefault, modifier = Modifier.padding(top = 4.dp).size(HqIconSize.sm))
        }
        TrustBadge(trust)
        CompatibilityBlock("Why you may fit", signals.take(3))
        if (seeker.bio.isNotBlank()) {
            Text(seeker.bio, style = HqType.bodyMedium, color = c.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        // The card opens the profile; one quiet action keeps the old Connect shortcut without a button pair.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            HqTextButton(text = "Connect", onClick = onConnect)
        }
    }
}

private fun Modifier.androidx_shadow(shape: androidx.compose.ui.graphics.Shape, tint: androidx.compose.ui.graphics.Color): Modifier =
    this.shadow(3.dp, shape, ambientColor = tint.copy(alpha = .06f), spotColor = tint.copy(alpha = .06f))

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
    Column(Modifier.fillMaxSize().background(c.canvas)) {
        HqBackAppBar(title = "Profile", onBack = onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.xl).padding(bottom = HqSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.md)
        ) {
            // Figma profile-person: centred identity block, then two key facts.
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                habitiq.app.ui.components.HqAvatar(name = name, size = habitiq.app.ui.components.HqAvatarSize.XL, tone = habitiq.app.ui.components.hqToneFor(name, false))
                Text(name, style = HqType.titleLarge2, color = c.textPrimary, modifier = Modifier.padding(top = 8.dp))
                Text(
                    listOf(seeker.city, seeker.lookingIn.takeIf { it.isNotBlank() }?.let { "Looking in $it" }).filterNotNull().filter { it.isNotBlank() }.distinct().joinToString(" \u00b7 "),
                    style = HqType.bodyMedium,
                    color = c.textSecondary
                )
                TrustBadge(trust)
            }
            val factShape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
            Row(Modifier.fillMaxWidth().padding(top = 8.dp).clip(factShape).border(1.dp, c.borderSubtle, factShape)) {
                Column(Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("BUDGET", style = HqType.labelSmall, color = c.textMuted, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                    Text(if (seeker.budget > 0) "Up to ${formatInr(seeker.budget)}" else "Not listed", style = HqType.rowTitle, color = c.textPrimary)
                }
                Box(Modifier.width(1.dp).height(60.dp).background(c.borderSubtle))
                Column(Modifier.weight(1f).padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("LOOKING IN", style = HqType.labelSmall, color = c.textMuted, fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold)
                    Text(seeker.lookingIn.ifBlank { seeker.city }.ifBlank { "Open to areas" }, style = HqType.rowTitle, color = c.textPrimary)
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
            Text(
                "Company, college and hometown are not shown — those fields are not on this profile yet.",
                style = HqType.caption,
                color = c.textMuted
            )
            Row(
                Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp)).background(c.statusWarningBg).padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(habitiq.app.ui.components.HqIcons.Shield, null, tint = c.statusWarningFg, modifier = Modifier.size(HqIconSize.sm))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Connect safely", style = HqType.labelMedium, color = c.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text("Personal contact details stay private until you choose to share them.", style = HqType.bodyMedium, color = c.textPrimary)
                }
            }
            if (isSelf) {
                Text("This is your looking post.", style = HqType.bodyMedium, color = c.textMuted)
            } else if (blocked) {
                Text("Blocked", style = HqType.titleMedium, color = c.statusDangerFg)
            } else {
                ConnectionActions(
                    status = connectionStatus,
                    incomingRequestId = incomingRequestId,
                    connectLabel = "Connect with ${name.substringBefore(" ")}",
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
