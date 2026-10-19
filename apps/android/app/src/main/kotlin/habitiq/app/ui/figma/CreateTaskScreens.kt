package habitiq.app.ui.figma

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import habitiq.app.flat.FlatViewModel
import habitiq.app.flats.Member
import habitiq.app.ui.collectAsStateWithLifecycleCompat
import habitiq.app.ui.components.HqBackAppBar
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqChipFlow
import habitiq.app.ui.components.HqDateField
import habitiq.app.ui.components.HqDiscardChangesDialog
import habitiq.app.ui.components.HqInlineError
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import kotlinx.coroutines.launch
import java.time.LocalDate

/** How often a task comes back. [frequency] is the stored value the task write already uses. */
enum class TaskRepeat(val label: String, val frequency: String) {
    Once("Once", "one_time"),
    Daily("Every day", "daily"),
    Weekly("Every week", "weekly"),
    Fortnightly("Every 2 weeks", "fortnightly"),
    Monthly("Every month", "monthly"),
}

/** Everything the Add task form collected, ready for [submitAddTask]. */
data class AddTaskDraft(
    val name: String,
    val repeat: TaskRepeat,
    /** Once: the one person who does it. Repeats: everyone who takes turns, in order. */
    val memberUids: List<String>,
    /** ISO date: the due date (Once) or the first turn (repeats). */
    val date: String,
    val notes: String,
)

/** Priority is no longer asked for; every task gets the default the old forms started on. */
private const val DefaultPriority = "medium"

/**
 * Maps the one form onto the two existing writes: Once is the one-time task for a single person,
 * anything that repeats is the takes-turns (rotating) task with its frequency.
 */
fun submitAddTask(viewModel: FlatViewModel, draft: AddTaskDraft, onDone: (Boolean) -> Unit) {
    if (draft.repeat == TaskRepeat.Once) {
        viewModel.createTask(
            draft.name, "one_time", DefaultPriority, draft.memberUids,
            dueDate = draft.date, notes = draft.notes, onDone = onDone,
        )
    } else {
        viewModel.createTask(
            draft.name, draft.repeat.frequency, DefaultPriority, draft.memberUids, "rotating_duty",
            dueDate = draft.date, notes = draft.notes, onDone = onDone,
        )
    }
}

