package habitiq.app.ui.discover

import androidx.compose.material3.Icon
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.components.HqTagFlow
import habitiq.app.ui.components.HqSectionTitle
import habitiq.app.ui.components.HqBadgeTone
import habitiq.app.ui.components.HqBadge
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqHeroBleedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import habitiq.app.R
import habitiq.app.data.VacancyListing
import habitiq.app.lib.formatInr
import habitiq.app.discover.CompatibilitySignal
import habitiq.app.discover.ConnectionStatus
import habitiq.app.discover.DiscoverFilterLogic
import habitiq.app.discover.TrustPresentation
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import coil3.compose.AsyncImage

@Composable
fun FlatListingDetailScreen(
    listing: VacancyListing,
    trust: TrustPresentation,
    signals: List<CompatibilitySignal>,
    connectionStatus: ConnectionStatus,
    incomingRequestId: String?,
    blocked: Boolean,
    isOwnListing: Boolean,
    interestedCount: Int,
    interestedPeople: List<Pair<String, String>>,
    onOpenInterested: (String) -> Unit,
    onBack: () -> Unit,
    onConnect: () -> Unit,
    onMessage: () -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onReport: () -> Unit,
    onBlock: () -> Unit,
    saved: Boolean = false,
    onToggleSave: (() -> Unit)? = null,
) {
    val c = LocalHqColors.current
    val approx = listOf(listing.area, listing.city).filter { it.isNotBlank() }.joinToString(" · ")
    HqHeroBleedEffect()
    Column(Modifier.fillMaxSize().background(c.canvas).verticalScroll(rememberScrollState())) {
        // Figma detail photo: full-bleed 310dp, floating round back button, photo counter.
        Box(Modifier.fillMaxWidth().height(310.dp).background(c.selectedBg)) {
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
                        "1 / ${listing.photoUrls.size}",
                        modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
                            .background(Color(0xCC142322), RoundedCornerShape(HqRadius.full))
                            .padding(horizontal = 11.dp, vertical = 6.dp),
                        style = HqType.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
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
            Box(
                Modifier.statusBarsPadding().padding(start = 18.dp, top = 12.dp).size(42.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = .92f))
                    .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onBack)
                    .semantics { contentDescription = "Back" },
                contentAlignment = Alignment.Center,
            ) { Icon(HqIcons.Back, null, tint = c.textPrimary, modifier = Modifier.size(HqIconSize.md)) }
            if (onToggleSave != null) {
                Box(
                    Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(end = 18.dp, top = 12.dp).size(42.dp).clip(CircleShape)
                        .background(Color.White.copy(alpha = if (saved) 1f else .92f))
                        .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onToggleSave)
                        .semantics { contentDescription = if (saved) "Remove from saved flats" else "Save flat" },
                    contentAlignment = Alignment.Center,
                ) { Icon(HqIcons.Heart, null, tint = if (saved) Color(0xFFD9584B) else c.textPrimary, modifier = Modifier.size(21.dp)) }
            }
        }
        Column(
            Modifier.padding(horizontal = HqSpacing.xl).padding(top = 18.dp, bottom = HqSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.md)
        ) {
            HqBadge(DiscoverFilterLogic.formatRoomType(listing.roomType, listing.bedsAvailable), HqBadgeTone.Success)
            Text(listing.flatName, style = HqType.titleLarge2, color = c.textPrimary)
            Text(
                approx.ifBlank { "Approximate location shared later" } + " · Approximate area",
                style = HqType.bodyMedium, color = c.textSecondary,
            )

            if (isOwnListing) {
                Text("This is your vacancy.", style = HqType.bodyMedium, color = c.textMuted)
            } else if (blocked) {
                Text("Blocked", style = HqType.rowTitle, color = c.statusDangerFg)
            } else {
                ConnectionActions(
                    status = connectionStatus,
                    incomingRequestId = incomingRequestId,
                    connectLabel = "Connect with this flat",
                    onConnect = onConnect,
                    onMessage = onMessage,
                    onAccept = onAccept,
                    onDecline = onDecline
                )
            }

            // Figma key facts: three bordered cells sharing one card.
            val shape = RoundedCornerShape(18.dp)
            Row(Modifier.fillMaxWidth().clip(shape).border(1.dp, c.borderSubtle, shape)) {
                KeyFact(Modifier.weight(1f), "RENT / HEAD", listing.rentPerHead?.let { formatInr(it) } ?: "On request", divider = true)
                KeyFact(Modifier.weight(1f), "ROOM", if (listing.roomType == "private") "Private" else "Shared", divider = true)
                KeyFact(Modifier.weight(1f), "FLAT", listing.flatType.takeIf { it.isNotBlank() }?.let { DiscoverFilterLogic.formatFlatType(it) } ?: "Any", divider = false)
            }

            if (listing.about.isNotBlank()) {
                HqSectionTitle("About this flat")
                Text(listing.about, style = HqType.bodyLarge, color = c.textSecondary)
            }
            if (listing.displayTags().isNotEmpty()) {
                HqSectionTitle("Lifestyle")
                HqTagFlow(listing.displayTags())
            }
            TrustBadge(trust)
            FlatHealthSection(listing)
            CurrentMembersSection(listing)
            InterestedPeopleSection(
                isOwnListing = isOwnListing,
                count = interestedCount,
                people = interestedPeople,
                onOpen = onOpenInterested
            )
            CompatibilityBlock("Why this may fit you", signals.map { it.label })

            // Figma safety banner.
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.statusWarningBg).padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(HqIcons.Shield, null, tint = c.statusWarningFg, modifier = Modifier.size(HqIconSize.sm))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Your safety matters", style = HqType.labelMedium, color = c.textPrimary, fontWeight = FontWeight.Bold)
                    Text(
                        "Keep chats in Oddroof. Never pay before viewing. Share an exact address only after you are comfortable.",
                        style = HqType.bodyMedium, color = c.textPrimary,
                    )
                }
            }
            if (!isOwnListing && !blocked) {
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
                    HqTextButton(text = "Report listing", onClick = onReport)
                    HqTextButton(text = "Block", onClick = onBlock)
                }
            }
            Text(
                "Matching here does not add anyone to the flat. Join still uses the flat invite flow.",
                style = HqType.labelSmall,
                color = c.textMuted
            )
        }
    }
}

