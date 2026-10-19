package habitiq.app.ui.discover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import habitiq.app.discover.DiscoverFilterLogic
import habitiq.app.discover.DiscoverMode
import habitiq.app.discover.SeekerFilters
import habitiq.app.discover.VacancyFilters
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqButtonPair
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqChipFlow
import habitiq.app.ui.components.HqIconButton
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqShapes
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.theme.rememberHqTransitions
import kotlinx.coroutines.launch

/**
 * Full-height filter sheet. Most-used questions first (budget, room, who, when), each with at most five
 * choices; the rest sits behind "More filters". Sticky footer: Reset + one primary "Show N flats" with the
 * live result count, in the thumb zone.
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
    fun close(then: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { then() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.surfaceRaised,
        shape = HqShapes.sheetTop,
        scrimColor = c.scrim.copy(alpha = 0.42f),
    ) {
        DiscoverFilterPanel(
            mode, vacancyFilters, seekerFilters, onVacancyFiltersChange, onSeekerFiltersChange, onClear, resultCount,
            onClose = { close(onDismiss) },
            onShow = { close { onApply(); onDismiss() } },
            modifier = Modifier.fillMaxHeight(0.94f),
        )
    }
}

/** The sheet's content: header, scrolling questions, sticky Reset + "Show N flats". */
@Composable
fun DiscoverFilterPanel(
    mode: DiscoverMode,
    vacancyFilters: VacancyFilters,
    seekerFilters: SeekerFilters,
    onVacancyFiltersChange: (VacancyFilters) -> Unit,
    onSeekerFiltersChange: (SeekerFilters) -> Unit,
    onClear: () -> Unit,
    resultCount: Int?,
    onClose: () -> Unit,
    onShow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalHqColors.current
    val focusManager = LocalFocusManager.current
        Column(
            modifier.fillMaxWidth().background(c.surfaceRaised).imePadding()
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
        ) {
            Row(
                Modifier.fillMaxWidth().padding(start = HqSpacing.screenHorizontal, end = HqSpacing.xs, bottom = HqSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Filters", style = HqType.titleMedium2, color = c.textPrimary, modifier = Modifier.weight(1f).semantics { heading() })
                HqIconButton(icon = Icons.Filled.Close, contentDescription = "Close filters", onClick = onClose)
            }
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(horizontal = HqSpacing.screenHorizontal).padding(top = HqSpacing.sm, bottom = HqSpacing.xxl),
            ) {
                when (mode) {
                    DiscoverMode.USE_A_FLAT -> VacancyFilterFields(vacancyFilters, onVacancyFiltersChange)
                    DiscoverMode.FIND_A_PERSON -> SeekerFilterFields(seekerFilters, onSeekerFiltersChange)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderSubtle))
            Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.md)) {
                val flats = mode == DiscoverMode.USE_A_FLAT
                val label = when (resultCount) {
                    null -> "Show results"
                    1 -> if (flats) "Show 1 flat" else "Show 1 person"
                    else -> if (flats) "Show $resultCount flats" else "Show $resultCount people"
                }
                HqButtonPair(
                    secondary = { HqButton(text = "Reset", onClick = onClear, variant = HqButtonVariant.Tertiary) },
                    primary = { HqButton(text = label, onClick = onShow) },
                )
            }
        }
}

