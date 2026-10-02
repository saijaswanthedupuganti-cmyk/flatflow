package habitiq.app.ui

import habitiq.app.ui.components.HqHeroBleedEffect
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import habitiq.app.R
import habitiq.app.ui.components.HqAvatar
import habitiq.app.ui.components.HqAvatarSize
import habitiq.app.ui.components.HqAvatarTone
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCalloutCard
import habitiq.app.ui.components.HqGroup
import habitiq.app.ui.components.HqIconTile
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqNavRow
import habitiq.app.ui.components.HqPill
import habitiq.app.ui.components.HqSectionTitle
import habitiq.app.ui.components.HqTaskCard
import habitiq.app.ui.components.HqTileTone
import habitiq.app.ui.components.HqTimelineItem
import habitiq.app.ui.theme.HqIconSize
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHeroBleed
import habitiq.app.ui.theme.LocalHqColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class HomeTaskKind { Rotating, Group, OneOff }

data class HomeTaskItem(
    val id: String,
    val name: String,
    val assigneeText: String,
    val dueText: String,
    val overdue: Boolean,
    val canComplete: Boolean,
    val kind: HomeTaskKind = HomeTaskKind.Rotating,
)

data class HomePendingItem(val kind: Kind, val title: String, val body: String) {
    enum class Kind { JoinRequests, SwapRequests }
}

data class HomeActivityItem(val text: String, val time: String)

/** Money position in rupees. Both directions are kept so neither is hidden behind a net figure. */
data class HomeBalance(val owe: Double, val owed: Double, val oweCount: Int, val owedCount: Int)

/** Everything Home shows, already resolved from real data. Optional modules are simply absent when empty. */
data class HomeUiModel(
    val flatName: String,
    val roleLabel: String,
    val canInvite: Boolean,
    val greetingName: String,
    val summaryHeadline: String,
    val assignedCount: Int,
    val memberCount: Int,
    val tasks: List<HomeTaskItem>,
    val pending: List<HomePendingItem>,
    val balance: HomeBalance,
    val balanceText: (Double) -> String,
    val activity: List<HomeActivityItem>,
    val unreadCount: Int = 0,
)

private val HeroInk = Color(0xFF0C1D1B)
private val HeroKicker = Color(0xFFB7F0E9)

/**
 * Authenticated Home, following the Figma Make `Home`: a photo hero with the greeting, flat chip and two
 * status cells, then today's tasks, pending activity, recent activity and a Discover teaser. The hero
 * bleeds under the status bar; the screen applies the status-bar inset itself while it is shown.
 */
@Composable
fun HomeContent(
    model: HomeUiModel,
    completingTaskId: String?,
    onOpenFlatSwitcher: () -> Unit,
    onInvite: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenTask: (String) -> Unit,
    onCompleteTask: (String) -> Unit,
    onReviewPending: (HomePendingItem.Kind) -> Unit,
    onOpenExpenses: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenDiscover: () -> Unit,
    onOpenMembers: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenNotifications: () -> Unit = onOpenActivity,
) {
    val c = LocalHqColors.current
    HqHeroBleedEffect()
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        HomeHero(model, onOpenFlatSwitcher, onInvite, onOpenExpenses, onOpenNotifications)

        Column(Modifier.padding(horizontal = HqSpacing.screenHorizontal).padding(bottom = HqSpacing.screenEnd)) {
            HqSectionTitle("Today's tasks", action = "See all", onAction = onOpenTasks)
            if (model.tasks.isEmpty()) {
                Text("Nothing is assigned to you right now.", style = HqType.bodyLarge, color = c.textSecondary)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                model.tasks.forEach { task ->
                    HqTaskCard(
                        title = task.name,
                        canComplete = task.canComplete,
                        completing = completingTaskId == task.id,
                        onOpen = { onOpenTask(task.id) },
                        onComplete = { onCompleteTask(task.id) },
                        meta = {
                            Text(
                                task.dueText,
                                style = HqType.bodyMedium,
                                color = if (task.overdue) c.statusWarningFg else c.textSecondary,
                                fontWeight = if (task.overdue) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        trailing = { HqPill("YOU") },
                    )
                }
            }

            if (model.pending.isNotEmpty()) {
                HqSectionTitle("Pending activity")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    model.pending.forEach { item ->
                        HqCalloutCard(
                            title = item.title,
                            support = item.body,
                            icon = HqIcons.Users,
                            tone = HqTileTone.Coral,
                            onClick = { onReviewPending(item.kind) },
                        )
                    }
                }
            }

            if (model.activity.isNotEmpty()) {
                HqSectionTitle("Recent activity", action = "View all", onAction = onOpenActivity)
                val spine = c.borderSubtle
                Column(
                    Modifier.padding(start = 14.dp).drawBehind {
                        drawLine(spine, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 1.dp.toPx())
                    },
                    verticalArrangement = Arrangement.spacedBy(17.dp),
                ) {
                    model.activity.forEach { item ->
                        // The 28dp marker is centred on the spine.
                        HqTimelineItem(item.text, item.time, Modifier.offset(x = (-14).dp))
                    }
                }
            }

            Spacer(Modifier.size(HqSpacing.section))
            HqGroup { HqNavRow(title = "Members", support = "${model.memberCount} ${if (model.memberCount == 1) "member" else "members"}", onClick = onOpenMembers) }

            DiscoverTeaser(onOpenDiscover)
        }
    }
}

