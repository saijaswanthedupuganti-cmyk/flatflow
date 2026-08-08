package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser
import habitiq.app.flats.HomeFlatStatus
import habitiq.app.flats.HomeViewModel
import habitiq.app.ui.theme.HabitiqBrand

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HabitiqBrand.Canvas)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Signed in as: ${user?.email ?: "unknown"}", color = HabitiqBrand.Ink)
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = HabitiqBrand.Ink
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        when (val status = flatStatus) {
            is HomeFlatStatus.Loading -> Text("Checking your flats…", color = HabitiqBrand.InkMute)
            is HomeFlatStatus.NoFlat -> {
                Text(
                    "What brings you here today?",
                    color = HabitiqBrand.Ink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onCreateFlat,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HabitiqBrand.Primary,
                        contentColor = HabitiqBrand.OnPrimary
                    )
                ) {
                    Text("Create a Flat")
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onJoinFlat,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HabitiqBrand.SecondaryFill,
                        contentColor = HabitiqBrand.SecondaryText
                    )
                ) {
                    Text("Join with Code")
                }
            }
            is HomeFlatStatus.InFlat -> {
                Button(
                    onClick = { onViewFlat(status.flatId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HabitiqBrand.Primary,
                        contentColor = HabitiqBrand.OnPrimary
                    )
                ) {
                    Text("View my flat")
                }
            }
            is HomeFlatStatus.Error -> {
                Text(status.message, color = HabitiqBrand.Error)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { homeViewModel.checkFlatStatus() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HabitiqBrand.Primary,
                        contentColor = HabitiqBrand.OnPrimary
                    )
                ) {
                    Text("Retry")
                }
            }
        }
    }
}
