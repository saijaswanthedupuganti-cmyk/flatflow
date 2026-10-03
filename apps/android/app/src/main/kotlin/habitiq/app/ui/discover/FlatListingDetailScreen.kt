package habitiq.app.ui.discover

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.Bed
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Chair
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CurrencyRupee
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image as ImageIcon
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.LocalLaundryService
import androidx.compose.material.icons.rounded.NoDrinks
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shower
import androidx.compose.material.icons.rounded.SmokeFree
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import habitiq.app.R
import habitiq.app.data.VacancyListing
import habitiq.app.discover.CompatibilitySignal
import habitiq.app.discover.ConnectionStatus
import habitiq.app.discover.DiscoverFilterLogic
import habitiq.app.discover.TrustPresentation
import habitiq.app.lib.formatInr
import habitiq.app.ui.components.HqAvatar
import habitiq.app.ui.components.HqAvatarSize
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqCardVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqHeroBleedEffect
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/** Palette from the "Pastel Flat Rental" listing reference (light). Dark mode uses theme tokens. */
private object ListingLook {
    val ink = Color(0xFF0F1F3D)
    val muted = Color(0xFF5E6B7E)
    val teal = Color(0xFF14937F)
    val tealSoft = Color(0xFFDDF4EF)
    val tealTop = Color(0xFF2EC3B5)
    val tealBottom = Color(0xFF0F8C7F)
    val tile = Color(0xFFF1F5FA)
    val chipBorder = Color(0xFFE2E8F0)
    val sheetTop = Color(0xFFFFFFFF)
    val sheetBottom = Color(0xFFF4F8FC)
    val amberBg = Color(0xFFFFF5E3)
    val amber = Color(0xFFE08A1E)
}

private fun sp(v: Float) = TextUnit(v, TextUnitType.Sp)

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
    val dark = c.isDark
    val ink = if (dark) c.textPrimary else ListingLook.ink
    val muted = if (dark) c.textSecondary else ListingLook.muted
    HqHeroBleedEffect()
    Column(Modifier.fillMaxSize().background(if (dark) c.canvas else ListingLook.sheetBottom).verticalScroll(rememberScrollState())) {
        ListingHero(listing, onBack, saved, onToggleSave)

        // The details sheet rises over the photo with rounded shoulders.
        Column(
            Modifier.offset(y = (-28).dp).padding(horizontal = 8.dp)
                .shadow(14.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp), ambientColor = Color(0xFF1B3A6B).copy(alpha = .10f), spotColor = Color(0xFF1B3A6B).copy(alpha = .10f))
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(if (dark) Brush.verticalGradient(listOf(c.surfaceRaised, c.canvas)) else Brush.verticalGradient(listOf(ListingLook.sheetTop, ListingLook.sheetBottom)))
                .padding(horizontal = 14.dp).padding(top = 20.dp, bottom = HqSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HeaderBlock(listing, ink, muted)
            FactTiles(listing, ink, muted)

            when {
                isOwnListing -> Text("This is your vacancy.", style = HqType.bodyMedium, color = muted)
                blocked -> Text("Blocked", style = HqType.rowTitle, color = c.statusDangerFg)
                connectionStatus == ConnectionStatus.NONE || connectionStatus == ConnectionStatus.DECLINED || connectionStatus == ConnectionStatus.EXPIRED ->
                    ConnectButton(onConnect)
                else -> ConnectionActions(connectionStatus, incomingRequestId, "Connect with this flat", onConnect, onMessage, onAccept, onDecline)
            }

            LifestyleSection(listing, ink, muted)
            MembersStrip(listing, ink)
            AboutSection(listing, ink, muted)
            CurrentMembersSection(listing, ink, muted)
            InterestedPeopleSection(isOwnListing, interestedCount, interestedPeople, onOpenInterested, ink, muted)
            TrustBadge(trust)
            CompatibilityBlock("Why this may fit you", signals.map { it.label })
            SafetyBanner(ink, muted)
            if (!isOwnListing && !blocked) FooterActions(onReport, onBlock, muted)
            Text(
                "Matching here does not add anyone to the flat. Join still uses the flat invite flow.",
                style = HqType.labelSmall, color = c.textMuted,
            )
        }
    }
}

