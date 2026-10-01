package habitiq.app.ui.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import habitiq.app.discover.DiscoverFilterLogic
import habitiq.app.discover.DiscoverMode
import habitiq.app.discover.SeekerFilters
import habitiq.app.discover.VacancyFilters
import habitiq.app.ui.components.HqBottomSheet
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqRadius
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
                HqButton(text = "Clear all", onClick = onClear, variant = HqButtonVariant.Secondary, modifier = Modifier.weight(1f))
                HqButton(
                    text = "Apply",
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
        label = "City or area"
    )
    Spacer(Modifier.height(HqSpacing.md))
    Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
        HqTextField(
            value = filters.rentMin,
            onValueChange = { onChange(filters.copy(rentMin = it.filter { c -> c.isDigit() })) },
            label = "Rent min (₹)",
            modifier = Modifier.weight(1f)
        )
        HqTextField(
            value = filters.rentMax,
            onValueChange = { onChange(filters.copy(rentMax = it.filter { c -> c.isDigit() })) },
            label = "Rent max (₹)",
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(HqSpacing.md))
    FilterDropdown(
        label = "Gender preference",
        value = filters.genderPreference,
        options = DiscoverFilterLogic.vacancyGenderOptions,
        onSelect = { onChange(filters.copy(genderPreference = it)) }
    )
    Spacer(Modifier.height(HqSpacing.sm))
    FilterDropdown(
        label = "Flat type",
        value = filters.flatType,
        options = DiscoverFilterLogic.flatTypeOptions,
        onSelect = { onChange(filters.copy(flatType = it)) }
    )
    Spacer(Modifier.height(HqSpacing.sm))
    FilterDropdown(
        label = "Room type",
        value = filters.roomType,
        options = DiscoverFilterLogic.roomTypeOptions,
        onSelect = { onChange(filters.copy(roomType = it)) }
    )
    Spacer(Modifier.height(HqSpacing.sm))
    FilterDropdown(
        label = "Listed within",
        value = filters.availabilityDays?.toString() ?: "any",
        options = DiscoverFilterLogic.availabilityOptions.map { (k, v) -> (k?.toString() ?: "any") to v },
        onSelect = { selected ->
            val days = if (selected == "any") null else selected.toIntOrNull()
            onChange(filters.copy(availabilityDays = days))
        }
    )
    Spacer(Modifier.height(HqSpacing.md))
    val c = LocalHqColors.current
    Text("Lifestyle & tags", style = HqType.labelLarge, color = c.textPrimary)
    Spacer(Modifier.height(HqSpacing.sm))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
        items(DiscoverFilterLogic.lifestyleTagOptions) { tag ->
            val selected = filters.lifestyleTags.contains(tag)
            HqChip(
                label = tag,
                selected = selected,
                onClick = {
                    val next = if (selected) filters.lifestyleTags - tag else filters.lifestyleTags + tag
                    onChange(filters.copy(lifestyleTags = next))
                }
            )
        }
    }
}

@Composable
private fun SeekerFilterFields(filters: SeekerFilters, onChange: (SeekerFilters) -> Unit) {
    HqTextField(
        value = filters.cityArea,
        onValueChange = { onChange(filters.copy(cityArea = it)) },
        label = "City or area"
    )
    Spacer(Modifier.height(HqSpacing.md))
    Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
        HqTextField(
            value = filters.budgetMin,
            onValueChange = { onChange(filters.copy(budgetMin = it.filter { c -> c.isDigit() })) },
            label = "Budget min (₹)",
            modifier = Modifier.weight(1f)
        )
        HqTextField(
            value = filters.budgetMax,
            onValueChange = { onChange(filters.copy(budgetMax = it.filter { c -> c.isDigit() })) },
            label = "Budget max (₹)",
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(HqSpacing.md))
    FilterDropdown(
        label = "Gender",
        value = filters.gender,
        options = DiscoverFilterLogic.seekerGenderOptions,
        onSelect = { onChange(filters.copy(gender = it)) }
    )
    Spacer(Modifier.height(HqSpacing.md))
    val c = LocalHqColors.current
    Text("Lifestyle tags", style = HqType.labelLarge, color = c.textPrimary)
    Spacer(Modifier.height(HqSpacing.sm))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
        items(DiscoverFilterLogic.lifestyleTagOptions) { tag ->
            val selected = filters.lifestyleTags.contains(tag)
            HqChip(
                label = tag,
                selected = selected,
                onClick = {
                    val next = if (selected) filters.lifestyleTags - tag else filters.lifestyleTags + tag
                    onChange(filters.copy(lifestyleTags = next))
                }
            )
        }
    }
}

/**
 * No Hq dropdown/select component exists yet, so this keeps the native
 * ExposedDropdownMenuBox + OutlinedTextField (HqTextField has no readOnly/trailing-icon dropdown
 * affordance) but pulls its colors and type from the design tokens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    label: String,
    value: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit
) {
    val c = LocalHqColors.current
    var expanded by remember { mutableStateOf(false) }
    val display = options.find { it.first == value }?.second ?: value

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, style = HqType.labelLarge) },
            textStyle = HqType.bodyLarge,
            shape = RoundedCornerShape(HqRadius.md),
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.borderFocus,
                unfocusedBorderColor = c.borderDefault,
                focusedTextColor = c.textPrimary,
                unfocusedTextColor = c.textPrimary,
                focusedLabelColor = c.brandPrimary,
                unfocusedLabelColor = c.textSecondary,
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (key, labelText) ->
                DropdownMenuItem(
                    text = { Text(labelText, style = HqType.bodyMedium, color = c.textPrimary) },
                    onClick = {
                        onSelect(key)
                        expanded = false
                    }
                )
            }
        }
    }
}
