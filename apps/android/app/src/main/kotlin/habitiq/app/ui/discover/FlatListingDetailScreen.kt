package habitiq.app.ui.discover

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import habitiq.app.data.VacancyListing
import habitiq.app.discover.CompatibilitySignal
import habitiq.app.discover.ConnectionStatus
import habitiq.app.discover.ListingFacts
import habitiq.app.discover.TrustPresentation
import habitiq.app.lib.formatInr
import habitiq.app.ui.components.HqAvatar
import habitiq.app.ui.components.HqAvatarSize
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonPair
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqFact
import habitiq.app.ui.components.HqGroup
import habitiq.app.ui.components.HqHeroBleedEffect
import habitiq.app.ui.components.HqIconButton
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqRow
import habitiq.app.ui.components.HqRowChevron
import habitiq.app.ui.components.HqRowDivider
import habitiq.app.ui.components.HqRowValue
import habitiq.app.ui.components.HqTagFlow
import habitiq.app.ui.theme.HqShapes
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * A room listing: hero photo, title and area, key facts, rent breakdown, grouped tags, who lives
 * here, trust and safety. One sticky primary at the bottom ("Send request", or the request's status).
 * Share, report and block live in the overflow menu so they never compete with the primary.
 */
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
    HqHeroBleedEffect()
    Column(Modifier.fillMaxSize().background(c.canvas)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            ListingHero(listing, onBack, saved, onToggleSave, canReport = !isOwnListing && !blocked, onReport = onReport, onBlock = onBlock)
            Column(
                Modifier.padding(horizontal = HqSpacing.screenHorizontal).padding(top = HqSpacing.xl, bottom = HqSpacing.xxxl),
                verticalArrangement = Arrangement.spacedBy(HqSpacing.xxl),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                    Text(listing.flatName.ifBlank { "Room in a flat" }, style = HqType.titleMedium2, color = c.textPrimary, modifier = Modifier.semantics { heading() })
                    ListingFacts.areaLine(listing).takeIf { it.isNotBlank() }?.let { IconLine(HqIcons.Pin, "$it · approximate area") }
                }
                KeyFacts(listing)
                RentBreakdown(listing)
                if (listing.about.isNotBlank()) {
                    Section("About this room") { Text(listing.about, style = HqType.bodyLarge, color = c.textSecondary) }
                }
                TagSections(listing)
                // Room type and area are already in the facts and the area line above; only new reasons show here.
                CompatibilityBlock("Why this may fit you", signals.filter { it.id != "room" && it.id != "location" }.map { it.label })
                WhoLivesHere(listing)
                Interested(isOwnListing, interestedCount, interestedPeople, onOpenInterested)
                Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    TrustBadge(trust)
                    SafetyNote()
                }
            }
        }
        // Sticky action: one primary in the thumb zone.
        Column(Modifier.fillMaxWidth().background(c.canvas).navigationBarsPadding()) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderSubtle))
            Column(Modifier.padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.md)) {
                when {
                    isOwnListing -> Text("This is your room listing.", style = HqType.bodyMedium, color = c.textSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = HqSpacing.md))
                    blocked -> Text("You blocked this flat.", style = HqType.bodyMedium, color = c.statusDangerFg, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = HqSpacing.md))
                    else -> ConnectionActions(connectionStatus, incomingRequestId, "Send request", onConnect, onMessage, onAccept, onDecline)
                }
            }
        }
    }
}