@Composable
private fun ListingHero(listing: VacancyListing, onBack: () -> Unit, saved: Boolean, onToggleSave: (() -> Unit)?) {
    val c = LocalHqColors.current
    val context = LocalContext.current
    Box(Modifier.fillMaxWidth().height(300.dp).background(c.selectedBg)) {
        val cover = listing.photoUrls.firstOrNull()
        if (cover != null) {
            AsyncImage(
                model = cover, contentDescription = "Photo of ${listing.flatName}",
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                placeholder = ColorPainter(c.surfaceSubtle), error = ColorPainter(c.surfaceSubtle), fallback = ColorPainter(c.surfaceSubtle),
            )
        } else {
            Image(
                painter = painterResource(R.drawable.home_hero),
                contentDescription = "Placeholder photo; this listing has no room photo yet",
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            )
        }
        // Soft dark edge at top for the floating buttons, and at the bottom for the thumbnails.
        Box(Modifier.matchParentSize().background(Brush.verticalGradient(0f to Color.Black.copy(alpha = .28f), .3f to Color.Transparent, .75f to Color.Transparent, 1f to Color.Black.copy(alpha = .35f))))

        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            RoundButton(Icons.AutoMirrored.Rounded.ArrowBack, "Back", onBack)
            Spacer(Modifier.weight(1f))
            if (onToggleSave != null) {
                RoundButton(
                    if (saved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    if (saved) "Remove from saved flats" else "Save flat", onToggleSave, tint = Color(0xFFE5484D),
                )
                Spacer(Modifier.width(12.dp))
            }
            RoundButton(Icons.Rounded.Share, "Share listing", {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Have a look at ${listing.flatName} on Oddroof.")
                }
                context.startActivity(Intent.createChooser(send, "Share listing"))
            })
        }

        // Photo strip and counter, bottom-right, above the sheet's rounded edge.
        Row(
            Modifier.align(Alignment.BottomEnd).padding(end = 18.dp, bottom = 42.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listing.photoUrls.drop(1).take(3).forEach { url ->
                AsyncImage(
                    model = url, contentDescription = null, contentScale = ContentScale.Crop,
                    placeholder = ColorPainter(c.surfaceSubtle), error = ColorPainter(c.surfaceSubtle),
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).border(2.dp, Color.White, RoundedCornerShape(10.dp)),
                )
            }
            if (listing.photoUrls.isNotEmpty()) {
                Row(
                    Modifier.clip(RoundedCornerShape(14.dp)).background(Color(0xFF101828).copy(alpha = .62f))
                        .border(1.dp, Color.White.copy(alpha = .25f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.ImageIcon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Text("1/${listing.photoUrls.size}", style = HqType.labelMedium, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun RoundButton(icon: ImageVector, label: String, onClick: () -> Unit, tint: Color = ListingLook.ink) {
    Box(
        Modifier.size(46.dp)
            .shadow(8.dp, CircleShape, ambientColor = Color.Black.copy(alpha = .2f), spotColor = Color.Black.copy(alpha = .2f))
            .clip(CircleShape).background(Color.White.copy(alpha = .94f))
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp)) }
}

@Composable
private fun HeaderBlock(listing: VacancyListing, ink: Color, muted: Color) {
    val c = LocalHqColors.current
    val badge = listOfNotNull(
        when (listing.roomType) { "private" -> "PRIVATE"; null -> null; else -> "SHARED" },
        listing.bedsAvailable.takeIf { it > 0 }?.let { "$it ${if (it == 1) "BED" else "BEDS"}" },
    ).joinToString(" • ")
    val place = listOf(listing.area, listing.city).filter { it.isNotBlank() }.joinToString(", ")
    Row(verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (badge.isNotBlank()) {
                Text(
                    badge, style = HqType.labelMedium.copy(letterSpacing = sp(.4f)), fontWeight = FontWeight.SemiBold,
                    color = if (c.isDark) c.textBrand else ListingLook.teal,
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(if (c.isDark) c.selectedBg else ListingLook.tealSoft).padding(horizontal = 12.dp, vertical = 5.dp),
                )
            }
            Text(listing.flatName, style = HqType.titleLarge2.copy(fontSize = sp(28f), lineHeight = sp(34f)), color = ink)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Place, null, tint = if (c.isDark) c.textBrand else ListingLook.teal, modifier = Modifier.size(18.dp))
                Text(
                    (place.ifBlank { "Location shared later" }) + " · Approx. area",
                    style = HqType.bodyMedium, color = muted, maxLines = 2, modifier = Modifier.padding(start = 6.dp),
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        MapThumb()
    }
}

/** Stylised neighbourhood map with a teal pin; exact location is never shown on a listing. */
@Composable
private fun MapThumb() {
    val c = LocalHqColors.current
    val shape = RoundedCornerShape(18.dp)
    Box(Modifier.size(width = 112.dp, height = 78.dp).clip(shape).border(1.dp, if (c.isDark) c.borderSubtle else ListingLook.chipBorder, shape)) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(if (c.isDark) Color(0xFF243330) else Color(0xFFEFF4F1))
            val park = if (c.isDark) Color(0xFF2E4A3F) else Color(0xFFCFEBD8)
            drawRoundRect(park, Offset(size.width * .05f, size.height * .55f), Size(size.width * .35f, size.height * .35f), CornerRadius(8f))
            drawRoundRect(park, Offset(size.width * .7f, size.height * .08f), Size(size.width * .26f, size.height * .3f), CornerRadius(8f))
            val road = if (c.isDark) Color(0xFF3A4A47) else Color.White
            drawLine(road, Offset(0f, size.height * .38f), Offset(size.width, size.height * .28f), strokeWidth = 7f)
            drawLine(road, Offset(size.width * .45f, 0f), Offset(size.width * .58f, size.height), strokeWidth = 7f)
            drawLine(Color(0xFFF6C26B), Offset(0f, size.height * .8f), Offset(size.width * .6f, size.height * .58f), strokeWidth = 5f)
            // Pin
            val pin = Color(0xFF0F8C7F)
            val px = size.width * .55f
            val py = size.height * .36f
            drawCircle(pin, radius = 9.dp.toPx(), center = Offset(px, py))
            drawCircle(Color.White, radius = 3.5.dp.toPx(), center = Offset(px, py))
            val tip = androidx.compose.ui.graphics.Path().apply {
                moveTo(px - 6.dp.toPx(), py + 5.dp.toPx()); lineTo(px, py + 15.dp.toPx()); lineTo(px + 6.dp.toPx(), py + 5.dp.toPx()); close()
            }
            drawPath(tip, pin)
        }
        Text(
            "Approx. area",
            style = HqType.labelSmall, color = if (c.isDark) c.textBrand else ListingLook.teal, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp)
                .clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = .94f)).padding(horizontal = 10.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun FactTiles(listing: VacancyListing, ink: Color, muted: Color) {
    val c = LocalHqColors.current
    val dark = c.isDark
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FactTile(Modifier.weight(.95f), Icons.Rounded.CurrencyRupee, Color(0xFF2F6BEF), Color(0xFFE2ECFD),
            listing.rentPerHead?.let { formatInr(it) } ?: "On request", "Rent/head", ink, muted, dark)
        FactTile(Modifier.weight(.95f), Icons.Rounded.Group, ListingLook.teal, ListingLook.tealSoft,
            if (listing.roomType == "private") "Private" else "Shared", "Room type", ink, muted, dark)
        FactTile(Modifier.weight(1.12f), Icons.Rounded.Home, ListingLook.ink, Color(0xFFE3EAF5),
            listing.flatType.takeIf { it.isNotBlank() }?.let { DiscoverFilterLogic.formatFlatType(it) } ?: "Any", "Flat type", ink, muted, dark)
    }
}

@Composable
private fun FactTile(modifier: Modifier, icon: ImageVector, glyph: Color, chip: Color, value: String, label: String, ink: Color, muted: Color, dark: Boolean) {
    val c = LocalHqColors.current
    Row(
        modifier.fillMaxHeight().clip(RoundedCornerShape(18.dp)).background(if (dark) c.surfaceSubtle else ListingLook.tile).padding(start = 7.dp, end = 4.dp, top = 11.dp, bottom = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(28.dp).clip(CircleShape).background(if (dark) chip.copy(alpha = .18f) else chip), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = if (dark) c.textBrand else glyph, modifier = Modifier.size(16.dp))
        }
        Column(Modifier.padding(start = 6.dp)) {
            Text(value, style = HqType.rowTitle.copy(fontSize = sp(13.5f), lineHeight = sp(18f), letterSpacing = sp(-0.1f)), color = ink, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = HqType.bodySmall.copy(fontSize = sp(11.5f)), color = muted, maxLines = 1)
        }
    }
}

@Composable
private fun ConnectButton(onConnect: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        Modifier.fillMaxWidth().heightIn(min = 58.dp)
            .shadow(10.dp, shape, ambientColor = ListingLook.tealBottom.copy(alpha = .35f), spotColor = ListingLook.tealBottom.copy(alpha = .35f))
            .clip(shape).background(Brush.horizontalGradient(listOf(ListingLook.tealTop, ListingLook.tealBottom)))
            .clickable(role = Role.Button, onClick = onConnect).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.AutoMirrored.Rounded.Chat, null, tint = Color.White, modifier = Modifier.size(24.dp))
        Text("Connect with this flat", style = HqType.titleSmall2, color = Color.White, modifier = Modifier.weight(1f).padding(start = 12.dp))
        Box(Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = .22f)), contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

private fun lifestyleIcon(tag: String): ImageVector {
    val t = tag.lowercase()
    return when {
        "smok" in t -> Icons.Rounded.SmokeFree
        "alcohol" in t || "drink" in t -> Icons.Rounded.NoDrinks
        "work from home" in t || "wfh" in t -> Icons.Rounded.Laptop
        "quiet" in t -> Icons.Rounded.VolumeDown
        "room" in t && "shared" in t -> Icons.Rounded.Group
        "room" in t || "bed" in t -> Icons.Rounded.Bed
        "furnish" in t -> Icons.Rounded.Chair
        "wifi" in t || "internet" in t -> Icons.Rounded.Wifi
        t == "ac" || "air" in t -> Icons.Rounded.AcUnit
        "bath" in t -> Icons.Rounded.Shower
        "wash" in t || "laundry" in t -> Icons.Rounded.LocalLaundryService
        "geyser" in t || "water" in t -> Icons.Rounded.WaterDrop
        "gym" in t -> Icons.Rounded.FitnessCenter
        "women" in t || "men" in t || "female" in t || "male" in t -> Icons.Rounded.Person
        "professional" in t || "work" in t -> Icons.Rounded.Work
        "immediate" in t || "now" in t -> Icons.Rounded.Bolt
        else -> Icons.Rounded.Check
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LifestyleSection(listing: VacancyListing, ink: Color, muted: Color) {
    val c = LocalHqColors.current
    val perks = (listing.displayTags() + listing.amenities.map { it.replace('_', ' ').replaceFirstChar(Char::uppercase) } +
        listOfNotNull(listing.furnishing.takeIf { it.isNotBlank() }?.replace('_', ' ')?.replaceFirstChar(Char::uppercase)))
        .map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }
    if (perks.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Lifestyle", style = HqType.titleMedium2.copy(fontSize = sp(22f)), color = ink, modifier = Modifier.weight(1f))
            Text("${perks.size} perks", style = HqType.bodyMedium, color = muted)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            perks.forEach { perk ->
                Row(
                    Modifier.clip(RoundedCornerShape(50)).background(if (c.isDark) c.surfaceRaised else Color.White)
                        .border(1.dp, if (c.isDark) c.borderSubtle else ListingLook.chipBorder, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val icon = lifestyleIcon(perk)
                    val warn = icon == Icons.Rounded.SmokeFree || icon == Icons.Rounded.NoDrinks
                    Icon(icon, null, tint = if (warn) Color(0xFFE5484D) else ink, modifier = Modifier.size(18.dp))
                    Text(perk, style = HqType.bodyMedium, color = ink, modifier = Modifier.padding(start = 7.dp))
                }
            }
        }
    }
}

@Composable
private fun MembersStrip(listing: VacancyListing, ink: Color) {
    val c = LocalHqColors.current
    if (listing.memberCount <= 0) return
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (c.isDark) c.selectedBg else ListingLook.tealSoft.copy(alpha = .6f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Group, null, tint = if (c.isDark) c.textBrand else ListingLook.teal, modifier = Modifier.size(20.dp))
        Text(
            "${listing.memberCount} current ${if (listing.memberCount == 1) "member" else "members"} on Oddroof",
            style = HqType.bodyMedium, color = ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 10.dp),
        )
    }
}