@Composable
private fun HomeHero(model: HomeUiModel, onOpenFlatSwitcher: () -> Unit, onInvite: () -> Unit, onOpenExpenses: () -> Unit, onOpenNotifications: () -> Unit) {
    val shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp)
    Box(
        Modifier.fillMaxWidth()
            .shadow(16.dp, shape, ambientColor = HeroInk.copy(alpha = .18f), spotColor = HeroInk.copy(alpha = .18f))
            .clip(shape).background(HeroInk),
    ) {
        Image(
            painterResource(R.drawable.home_hero),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(0f, 0.08f),
            modifier = Modifier.matchParentSize(),
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    0f to HeroInk.copy(alpha = .68f),
                    .38f to HeroInk.copy(alpha = .18f),
                    .75f to HeroInk.copy(alpha = .74f),
                    1f to HeroInk.copy(alpha = .94f),
                ),
            ),
        )
        Column(Modifier.fillMaxWidth().heightIn(min = 404.dp)) {
            Column(Modifier.statusBarsPadding().padding(horizontal = HqSpacing.screenHorizontal).padding(top = 12.dp)) {
                HeroTopBar(model, onInvite, onOpenNotifications)
                Spacer(Modifier.size(64.dp))
                Text(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())).uppercase(),
                    style = HqType.labelSmall.copy(letterSpacing = androidx.compose.ui.unit.TextUnit(1.5f, androidx.compose.ui.unit.TextUnitType.Sp)),
                    color = HeroKicker,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    "${greeting()}, ${model.greetingName}.",
                    style = HqType.display.copy(fontSize = androidx.compose.ui.unit.TextUnit(30f, androidx.compose.ui.unit.TextUnitType.Sp)),
                    color = Color.White,
                    modifier = Modifier.padding(top = 10.dp),
                )
                Text(
                    "${model.summaryHeadline} A calm look at ${model.flatName} today.",
                    style = HqType.bodyMedium,
                    color = Color.White.copy(alpha = .82f),
                    modifier = Modifier.padding(top = 9.dp),
                )
                FlatChip(model.flatName, onOpenFlatSwitcher)
            }
            Spacer(Modifier.weight(1f))
            HeroStatusBand(model, onOpenExpenses)
        }
    }
}

