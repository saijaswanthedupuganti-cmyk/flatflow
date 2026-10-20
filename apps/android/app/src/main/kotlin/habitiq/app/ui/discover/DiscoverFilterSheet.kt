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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
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
import habitiq.app.ui.components.HqInlineError
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqShapes
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.theme.rememberHqTransitions
import kotlinx.coroutines.launch

/** Keeps Find a flat filters across rotation and process death. */
val VacancyFiltersSaver: Saver<VacancyFilters, Any> = listSaver(
    save = {
        listOf(it.cityArea, it.rentMin, it.rentMax, it.genderPreference, it.flatType, it.roomType, ArrayList(it.lifestyleTags), it.furnishing, it.available)
    },
    restore = {
        VacancyFilters(
            cityArea = it[0] as String, rentMin = it[1] as String, rentMax = it[2] as String,
            genderPreference = it[3] as String, flatType = it[4] as String, roomType = it[5] as String,
            lifestyleTags = (it[6] as List<*>).filterIsInstance<String>().toSet(),
            furnishing = it[7] as String, available = it[8] as String,
        )
    },
)

/** Keeps Find a flatmate filters across rotation and process death. */
val SeekerFiltersSaver: Saver<SeekerFilters, Any> = listSaver(
    save = { listOf(it.cityArea, it.budgetMin, it.budgetMax, it.gender, ArrayList(it.lifestyleTags)) },
    restore = {
        SeekerFilters(
            cityArea = it[0] as String, budgetMin = it[1] as String, budgetMax = it[2] as String, gender = it[3] as String,
            lifestyleTags = (it[4] as List<*>).filterIsInstance<String>().toSet(),
        )
    },
)

/**
 * Full-height filter sheet. Most-used questions first (budget, room, who, when), each with at most five
 * choices; the rest sits behind "More filters". Changes go into a draft: "Show N" applies it, closing
 * the sheet any other way discards it, and Reset clears the draft. A budget whose minimum is above its
 * maximum shows the error under the fields when Show is pressed instead of applying.
 *
 * Kept on [ModalBottomSheet] rather than HqBottomSheet: this panel needs its own header with a close
 * button and a sticky footer outside the scrolling area, which HqBottomSheet's padded column can't hold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverFilterSheet(
    mode: DiscoverMode,
    vacancyFilters: VacancyFilters,
    seekerFilters: SeekerFilters,
    onApply: (VacancyFilters, SeekerFilters) -> Unit,
    onDismiss: () -> Unit,
    /** Results the draft would show, so the button can say "Show 4 flats" before applying. */
    countResults: ((VacancyFilters, SeekerFilters) -> Int)? = null,
) {
    val c = LocalHqColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var vacancyDraft by rememberSaveable(stateSaver = VacancyFiltersSaver) { mutableStateOf(vacancyFilters) }
    var seekerDraft by rememberSaveable(stateSaver = SeekerFiltersSaver) { mutableStateOf(seekerFilters) }
    var triedToApply by rememberSaveable { mutableStateOf(false) }
    val budgetError = when (mode) {
        DiscoverMode.USE_A_FLAT -> DiscoverFilterLogic.budgetError(vacancyDraft.rentMin, vacancyDraft.rentMax)
        DiscoverMode.FIND_A_PERSON -> DiscoverFilterLogic.budgetError(seekerDraft.budgetMin, seekerDraft.budgetMax)
    }
    val count = remember(vacancyDraft, seekerDraft, countResults) { countResults?.invoke(vacancyDraft, seekerDraft) }
    fun close(then: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { then() }
    }

    // The sheet applies the keyboard and navigation-bar insets itself (contentWindowInsets), so the
    // panel doesn't add them again.
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.surfaceRaised,
        shape = HqShapes.sheetTop,
        scrimColor = c.scrim.copy(alpha = 0.42f),
    ) {
        DiscoverFilterPanel(
            mode, vacancyDraft, seekerDraft,
            onVacancyFiltersChange = { vacancyDraft = it },
            onSeekerFiltersChange = { seekerDraft = it },
            onClear = {
                vacancyDraft = VacancyFilters(cityArea = vacancyDraft.cityArea)
                seekerDraft = SeekerFilters(cityArea = seekerDraft.cityArea)
                triedToApply = false
            },
            resultCount = count,
            onClose = { close(onDismiss) },
            onShow = {
                if (budgetError != null) triedToApply = true
                else close { onApply(vacancyDraft, seekerDraft) }
            },
            modifier = Modifier.fillMaxHeight(0.94f),
            budgetError = budgetError.takeIf { triedToApply },
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
    /** Shown under the budget fields after Show was pressed with an unusable range. */
    budgetError: String? = null,
) {
    val c = LocalHqColors.current
    val focusManager = LocalFocusManager.current
    Column(
        modifier.fillMaxWidth().background(c.surfaceRaised)
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
                DiscoverMode.USE_A_FLAT -> VacancyFilterFields(vacancyFilters, onVacancyFiltersChange, budgetError)
                DiscoverMode.FIND_A_PERSON -> SeekerFilterFields(seekerFilters, onSeekerFiltersChange, budgetError)
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.borderSubtle))
        Box(Modifier.fillMaxWidth().padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.md)) {
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
private fun VacancyFilterFields(filters: VacancyFilters, onChange: (VacancyFilters) -> Unit, budgetError: String?) {
    BudgetQuestion("Budget per person", filters.rentMin, filters.rentMax, budgetError) { min, max -> onChange(filters.copy(rentMin = min, rentMax = max)) }
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
        LifestyleQuestion(DiscoverFilterLogic.vacancyLifestyleFilterOptions, filters.lifestyleTags) { onChange(filters.copy(lifestyleTags = it)) }
    }
}