/** The single "Add task" screen behind every entry point. [onAdded] runs only after the write succeeded. */
@Composable
fun AddTaskScreen(viewModel: FlatViewModel, onBack: () -> Unit, onAdded: () -> Unit) {
    val members by viewModel.members.collectAsStateWithLifecycleCompat()
    val user by viewModel.currentUser.collectAsStateWithLifecycleCompat()
    AddTaskForm(
        members = members,
        currentUid = user?.uid.orEmpty(),
        onBack = onBack,
        onSubmit = { draft, done -> submitAddTask(viewModel, draft, done) },
        onAdded = onAdded,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddTaskForm(
    members: List<Member>,
    currentUid: String,
    onBack: () -> Unit,
    onSubmit: (AddTaskDraft, (Boolean) -> Unit) -> Unit,
    onAdded: () -> Unit,
    autoFocus: Boolean = true,
) {
    val c = LocalHqColors.current
    val scope = rememberCoroutineScope()
    var name by rememberSaveable { mutableStateOf("") }
    var repeat by rememberSaveable { mutableStateOf(TaskRepeat.Weekly) }
    // Takes turns: everyone by default. Null until the person changes it, so late-loading members are included.
    var turns by rememberSaveable { mutableStateOf<List<String>?>(null) }
    var doer by rememberSaveable { mutableStateOf<String?>(null) }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var notes by rememberSaveable { mutableStateOf("") }
    var showNotes by rememberSaveable { mutableStateOf(false) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var peopleError by remember { mutableStateOf<String?>(null) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }

    val nameFocus = remember { FocusRequester() }
    val peopleBring = remember { BringIntoViewRequester() }
    val once = repeat == TaskRepeat.Once
    val selectedTurns = turns ?: members.map { it.uid }
    val selectedDoer = doer ?: currentUid.takeIf { uid -> members.isEmpty() || members.any { it.uid == uid } }
        ?: members.firstOrNull()?.uid
    val dirty = name.isNotBlank() || notes.isNotBlank()

    LaunchedEffect(Unit) { if (autoFocus) runCatching { nameFocus.requestFocus() } }

    fun close() { if (dirty && !saving) confirmDiscard = true else onBack() }
    BackHandler(enabled = dirty || saving) { if (!saving) confirmDiscard = true }

    fun submit() {
        if (saving) return
        val trimmed = name.trim()
        val people = if (once) listOfNotNull(selectedDoer) else members.map { it.uid }.filter { it in selectedTurns }
        nameError = if (trimmed.isEmpty()) "Enter what needs doing, like \"Take out the bins\"." else null
        peopleError = when {
            people.isNotEmpty() -> null
            once -> "Choose who does it."
            else -> "Choose at least one person to take turns."
        }
        when {
            nameError != null -> { runCatching { nameFocus.requestFocus() }; return }
            peopleError != null -> { scope.launch { peopleBring.bringIntoView() }; return }
        }
        saving = true
        saveError = null
        onSubmit(AddTaskDraft(trimmed, repeat, people, date.ifBlank { LocalDate.now().toString() }, notes.trim())) { ok ->
            saving = false
            if (ok) onAdded() else saveError = "Unable to add the task. Check your connection and try again."
        }
    }

    FigmaScreenBackground {
        HqBackAppBar(title = "Add task", onBack = { close() })
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = HqSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.group),
        ) {
            HqTextField(
                value = name,
                onValueChange = { name = it; if (nameError != null && it.isNotBlank()) nameError = null },
                label = "What needs doing?",
                placeholder = "e.g. Take out the bins",
                leadingIcon = Icons.Filled.TaskAlt,
                errorText = nameError,
                imeAction = ImeAction.Next,
                modifier = Modifier.focusRequester(nameFocus),
            )

            FieldGroup("Repeats") {
                HqChipFlow {
                    TaskRepeat.entries.forEach { r ->
                        HqChip(label = r.label, selected = repeat == r, onClick = { repeat = r; peopleError = null })
                    }
                }
            }

            FieldGroup(if (once) "Who does it" else "Who takes turns", Modifier.bringIntoViewRequester(peopleBring)) {
                if (!once) {
                    Text("Turns go in this order, one person each time.", style = HqType.bodyMedium, color = c.textSecondary)
                }
                HqChipFlow {
                    members.forEach { m ->
                        val label = if (m.uid == currentUid) "You" else m.nickname.substringBefore(' ').ifBlank { "Flatmate" }
                        if (once) {
                            HqChip(label = label, selected = selectedDoer == m.uid, onClick = { doer = m.uid; peopleError = null })
                        } else {
                            val on = m.uid in selectedTurns
                            HqChip(label = label, selected = on, onClick = {
                                turns = if (on) selectedTurns - m.uid else selectedTurns + m.uid
                                peopleError = null
                            })
                        }
                    }
                }
                peopleError?.let { HqInlineError(it) }
            }

            HqDateField(
                value = date,
                onValueChange = { date = it },
                label = if (once) "Due" else "Starts",
                placeholder = "Today",
                todayLabel = "Today",
                clearable = false,
            )

            if (showNotes || notes.isNotBlank()) {
                HqTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes (optional)",
                    placeholder = "e.g. Include the bathroom",
                    leadingIcon = Icons.Filled.EditNote,
                    singleLine = false,
                    minLines = 2,
                )
            } else {
                HqTextButton(text = "Add notes (optional)", onClick = { showNotes = true })
            }
            Spacer(Modifier.height(HqSpacing.lg))
        }
        Column(
            Modifier.fillMaxWidth().background(c.canvas).navigationBarsPadding().imePadding()
                .padding(horizontal = HqSpacing.screenHorizontal, vertical = HqSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(HqSpacing.sm),
        ) {
            saveError?.let { HqInlineError(it) }
            HqButton(text = "Add task", loading = saving, onClick = { submit() })
        }
    }

    if (confirmDiscard) {
        HqDiscardChangesDialog(onKeepEditing = { confirmDiscard = false }, onDiscard = { confirmDiscard = false; onBack() })
    }
}

/** A short label directly above the controls it names (chunking + proximity). */
@Composable
private fun FieldGroup(label: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val c = LocalHqColors.current
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
        Text(label, style = HqType.labelMedium, color = c.textPrimary, modifier = Modifier.semantics { heading() })
        content()
    }
}
