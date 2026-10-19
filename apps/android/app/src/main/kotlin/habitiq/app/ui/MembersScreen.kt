package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.ui.components.HqAvatar
import habitiq.app.ui.components.HqAvatarSize
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqConfirmDialog
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

@Composable
fun MembersScreen(viewModel: FlatViewModel, onBack: () -> Unit) {
    val c = LocalHqColors.current
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val joinRequests by viewModel.joinRequests.collectAsStateWithLifecycleCompat()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val uid = currentUser?.uid.orEmpty()
    val pending = joinRequests.filter { it.status == "pending" }
    var removing by remember { mutableStateOf<Member?>(null) }
    removing?.let { m ->
        HqConfirmDialog(
            title = "Remove ${m.nickname.ifBlank { "this member" }}?",
            message = "They'll lose access to this flat's tasks, expenses and bills.",
            confirmLabel = "Remove",
            onConfirm = { viewModel.kickMember(m.uid); removing = null },
            onDismiss = { removing = null },
        )
    }

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        HqBackAppBar(title = "Members", onBack = onBack)
        if (isAdmin && pending.isNotEmpty()) {
            Text("Join requests", modifier = Modifier.padding(horizontal = HqSpacing.lg), style = HqType.titleSmall, color = c.textPrimary)
            pending.forEach { req ->
                HqCard(modifier = Modifier.padding(horizontal = HqSpacing.lg, vertical = HqSpacing.xs)) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                        Text(req.nickname, style = HqType.titleSmall, color = c.textPrimary)
                        Text(req.email, style = HqType.bodySmall, color = c.textSecondary)
                        habitiq.app.ui.components.HqButtonPair(
                            secondary = { HqButton(text = "Decline", onClick = { viewModel.rejectJoinRequest(req.id) }, variant = HqButtonVariant.Secondary) },
                            primary = { HqButton(text = "Approve", onClick = { viewModel.approveJoinRequest(req) }) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(HqSpacing.md))
        }
        LazyColumn(contentPadding = PaddingValues(HqSpacing.lg), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            items(members, key = { it.uid }) { member ->
                MemberRow(member, isAdmin, member.uid != uid, onKick = { removing = member })
            }
        }
    }
}

@Composable
private fun MemberRow(member: Member, isAdmin: Boolean, canKick: Boolean, onKick: () -> Unit) {
    val c = LocalHqColors.current
    HqCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
                HqAvatar(name = member.nickname, size = HqAvatarSize.SM)
                Column {
                    Text(member.nickname, style = HqType.titleSmall, color = c.textPrimary)
                    Text(
                        member.role.replaceFirstChar { it.uppercase() } +
                            if (member.status == "out_of_station") " · Away" else "",
                        style = HqType.bodySmall, color = c.textSecondary
                    )
                }
            }
            if (isAdmin && canKick && member.role != "admin") {
                HqButton(text = "Remove", onClick = onKick, variant = HqButtonVariant.Destructive, fullWidth = false)
            }
        }
    }
}
