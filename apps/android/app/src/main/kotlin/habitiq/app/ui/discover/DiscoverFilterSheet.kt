package habitiq.app.ui.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import habitiq.app.discover.DiscoverFilterLogic
import habitiq.app.discover.DiscoverMode
import habitiq.app.discover.SeekerFilters
import habitiq.app.discover.VacancyFilters
import habitiq.app.ui.components.HqBottomSheet
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqChipFlow
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverFilterSheet(
    mode: DiscoverMode,
    vacancyFilters: VacancyFilters,
    seekerFilters: SeekerFilters,
    onVacancyFiltersChange: (VacancyFilters) -> Unit,
    onSeekerFiltersChange: (SeekerFilters) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit
) {
    HqBottomSheet(onDismiss = onDismiss, title = "Filters") {
        // HqBottomSheet's own content Column isn't scrollable; the filter form is long enough
        // (rent/budget, several dropdowns, a tag row) to need its own scroll, as the original did.
        Column(Modifier.verticalScroll(rememberScrollState())) {
            when (mode) {
                DiscoverMode.USE_A_FLAT -> VacancyFilterFields(vacancyFilters, onVacancyFiltersChange)
                DiscoverMode.FIND_A_PERSON -> SeekerFilterFields(seekerFilters, onSeekerFiltersChange)
            }

            Spacer(Modifier.height(HqSpacing.xl))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
                HqButton(text = "Reset", onClick = onClear, variant = HqButtonVariant.Secondary, modifier = Modifier.weight(1f))
                HqButton(
                    text = "Show results",
                    onClick = { onApply(); onDismiss() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun VacancyFilterFields(filters: VacancyFilters, onChange: (VacancyFilters) -> Unit) {
    HqTextField(
        value = filters.cityArea,
        onValueChange = { onChange(filters.copy(cityArea = it)) },
        label = "City or area",
        placeholder = "e.g. Kondapur, Hyderabad",
    )
    FilterGap()
    MoneyRange(
        label = "Monthly rent",
        min = filters.rentMin,
        max = filters.rentMax,
        onMin = { onChange(filters.copy(rentMin = it)) },
        onMax = { onChange(filters.copy(rentMax = it)) },
    )
    FilterGap()
    ChoiceGroup("Who can live here", DiscoverFilterLogic.vacancyGenderOptions, filters.genderPreference) { onChange(filters.copy(genderPreference = it)) }
    FilterGap()
    ChoiceGroup("Flat type", DiscoverFilterLogic.flatTypeOptions, filters.flatType) { onChange(filters.copy(flatType = it)) }
    FilterGap()
    ChoiceGroup("Room type", DiscoverFilterLogic.roomTypeOptions, filters.roomType) { onChange(filters.copy(roomType = it)) }
    FilterGap()
    ChoiceGroup(
        "Listed within",
        DiscoverFilterLogic.availabilityOptions.map { (k, v) -> (k?.toString() ?: "any") to v },
        filters.availabilityDays?.toString() ?: "any",
    ) { selected -> onChange(filters.copy(availabilityDays = if (selected == "any") null else selected.toIntOrNull())) }
    FilterGap()
    TagGroup("Lifestyle & amenities", filters.lifestyleTags) { onChange(filters.copy(lifestyleTags = it)) }
}

@Composable
private fun SeekerFilterFields(filters: SeekerFilters, onChange: (SeekerFilters) -> Unit) {
    HqTextField(
        value = filters.cityArea,
        onValueChange = { onChange(filters.copy(cityArea = it)) },
        label = "City or area",
        placeholder = "e.g. Gachibowli, Hyderabad",
    )
    FilterGap()
    MoneyRange(
        label = "Their budget",
        min = filters.budgetMin,
        max = filters.budgetMax,
        onMin = { onChange(filters.copy(budgetMin = it)) },
        onMax = { onChange(filters.copy(budgetMax = it)) },
    )
    FilterGap()
    ChoiceGroup("Gender", DiscoverFilterLogic.seekerGenderOptions, filters.gender) { onChange(filters.copy(gender = it)) }
    FilterGap()
    TagGroup("Lifestyle", filters.lifestyleTags) { onChange(filters.copy(lifestyleTags = it)) }
}

@Composable private fun FilterGap() = Spacer(Modifier.height(22.dp))

@Composable
private fun FilterLabel(text: String) {
    Text(text, style = HqType.labelLarge, color = LocalHqColors.current.textPrimary, modifier = Modifier.padding(bottom = 10.dp))
}

/** Min / max amount pair with a rupee prefix, number keypad and "Any" placeholders. */
@Composable
private fun MoneyRange(label: String, min: String, max: String, onMin: (String) -> Unit, onMax: (String) -> Unit) {
    Column {
        FilterLabel(label)
        Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            HqTextField(
                value = min,
                onValueChange = { onMin(it.filter(Char::isDigit)) },
                label = "Min (₹)",
                placeholder = "Any",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
            HqTextField(
                value = max,
                onValueChange = { onMax(it.filter(Char::isDigit)) },
                label = "Max (₹)",
                placeholder = "Any",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Single-choice chips: every option is visible and one tap away (replaces a dropdown). */
@Composable
private fun ChoiceGroup(label: String, options: List<Pair<String, String>>, value: String, onSelect: (String) -> Unit) {
    Column(Modifier.selectableGroup()) {
        FilterLabel(label)
        HqChipFlow {
            options.forEach { (key, text) ->
                HqChip(label = text, selected = key == value, onClick = { onSelect(key) })
            }
        }
    }
}

/** Multi-select tags that wrap onto new lines instead of hiding off-screen. */
@Composable
private fun TagGroup(label: String, selected: Set<String>, onChange: (Set<String>) -> Unit) {
    Column {
        FilterLabel(label)
        HqChipFlow {
            DiscoverFilterLogic.lifestyleTagOptions.forEach { tag ->
                val on = selected.contains(tag)
                HqChip(label = tag, selected = on, onClick = { onChange(if (on) selected - tag else selected + tag) })
            }
        }
    }
}
