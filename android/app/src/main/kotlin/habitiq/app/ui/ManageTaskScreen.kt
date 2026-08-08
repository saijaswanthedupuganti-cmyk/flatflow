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
import habitiq.app.data.VacancyListing
import habitiq.app.flat.FlatViewModel
import habitiq.app.ui.theme.FigmaColors

@Composable
fun ManageTaskScreen(viewModel: FlatViewModel, initialTab: String = "Chores") {
    var tab by remember { mutableStateOf(initialTab) }
    Column(Modifier.fillMaxSize().background(FigmaColors.Background)) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Chores", "Money").forEach { label ->
                FilterChip(
                    selected = tab == label,
                    onClick = { tab = label },
                    label = { Text(label) }
                )
            }
        }
        when (tab) {
            "Chores" -> TasksScreen(viewModel)
            else -> ExpensesScreen(viewModel)
        }
    }
}

@Composable
fun FlatBoardScreen(viewModel: FlatViewModel) {
    val vacancies by viewModel.vacancies.collectAsStateWithLifecycleCompat()
  var cityFilter by remember { mutableStateOf("") }

    val filtered = remember(vacancies, cityFilter) {
        vacancies.filter {
            cityFilter.isBlank() || it.city.contains(cityFilter, ignoreCase = true) || it.area.contains(cityFilter, ignoreCase = true)
        }
    }

    Column(Modifier.fillMaxSize().background(FigmaColors.Background)) {
        Text("Discover flats", modifier = Modifier.padding(20.dp), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = FigmaColors.Ink)
        OutlinedTextField(
            value = cityFilter,
            onValueChange = { cityFilter = it },
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
            label = { Text("Filter by city or area") },
            singleLine = true
        )
        if (filtered.isEmpty()) {
            Text(
                "No active vacancy listings yet. Flats with an open room appear here when admins publish on the web app.",
                modifier = Modifier.padding(16.dp),
                color = FigmaColors.InkSecondary,
                fontSize = 14.sp
            )
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered, key = { it.flatId }) { listing -> VacancyCard(listing) }
        }
    }
}

@Composable
private fun VacancyCard(listing: VacancyListing) {
    Card(colors = CardDefaults.cardColors(containerColor = FigmaColors.Surface)) {
        Column(Modifier.padding(12.dp)) {
            Text(listing.flatName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("${listing.area}, ${listing.city}", fontSize = 13.sp, color = FigmaColors.InkSecondary)
            listing.rentPerHead?.let { Text("₹${it.toInt()}/head", fontSize = 13.sp) }
            if (listing.about.isNotBlank()) Text(listing.about, fontSize = 12.sp, color = FigmaColors.InkMuted)
            Text("${listing.bedsAvailable} bed(s) · ${listing.preferredGender}", fontSize = 11.sp, color = FigmaColors.InkMuted)
        }
    }
}