private fun healthIcon(area: String): ImageVector = when (area) {
    "Tasks" -> Icons.Rounded.TaskAlt
    "Expenses" -> Icons.Rounded.Receipt
    "Rotation" -> Icons.Rounded.Sync
    "Activity" -> Icons.Rounded.BarChart
    else -> Icons.Rounded.Group
}

@Composable
private fun AboutSection(listing: VacancyListing, ink: Color, muted: Color) {
    val c = LocalHqColors.current
    val health = listing.health ?: habitiq.app.discover.FlatHealthSnapshot.empty()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("About this flat", style = HqType.titleMedium2.copy(fontSize = sp(22f)), color = ink)
        if (listing.about.isNotBlank()) Text(listing.about, style = HqType.bodyLarge, color = muted)
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.weight(1.25f).fillMaxHeight().clip(RoundedCornerShape(18.dp)).background(if (c.isDark) c.surfaceSubtle else ListingLook.tile).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Brush.verticalGradient(listOf(ListingLook.tealTop, ListingLook.tealBottom))),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Rounded.Apartment, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
                Column(Modifier.padding(start = 10.dp)) {
                    Text(health.headline, style = HqType.rowTitle.copy(fontSize = sp(15f)), color = ink, fontWeight = FontWeight.Bold)
                    Text(
                        "Signals from Oddroof activity, not a score or a ranking.",
                        style = HqType.bodySmall.copy(fontSize = sp(12f)), color = muted,
                    )
                }
            }
            // The photo takes the card's height instead of its own.
            Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(18.dp))) {
                Image(
                    painterResource(R.drawable.home_hero), null,
                    contentScale = ContentScale.Crop,
                    alignment = androidx.compose.ui.BiasAlignment(0.4f, 0.2f),
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
        val rows = health.rows()
        val shape = RoundedCornerShape(18.dp)
        Row(
            Modifier.fillMaxWidth().height(IntrinsicSize.Max).clip(shape).background(if (c.isDark) c.surfaceRaised else Color.White)
                .border(1.dp, if (c.isDark) c.borderSubtle else ListingLook.chipBorder, shape).padding(vertical = 10.dp),
        ) {
            rows.forEachIndexed { i, (area, signal) ->
                Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
                    Icon(healthIcon(area), null, tint = ink, modifier = Modifier.size(17.dp))
                    Text(area, style = HqType.labelSmall.copy(fontSize = sp(11.5f)), color = ink, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
                    Text(signal, style = HqType.labelSmall.copy(fontSize = sp(11f), lineHeight = sp(14f)), color = muted, modifier = Modifier.padding(top = 3.dp))
                }
                if (i < rows.lastIndex) Box(Modifier.width(1.dp).fillMaxHeight().background(if (c.isDark) c.borderSubtle else ListingLook.chipBorder))
            }
        }
    }
}