@Composable
private fun SeekerFilterFields(filters: SeekerFilters, onChange: (SeekerFilters) -> Unit, budgetError: String?) {
    BudgetQuestion("Their budget", filters.budgetMin, filters.budgetMax, budgetError) { min, max -> onChange(filters.copy(budgetMin = min, budgetMax = max)) }
    QuestionGap()
    FormQuestion("Who") {
        ChoiceChips(DiscoverFilterLogic.suitsFilterOptions, { it == filters.gender }, { onChange(filters.copy(gender = it)) })
    }
    MoreFilters(filters.lifestyleTags.size) {
        LifestyleQuestion(DiscoverFilterLogic.lifestyleTagOptions, filters.lifestyleTags) { onChange(filters.copy(lifestyleTags = it)) }
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
private fun BudgetQuestion(label: String, min: String, max: String, error: String?, onChange: (String, String) -> Unit) {
    val t = rememberHqTransitions()
    FormQuestion(label) {
        ChoiceChips(
            DiscoverFilterLogic.budgetPresets.map { it.label to it.label },
            { key -> DiscoverFilterLogic.presetMatches(DiscoverFilterLogic.budgetPresets.first { it.label == key }, min, max) },
            { key ->
                val p = DiscoverFilterLogic.budgetPresets.first { it.label == key }
                if (DiscoverFilterLogic.presetMatches(p, min, max)) onChange("", "") else onChange(p.min, p.max)
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
        // Keeps the last message on screen while it collapses away.
        var lastError by remember { mutableStateOf("") }
        if (error != null) lastError = error
        AnimatedVisibility(error != null, enter = t.expandEnter, exit = t.collapseExit) {
            HqInlineError(lastError, Modifier.padding(top = HqSpacing.sm))
        }
    }
}

@Composable
private fun LifestyleQuestion(options: List<String>, selected: Set<String>, onChange: (Set<String>) -> Unit) {
    FormQuestion("Lifestyle") {
        HqChipFlow {
            // A tag picked before it left the list (e.g. "AC") stays visible so it can be removed.
            (options + selected.filter { it !in options }).forEach { tag ->
                val on = tag in selected
                HqChip(label = tag, selected = on, onClick = { onChange(if (on) selected - tag else selected + tag) })
            }
        }
    }
}
