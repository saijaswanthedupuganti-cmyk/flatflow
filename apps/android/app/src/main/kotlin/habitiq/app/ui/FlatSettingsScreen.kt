package habitiq.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import habitiq.app.data.VacancyData
import habitiq.app.flat.FlatViewModel
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Flat admin settings: rename the flat, set join mode, configure the Discover vacancy listing.
 * Reached from Profile -- distinct from [ManageFlatHub], the bottom-nav "Manage" operational hub
 * (Tasks/Expenses/Bills/attention items). Renamed from ManageFlatScreen to stop the two being
 * confused with each other (master spec section 4/57: one name per concept).
 */
@Composable
fun FlatSettingsScreen(viewModel: FlatViewModel, onBack: () -> Unit) {
    val c = LocalHqColors.current
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
    var preferredGender by remember(vacancy?.preferredGender) { mutableStateOf(vacancy?.preferredGender ?: "any") }
    val genderOptions = listOf("any" to "Anyone", "male" to "Male", "female" to "Female", "women_only" to "Women only")

    Column(Modifier.fillMaxSize().background(c.canvas).verticalScroll(rememberScrollState()).padding(HqSpacing.xl)) {
        Row { TextButton(onClick = onBack) { Text("← Back", style = HqType.labelLarge, color = c.textBrand) } }
        Text("Manage flat", style = HqType.headlineMedium, color = c.textPrimary)
        Spacer(Modifier.height(HqSpacing.lg))
        HqTextField(
            value = flatName,
            onValueChange = { flatName = it },
            label = "Flat name",
            placeholder = "e.g. Sunrise Apartments 4B",
            imeAction = ImeAction.Done,
            onImeAction = if (flatName.isNotBlank()) ({ viewModel.renameFlat(flatName) }) else null
        )
        Spacer(Modifier.height(HqSpacing.sm))
        HqButton(text = "Save name", onClick = { viewModel.renameFlat(flatName) })
        Spacer(Modifier.height(HqSpacing.lg))
        Text("Join mode", style = HqType.titleSmall, color = c.textPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            HqChip(label = "Auto join", selected = joinMode == "auto", onClick = { joinMode = "auto"; viewModel.setJoinMode("auto") })
            HqChip(label = "Approval required", selected = joinMode == "approval", onClick = { joinMode = "approval"; viewModel.setJoinMode("approval") })
        }
        Spacer(Modifier.height(HqSpacing.xxl))
        Text("Vacancy listing (Discover)", style = HqType.titleSmall, color = c.textPrimary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = vacancyActive, onCheckedChange = { vacancyActive = it })
            Text("List flat on Discover board", style = HqType.bodyMedium, color = c.textPrimary)
        }
        HqTextField(value = city, onValueChange = { city = it }, label = "City", placeholder = "e.g. Hyderabad")
        Spacer(Modifier.height(HqSpacing.sm))
        HqTextField(value = area, onValueChange = { area = it }, label = "Area", placeholder = "e.g. Gachibowli")
        Spacer(Modifier.height(HqSpacing.sm))
        HqTextField(value = rent, onValueChange = { rent = it }, label = "Rent per head (₹)", placeholder = "e.g. 12000", keyboardType = KeyboardType.Number)
        Spacer(Modifier.height(HqSpacing.sm))
        HqTextField(value = beds, onValueChange = { beds = it }, label = "Beds available", placeholder = "e.g. 1", keyboardType = KeyboardType.Number)
        Spacer(Modifier.height(HqSpacing.sm))
        Text("Gender preference", style = HqType.labelLarge, color = c.textPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            genderOptions.forEach { (value, label) ->
                HqChip(label = label, selected = preferredGender == value, onClick = { preferredGender = value })
            }
        }
        Spacer(Modifier.height(HqSpacing.sm))
        HqTextField(value = about, onValueChange = { about = it }, label = "About the room", placeholder = "e.g. Sunny room, attached bath, 5 min to metro", singleLine = false, minLines = 3)
        Spacer(Modifier.height(HqSpacing.lg))
        HqButton(text = "Publish vacancy", onClick = {
            viewModel.updateVacancy(
                VacancyData(
                    active = vacancyActive,
                    city = city.trim(),
                    area = area.trim(),
                    rentPerHead = rent.toDoubleOrNull(),
                    bedsAvailable = beds.toIntOrNull() ?: 1,
                    preferredGender = preferredGender,
                    about = about.trim()
                )
            )
        })
        flatInfo?.let {
            Spacer(Modifier.height(HqSpacing.sm))
            Text("Invite code: ${it.id}", style = HqType.bodySmall, color = c.textSecondary)
        }
    }
}