@Composable
private fun CurrentMembersSection(listing: VacancyListing, ink: Color, muted: Color) {
    val c = LocalHqColors.current
    val names = listing.health?.members.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            if (names.isEmpty()) "Current members" else "Current members (${names.size})",
            style = HqType.titleMedium2.copy(fontSize = sp(20f)), color = ink,
        )
        if (names.isEmpty()) {
            Text(
                if (listing.memberCount > 0) "${listing.memberCount} people live here. Names are shared when the admin publishes health."
                else "Member list not published yet.",
                style = HqType.bodySmall, color = muted,
            )
        } else {
            names.forEach { member ->
                val shape = RoundedCornerShape(50)
                Row(
                    Modifier.fillMaxWidth().clip(shape).background(if (c.isDark) c.surfaceRaised else Color.White)
                        .border(1.dp, if (c.isDark) c.borderSubtle else ListingLook.chipBorder, shape).padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HqAvatar(member.nickname, size = HqAvatarSize.MD)
                    Text(member.nickname, style = HqType.bodyLarge, color = ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(start = 12.dp))
                    if (member.role == "admin") {
                        Text(
                            "Admin", style = HqType.labelMedium, color = if (c.isDark) c.textBrand else ListingLook.teal, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(end = 6.dp).clip(RoundedCornerShape(50)).background(if (c.isDark) c.selectedBg else ListingLook.tealSoft).padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InterestedPeopleSection(
    isOwnListing: Boolean, count: Int, people: List<Pair<String, String>>, onOpen: (String) -> Unit, ink: Color, muted: Color,
) {
    val c = LocalHqColors.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("People interested", style = HqType.titleMedium2.copy(fontSize = sp(20f)), color = ink)
                if (!isOwnListing) {
                    Text(
                        if (count > 0) "$count ${if (count == 1) "person has" else "people have"} asked to connect. Names stay private to the flat admin."
                        else "Be the first to ask to connect.",
                        style = HqType.bodyMedium, color = muted, modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            if (!isOwnListing) {
                Row {
                    repeat(4) { i ->
                        Box(
                            Modifier.offset(x = (-8 * i).dp).size(30.dp).clip(CircleShape)
                                .background(if (c.isDark) c.surfaceSubtle else Color(0xFFE8EDF4)).border(2.dp, if (c.isDark) c.canvas else Color.White, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Rounded.Person, null, tint = if (c.isDark) c.textMuted else Color(0xFFC3CCD8), modifier = Modifier.size(18.dp)) }
                    }
                }
            }
        }
        if (isOwnListing) {
            if (people.isEmpty()) {
                Text("No connection requests yet.", style = HqType.bodySmall, color = muted)
            } else {
                people.forEach { (id, name) ->
                    HqCard(variant = HqCardVariant.Interactive, onClick = { onOpen(id) }, padding = HqSpacing.md) {
                        Text(name, style = HqType.labelLarge, color = c.textPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SafetyBanner(ink: Color, muted: Color) {
    val c = LocalHqColors.current
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(if (c.isDark) c.statusWarningBg else ListingLook.amberBg).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Shield, null, tint = if (c.isDark) c.statusWarningFg else ListingLook.amber, modifier = Modifier.size(28.dp))
        Column(Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Your safety matters", style = HqType.rowTitle, color = ink, fontWeight = FontWeight.Bold)
            Text(
                "Keep chats in Oddroof. Never pay before viewing. Share an exact address only after you are comfortable.",
                style = HqType.bodySmall, color = muted,
            )
        }
    }
}

@Composable
private fun FooterActions(onReport: () -> Unit, onBlock: () -> Unit, muted: Color) {
    val c = LocalHqColors.current
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
        FooterAction(Modifier.weight(1f), Icons.Rounded.Flag, "Report listing", onReport)
        Box(Modifier.width(1.dp).height(28.dp).background(if (c.isDark) c.borderSubtle else ListingLook.chipBorder))
        FooterAction(Modifier.weight(1f), Icons.Rounded.Block, "Block", onBlock)
    }
}

@Composable
private fun FooterAction(modifier: Modifier, icon: ImageVector, text: String, onClick: () -> Unit) {
    val c = LocalHqColors.current
    val ink = if (c.isDark) c.textPrimary else ListingLook.ink
    Row(
        modifier.heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = ink, modifier = Modifier.size(20.dp))
        Text(text, style = HqType.bodyLarge, color = ink, modifier = Modifier.padding(start = 8.dp))
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
