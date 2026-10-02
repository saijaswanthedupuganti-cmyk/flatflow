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
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.theme.HqRadius
import habitiq.app.ui.theme.HqSize
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import habitiq.app.discover.DiscoverFilterLogic
import habitiq.app.discover.DiscoverMode
import habitiq.app.discover.SeekerFilters
import habitiq.app.discover.VacancyFilters
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqChipFlow
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors

/**
 * Full-height filter sheet (opens fully, never half-way): fixed header with title, active-count pill,
 * Reset and close; a scrolling body of evenly spaced sections divided by hairlines; and a fixed footer
 * whose button shows the live result count, so people see the effect before they apply.
 */
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
    onClear: () -> Unit,
    resultCount: Int? = null,
) {
    val c = LocalHqColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val active = if (mode == DiscoverMode.USE_A_FLAT) vacancyFilters.activeCount else seekerFilters.activeCount
    fun close(then: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { then() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.surfaceRaised,
        shape = RoundedCornerShape(topStart = HqRadius.sheet, topEnd = HqRadius.sheet),
        scrimColor = if (c.isDark) Color.Black.copy(alpha = 0.64f) else Color(0xFF091C1A).copy(alpha = 0.42f),
        dragHandle = {
            Box(Modifier.padding(top = 10.dp, bottom = 4.dp).size(width = 40.dp, height = 4.dp).clip(CircleShape).background(c.borderControl))
        },
    ) {
        Column(
            Modifier.fillMaxWidth().fillMaxHeight(0.94f).imePadding()
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
        ) {
            // Fixed header
            Row(
                Modifier.fillMaxWidth().padding(start = 22.dp, end = 10.dp, top = 6.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Filters", style = HqType.titleMedium2, color = c.textPrimary)
                if (active > 0) {
                    Box(
                        Modifier.clip(RoundedCornerShape(HqRadius.pill)).background(c.selectedBg).padding(horizontal = 9.dp, vertical = 2.dp),
                    ) { Text("$active", style = HqType.labelMedium, color = c.textBrand, fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.weight(1f))
                HqTextButton(text = "Reset", onClick = onClear, enabled = active > 0)
                IconButton(onClick = { close(onDismiss) }, modifier = Modifier.size(HqSize.target)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close filters", tint = c.iconDefault)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderSubtle))

            // Scrolling body
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 22.dp),
            ) {
                when (mode) {
                    DiscoverMode.USE_A_FLAT -> VacancyFilterFields(vacancyFilters, onVacancyFiltersChange)
                    DiscoverMode.FIND_A_PERSON -> SeekerFilterFields(seekerFilters, onSeekerFiltersChange)
                }
            }

            // Fixed footer with the live result count
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderSubtle))
            Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 22.dp, vertical = 14.dp)) {
                val noun = if (mode == DiscoverMode.USE_A_FLAT) "place" else "person"
                val nouns = if (mode == DiscoverMode.USE_A_FLAT) "places" else "people"
                val label = when (resultCount) {
                    null -> "Show results"
                    0 -> "No matches yet"
                    1 -> "Show 1 $noun"
                    else -> "Show $resultCount $nouns"
                }
                HqButton(text = label, onClick = { close { onApply(); onDismiss() } })
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

@Composable
private fun FilterGap() {
    Spacer(Modifier.height(20.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(LocalHqColors.current.borderSubtle))
    Spacer(Modifier.height(20.dp))
}

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
                imeAction = ImeAction.Done,
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
