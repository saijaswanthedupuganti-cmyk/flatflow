package habitiq.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import habitiq.app.flats.FlatHomeViewModel
import habitiq.app.flats.launchShareInviteCode

@Composable
fun FlatHomeScreen(viewModel: FlatHomeViewModel, currentUid: String) {
    val flat by viewModel.flat.collectAsStateWithLifecycleCompat()
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val context = LocalContext.current

    Column(modifier = Modifier.padding(24.dp)) {
        val currentFlat = flat
        if (currentFlat == null) {
            Text("Loading your flat…")
        } else {
            Text(currentFlat.name)
            Text("${currentFlat.memberCount} member(s)")
            if (currentFlat.adminUid == currentUid) {
                Text("Invite code: ${currentFlat.id}")
                Button(onClick = { launchShareInviteCode(context, currentFlat.name, currentFlat.id) }) {
                    Text("Share invite code")
                }
            }
            Text("Roommates:")
            members.forEach { member ->
                Text("${member.nickname} (${member.role})")
            }
        }
    }
}
