package habitiq.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import habitiq.app.flats.HomeFlatStatus
import habitiq.app.flats.HomeViewModel

@Composable
fun HomeScreen(
    user: FirebaseUser?,
    homeViewModel: HomeViewModel,
    onOpenSettings: () -> Unit,
    onCreateFlat: () -> Unit,
    onJoinFlat: () -> Unit,
    onViewFlat: (flatId: String) -> Unit
) {
    val flatStatus by homeViewModel.flatStatus.collectAsStateWithLifecycleCompat()

    Column(modifier = Modifier.padding(24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Signed in as: ${user?.email ?: "unknown"}")
            IconButton(onClick = onOpenSettings) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")
            }
        }
        when (val status = flatStatus) {
            is HomeFlatStatus.Loading -> Text("Checking your flats…")
            is HomeFlatStatus.NoFlat -> {
                Text("What brings you here today?")
                Button(onClick = onCreateFlat) {
                    Text("Create a Flat")
                }
                Button(onClick = onJoinFlat) {
                    Text("Join with Code")
                }
            }
            is HomeFlatStatus.InFlat -> {
                Button(onClick = { onViewFlat(status.flatId) }) {
                    Text("View my flat")
                }
            }
            is HomeFlatStatus.Error -> {
                Text(status.message)
                Button(onClick = { homeViewModel.checkFlatStatus() }) {
                    Text("Retry")
                }
            }
        }
    }
}
