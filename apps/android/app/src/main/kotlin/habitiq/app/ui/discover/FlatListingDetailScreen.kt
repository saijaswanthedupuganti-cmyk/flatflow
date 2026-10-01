package habitiq.app.ui.discover

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
    onBlock: () -> Unit
) {
    val c = LocalHqColors.current
    val approx = listOf(listing.area, listing.city).filter { it.isNotBlank() }.joinToString(" · ")
    Column(Modifier.fillMaxSize().background(c.background)) {
        HqBackAppBar(title = "Flat details", onBack = onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.xl).padding(bottom = HqSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.md)
        ) {
            Box(Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(HqRadius.lg))) {
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
                            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomEnd)
                                .padding(HqSpacing.sm)
                                .background(c.surface.copy(alpha = 0.92f), RoundedCornerShape(HqRadius.full))
                                .padding(horizontal = HqSpacing.sm, vertical = HqSpacing.xs),
                            style = HqType.labelSmall,
                            color = c.textPrimary
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(R.drawable.onboard_create),
                        contentDescription = "Illustration placeholder; this listing has no room photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alignment = androidx.compose.ui.Alignment.Center
                    )
                }
            }
            Text(listing.flatName, style = HqType.headlineMedium, color = c.textPrimary)
            Text(approx.ifBlank { "Approximate location shared later" }, style = HqType.bodyMedium, color = c.textSecondary)
            listing.rentPerHead?.let {
                Text("${formatInr(it)} / month per head", style = HqType.titleLarge, color = c.brandPrimary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                MetaChip(DiscoverFilterLogic.formatRoomType(listing.roomType, listing.bedsAvailable))
                if (listing.flatType.isNotBlank()) MetaChip(DiscoverFilterLogic.formatFlatType(listing.flatType))
                MetaChip(DiscoverFilterLogic.formatGenderPreference(listing.preferredGender))
            }
            if (listing.memberCount > 0) {
                Text("${listing.memberCount} current members", style = HqType.bodySmall, color = c.textSecondary)
            }
            TrustBadge(trust)
            Surface(color = c.warningContainer, shape = RoundedCornerShape(HqRadius.md)) {
                Text(
                    "Approx. $approx. Share an exact address only after you are comfortable with the other person.",
                    Modifier.padding(HqSpacing.md),
                    style = HqType.caption,
                    color = c.textPrimary
                )
            }
            if (listing.about.isNotBlank()) {
                Text("About", style = HqType.titleMedium, color = c.textPrimary)
                Text(listing.about, style = HqType.bodyMedium, color = c.textSecondary)
            }
            if (listing.displayTags().isNotEmpty()) {
                Text("House preferences", style = HqType.titleMedium, color = c.textPrimary)
                Text(listing.displayTags().joinToString(" · "), style = HqType.bodySmall, color = c.textSecondary)
            }
            FlatHealthSection(listing)
            CurrentMembersSection(listing)
            InterestedPeopleSection(
                isOwnListing = isOwnListing,
                count = interestedCount,
                people = interestedPeople,
                onOpen = onOpenInterested
            )
            CompatibilityBlock("Why this may fit you", signals.map { it.label })
            Spacer(Modifier.height(HqSpacing.sm))
            if (isOwnListing) {
                Text("This is your vacancy.", style = HqType.bodySmall, color = c.textTertiary)
            } else if (blocked) {
                Text("Blocked", style = HqType.titleMedium, color = c.error)
            } else {
                ConnectionActions(
                    status = connectionStatus,
                    incomingRequestId = incomingRequestId,
                    connectLabel = "Ask to connect",
                    onConnect = onConnect,
                    onMessage = onMessage,
                    onAccept = onAccept,
                    onDecline = onDecline
                )
                Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
                    HqTextButton(text = "Report listing", onClick = onReport)
                    HqTextButton(text = "Block", onClick = onBlock)
                }
            }
            Text(
                "Matching here does not add anyone to the flat. Join still uses the flat invite flow.",
                style = HqType.caption,
                color = c.textTertiary
            )
        }
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
            color = c.brandPrimary
        )
        Text(
            "Qualitative signals from Habitiq activity — not a score, not a ranking against other flats.",
            style = HqType.caption,
            color = c.textTertiary
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
        ConnectionStatus.BLOCKED -> Text("Blocked", style = HqType.titleMedium, color = c.error)
    }
}

@Composable
fun MetaChip(text: String) {
    HqChip(label = text)
}