@Composable
private fun ListingHero(
    listing: VacancyListing,
    onBack: () -> Unit,
    saved: Boolean,
    onToggleSave: (() -> Unit)?,
    canReport: Boolean,
    onReport: () -> Unit,
    onBlock: () -> Unit,
) {
    val c = LocalHqColors.current
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth().aspectRatio(4f / 3f)) {
        val cover = listing.photoUrls.firstOrNull()
        // The listing's own placeholder: shown with no photo, while the photo loads, and if it fails.
        ListingPlaceholder(listing.flatId)
        if (cover != null) {
            AsyncImage(
                model = cover, contentDescription = "Photo of ${listing.flatName}",
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            )
        }
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = HqSpacing.sm, vertical = HqSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeroButton { HqIconButton(icon = HqIcons.Back, contentDescription = "Back", onClick = onBack) }
            Spacer(Modifier.weight(1f))
            if (onToggleSave != null) {
                HeroButton {
                    HqIconButton(
                        icon = if (saved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (saved) "Remove from saved flats" else "Save flat",
                        onClick = onToggleSave,
                        tint = if (saved) c.statusDangerFg else c.iconDefault,
                    )
                }
                Spacer(Modifier.size(HqSpacing.sm))
            }
            Box {
                HeroButton { HqIconButton(icon = HqIcons.More, contentDescription = "More options for this listing", onClick = { menuOpen = true }) }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = c.surfaceRaised) {
                    DropdownMenuItem(text = { Text("Share listing", style = HqType.bodyLarge, color = c.textPrimary) }, onClick = {
                        menuOpen = false
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Have a look at ${listing.flatName} on Oddroof.")
                        }
                        context.startActivity(Intent.createChooser(send, "Share listing"))
                    })
                    if (canReport) {
                        DropdownMenuItem(text = { Text("Report listing", style = HqType.bodyLarge, color = c.textPrimary) }, onClick = { menuOpen = false; onReport() })
                        DropdownMenuItem(text = { Text("Block", style = HqType.bodyLarge, color = c.statusDangerFg) }, onClick = { menuOpen = false; onBlock() })
                    }
                }
            }
        }
        if (listing.photoUrls.size > 1) {
            Text(
                "${listing.photoUrls.size} photos", style = HqType.labelSmall, color = c.onBrandInk,
                modifier = Modifier.align(Alignment.BottomStart).padding(HqSpacing.lg)
                    .clip(HqShapes.pill).background(c.brandInk.copy(alpha = 0.72f))
                    .padding(horizontal = HqSpacing.sm, vertical = HqSpacing.xs / 2),
            )
        }
    }
}

/** A light disc behind a hero icon so it reads on any photo. */
@Composable
private fun HeroButton(content: @Composable () -> Unit) {
    Box(Modifier.clip(HqShapes.pill).background(LocalHqColors.current.surfaceBase.copy(alpha = 0.92f))) { content() }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
        Text(title, style = HqType.titleSmall2, color = LocalHqColors.current.textPrimary, modifier = Modifier.semantics { heading() })
        content()
    }
}

