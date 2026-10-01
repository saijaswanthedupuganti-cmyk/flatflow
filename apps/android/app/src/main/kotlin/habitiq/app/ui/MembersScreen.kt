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
import habitiq.app.ui.components.HqTextButton
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

    Column(Modifier.fillMaxSize().background(c.background)) {
        Row(Modifier.padding(HqSpacing.lg), verticalAlignment = Alignment.CenterVertically) {
            HqTextButton(text = "← Back", onClick = onBack)
            Text("Members", style = HqType.headlineSmall, color = c.textPrimary, modifier = Modifier.padding(start = HqSpacing.sm))
        }
        if (isAdmin && pending.isNotEmpty()) {
            Text("Join requests", modifier = Modifier.padding(horizontal = HqSpacing.lg), style = HqType.titleSmall, color = c.textPrimary)
            pending.forEach { req ->
                HqCard(modifier = Modifier.padding(horizontal = HqSpacing.lg, vertical = HqSpacing.xs)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(req.nickname, style = HqType.titleSmall, color = c.textPrimary)
                            Text(req.email, style = HqType.bodySmall, color = c.textSecondary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                            HqButton(text = "Approve", onClick = { viewModel.approveJoinRequest(req) }, fullWidth = false)
                            HqButton(text = "Reject", onClick = { viewModel.rejectJoinRequest(req.id) }, variant = HqButtonVariant.Secondary, fullWidth = false)
                        }
                    }
                }
            }
            Spacer(Modifier.height(HqSpacing.md))
        }
        LazyColumn(contentPadding = PaddingValues(HqSpacing.lg), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            items(members, key = { it.uid }) { member ->
                MemberRow(member, isAdmin, member.uid != uid, onKick = { viewModel.kickMember(member.uid) })
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
                            if (member.status == "out_of_station") " · away" else "",
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
