package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import habitiq.app.data.VacancyData
import habitiq.app.flat.FlatViewModel
import habitiq.app.ui.figma.FigmaPrimaryButton
import habitiq.app.ui.theme.FigmaColors

@Composable
fun ManageFlatScreen(viewModel: FlatViewModel, onBack: () -> Unit) {
    val flatInfo by viewModel.flatInfo.collectAsStateWithLifecycleCompat()
    val vacancy = flatInfo?.vacancy
    var flatName by remember(flatInfo?.name) { mutableStateOf(flatInfo?.name.orEmpty()) }
    var joinMode by remember(flatInfo?.joinMode) { mutableStateOf(flatInfo?.joinMode ?: "auto") }
    var vacancyActive by remember(vacancy?.active) { mutableStateOf(vacancy?.active ?: false) }
    var city by remember(vacancy?.city) { mutableStateOf(vacancy?.city.orEmpty()) }
    var area by remember(vacancy?.area) { mutableStateOf(vacancy?.area.orEmpty()) }
    var rent by remember(vacancy?.rentPerHead) { mutableStateOf(vacancy?.rentPerHead?.toInt()?.toString() ?: "") }
    var beds by remember(vacancy?.bedsAvailable) { mutableStateOf(vacancy?.bedsAvailable?.toString() ?: "1") }
    var about by remember(vacancy?.about) { mutableStateOf(vacancy?.about.orEmpty()) }

    Column(Modifier.fillMaxSize().background(FigmaColors.Background).verticalScroll(rememberScrollState()).padding(20.dp)) {
        Row { TextButton(onClick = onBack) { Text("← Back") } }
        Text("Manage flat", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(flatName, { flatName = it }, Modifier.fillMaxWidth(), label = { Text("Flat name") })
        Spacer(Modifier.height(8.dp))
        Button(onClick = { viewModel.renameFlat(flatName) }, modifier = Modifier.fillMaxWidth()) { Text("Save name") }
        Spacer(Modifier.height(16.dp))
        Text("Join mode", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = joinMode == "auto", onClick = { joinMode = "auto"; viewModel.setJoinMode("auto") }, label = { Text("Auto join") })
            FilterChip(selected = joinMode == "approval", onClick = { joinMode = "approval"; viewModel.setJoinMode("approval") }, label = { Text("Approval required") })
        }
        Spacer(Modifier.height(24.dp))
        Text("Vacancy listing (Discover)", fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = vacancyActive, onCheckedChange = { vacancyActive = it })
            Text("List flat on Discover board")
        }
        OutlinedTextField(city, { city = it }, Modifier.fillMaxWidth(), label = { Text("City") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(area, { area = it }, Modifier.fillMaxWidth(), label = { Text("Area") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(rent, { rent = it }, Modifier.fillMaxWidth(), label = { Text("Rent per head (₹)") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(beds, { beds = it }, Modifier.fillMaxWidth(), label = { Text("Beds available") })
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(about, { about = it }, Modifier.fillMaxWidth(), label = { Text("About the room") })
        Spacer(Modifier.height(16.dp))
        FigmaPrimaryButton("Publish vacancy", onClick = {
            viewModel.updateVacancy(
                VacancyData(
                    active = vacancyActive,
                    city = city.trim(),
                    area = area.trim(),
                    rentPerHead = rent.toDoubleOrNull(),
                    bedsAvailable = beds.toIntOrNull() ?: 1,
                    about = about.trim()
                )
            )
        })
        flatInfo?.let {
            Spacer(Modifier.height(8.dp))
            Text("Invite code: ${it.id}", fontSize = 12.sp, color = FigmaColors.InkSecondary)
        }
    }
}