@Composable
private fun HeroTopBar(model: HomeUiModel, onInvite: () -> Unit, onOpenNotifications: () -> Unit) {
    Row(Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Box(
                Modifier.size(30.dp).clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomEnd = 10.dp, bottomStart = 4.dp))
                    .background(Color.White.copy(alpha = .94f)),
                contentAlignment = Alignment.Center,
            ) { Text("H", style = HqType.titleSmall2, color = LocalHqColors.current.textBrand) }
            Text("habitiq", style = HqType.titleSmall2, color = Color.White)
        }
        Box(
            Modifier.size(HqSize.target).clickable(role = Role.Button, onClick = onOpenNotifications)
                .semantics { contentDescription = if (model.unreadCount > 0) "Notifications, ${model.unreadCount} unread" else "Notifications" },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.size(39.dp).clip(RoundedCornerShape(12.dp)).background(HeroInk.copy(alpha = .28f))
                    .border(1.dp, Color.White.copy(alpha = .25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) { Icon(HqIcons.Bell, null, tint = Color.White, modifier = Modifier.size(HqIconSize.sm)) }
            if (model.unreadCount > 0) {
                Box(
                    Modifier.align(Alignment.TopEnd).size(19.dp).clip(CircleShape).background(Color(0xFFFF6B5A)).border(2.dp, Color.White.copy(alpha = .95f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text(if (model.unreadCount > 9) "9+" else "${model.unreadCount}", style = HqType.labelSmall, color = Color.White, fontWeight = FontWeight.ExtraBold) }
            }
        }
        if (model.canInvite) {
            Box(
                Modifier.size(HqSize.target).clip(RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button, onClick = onInvite)
                    .semantics { contentDescription = "Share invite code" },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.size(39.dp).clip(RoundedCornerShape(12.dp)).background(HeroInk.copy(alpha = .28f))
                        .border(1.dp, Color.White.copy(alpha = .25f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) { Icon(HqIcons.Users, null, tint = Color.White, modifier = Modifier.size(HqIconSize.sm)) }
            }
            Spacer(Modifier.size(4.dp))
        }
        HqAvatar(model.greetingName, size = HqAvatarSize.MD, tone = HqAvatarTone.Teal, modifier = Modifier.border(2.dp, Color.White.copy(alpha = .45f), CircleShape))
    }
}

@Composable
private fun FlatChip(flatName: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier.padding(top = 17.dp, bottom = 13.dp).fillMaxWidth().clip(shape)
            .background(HeroInk.copy(alpha = .36f)).border(1.dp, Color.White.copy(alpha = .2f), shape)
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 11.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(31.dp).clip(RoundedCornerShape(9.dp)).background(Color(0x3314B8A6)), contentAlignment = Alignment.Center) {
            Icon(HqIcons.Home, null, tint = Color(0xFF82DFD5), modifier = Modifier.size(HqIconSize.sm))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("YOUR FLAT", style = HqType.labelSmall, color = Color(0xFFB4C7C4), fontWeight = FontWeight.ExtraBold)
            Text(flatName, style = HqType.rowTitle, color = Color.White, maxLines = 1)
        }
        Icon(HqIcons.Chevron, null, tint = Color.White, modifier = Modifier.size(HqIconSize.sm))
    }
}

@Composable
private fun HeroStatusBand(model: HomeUiModel, onOpenExpenses: () -> Unit) {
    val b = model.balance
    val moneyTitle = when {
        b.owe > 0 -> "You owe ${model.balanceText(b.owe)}"
        b.owed > 0 -> "You're owed ${model.balanceText(b.owed)}"
        else -> "Expenses settled"
    }
    val moneySupport = when {
        b.owe > 0 -> "To ${b.oweCount} ${if (b.oweCount == 1) "person" else "people"}"
        b.owed > 0 -> "From ${b.owedCount} ${if (b.owedCount == 1) "person" else "people"}"
        else -> "Nobody owes anything"
    }
    Row(
        Modifier.fillMaxWidth().background(Color(0xFF091917).copy(alpha = .76f)).drawBehind {
            drawLine(Color.White.copy(alpha = .09f), Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
        },
    ) {
        StatusCell(Modifier.weight(1f), "${model.assignedCount}", "Tasks for you", model.summaryHeadline.trimEnd('.'), null)
        StatusCell(Modifier.weight(1f), "₹", moneyTitle, moneySupport, onOpenExpenses)
    }
}

@Composable
private fun StatusCell(modifier: Modifier, number: String, title: String, support: String, onClick: (() -> Unit)?) {
    Row(
        modifier.then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .defaultMinSize(minHeight = HqSize.target).padding(horizontal = HqSpacing.screenHorizontal, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
            Text(number, style = HqType.titleSmall2, color = Color.White)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = HqType.labelMedium, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2)
            Text(support, style = HqType.labelSmall, color = Color.White.copy(alpha = .72f), maxLines = 2)
        }
    }
}

@Composable
private fun DiscoverTeaser(onOpenDiscover: () -> Unit) {
    val c = LocalHqColors.current
    Box(Modifier.padding(top = 26.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(c.surfaceSubtle)) {
        Box(Modifier.align(Alignment.TopEnd).offset(x = 28.dp, y = (-32).dp).size(95.dp).clip(CircleShape).background(c.selectedBg))
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("DISCOVER", style = HqType.labelSmall, color = c.textBrand, fontWeight = FontWeight.ExtraBold)
            Text("Looking for a flatmate?", style = HqType.titleSmall2, color = c.textPrimary, modifier = Modifier.padding(top = 3.dp))
            Text("Find people and flats that fit the way you live.", style = HqType.bodyMedium, color = c.textSecondary, modifier = Modifier.padding(bottom = 9.dp))
            HqButton(text = "Explore Discover", onClick = onOpenDiscover, variant = HqButtonVariant.Secondary, fullWidth = false, trailingIcon = HqIcons.Arrow)
        }
    }
}

private fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}
