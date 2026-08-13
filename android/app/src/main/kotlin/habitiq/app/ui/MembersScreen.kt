package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.ui.theme.FigmaColors

@Composable
fun MembersScreen(viewModel: FlatViewModel, onBack: () -> Unit) {
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val joinRequests by viewModel.joinRequests.collectAsStateWithLifecycleCompat()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycleCompat()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    val uid = currentUser?.uid.orEmpty()
    val pending = joinRequests.filter { it.status == "pending" }

    Column(Modifier.fillMaxSize().background(FigmaColors.Background)) {
        Row(Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Back") }
            Text("Members", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(start = 8.dp))
        }
        if (isAdmin && pending.isNotEmpty()) {
            Text("Join requests", modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.SemiBold)
            pending.forEach { req ->
                Card(Modifier.padding(horizontal = 16.dp, vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface)) {
                    Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(req.nickname, fontWeight = FontWeight.Bold)
                            Text(req.email, fontSize = 12.sp, color = FigmaColors.InkSecondary)
                        }
                        Row {
                            Button(onClick = { viewModel.approveJoinRequest(req) }) { Text("Approve") }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(onClick = { viewModel.rejectJoinRequest(req.id) }) { Text("Reject") }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(members, key = { it.uid }) { member ->
                MemberRow(member, isAdmin, member.uid != uid, onKick = { viewModel.kickMember(member.uid) })
            }
        }
    }
}

@Composable
private fun MemberRow(member: Member, isAdmin: Boolean, canKick: Boolean, onKick: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface)) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(member.nickname, fontWeight = FontWeight.Bold)
                Text(
                    "${member.role} · ${member.status} · reliability ${member.reliabilityScore}",
                    fontSize = 12.sp, color = FigmaColors.InkSecondary
                )
            }
            if (isAdmin && canKick && member.role != "admin") {
                TextButton(onClick = onKick) { Text("Remove", color = FigmaColors.Error) }
            }
        }
    }
}