@Composable
private fun VacancyFilterFields(filters: VacancyFilters, onChange: (VacancyFilters) -> Unit) {
    BudgetQuestion("Budget per person", filters.rentMin, filters.rentMax) { min, max -> onChange(filters.copy(rentMin = min, rentMax = max)) }
    QuestionGap()
    FormQuestion("Room") {
        ChoiceChips(DiscoverFilterLogic.roomFilterOptions, { it == filters.roomType }, { onChange(filters.copy(roomType = it)) })
    }
    QuestionGap()
    FormQuestion("For") {
        ChoiceChips(DiscoverFilterLogic.suitsFilterOptions, { it == filters.genderPreference }, { onChange(filters.copy(genderPreference = it)) })
    }
    QuestionGap()
    FormQuestion("Available") {
        ChoiceChips(DiscoverFilterLogic.availableOptions, { it == filters.available }, { onChange(filters.copy(available = it)) })
    }
    val moreCount = listOf(filters.furnishing != "any", filters.flatType != "any").count { it } + filters.lifestyleTags.size
    MoreFilters(moreCount) {
        FormQuestion("Furnishing") {
            ChoiceChips(DiscoverFilterLogic.furnishingFilterOptions, { it == filters.furnishing }, { onChange(filters.copy(furnishing = it)) })
        }
        QuestionGap()
        FormQuestion("Flat type") {
            ChoiceChips(DiscoverFilterLogic.flatTypeOptions, { it == filters.flatType }, { onChange(filters.copy(flatType = it)) })
        }
        QuestionGap()
        LifestyleQuestion(filters.lifestyleTags) { onChange(filters.copy(lifestyleTags = it)) }
    }
}

@Composable
private fun SeekerFilterFields(filters: SeekerFilters, onChange: (SeekerFilters) -> Unit) {
    BudgetQuestion("Their budget", filters.budgetMin, filters.budgetMax) { min, max -> onChange(filters.copy(budgetMin = min, budgetMax = max)) }
    QuestionGap()
    FormQuestion("Who") {
        ChoiceChips(DiscoverFilterLogic.suitsFilterOptions, { it == filters.gender }, { onChange(filters.copy(gender = it)) })
    }
    MoreFilters(filters.lifestyleTags.size) {
        LifestyleQuestion(filters.lifestyleTags) { onChange(filters.copy(lifestyleTags = it)) }
    }
}

/** Progressive disclosure for the less-used filters; opens by itself when one of them is already set. */
@Composable
private fun MoreFilters(activeInside: Int, content: @Composable () -> Unit) {
    var open by rememberSaveable { mutableStateOf(activeInside > 0) }
    val t = rememberHqTransitions()
    Spacer(Modifier.height(HqSpacing.md))
    HqTextButton(
        text = when {
            open -> "Fewer filters"
            activeInside > 0 -> "More filters ($activeInside)"
            else -> "More filters"
        },
        onClick = { open = !open },
    )
    AnimatedVisibility(open, enter = t.expandEnter, exit = t.collapseExit) {
        Column(Modifier.padding(top = HqSpacing.md)) { content() }
    }
}

/** One-tap ranges plus a custom min-max that accepts 8000, 8,000 or 8k. */
@Composable
private fun BudgetQuestion(label: String, min: String, max: String, onChange: (String, String) -> Unit) {
    FormQuestion(label) {
        ChoiceChips(
            DiscoverFilterLogic.budgetPresets.map { it.label to it.label },
            { key -> DiscoverFilterLogic.budgetPresets.first { it.label == key }.let { it.min == min && it.max == max } },
            { key ->
                val p = DiscoverFilterLogic.budgetPresets.first { it.label == key }
                if (p.min == min && p.max == max) onChange("", "") else onChange(p.min, p.max)
            },
        )
        Spacer(Modifier.height(HqSpacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
            HqTextField(
                value = min, onValueChange = { onChange(it.take(12), max) }, label = "Min",
                placeholder = "Any", prefix = "₹", keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f),
            )
            HqTextField(
                value = max, onValueChange = { onChange(min, it.take(12)) }, label = "Max",
                placeholder = "Any", prefix = "₹", keyboardType = KeyboardType.Number, imeAction = ImeAction.Done, modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LifestyleQuestion(selected: Set<String>, onChange: (Set<String>) -> Unit) {
    FormQuestion("Lifestyle") {
        HqChipFlow {
            DiscoverFilterLogic.lifestyleTagOptions.forEach { tag ->
                val on = tag in selected
                HqChip(label = tag, selected = on, onClick = { onChange(if (on) selected - tag else selected + tag) })
            }
        }
    }
}
