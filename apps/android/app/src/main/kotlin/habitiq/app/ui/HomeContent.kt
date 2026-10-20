package habitiq.app.ui

import androidx.compose.ui.semantics.clearAndSetSemantics
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
import habitiq.app.ui.theme.HqShapes
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
import androidx.compose.ui.semantics.heading
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
import habitiq.app.ui.theme.hqShadow
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
    enum class Kind { JoinRequests, SwapRequests, VacancyRequests }
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

/**
 * Colour grade for the evening hero photo only (a dusk tint and warm lamp light). Everything that is not drawn
 * over the photo uses theme tokens, so light and dark mode follow the design system.
 */
private object HeroPhotoGrade {
    val dusk = Color(0xFF221B33)
    val warm = Color(0xFFFF8A3D)
}

/**
 * Authenticated Home, in reading order: greeting and the flat summary (your flat, tasks, balance) over the
 * evening photo, then "Your tasks" (overdue first), then anything waiting for the person (join and swap
 * requests, only when present), then "Recent activity". Adding things lives behind the + button, not here.
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
    val page = c.canvas
    HqHeroBleedEffect()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val pinAtPx = WindowInsets.statusBars.getTop(density) + with(density) { 56.dp.toPx() }
    val heroPinnedState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    habitiq.app.ui.components.HqPinnedTopBar(
        pinned = heroPinnedState.value,
        showLogo = true,
        actions = {
            habitiq.app.ui.components.HqIconButton(icon = HqIcons.Bell, contentDescription = "Notifications", onClick = onOpenNotifications)
        },
    )
    Column(modifier.fillMaxSize().background(page).verticalScroll(rememberScrollState())) {
        Box(Modifier.onGloballyPositioned { heroPinnedState.value = it.positionInWindow().y + it.size.height < pinAtPx }) {
            HomeHero(model, page, onOpenFlatSwitcher, onOpenExpenses, onOpenNotifications, onOpenMembers, onOpenProfile)
        }

        habitiq.app.ui.theme.HqFadeUp(index = 2) {
            Column(Modifier.padding(horizontal = HqSpacing.sm).padding(bottom = HqSpacing.fabClearance)) {
                TodayPanel(model, completingTaskId, onOpenTasks, onOpenTask, onCompleteTask)

                if (model.pending.isNotEmpty()) {
                    Column(Modifier.padding(horizontal = HqSpacing.md)) {
                        HqSectionTitle("Waiting for you")
                        Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
                            model.pending.forEach { item ->
                                HqCalloutCard(
                                    title = item.title, support = item.body, icon = HqIcons.Users,
                                    tone = HqTileTone.Coral, onClick = { onReviewPending(item.kind) },
                                )
                            }
                        }
                    }
                }

                if (model.activity.isNotEmpty()) {
                    Column(Modifier.padding(horizontal = HqSpacing.md)) {
                        HqSectionTitle("Recent activity", action = "See all", onAction = onOpenActivity)
                        val spine = c.borderSubtle
                        Column(
                            Modifier.padding(start = 14.dp).drawBehind {
                                drawLine(spine, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 1.dp.toPx())
                            },
                            verticalArrangement = Arrangement.spacedBy(HqSpacing.lg),
                        ) {
                            model.activity.forEach { item -> HqTimelineItem(item.text, item.time, Modifier.offset(x = (-14).dp)) }
                        }
                    }
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
    val c = LocalHqColors.current
    Box(Modifier.fillMaxWidth().background(HeroPhotoGrade.dusk)) {
        Image(
            painterResource(R.drawable.home_hero),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(0f, -0.1f),
            modifier = Modifier.matchParentSize(),
        )
        // Evening grade: dusk at the top for the logo and greeting, warm lamp light through the middle.
        Box(Modifier.matchParentSize().background(HeroPhotoGrade.warm.copy(alpha = .22f)))
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    0f to HeroPhotoGrade.dusk.copy(alpha = .80f),
                    // Mid stops hold white body text at 4.5:1 even over the sunlit wall in the photo.
                    .30f to HeroPhotoGrade.dusk.copy(alpha = .66f),
                    .60f to HeroPhotoGrade.dusk.copy(alpha = .62f),
                    .88f to HeroPhotoGrade.dusk.copy(alpha = .44f),
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
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = HqSpacing.screenHorizontal).padding(top = 10.dp, bottom = 44.dp),
        ) {
            HeroTopBar(model, onOpenNotifications, onOpenMembers, onOpenProfile)
            Spacer(Modifier.size(HqSpacing.xxxl))
            Text(
                LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)),
                style = HqType.labelMedium,
                color = c.onImage.copy(alpha = .88f),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 6.dp),
            )
            Text(
                "${greeting()}, ${model.greetingName}",
                style = HqType.display,
                color = c.onImage,
                modifier = Modifier.padding(start = 6.dp, top = HqSpacing.sm).semantics { heading() },
            )
            Text(
                model.summaryHeadline,
                style = HqType.bodyLarge,
                color = c.onImage,
                modifier = Modifier.padding(start = 6.dp, top = HqSpacing.sm, end = HqSpacing.huge),
            )
            Spacer(Modifier.size(HqSpacing.xl))
            FlatCard(model, onOpenFlatSwitcher, onOpenMembers)
            Spacer(Modifier.size(HqSpacing.sm))
            SummaryCard(model, onOpenExpenses)
        }
    }
}

private fun Modifier.heroGlass(shape: androidx.compose.ui.graphics.Shape): Modifier = this
    .clip(shape)
    // Smoky glass: a dark tint of the photo grade keeps white text readable over bright parts of the photo.
    .background(Brush.verticalGradient(listOf(HeroPhotoGrade.dusk.copy(alpha = .48f), HeroPhotoGrade.dusk.copy(alpha = .40f))))
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
    val c = LocalHqColors.current
    Row(Modifier.fillMaxWidth().defaultMinSize(minHeight = HqSize.target), verticalAlignment = Alignment.CenterVertically) {
        habitiq.app.ui.components.HqWordmark(height = 30.dp, tint = c.onImage)
        Spacer(Modifier.weight(1f))
        GlassIconButton(if (model.unreadCount > 0) "Notifications, ${model.unreadCount} unread" else "Notifications", onOpenNotifications) {
            Box {
                Icon(HqIcons.Bell, null, tint = c.onImage, modifier = Modifier.size(HqIconSize.md))
                if (model.unreadCount > 0) {
                    Box(Modifier.align(Alignment.TopEnd).offset(x = 3.dp, y = (-2).dp).size(9.dp).clip(CircleShape).background(c.brandCoral))
                }
            }
        }
        Spacer(Modifier.size(HqSpacing.md))
        GlassIconButton("Members", onOpenMembers) { Icon(HqIcons.Users, null, tint = c.onImage, modifier = Modifier.size(HqIconSize.md)) }
        Spacer(Modifier.size(HqSpacing.md))
        Box(
            Modifier.size(HqSize.target).clip(CircleShape).background(c.surfaceRaised)
                .clickable(role = Role.Button, onClickLabel = "Profile", onClick = onOpenProfile)
                .semantics { contentDescription = "Profile" },
            contentAlignment = Alignment.Center,
        ) {
            Text(model.greetingName.take(1).uppercase(), style = HqType.titleSmall2, color = c.textBrand, modifier = Modifier.clearAndSetSemantics {})
        }
    }
}

@Composable
private fun FlatCard(model: HomeUiModel, onOpenFlatSwitcher: () -> Unit, onOpenMembers: () -> Unit) {
    val c = LocalHqColors.current
    val shape = HqShapes.card
    Row(Modifier.fillMaxWidth().heightIn(min = 76.dp).heroGlass(shape), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier.weight(1f).clickable(role = Role.Button, onClickLabel = "Switch flat", onClick = onOpenFlatSwitcher)
                .padding(horizontal = 14.dp, vertical = HqSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(46.dp).clip(HqShapes.control).background(c.brandTeal.copy(alpha = .55f))
                    .border(1.dp, c.onImage.copy(alpha = .25f), HqShapes.control),
                contentAlignment = Alignment.Center,
            ) { Icon(HqIcons.Home, null, tint = c.onImage, modifier = Modifier.size(HqIconSize.md)) }
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Your flat", style = HqType.labelMedium, color = c.onImage.copy(alpha = .86f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(model.flatName, style = HqType.titleSmall2, color = c.onImage, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    Icon(HqIcons.Chevron, null, tint = c.onImage, modifier = Modifier.padding(start = 6.dp).size(HqIconSize.sm))
                }
            }
        }
        Box(Modifier.size(width = 1.dp, height = 44.dp).background(c.onImage.copy(alpha = .22f)))
        Column(
            Modifier.clickable(role = Role.Button, onClickLabel = "Members", onClick = onOpenMembers).padding(horizontal = 10.dp, vertical = HqSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val shown = model.memberNames.take(3)
            val extra = model.memberCount - shown.size
            Row {
                shown.forEachIndexed { i, name ->
                    HqAvatar(
                        name, size = HqAvatarSize.SM,
                        tone = listOf(HqAvatarTone.Teal, HqAvatarTone.Sand, HqAvatarTone.Coral)[i % 3],
                        modifier = Modifier.offset(x = (-8 * i).dp).border(2.dp, c.onImage, CircleShape),
                    )
                }
                if (extra > 0) {
                    Box(
                        Modifier.offset(x = (-8 * shown.size).dp).size(32.dp).heroGlass(CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Text("+$extra", style = HqType.labelMedium, color = c.onImage, fontWeight = FontWeight.SemiBold) }
                }
            }
            Text(
                "${model.memberCount} ${if (model.memberCount == 1) "member" else "members"}",
                style = HqType.bodyMedium, color = c.onImage.copy(alpha = .9f), modifier = Modifier.padding(top = HqSpacing.xs),
            )
        }
    }
}

@Composable
private fun SummaryCard(model: HomeUiModel, onOpenExpenses: () -> Unit) {
    val c = LocalHqColors.current
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
    val shape = HqShapes.card
    Row(Modifier.fillMaxWidth().heightIn(min = 76.dp).heroGlass(shape), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f).padding(start = HqSpacing.md, end = 6.dp, top = HqSpacing.md, bottom = HqSpacing.md), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(38.dp).clip(CircleShape).background(c.brandTeal.copy(alpha = .35f)), contentAlignment = Alignment.Center) {
                Box(Modifier.size(22.dp).clip(CircleShape).background(c.brandTeal), contentAlignment = Alignment.Center) {
                    Icon(HqIcons.Check, null, tint = c.onImage, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.size(10.dp))
            Column {
                Text("${model.assignedCount}", style = HqType.titleSmall2, color = c.onImage)
                Text(if (model.assignedCount == 1) "Task for you" else "Tasks for you", style = HqType.rowTitle, color = c.onImage, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        Box(Modifier.size(width = 1.dp, height = 52.dp).background(c.onImage.copy(alpha = .22f)))
        Row(
            Modifier.weight(1f).clickable(role = Role.Button, onClickLabel = "Open balances", onClick = onOpenExpenses)
                .padding(start = 14.dp, end = HqSpacing.md, top = HqSpacing.md, bottom = HqSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(42.dp).heroGlass(CircleShape), contentAlignment = Alignment.Center) {
                Text("₹", style = HqType.titleSmall2, color = c.onImage)
            }
            Spacer(Modifier.size(HqSpacing.md))
            Column {
                Text(moneyTitle, style = HqType.amountRow, color = c.onImage, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(moneySupport, style = HqType.bodyMedium, color = c.onImage.copy(alpha = .82f), maxLines = 1)
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
    val shape = HqShapes.card
    Column(
        Modifier.fillMaxWidth()
            .hqShadow(habitiq.app.ui.theme.HqShadow.Floating, shape)
            .clip(shape).background(c.surfaceRaised).padding(horizontal = HqSpacing.lg, vertical = HqSpacing.lg),
    ) {
        Row(Modifier.fillMaxWidth().padding(start = 2.dp, bottom = HqSpacing.md), verticalAlignment = Alignment.CenterVertically) {
            Text("Your tasks", style = HqType.titleMedium2, color = c.textPrimary, modifier = Modifier.weight(1f).semantics { heading() })
            Row(
                Modifier.defaultMinSize(minHeight = HqSize.target).clip(HqShapes.control).clickable(role = Role.Button, onClick = onOpenTasks)
                    .semantics { contentDescription = "See all tasks" }
                    .padding(horizontal = 6.dp, vertical = HqSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("See all", style = HqType.rowTitle, color = c.textBrand, fontWeight = FontWeight.SemiBold, modifier = Modifier.clearAndSetSemantics {})
                Icon(HqIcons.Arrow, null, tint = c.textBrand, modifier = Modifier.padding(start = 6.dp).size(HqIconSize.sm))
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
                    )
                }
            }
        }
    }
}

/** Nothing assigned: a calm note with the clipboard illustration, no call to action. */
@Composable
private fun HomeEmptyTasks() {
    val c = LocalHqColors.current
    val shape = HqShapes.card
    Column(
        Modifier.fillMaxWidth().clip(shape).background(c.surfaceSubtle).padding(vertical = HqSpacing.xxl, horizontal = HqSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ClipboardDone(Modifier.size(width = 120.dp, height = 84.dp))
        Text("No tasks for you right now", style = HqType.titleSmall2, color = c.textPrimary, modifier = Modifier.padding(top = 14.dp))
        Text(
            "Tasks your flat gives you show up here.",
            style = HqType.bodyLarge, color = c.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun ClipboardDone(modifier: Modifier) {
    val c = LocalHqColors.current
    val ray = c.brandTeal.copy(alpha = .45f)
    val boardBack = c.borderControl
    val boardTop = c.surfaceRaised
    val boardBottom = c.selectedBg
    val clip = c.borderControl
    val check = c.brandTeal
    androidx.compose.foundation.Canvas(modifier) {
        val cx = size.width / 2
        val boardW = size.height * 0.62f
        val boardH = size.height * 0.80f
        val top = size.height * 0.14f
        val left = cx - boardW / 2
        val cy = top + boardH / 2
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
        drawRoundRect(boardBack, Offset(left - 6.dp.toPx(), top + 4.dp.toPx()), androidx.compose.ui.geometry.Size(boardW, boardH), radius)
        drawRoundRect(
            Brush.verticalGradient(listOf(boardTop, boardBottom), startY = top, endY = top + boardH),
            Offset(left, top), androidx.compose.ui.geometry.Size(boardW, boardH), radius,
        )
        drawRoundRect(
            clip, Offset(cx - boardW * 0.28f, top - 5.dp.toPx()),
            androidx.compose.ui.geometry.Size(boardW * 0.56f, 10.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()),
        )
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(cx - boardW * 0.2f, top + boardH * 0.55f)
            lineTo(cx - boardW * 0.04f, top + boardH * 0.70f)
            lineTo(cx + boardW * 0.24f, top + boardH * 0.40f)
        }
        drawPath(
            path, check,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round,
            ),
        )
    }
}

private fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}