/** Up to four facts in a 2 x 2 block: room, beds, furnishing, when it's free. Facts the flat didn't give are left out. */
@Composable
private fun KeyFacts(listing: VacancyListing) {
    val c = LocalHqColors.current
    val facts = listOfNotNull(
        ListingFacts.roomTypeLabel(listing.roomType)?.let { it to "Room" },
        listing.bedsAvailable.takeIf { it > 0 }?.let { "$it free" to "Beds" },
        ListingFacts.furnishingLabel(listing.furnishing)?.let { it to "Furnishing" },
        ListingFacts.availabilityLabel(listing.availableFrom, listing.moveInTiming)
            ?.removePrefix("Available ")?.replaceFirstChar { it.uppercase() }?.let { it to "Available" },
    )
    if (facts.isEmpty()) return
    Column(
        Modifier.fillMaxWidth().clip(HqShapes.card).background(c.surfaceBase).border(1.dp, c.borderSubtle, HqShapes.card).padding(HqSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(HqSpacing.lg),
    ) {
        facts.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.lg)) {
                row.forEach { (value, label) -> HqFact(value = value, label = label, modifier = Modifier.weight(1f)) }
                // An odd last fact keeps half the width, so the columns stay aligned.
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** Only the money facts the flat gave; the section is left out when there are none. */
@Composable
private fun RentBreakdown(listing: VacancyListing) {
    val c = LocalHqColors.current
    val rows = listOfNotNull(
        listing.rentPerHead?.let { Triple("Rent per person", "${formatInr(it)}/mo", c.textPrimary) },
        listing.securityDeposit?.let { Triple("Security deposit", formatInr(it), null) },
        ListingFacts.noticeLabel(listing.noticePeriod)?.let { Triple("Notice period", it, null) },
    )
    if (rows.isEmpty()) return
    Section("Rent") {
        HqGroup {
            rows.forEachIndexed { i, (title, value, color) ->
                if (i > 0) HqRowDivider()
                HqRow(title = title, trailing = { if (color != null) HqRowValue(value, color) else HqRowValue(value) })
            }
        }
    }
}

/** Tags grouped under headings, so each one says what kind of thing it is. */
@Composable
private fun TagSections(listing: VacancyListing) {
    val suits = listOfNotNull(
        ListingFacts.suitsLabel(listing.preferredGender).let { if (it == "Anyone") "Open to anyone" else "$it only" },
        ListingFacts.occupationLabel(listing.preferredOccupation),
    )
    val rules = listing.lifestyle.map { it.trim() }.filter { it.isNotEmpty() }
    val inFlat = listing.amenities.map { it.trim() }.filter { it.isNotEmpty() }
    // Older listings only have generated tags; show what's left after the facts above.
    val other = if (rules.isEmpty() && inFlat.isEmpty()) ListingFacts.cardTags(listing) else emptyList()
    Section("Who it suits") { HqTagFlow(suits) }
    if (inFlat.isNotEmpty()) Section("In the flat") { HqTagFlow(inFlat) }
    if (rules.isNotEmpty()) Section("House rules") { HqTagFlow(rules) }
    if (other.isNotEmpty()) Section("More about it") { HqTagFlow(other) }
}

@Composable
private fun WhoLivesHere(listing: VacancyListing) {
    val c = LocalHqColors.current
    val names = listing.health?.members.orEmpty()
    val headline = listing.health?.headline?.takeIf { it.isNotBlank() }
    // Nothing known about the people here: leave the section out rather than say so.
    if (names.isEmpty() && headline == null && listing.memberCount <= 0) return
    Section(if (listing.memberCount > 0) "Who lives here (${listing.memberCount})" else "Who lives here") {
        if (headline != null) Text("Flat activity: $headline", style = HqType.bodyMedium, color = c.textSecondary)
        if (names.isEmpty()) {
            if (listing.memberCount > 0) {
                Text("Names are shared when the flat admin chooses to.", style = HqType.bodyMedium, color = c.textSecondary)
            }
        } else {
            HqGroup {
                names.forEachIndexed { i, member ->
                    if (i > 0) HqRowDivider(startInset = HqSpacing.lg + HqSpacing.huge + HqSpacing.md)
                    HqRow(
                        title = member.nickname,
                        leading = { HqAvatar(member.nickname, size = HqAvatarSize.MD) },
                        trailing = if (member.role == "admin") ({ HqChip(label = "Admin") }) else null,
                    )
                }
            }
        }
    }
}

@Composable
private fun Interested(isOwnListing: Boolean, count: Int, people: List<Pair<String, String>>, onOpen: (String) -> Unit) {
    val c = LocalHqColors.current
    if (!isOwnListing) {
        Section("Requests") {
            Text(
                if (count > 0) "$count ${if (count == 1) "person has" else "people have"} sent a request. Only the flat sees who."
                else "No requests yet. Be the first to send one.",
                style = HqType.bodyMedium, color = c.textSecondary,
            )
        }
        return
    }
    Section("Requests for this room") {
        if (people.isEmpty()) {
            Text("No requests yet.", style = HqType.bodyMedium, color = c.textSecondary)
        } else {
            HqGroup {
                people.forEachIndexed { i, (id, name) ->
                    if (i > 0) HqRowDivider(startInset = HqSpacing.lg + HqSpacing.huge + HqSpacing.md)
                    HqRow(title = name, leading = { HqAvatar(name, size = HqAvatarSize.MD) }, trailing = { HqRowChevron() }, onClick = { onOpen(id) })
                }
            }
        }
    }
}

@Composable
private fun SafetyNote() {
    val c = LocalHqColors.current
    Row(
        Modifier.fillMaxWidth().clip(HqShapes.control).background(c.statusWarningBg).padding(HqSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm),
    ) {
        Icon(HqIcons.Shield, null, tint = c.statusWarningFg, modifier = Modifier.size(habitiq.app.ui.theme.HqIconSize.sm))
        // The one safety note on the listing (chats, money, address, and what a request does).
        Text(
            "Keep chats in Oddroof and never pay before viewing. Share an exact address only when you're comfortable. " +
                "Sending a request doesn't add anyone to the flat; joining still uses the flat's invite.",
            style = HqType.bodyMedium, color = c.textPrimary,
        )
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
                Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                    Text("They sent you a request", style = HqType.labelMedium, color = c.textPrimary)
                    HqButtonPair(
                        secondary = { HqButton(text = "Decline", onClick = { onDecline(incomingRequestId) }, variant = HqButtonVariant.Secondary) },
                        primary = { HqButton(text = "Accept", onClick = { onAccept(incomingRequestId) }) },
                    )
                }
            } else {
                HqButton(text = "Request sent", onClick = {}, enabled = false, variant = HqButtonVariant.Secondary)
            }
        }
        ConnectionStatus.ACCEPTED, ConnectionStatus.CONVERSATION_OPEN, ConnectionStatus.MATCHED -> {
            HqButton(text = "Message", onClick = onMessage)
        }
        ConnectionStatus.BLOCKED -> Text("Blocked", style = HqType.titleMedium, color = c.statusDangerFg)
    }
}

@Composable
fun MetaChip(text: String) {
    HqChip(label = text)
}