@Composable
private fun KeyFact(modifier: Modifier, label: String, value: String, divider: Boolean) {
    val c = LocalHqColors.current
    Row(modifier) {
        Column(Modifier.weight(1f).padding(horizontal = 10.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, style = HqType.labelSmall, color = c.textMuted, fontWeight = FontWeight.ExtraBold)
            Text(value, style = HqType.titleSmall2.copy(fontSize = HqType.labelMedium.fontSize), color = c.textPrimary)
        }
        if (divider) Box(Modifier.width(1.dp).height(56.dp).background(c.borderSubtle))
    }
}

@Composable
private fun FlatHealthSection(listing: VacancyListing) {
    val c = LocalHqColors.current
    val health = listing.health
    Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
        Text("How this flat is doing", style = HqType.titleMedium, color = c.textPrimary)
        Text(
            health?.headline ?: "Limited history",
            style = HqType.titleLarge,
            color = c.textBrand
        )
        Text(
            "Qualitative signals from Oddroof activity — not a score, not a ranking against other flats.",
            style = HqType.caption,
            color = c.textMuted
        )
        (health ?: habitiq.app.discover.FlatHealthSnapshot.empty()).rows().forEach { (area, signal) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(area, style = HqType.bodySmall, color = c.textSecondary)
                Text(signal, style = HqType.labelLarge, color = c.textPrimary)
            }
        }
    }
}

@Composable
private fun CurrentMembersSection(listing: VacancyListing) {
    val c = LocalHqColors.current
    val names = listing.health?.members.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
        Text("Current members", style = HqType.titleMedium, color = c.textPrimary)
        if (names.isEmpty()) {
            Text(
                if (listing.memberCount > 0) "${listing.memberCount} people live here. Names are shared when the admin publishes health."
                else "Member list not published yet.",
                style = HqType.bodySmall,
                color = c.textSecondary
            )
        } else {
            names.forEach { member ->
                HqCard(variant = HqCardVariant.Standard, padding = HqSpacing.md) {
                    Text(
                        "${member.nickname} · ${if (member.role == "admin") "Admin" else "Member"}",
                        style = HqType.bodyMedium,
                        color = c.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun InterestedPeopleSection(
    isOwnListing: Boolean,
    count: Int,
    people: List<Pair<String, String>>,
    onOpen: (String) -> Unit
) {
    val c = LocalHqColors.current
    Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
        Text("People interested", style = HqType.titleMedium, color = c.textPrimary)
        if (isOwnListing) {
            if (people.isEmpty()) {
                Text("No connection requests yet.", style = HqType.bodySmall, color = c.textSecondary)
            } else {
                people.forEach { (id, name) ->
                    HqCard(variant = HqCardVariant.Interactive, onClick = { onOpen(id) }, padding = HqSpacing.md) {
                        Text(name, style = HqType.labelLarge, color = c.textPrimary)
                    }
                }
            }
        } else {
            Text(
                if (count > 0) "$count ${if (count == 1) "person has" else "people have"} asked to connect. Names stay private to the flat admin."
                else "Be the first to ask to connect.",
                style = HqType.bodySmall,
                color = c.textSecondary
            )
        }
    }
}

@Composable
fun ConnectionActions(
    status: ConnectionStatus,
    incomingRequestId: String?,
    connectLabel: String,
    onConnect: () -> Unit,
    onMessage: () -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit
) {
    val c = LocalHqColors.current
    when (status) {
        ConnectionStatus.NONE, ConnectionStatus.DECLINED, ConnectionStatus.EXPIRED -> {
            HqButton(text = connectLabel, onClick = onConnect)
        }
        ConnectionStatus.REQUEST_SENT -> {
            if (incomingRequestId != null) {
                Text("They want to connect", style = HqType.titleMedium, color = c.textPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    HqButton(text = "Accept", onClick = { onAccept(incomingRequestId) }, modifier = Modifier.weight(1f))
                    HqButton(text = "Decline", onClick = { onDecline(incomingRequestId) }, variant = HqButtonVariant.Secondary, modifier = Modifier.weight(1f))
                }
            } else {
                HqButton(text = "Request sent", onClick = {}, enabled = false, variant = HqButtonVariant.Secondary)
            }
        }
        ConnectionStatus.ACCEPTED, ConnectionStatus.CONVERSATION_OPEN, ConnectionStatus.MATCHED -> {
            HqButton(text = if (status == ConnectionStatus.MATCHED) "Matched · Message" else "Message", onClick = onMessage)
        }
        ConnectionStatus.BLOCKED -> Text("Blocked", style = HqType.titleMedium, color = c.statusDangerFg)
    }
}

@Composable
fun MetaChip(text: String) {
    HqChip(label = text)
}
