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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.core.view.WindowCompat
import habitiq.app.R
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Group
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
    /** Member display names, the person first, for the flat card's avatar stack. */
    val memberNames: List<String> = emptyList(),
    val isAdmin: Boolean = false,
)

/** Palette taken from the Oddroof dashboard reference (light). Dark mode falls back to theme tokens. */
private object HomeLook {
    val ink = Color(0xFF0F1F3D)
    val muted = Color(0xFF5E6B7E)
    val link = Color(0xFF0E8C7E)
    val page = Color(0xFFF2F6FA)
    val emptyBox = Color(0xFFF2F6FC)
    val dusk = Color(0xFF221B33)
    val warm = Color(0xFFFF8A3D)
}

private fun sp(v: Float) = androidx.compose.ui.unit.TextUnit(v, androidx.compose.ui.unit.TextUnitType.Sp)

private data class QuickAction(
    val title: String, val support: String, val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tile: Color, val chip: Color, val glyph: Color, val onClick: () -> Unit,
)

/**
 * Authenticated Home, matching the Oddroof dashboard reference: an evening photo hero with the
 * greeting, a glass "Your flat" card and a glass summary card, then a white Today's tasks panel and
 * pastel Quick actions. Pending requests, recent activity and Discover follow below.
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
    onAddTask: () -> Unit = onOpenTasks,
    onAddExpense: () -> Unit = onOpenExpenses,
    onAddBill: () -> Unit = onOpenExpenses,
    onOpenProfile: () -> Unit = {},
) {
    val c = LocalHqColors.current
    val page = if (c.isDark) c.canvas else HomeLook.page
    HqHeroBleedEffect()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val pinAtPx = WindowInsets.statusBars.getTop(density) + with(density) { 56.dp.toPx() }
    val heroPinnedState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    habitiq.app.ui.components.HqPinnedTopBar(
        pinned = heroPinnedState.value,
        showLogo = true,
        actions = {
            androidx.compose.material3.IconButton(onClick = onOpenNotifications, modifier = Modifier.size(HqSize.target)) {
                Icon(HqIcons.Bell, contentDescription = "Notifications", tint = c.iconDefault, modifier = Modifier.size(HqIconSize.md))
            }
        },
    )
    Column(modifier.fillMaxSize().background(page).verticalScroll(rememberScrollState())) {
        Box(Modifier.onGloballyPositioned { heroPinnedState.value = it.positionInWindow().y + it.size.height < pinAtPx }) {
            HomeHero(model, page, onOpenFlatSwitcher, onOpenExpenses, onOpenNotifications, onOpenMembers, onOpenProfile)
        }

        habitiq.app.ui.theme.HqFadeUp(index = 2) {
            Column(Modifier.padding(horizontal = 8.dp).padding(bottom = HqSpacing.screenEnd)) {
                TodayPanel(model, completingTaskId, onOpenTasks, onOpenTask, onCompleteTask)

                if (model.pending.isNotEmpty()) {
                    Column(Modifier.padding(horizontal = 10.dp)) {
                        HqSectionTitle("Pending activity")
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            model.pending.forEach { item ->
                                HqCalloutCard(
                                    title = item.title, support = item.body, icon = HqIcons.Users,
                                    tone = HqTileTone.Coral, onClick = { onReviewPending(item.kind) },
                                )
                            }
                        }
                    }
                }

                QuickActions(model, onAddTask, onAddExpense, onAddBill, onInvite)

                Column(Modifier.padding(horizontal = 10.dp)) {
                    if (model.activity.isNotEmpty()) {
                        HqSectionTitle("Recent activity", action = "View all", onAction = onOpenActivity)
                        val spine = c.borderSubtle
                        Column(
                            Modifier.padding(start = 14.dp).drawBehind {
                                drawLine(spine, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 1.dp.toPx())
                            },
                            verticalArrangement = Arrangement.spacedBy(17.dp),
                        ) {
                            model.activity.forEach { item -> HqTimelineItem(item.text, item.time, Modifier.offset(x = (-14).dp)) }
                        }
                    }
                    DiscoverTeaser(onOpenDiscover)
                }
            }
        }
    }
}

@Composable
private fun HomeHero(
    model: HomeUiModel, page: Color,
    onOpenFlatSwitcher: () -> Unit, onOpenExpenses: () -> Unit,
    onOpenNotifications: () -> Unit, onOpenMembers: () -> Unit, onOpenProfile: () -> Unit,
) {
    Box(Modifier.fillMaxWidth().background(HomeLook.dusk)) {
        Image(
            painterResource(R.drawable.home_hero),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(0f, -0.1f),
            modifier = Modifier.matchParentSize(),
        )
        // Evening grade: dusk at the top for the logo and greeting, warm lamp light through the middle.
        Box(Modifier.matchParentSize().background(HomeLook.warm.copy(alpha = .22f)))
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    0f to HomeLook.dusk.copy(alpha = .80f),
                    .30f to HomeLook.dusk.copy(alpha = .52f),
                    .60f to HomeLook.dusk.copy(alpha = .46f),
                    .88f to HomeLook.dusk.copy(alpha = .30f),
                    1f to Color.Transparent,
                ),
            ),
        )
        // The photo melts into the page below the summary card.
        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().heightIn(min = 70.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, page.copy(alpha = .85f), page))),
        )
        Column(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 18.dp).padding(top = 10.dp, bottom = 44.dp),
        ) {
            HeroTopBar(model, onOpenNotifications, onOpenMembers, onOpenProfile)
            Spacer(Modifier.size(42.dp))
            Text(
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, d MMMM", Locale.ENGLISH)).uppercase(),
                style = HqType.labelMedium.copy(letterSpacing = sp(3f)),
                color = Color.White.copy(alpha = .86f),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 6.dp),
            )
            Text(
                "${greeting()},\n${model.greetingName} 👋",
                style = HqType.display.copy(fontSize = sp(32f), lineHeight = sp(38f)),
                color = Color.White,
                modifier = Modifier.padding(start = 6.dp, top = 10.dp),
            )
            Text(
                "${model.summaryHeadline} A calm look at ${model.flatName} today.",
                style = HqType.bodyLarge,
                color = Color.White.copy(alpha = .9f),
                modifier = Modifier.padding(start = 6.dp, top = 8.dp, end = 40.dp),
            )
            Spacer(Modifier.size(22.dp))
            FlatCard(model, onOpenFlatSwitcher, onOpenMembers)
            Spacer(Modifier.size(8.dp))
            SummaryCard(model, onOpenExpenses)
        }
    }
}

private fun Modifier.heroGlass(shape: androidx.compose.ui.graphics.Shape): Modifier = this
    .clip(shape)
    // Smoky glass: a warm-dark tint keeps white text readable over bright parts of the photo.
    .background(Brush.verticalGradient(listOf(Color(0xFF3A2E3E).copy(alpha = .42f), Color(0xFF2A2232).copy(alpha = .36f))))
    .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = .16f), Color.White.copy(alpha = .06f))))
    .border(1.dp, Brush.verticalGradient(listOf(Color.White.copy(alpha = .42f), Color.White.copy(alpha = .12f))), shape)

@Composable
private fun GlassIconButton(label: String, onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier.size(HqSize.target).heroGlass(CircleShape)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun HeroTopBar(model: HomeUiModel, onOpenNotifications: () -> Unit, onOpenMembers: () -> Unit, onOpenProfile: () -> Unit) {
    Row(Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        habitiq.app.ui.components.HqWordmark(height = 30.dp, tint = Color.White)
        Spacer(Modifier.weight(1f))
        GlassIconButton(if (model.unreadCount > 0) "Notifications, ${model.unreadCount} unread" else "Notifications", onOpenNotifications) {
            Box {
                Icon(HqIcons.Bell, null, tint = Color.White, modifier = Modifier.size(22.dp))
                if (model.unreadCount > 0) {
                    Box(Modifier.align(Alignment.TopEnd).offset(x = 3.dp, y = (-2).dp).size(9.dp).clip(CircleShape).background(Color(0xFFFF3B30)))
                }
            }
        }
        Spacer(Modifier.size(10.dp))
        GlassIconButton("Members", onOpenMembers) { Icon(HqIcons.Users, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
        Spacer(Modifier.size(10.dp))
        Box(
            Modifier.size(HqSize.target).clip(CircleShape).background(Color.White.copy(alpha = .92f))
                .clickable(role = Role.Button, onClickLabel = "Profile", onClick = onOpenProfile),
            contentAlignment = Alignment.Center,
        ) {
            Text(model.greetingName.take(1).uppercase(), style = HqType.titleSmall2, color = HomeLook.link)
        }
    }
}

@Composable
private fun FlatCard(model: HomeUiModel, onOpenFlatSwitcher: () -> Unit, onOpenMembers: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Row(Modifier.fillMaxWidth().heightIn(min = 76.dp).heroGlass(shape), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f).clickable(role = Role.Button, onClickLabel = "Switch flat", onClick = onOpenFlatSwitcher)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFF2E8C80).copy(alpha = .75f))
                    .border(1.dp, Color.White.copy(alpha = .25f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center,
            ) { Icon(HqIcons.Home, null, tint = Color(0xFFB8F2EA), modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text("YOUR FLAT", style = HqType.labelSmall.copy(letterSpacing = sp(2f)), color = Color.White.copy(alpha = .82f), fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(model.flatName, style = HqType.titleSmall2.copy(lineHeight = sp(22f)), color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    Icon(HqIcons.Chevron, null, tint = Color.White, modifier = Modifier.padding(start = 6.dp).size(18.dp))
                }
            }
        }
        Box(Modifier.size(width = 1.dp, height = 44.dp).background(Color.White.copy(alpha = .22f)))
        Column(
            Modifier.clickable(role = Role.Button, onClickLabel = "Members", onClick = onOpenMembers).padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val shown = model.memberNames.take(3)
            val extra = model.memberCount - shown.size
            Row {
                shown.forEachIndexed { i, name ->
                    HqAvatar(
                        name, size = HqAvatarSize.SM,
                        tone = listOf(HqAvatarTone.Teal, HqAvatarTone.Sand, HqAvatarTone.Coral)[i % 3],
                        modifier = Modifier.offset(x = (-8 * i).dp).border(2.dp, Color.White, CircleShape),
                    )
                }
                if (extra > 0) {
                    Box(
                        Modifier.offset(x = (-8 * shown.size).dp).size(32.dp).heroGlass(CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Text("+$extra", style = HqType.labelMedium, color = Color.White, fontWeight = FontWeight.SemiBold) }
                }
            }
            Text(
                "${model.memberCount} ${if (model.memberCount == 1) "member" else "members"}",
                style = HqType.bodyMedium, color = Color.White.copy(alpha = .9f), modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun SummaryCard(model: HomeUiModel, onOpenExpenses: () -> Unit) {
    val b = model.balance
    val moneyTitle = when {
        b.owe > 0 -> "You owe ${model.balanceText(b.owe)}"
        b.owed > 0 -> "You're owed ${model.balanceText(b.owed)}"
        else -> "All settled"
    }
    val moneySupport = when {
        b.owe > 0 -> "To ${b.oweCount} ${if (b.oweCount == 1) "person" else "people"}"
        b.owed > 0 -> "From ${b.owedCount} ${if (b.owedCount == 1) "person" else "people"}"
        else -> "No dues"
    }
    val shape = RoundedCornerShape(22.dp)
    Row(Modifier.fillMaxWidth().heightIn(min = 76.dp).heroGlass(shape), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f).padding(start = 12.dp, end = 6.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(Color(0xFF8FE3D3).copy(alpha = .35f)), contentAlignment = Alignment.Center) {
                Box(Modifier.size(22.dp).clip(CircleShape).background(Color(0xFF3CC3AA)), contentAlignment = Alignment.Center) {
                    Icon(HqIcons.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.size(10.dp))
            Column {
                Text("${model.assignedCount}", style = HqType.titleSmall2, color = Color.White)
                Text(if (model.assignedCount == 1) "Task for you" else "Tasks for you", style = HqType.rowTitle.copy(fontSize = sp(15f)), color = Color.White, maxLines = 1, softWrap = false)
                Text(if (model.assignedCount == 0) "All caught up" else "Due soon", style = HqType.bodySmall, color = Color.White.copy(alpha = .78f), maxLines = 1)
            }
        }
        Box(Modifier.size(width = 1.dp, height = 52.dp).background(Color.White.copy(alpha = .22f)))
        Row(
            Modifier.weight(1f).clickable(role = Role.Button, onClickLabel = "Open balances", onClick = onOpenExpenses)
                .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(42.dp).heroGlass(CircleShape), contentAlignment = Alignment.Center) {
                Text("₹", style = HqType.titleSmall2, color = Color.White)
            }
            Spacer(Modifier.size(12.dp))
            Column {
                Text(moneyTitle, style = HqType.rowTitle.copy(lineHeight = sp(20f)), color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(moneySupport, style = HqType.bodyMedium, color = Color.White.copy(alpha = .78f), maxLines = 1)
            }
        }
    }
}

@Composable
private fun TodayPanel(
    model: HomeUiModel, completingTaskId: String?,
    onOpenTasks: () -> Unit, onOpenTask: (String) -> Unit, onCompleteTask: (String) -> Unit,
) {
    val c = LocalHqColors.current
    val ink = if (c.isDark) c.textPrimary else HomeLook.ink
    val link = if (c.isDark) c.textBrand else HomeLook.link
    val shape = RoundedCornerShape(26.dp)
    Column(
        Modifier.fillMaxWidth()
            .shadow(12.dp, shape, ambientColor = Color(0xFF1B3A6B).copy(alpha = .08f), spotColor = Color(0xFF1B3A6B).copy(alpha = .08f))
            .clip(shape).background(c.surfaceRaised).padding(horizontal = 16.dp, vertical = 18.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(start = 2.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Today's tasks", style = HqType.titleMedium2.copy(fontSize = sp(22f)), color = ink, modifier = Modifier.weight(1f))
            Row(
                Modifier.clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button, onClick = onOpenTasks).padding(horizontal = 6.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("See all", style = HqType.rowTitle, color = link, fontWeight = FontWeight.SemiBold)
                Icon(HqIcons.Arrow, null, tint = link, modifier = Modifier.padding(start = 6.dp).size(18.dp))
            }
        }
        if (model.tasks.isEmpty()) {
            HomeEmptyTasks()
        } else {
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
                                task.dueText, style = HqType.bodyMedium,
                                color = if (task.overdue) c.statusWarningFg else c.textSecondary,
                                fontWeight = if (task.overdue) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        trailing = { HqPill("YOU") },
                    )
                }
            }
        }
    }
}

/** "You're all caught up!" with the clipboard-and-sparkles illustration from the reference. */
@Composable
private fun HomeEmptyTasks() {
    val c = LocalHqColors.current
    val ink = if (c.isDark) c.textPrimary else HomeLook.ink
    val muted = if (c.isDark) c.textSecondary else HomeLook.muted
    val shape = RoundedCornerShape(22.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape).background(if (c.isDark) c.surfaceSubtle else HomeLook.emptyBox).padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ClipboardDone(Modifier.size(width = 120.dp, height = 84.dp))
        Text("You're all caught up!", style = HqType.titleSmall2.copy(fontSize = sp(20f)), color = ink, modifier = Modifier.padding(top = 14.dp))
        Text(
            "Nothing is assigned to you right now.\nEnjoy your day! 🎉",
            style = HqType.bodyLarge, color = muted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun ClipboardDone(modifier: Modifier) {
    androidx.compose.foundation.Canvas(modifier) {
        val cx = size.width / 2
        val boardW = size.height * 0.62f
        val boardH = size.height * 0.80f
        val top = size.height * 0.14f
        val left = cx - boardW / 2
        val cy = top + boardH / 2
        val ray = Color(0xFF8FB8F2)
        listOf(-150.0, -125.0, -55.0, -30.0, 150.0, 30.0).forEach { deg ->
            val a = Math.toRadians(deg)
            val r1 = boardW * 0.80f
            val r2 = boardW * 0.98f
            drawLine(
                ray,
                Offset(cx + (r1 * kotlin.math.cos(a)).toFloat(), cy + (r1 * kotlin.math.sin(a)).toFloat()),
                Offset(cx + (r2 * kotlin.math.cos(a)).toFloat(), cy + (r2 * kotlin.math.sin(a)).toFloat()),
                strokeWidth = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }
        val radius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
        drawRoundRect(Color(0xFFBCD6F7), Offset(left - 6.dp.toPx(), top + 4.dp.toPx()), androidx.compose.ui.geometry.Size(boardW, boardH), radius)
        drawRoundRect(
            Brush.verticalGradient(listOf(Color(0xFFF5F9FF), Color(0xFFE3EEFD)), startY = top, endY = top + boardH),
            Offset(left, top), androidx.compose.ui.geometry.Size(boardW, boardH), radius,
        )
        drawRoundRect(
            Color(0xFFA9C8F2), Offset(cx - boardW * 0.28f, top - 5.dp.toPx()),
            androidx.compose.ui.geometry.Size(boardW * 0.56f, 10.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()),
        )
        val check = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx - boardW * 0.2f, top + boardH * 0.55f)
            lineTo(cx - boardW * 0.04f, top + boardH * 0.70f)
            lineTo(cx + boardW * 0.24f, top + boardH * 0.40f)
        }
        drawPath(
            check, Color(0xFF14A08F),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round,
            ),
        )
    }
}

