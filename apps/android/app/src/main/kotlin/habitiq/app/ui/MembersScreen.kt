package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import habitiq.app.data.JoinRequest
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.ui.components.HqAvatar
import habitiq.app.ui.components.HqAvatarSize
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonPair
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqConfirmDialog
import habitiq.app.ui.components.HqGroup
import habitiq.app.ui.components.HqInlineInfo
import habitiq.app.ui.components.HqListSkeleton
import habitiq.app.ui.components.HqRow
import habitiq.app.ui.components.HqRowDivider
import habitiq.app.ui.components.HqSectionHeader
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.theme.HqSize
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.LocalHqColors

/**
 * A name to show for a flatmate: their nickname, else the local part of their email when we know it,
 * else "Flatmate". Never empty.
 */
internal fun memberDisplayName(nickname: String, email: String? = null): String =
    nickname.trim()
        .ifBlank { email?.substringBefore("@")?.trim().orEmpty() }
        .ifBlank { "Flatmate" }

/** Divider inset for rows with a leading avatar: row padding + leading column + gap. */
private val AvatarRowInset = HqSpacing.component + HqSize.avatarMedium + HqSpacing.related

@Composable
fun MembersScreen(viewModel: FlatViewModel, onBack: () -> Unit) {
    val c = LocalHqColors.current
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val joinRequests by viewModel.joinRequests.collectAsStateWithLifecycleCompat()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycleCompat()
    val loading by viewModel.loading.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val uid = currentUser?.uid.orEmpty()
    val pending = joinRequests.filter { it.status == "pending" }
    val nameOf = { m: Member -> memberDisplayName(m.nickname, currentUser?.email.takeIf { m.uid == uid }) }

    var removing by remember { mutableStateOf<Member?>(null) }
    var declining by remember { mutableStateOf<JoinRequest?>(null) }
    removing?.let { m ->
        HqConfirmDialog(
            title = "Remove ${nameOf(m)}?",
            message = "They'll lose access to this flat's tasks, expenses and bills.",
            confirmLabel = "Remove",
            onConfirm = { viewModel.kickMember(m.uid); removing = null },
            onDismiss = { removing = null },
        )
    }
    declining?.let { req ->
        HqConfirmDialog(
            title = "Decline ${memberDisplayName(req.nickname, req.email)}?",
            message = "They won't be added to this flat.",
            confirmLabel = "Decline",
            onConfirm = { viewModel.rejectJoinRequest(req.id); declining = null },
            onDismiss = { declining = null },
        )
    }

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        HqBackAppBar(title = "Members", onBack = onBack)
        LazyColumn(
            contentPadding = PaddingValues(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.sm),
        ) {
            if (isAdmin && pending.isNotEmpty()) {
                item(key = "requests-header") { HqSectionHeader("Join requests") }
                item(key = "requests") {
                    HqGroup {
                        pending.forEachIndexed { i, req ->
                            JoinRequestRow(
                                req,
                                onApprove = { viewModel.approveJoinRequest(req) },
                                onDecline = { declining = req },
                            )
                            if (i < pending.lastIndex) HqRowDivider(startInset = AvatarRowInset)
                        }
                    }
                }
                item(key = "members-header") { HqSectionHeader("In this flat") }
            }
            when {
                // A flat always has at least one member (you), so an empty list means it hasn't loaded yet.
                loading || members.isEmpty() -> item(key = "loading") { HqListSkeleton(rows = 3) }
                else -> {
                    item(key = "members") {
                        HqGroup {
                            members.forEachIndexed { i, member ->
                                MemberRow(
                                    name = nameOf(member),
                                    member = member,
                                    isYou = member.uid == uid,
                                    canKick = isAdmin && member.uid != uid && member.role != "admin",
                                    onKick = { removing = member },
                                )
                                if (i < members.lastIndex) HqRowDivider(startInset = AvatarRowInset)
                            }
                        }
                    }
                    if (members.size == 1) {
                        item(key = "alone") {
                            HqInlineInfo("Only you so far. Share your invite code to add flatmates.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberRow(name: String, member: Member, isYou: Boolean, canKick: Boolean, onKick: () -> Unit) {
    val c = LocalHqColors.current
    HqRow(
        title = if (isYou) "$name (you)" else name,
        supporting = member.role.replaceFirstChar { it.uppercase() } +
            if (member.status == "out_of_station") " · Away" else "",
        leading = { HqAvatar(name = name, size = HqAvatarSize.MD) },
        trailing = if (canKick) ({ HqTextButton(text = "Remove", onClick = onKick, color = c.statusDangerFg) }) else null,
    )
}

@Composable
private fun JoinRequestRow(req: JoinRequest, onApprove: () -> Unit, onDecline: () -> Unit) {
    val name = memberDisplayName(req.nickname, req.email)
    Column {
        HqRow(
            title = name,
            supporting = req.email.ifBlank { null },
            leading = { HqAvatar(name = name, size = HqAvatarSize.MD) },
        )
        HqButtonPair(
            secondary = { HqButton(text = "Decline", onClick = onDecline, variant = HqButtonVariant.Secondary) },
            primary = { HqButton(text = "Approve", onClick = onApprove) },
            modifier = Modifier.padding(start = AvatarRowInset, end = HqSpacing.component, bottom = HqSpacing.related),
        )
    }
}