@Composable
private fun QuickActions(model: HomeUiModel, onAddTask: () -> Unit, onAddExpense: () -> Unit, onAddBill: () -> Unit, onInvite: () -> Unit) {
    val c = LocalHqColors.current
    val ink = if (c.isDark) c.textPrimary else HomeLook.ink
    val muted = if (c.isDark) c.textSecondary else HomeLook.muted
    val dark = c.isDark
    fun tone(light: Long, darkAlpha: Float) = if (dark) Color(light).copy(alpha = darkAlpha) else Color(light)
    val icons = androidx.compose.material.icons.Icons.Rounded
    val actions = buildList {
        if (model.isAdmin) add(QuickAction("Add Task", "Assign or\nrotate", icons.CheckCircle, tone(0xFFE6F6F1, .10f), tone(0xFFCBEDE3, .22f), Color(0xFF14A08F), onAddTask))
        add(QuickAction("Add Expense", "Split with\nflatmates", icons.CreditCard, tone(0xFFE9F0FC, .10f), tone(0xFFD3E2FA, .22f), Color(0xFF2F6BEF), onAddExpense))
        if (model.isAdmin) add(QuickAction("Add Bill", "Set up\nrecurring", icons.Description, tone(0xFFFFF3E6, .10f), tone(0xFFFFE0BD, .22f), Color(0xFFF08A1C), onAddBill))
        if (model.canInvite) add(QuickAction("Invite\nRoommate", "Share code", icons.Group, tone(0xFFF1EDFC, .10f), tone(0xFFE1D9FA, .22f), Color(0xFF7457E0), onInvite))
    }
    Column(Modifier.padding(horizontal = 10.dp).padding(top = 26.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Quick actions", style = HqType.titleMedium2.copy(fontSize = sp(22f)), color = ink, softWrap = false)
            Text(
                "Add, track or manage in seconds.", style = HqType.bodySmall.copy(fontSize = sp(12.5f)), color = muted, maxLines = 1,
                overflow = TextOverflow.Ellipsis, textAlign = androidx.compose.ui.text.style.TextAlign.End,
                modifier = Modifier.weight(1f).padding(start = 10.dp),
            )
        }
        Row(Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Max).padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            actions.forEach { a ->
                Column(
                    Modifier.weight(1f).fillMaxHeight().heightIn(min = 104.dp).clip(RoundedCornerShape(20.dp)).background(a.tile)
                        .clickable(role = Role.Button, onClick = a.onClick).padding(horizontal = 3.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(a.chip), contentAlignment = Alignment.Center) {
                        Icon(a.icon, null, tint = a.glyph, modifier = Modifier.size(24.dp))
                    }
                    Text(
                        a.title, style = HqType.rowTitle.copy(fontSize = sp(13.5f), lineHeight = sp(18f), letterSpacing = sp(-0.1f)), color = ink, fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 2, modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        a.support, style = HqType.bodySmall.copy(fontSize = sp(12f), lineHeight = sp(16f)), color = muted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }
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
