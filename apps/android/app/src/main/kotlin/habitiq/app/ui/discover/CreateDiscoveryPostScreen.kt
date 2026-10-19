package habitiq.app.ui.discover

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import habitiq.app.data.SeekerProfile
import habitiq.app.data.VacancyData
import habitiq.app.data.VacancyListing
import habitiq.app.discover.DiscoverFilterLogic
import habitiq.app.discover.DiscoverFlags
import habitiq.app.discover.DiscoveryDraftStore
import habitiq.app.discover.DiscoveryPostType
import habitiq.app.discover.ListingFacts
import habitiq.app.discover.LookingDraft
import habitiq.app.discover.MoneyInput
import habitiq.app.discover.TrustCopy
import habitiq.app.discover.VacancyDraft
import habitiq.app.flats.FlatInfo
import habitiq.app.ui.components.HqAppBar
import habitiq.app.ui.components.HqDateField
import habitiq.app.ui.components.HqDiscardChangesDialog
import habitiq.app.ui.components.HqEmptyState
import habitiq.app.ui.components.HqGroup
import habitiq.app.ui.components.HqIconButton
import habitiq.app.ui.components.HqIconButtonStyle
import habitiq.app.ui.components.HqIcons
import habitiq.app.ui.components.HqInlineError
import habitiq.app.ui.components.HqMenuRow
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.components.HqTileTone
import habitiq.app.ui.theme.HqMotion
import habitiq.app.ui.theme.HqShapes
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import habitiq.app.ui.theme.hqPressable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Posting to Discover. When the type is already known (the + menu's "List a room" / "I'm looking for a
 * room", My posts, Flat settings) the wizard opens directly; "What do you want to post?" only appears
 * when the screen is opened without a type.
 */
@Composable
fun CreateDiscoveryPostScreen(
    isAdmin: Boolean,
    flat: FlatInfo?,
    existingVacancy: VacancyData?,
    onPublishVacancy: (VacancyData, List<Uri>) -> Unit,
    onPublishLooking: (city: String, lookingIn: String, budget: Double, bio: String, gender: String, tags: String) -> Unit,
    onBack: () -> Unit,
    publishing: Boolean = false,
    uploadProgress: Float? = null,
    publishError: String? = null,
    onClearError: () -> Unit = {},
    /** "VACANCY" or "LOOKING" to skip the type choice when the person already picked one (e.g. from the + menu). */
    initialType: String? = null,
    /** Opens the wizard on this step (screenshots, deep links). */
    initialStep: Int = 0,
) {
    val context = LocalContext.current
    val draftStore = remember { DiscoveryDraftStore(context) }
    var typeName by rememberSaveable { mutableStateOf(initialType) }
    val type = typeName?.let { runCatching { DiscoveryPostType.valueOf(it) }.getOrNull() }
    // Leaving a wizard returns to the type choice only if the person came through it.
    val exitWizard = { if (initialType == null) typeName = null else onBack() }

    when (type) {
        null -> PostTypeSelection(
            lookingEnabled = DiscoverFlags.LOOKING_POSTS,
            onVacancy = { typeName = DiscoveryPostType.VACANCY.name },
            onLooking = { typeName = DiscoveryPostType.LOOKING.name },
            onBack = onBack,
        )
        DiscoveryPostType.VACANCY -> VacancyWizard(
            isAdmin = isAdmin,
            flat = flat,
            existing = existingVacancy,
            draftStore = draftStore,
            onPublish = onPublishVacancy,
            onExit = exitWizard,
            publishing = publishing,
            uploadProgress = uploadProgress,
            publishError = publishError,
            onClearError = onClearError,
            initialStep = initialStep,
        )
        DiscoveryPostType.LOOKING -> LookingWizard(
            draftStore = draftStore,
            onPublish = onPublishLooking,
            onExit = exitWizard,
            publishing = publishing,
            publishError = publishError,
            onClearError = onClearError,
            initialStep = initialStep,
        )
    }
}

@Composable
private fun PostTypeSelection(lookingEnabled: Boolean, onVacancy: () -> Unit, onLooking: () -> Unit, onBack: () -> Unit) {
    val c = LocalHqColors.current
    Column(Modifier.fillMaxSize().background(c.canvas)) {
        HqAppBar(title = "What do you want to post?", onBack = onBack)
        Column(Modifier.padding(horizontal = HqSpacing.screenHorizontal), verticalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
            Text("Your post shows up in Discover.", style = HqType.bodyMedium, color = c.textSecondary)
            HqGroup {
                HqMenuRow(
                    title = "List a room",
                    support = "Find a flatmate for a room in your flat",
                    icon = HqIcons.Home,
                    tone = HqTileTone.Teal,
                    lastRow = !lookingEnabled,
                    onClick = onVacancy,
                )
                if (lookingEnabled) {
                    HqMenuRow(
                        title = "I'm looking for a room",
                        support = "Tell flats what you need",
                        icon = HqIcons.Search,
                        tone = HqTileTone.Neutral,
                        lastRow = true,
                        onClick = onLooking,
                    )
                }
            }
        }
    }
}

private const val VACANCY_STEPS = 4
private val vacancyStepTitles = listOf("The room", "Rent", "Who it suits", "Photos and details")
private const val ABOUT_MAX = 800

@Composable
private fun VacancyWizard(
    isAdmin: Boolean,
    flat: FlatInfo?,
    existing: VacancyData?,
    draftStore: DiscoveryDraftStore,
    onPublish: (VacancyData, List<Uri>) -> Unit,
    onExit: () -> Unit,
    publishing: Boolean,
    uploadProgress: Float?,
    publishError: String?,
    onClearError: () -> Unit,
    initialStep: Int,
) {
    val c = LocalHqColors.current
    if (flat == null) {
        BackHandler { onExit() }
        Column(Modifier.fillMaxSize().background(c.canvas)) {
            HqAppBar(title = "List a room", onBack = onExit)
            HqEmptyState(
                icon = HqIcons.Home,
                title = "Join a flat to list a room",
                message = "You can list a room in the flat you live in.",
                primaryLabel = "Go back",
                onPrimaryClick = onExit,
            )
        }
        return
    }

    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now().toString() }
    var step by rememberSaveable { mutableIntStateOf(initialStep.coerceIn(0, VACANCY_STEPS - 1)) }
    var city by rememberSaveable { mutableStateOf(existing?.city.orEmpty()) }
    var area by rememberSaveable { mutableStateOf(existing?.area.orEmpty()) }
    var rent by rememberSaveable { mutableStateOf(existing?.rentPerHead?.toLong()?.toString().orEmpty()) }
    var beds by rememberSaveable { mutableStateOf((existing?.bedsAvailable ?: 1).toString()) }
    var gender by rememberSaveable { mutableStateOf(existing?.preferredGender ?: "any") }
    var about by rememberSaveable { mutableStateOf(existing?.about.orEmpty()) }
    var tags by rememberSaveable { mutableStateOf(existing?.lifestyle.orEmpty()) }
    var roomType by rememberSaveable { mutableStateOf(existing?.roomType ?: "private") }
    var deposit by rememberSaveable { mutableStateOf(existing?.securityDeposit?.toLong()?.toString().orEmpty()) }
    // Smart defaults (Tesler): free from today, one month's notice, open to anyone.
    var availableFrom by rememberSaveable { mutableStateOf(existing?.availableFrom?.ifBlank { null } ?: today) }
    var noticePeriod by rememberSaveable { mutableStateOf(existing?.noticePeriod ?: "1_month") }
    var furnishing by rememberSaveable { mutableStateOf(existing?.furnishing ?: "furnished") }
    var amenities by rememberSaveable { mutableStateOf(existing?.amenities.orEmpty()) }
    var occupation by rememberSaveable { mutableStateOf(existing?.preferredOccupation ?: "any") }
    // Not asked any more (the rent already says it); kept so editing never drops a stored value.
    val budgetMin by rememberSaveable { mutableStateOf(existing?.preferredBudgetMin?.toLong()?.toString().orEmpty()) }
    val budgetMax by rememberSaveable { mutableStateOf(existing?.preferredBudgetMax?.toLong()?.toString().orEmpty()) }
    var selectedPhotoStrings by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var coverPhotoString by rememberSaveable { mutableStateOf<String?>(null) }
    var errorsOnStep by rememberSaveable { mutableIntStateOf(-1) }
    var draftLoaded by rememberSaveable { mutableStateOf(false) }
    var baseline by rememberSaveable { mutableStateOf<String?>(null) }
    var showDiscard by remember { mutableStateOf(false) }
    var focusTarget by remember { mutableStateOf<FocusRequester?>(null) }
    val rentFocus = remember { FocusRequester() }
    val depositFocus = remember { FocusRequester() }
    val areaFocus = remember { FocusRequester() }
    val cityFocus = remember { FocusRequester() }

    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        }
        val combined = (selectedPhotoStrings + uris.map(Uri::toString)).distinct().take(8)
        selectedPhotoStrings = combined
        if (coverPhotoString == null) coverPhotoString = combined.firstOrNull()
        onClearError()
    }

    LaunchedEffect(flat.id) {
        if (!draftLoaded && existing == null) {
            draftStore.getVacancyDraft(flat.id).firstOrNull()?.let { d ->
                step = d.step.coerceIn(0, VACANCY_STEPS - 1)
                city = d.city; area = d.area; rent = d.rent; beds = d.beds; gender = d.gender; about = d.about
                tags = d.tags; roomType = d.roomType; deposit = d.deposit
                availableFrom = d.availableFrom.ifBlank { today }
                noticePeriod = d.noticePeriod; furnishing = d.furnishing; amenities = d.amenities; occupation = d.occupation
                selectedPhotoStrings = d.selectedPhotoUris; coverPhotoString = d.coverPhotoUri
            }
        }
        draftLoaded = true
    }

    val snapshot = listOf(city, area, rent, beds, gender, about, tags, roomType, deposit, availableFrom, noticePeriod, furnishing, amenities, occupation, selectedPhotoStrings, coverPhotoString).joinToString("|")
    LaunchedEffect(draftLoaded) { if (draftLoaded && baseline == null) baseline = snapshot }
    val dirty = baseline != null && snapshot != baseline

    LaunchedEffect(step, snapshot) {
        if (draftLoaded && dirty) {
            draftStore.saveVacancyDraft(
                flat.id,
                VacancyDraft(
                    step = step, city = city, area = area, rent = rent, beds = beds, gender = gender, about = about,
                    tags = tags, roomType = roomType, deposit = deposit, availableFrom = availableFrom,
                    noticePeriod = noticePeriod, furnishing = furnishing, amenities = amenities, occupation = occupation,
                    budgetMin = budgetMin, budgetMax = budgetMax, moveInTiming = ListingFacts.moveInTimingFor(availableFrom),
                    selectedPhotoUris = selectedPhotoStrings, coverPhotoUri = coverPhotoString,
                ),
            )
        }
    }
    LaunchedEffect(focusTarget) {
        val target = focusTarget ?: return@LaunchedEffect
        delay(HqMotion.slow.toLong()) // let the step slide in before moving focus into it
        runCatching { target.requestFocus() }
        focusTarget = null
    }

    val rentValue = MoneyInput.parse(rent)
    val depositValue = MoneyInput.parse(deposit)
    val rentError = if (rentValue == null || rentValue <= 0) "Enter the monthly rent, like 8000 or 8k." else null
    val depositError = if (deposit.isNotBlank() && depositValue == null) "Enter the deposit as a number, or leave it empty." else null
    val areaError = if (area.isBlank()) "Enter the area, like Gachibowli." else null
    val cityError = if (city.isBlank()) "Enter the city." else null

    /** First problem on a step as (step, field), or null when the step is complete. */
    fun firstError(s: Int): FocusRequester? = when (s) {
        1 -> when { rentError != null -> rentFocus; depositError != null -> depositFocus; else -> null }
        3 -> when { areaError != null -> areaFocus; cityError != null -> cityFocus; else -> null }
        else -> null
    }

    val selectedPhotos = selectedPhotoStrings.map(Uri::parse)
    val coverPhoto = coverPhotoString?.let(Uri::parse)
    fun publish() {
        // Check every step; send the person to the first one that needs fixing.
        val badStep = (0 until VACANCY_STEPS).firstOrNull { firstError(it) != null }
        if (badStep != null) {
            step = badStep
            errorsOnStep = badStep
            focusTarget = firstError(badStep)
            return
        }
        onPublish(
            VacancyData(
                active = true,
                city = city.trim(),
                area = area.trim(),
                rentPerHead = rentValue?.toDouble(),
                bedsAvailable = beds.toIntOrNull() ?: 1,
                preferredGender = gender,
                about = about.trim(),
                lifestyle = tags,
                customTags = smartTags(flat, beds, area, roomType, furnishing, tags, amenities, gender, occupation, ListingFacts.moveInTimingFor(availableFrom)),
                roomType = roomType,
                securityDeposit = depositValue?.toDouble(),
                availableFrom = availableFrom.trim(),
                noticePeriod = noticePeriod,
                furnishing = furnishing,
                amenities = amenities,
                preferredOccupation = occupation,
                preferredBudgetMin = MoneyInput.parse(budgetMin)?.toDouble(),
                preferredBudgetMax = MoneyInput.parse(budgetMax)?.toDouble(),
                moveInTiming = ListingFacts.moveInTimingFor(availableFrom),
                approximateLocationOnly = true,
                photoUrls = existing?.photoUrls.orEmpty(),
                existingMembersGender = existing?.existingMembersGender,
                postStatus = "PUBLISHED",
            ),
            selectedPhotos.sortedBy { if (it == coverPhoto) 0 else 1 },
        )
        scope.launch { draftStore.clearVacancyDraft(flat.id) }
    }
    fun next() {
        val problem = firstError(step)
        if (problem != null) {
            errorsOnStep = step
            focusTarget = problem
            return
        }
        errorsOnStep = -1
        if (rent.isNotBlank()) rent = MoneyInput.normalise(rent)
        if (deposit.isNotBlank()) deposit = MoneyInput.normalise(deposit)
        if (step < VACANCY_STEPS - 1) step += 1 else publish()
    }
    fun back() {
        when {
            step > 0 -> { step -= 1; errorsOnStep = -1 }
            dirty -> showDiscard = true
            else -> onExit()
        }
    }
    // System back = previous step, like the arrow (Jakob); on the first step it leaves (asking first if there is input).
    BackHandler(enabled = !showDiscard) { back() }

    val last = step == VACANCY_STEPS - 1
    DiscoverWizardScaffold(
        title = "List a room",
        step = step,
        stepCount = VACANCY_STEPS,
        stepTitle = { vacancyStepTitles[it] },
        stepSubtitle = {
            when (it) {
                0 -> "Defaults are set. Change what's different."
                1 -> "Per person, per month."
                2 -> "Helps the right people find your room."
                else -> "Add photos and a short note, then check the preview."
            }
        },
        onBack = { back() },
        primaryLabel = when {
            !last -> "Continue"
            !isAdmin -> "Send for approval"
            existing != null -> "Update listing"
            else -> "Post room"
        },
        onPrimary = { next() },
        loading = publishing,
        bottomNote = if (last) ({
            Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                if (publishError != null) HqInlineError(publishError)
                if (uploadProgress != null && uploadProgress > 0f) {
                    Text("Uploading photos… ${(uploadProgress * 100).toInt()}%", style = HqType.labelSmall, color = c.textSecondary)
                } else if (!isAdmin) {
                    Text("Your flat admin checks it before it goes live.", style = HqType.labelSmall, color = c.textSecondary)
                }
            }
        }) else null,
    ) { shown ->
        val showErrors = errorsOnStep == shown
        when (shown) {
            0 -> {
                FormQuestion("Room type") {
                    ChoiceChips(ListingFacts.roomTypeOptions, { it == roomType }, { roomType = it })
                }
                QuestionGap()
                FormQuestion("Beds free") {
                    val options = (1..5).map { it.toString() }.let { o -> if (beds !in o) o + beds else o }
                    ChoiceChips(options.map { it to it }, { it == beds }, { beds = it })
                }
                QuestionGap()
                FormQuestion("Furnishing") {
                    ChoiceChips(ListingFacts.furnishingOptions, { it == furnishing }, { furnishing = it })
                }
                QuestionGap()
                HqDateField(
                    value = availableFrom,
                    onValueChange = { availableFrom = it.ifBlank { today } },
                    label = "Available from",
                    todayLabel = "Today",
                    clearable = false,
                )
            }
            1 -> {
                HqTextField(
                    value = rent,
                    onValueChange = { rent = it.take(14); onClearError() },
                    label = "Rent per person",
                    modifier = Modifier.focusRequester(rentFocus),
                    placeholder = null,
                    prefix = "₹",
                    suffix = "/ month",
                    keyboardType = KeyboardType.Number,
                    helperText = "8000, 8,000 and 8k all work.",
                    errorText = if (showErrors) rentError else null,
                )
                QuestionGap()
                HqTextField(
                    value = deposit,
                    onValueChange = { deposit = it.take(14) },
                    label = "Security deposit (optional)",
                    modifier = Modifier.focusRequester(depositFocus),
                    placeholder = null,
                    prefix = "₹",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    errorText = if (showErrors) depositError else null,
                )
                QuestionGap()
                FormQuestion("Notice period", helper = "How long a flatmate tells you before moving out.") {
                    ChoiceChips(ListingFacts.noticeOptions, { it == noticePeriod }, { noticePeriod = it })
                }
            }
            2 -> {
                FormQuestion("Who can live here") {
                    ChoiceChips(ListingFacts.suitsOptions, { it == ListingFacts.suitsKey(gender) }, { key ->
                        if (key != ListingFacts.suitsKey(gender)) gender = key
                    })
                }
                QuestionGap()
                FormQuestion("Works or studies") {
                    ChoiceChips(ListingFacts.occupationOptions, { it == occupation || (it == "any" && occupation.isBlank()) }, { occupation = it })
                }
                QuestionGap()
                FormQuestion("What's in the flat and house rules") {
                    TagPicker(
                        groups = listOf(
                            TagGroup("In the flat", ListingFacts.amenityOptions, amenities) { t ->
                                amenities = if (amenities.any { it.equals(t, true) }) amenities.filterNot { it.equals(t, true) } else amenities + t
                            },
                            TagGroup("House rules", ListingFacts.houseRuleOptions, tags) { t ->
                                tags = if (tags.any { it.equals(t, true) }) tags.filterNot { it.equals(t, true) } else tags + t
                            },
                        ),
                        max = ListingFacts.MAX_TAGS,
                    )
                }
            }
            else -> {
                FormQuestion("Photos", helper = "Up to 8. Tap a photo to make it the cover.") {
                    Spacer(Modifier.height(HqSpacing.xs))
                    PhotoGrid(
                        published = existing?.photoUrls.orEmpty(),
                        selected = selectedPhotoStrings,
                        cover = coverPhotoString,
                        onAdd = { photoPicker.launch(arrayOf("image/*")) },
                        onSetCover = { coverPhotoString = it },
                        onRemove = { removed ->
                            selectedPhotoStrings = selectedPhotoStrings - removed
                            if (coverPhotoString == removed) coverPhotoString = selectedPhotoStrings.firstOrNull()
                        },
                    )
                }
                QuestionGap()
                HqTextField(
                    value = area,
                    onValueChange = { area = it.take(120); onClearError() },
                    label = "Area",
                    modifier = Modifier.focusRequester(areaFocus),
                    placeholder = "e.g. Gachibowli",
                    helperText = "Only the area is shown, never your address.",
                    errorText = if (showErrors) areaError else null,
                )
                QuestionGap()
                HqTextField(
                    value = city,
                    onValueChange = { city = it.take(80); onClearError() },
                    label = "City",
                    modifier = Modifier.focusRequester(cityFocus),
                    placeholder = "e.g. Hyderabad",
                    errorText = if (showErrors) cityError else null,
                )
                QuestionGap()
                HqTextField(
                    value = about,
                    onValueChange = { about = it.take(ABOUT_MAX) },
                    label = "About the room (optional)",
                    placeholder = "e.g. Sunny room, 5 min to the metro, quiet building",
                    singleLine = false,
                    minLines = 3,
                    helperText = "What's nearby and who lives there · ${about.length}/$ABOUT_MAX",
                )
                QuestionGap()
                FormQuestion("Preview", helper = "This is how your room looks in Discover.") {
                    Spacer(Modifier.height(HqSpacing.sm))
                    val photos = (listOfNotNull(coverPhotoString) + selectedPhotoStrings.filter { it != coverPhotoString } + existing?.photoUrls.orEmpty())
                    VacancyListingCard(
                        listing = VacancyListing(
                            flatId = flat.id, flatName = flat.name, active = true, city = city.trim(), area = area.trim(),
                            rentPerHead = rentValue?.toDouble(), currency = "INR", bedsAvailable = beds.toIntOrNull() ?: 1,
                            preferredGender = gender, about = about, memberCount = flat.memberCount, lifestyle = tags,
                            roomType = roomType, furnishing = furnishing, amenities = amenities, availableFrom = availableFrom,
                            moveInTiming = ListingFacts.moveInTimingFor(availableFrom), photoUrls = photos,
                        ),
                        onOpen = null,
                    )
                }
            }
        }
    }

    if (showDiscard) {
        HqDiscardChangesDialog(
            onKeepEditing = { showDiscard = false },
            onDiscard = {
                showDiscard = false
                scope.launch { draftStore.clearVacancyDraft(flat.id) }
                onExit()
            },
        )
    }
}

/** 3-column photo grid: an "Add photos" tile, then published photos, then new picks (removable, tap = cover). */
@Composable
private fun PhotoGrid(
    published: List<String>,
    selected: List<String>,
    cover: String?,
    onAdd: () -> Unit,
    onSetCover: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    val c = LocalHqColors.current
    val canAdd = selected.size < 8
    val tiles: List<String?> = (if (canAdd) listOf<String?>(null) else emptyList()) + published + selected
    Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
        tiles.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                row.forEach { item ->
                    val tileModifier = Modifier.weight(1f).aspectRatio(1f).clip(HqShapes.field)
                    when {
                        item == null -> Column(
                            tileModifier.background(c.selectedBg).border(1.dp, c.selectedBorder, HqShapes.field)
                                .hqPressable(onClick = onAdd).semantics { contentDescription = "Add photos" },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Icon(Icons.Filled.AddAPhoto, null, tint = c.selectedFg)
                            Text("Add photos", style = HqType.labelSmall, color = c.selectedFg, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(top = HqSpacing.xs, start = HqSpacing.xs, end = HqSpacing.xs))
                        }
                        item in published -> Box(tileModifier.semantics { contentDescription = "Published photo" }) {
                            PhotoThumb(item)
                            PhotoBadge("Published", Modifier.align(Alignment.BottomStart))
                        }
                        else -> {
                            val isCover = item == cover
                            Box(
                                tileModifier
                                    .hqPressable(role = Role.Button) { onSetCover(item) }
                                    .semantics { contentDescription = if (isCover) "Cover photo" else "Photo. Tap to make it the cover" },
                            ) {
                                PhotoThumb(item)
                                if (isCover) PhotoBadge("Cover", Modifier.align(Alignment.BottomStart))
                                HqIconButton(
                                    icon = Icons.Filled.Close,
                                    contentDescription = "Remove photo",
                                    onClick = { onRemove(item) },
                                    style = HqIconButtonStyle.Tonal,
                                    modifier = Modifier.align(Alignment.TopEnd),
                                )
                            }
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun PhotoThumb(model: String) {
    val c = LocalHqColors.current
    AsyncImage(
        model = model,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        placeholder = ColorPainter(c.surfaceSubtle),
        error = ColorPainter(c.surfaceSubtle),
        fallback = ColorPainter(c.surfaceSubtle),
    )
}

@Composable
private fun PhotoBadge(label: String, modifier: Modifier) {
    val c = LocalHqColors.current
    Text(
        label, style = HqType.labelSmall, color = c.onBrandInk,
        modifier = modifier.padding(HqSpacing.xs).clip(HqShapes.small).background(c.brandInk.copy(alpha = 0.8f))
            .padding(horizontal = HqSpacing.sm, vertical = HqSpacing.xs / 2),
    )
}

private fun smartTags(
    flat: FlatInfo,
    beds: String,
    area: String,
    roomType: String,
    furnishing: String,
    lifestyle: Collection<String>,
    amenities: Collection<String>,
    gender: String,
    occupation: String,
    moveInTiming: String
): List<String> = buildList {
    add("${beds.toIntOrNull() ?: 1} room${if ((beds.toIntOrNull() ?: 1) == 1) "" else "s"}")
    add(if (roomType == "private") "Private room" else "Shared room")
    if (area.isNotBlank()) add(area.trim())
    add(furnishing.replaceFirstChar { it.uppercase() })
    addAll(lifestyle)
    addAll(amenities)
    if (gender != "any") add("${gender.replaceFirstChar { it.uppercase() }} preferred")
    if (occupation != "any") add(occupation.replace('_', ' ').replaceFirstChar { it.uppercase() })
    add(moveInTiming.replace('_', ' ').replaceFirstChar { it.uppercase() })
    if (flat.memberCount > 0) add("${flat.memberCount} current member${if (flat.memberCount == 1) "" else "s"}")
}.distinct()

private const val LOOKING_STEPS = 3
private val lookingStepTitles = listOf("Where and budget", "About you", "Preview")
private const val BIO_MAX = 600

@Composable
private fun LookingWizard(
    draftStore: DiscoveryDraftStore,
    onPublish: (String, String, Double, String, String, String) -> Unit,
    onExit: () -> Unit,
    publishing: Boolean,
    publishError: String?,
    onClearError: () -> Unit,
    initialStep: Int,
) {
    val c = LocalHqColors.current
    val scope = rememberCoroutineScope()
    var step by rememberSaveable { mutableIntStateOf(initialStep.coerceIn(0, LOOKING_STEPS - 1)) }
    var city by rememberSaveable { mutableStateOf("") }
    var lookingIn by rememberSaveable { mutableStateOf("") }
    var budget by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("any") }
    var tags by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var errorsOnStep by rememberSaveable { mutableIntStateOf(-1) }
    var draftLoaded by rememberSaveable { mutableStateOf(false) }
    var baseline by rememberSaveable { mutableStateOf<String?>(null) }
    var showDiscard by remember { mutableStateOf(false) }
    var focusTarget by remember { mutableStateOf<FocusRequester?>(null) }
    val cityFocus = remember { FocusRequester() }
    val areasFocus = remember { FocusRequester() }
    val budgetFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (!draftLoaded) {
            draftStore.getLookingDraft().firstOrNull()?.let { d ->
                city = d.city; lookingIn = d.lookingIn; budget = d.budget; bio = d.bio; gender = d.gender; tags = d.tags
            }
            draftLoaded = true
        }
    }
    val snapshot = listOf(city, lookingIn, budget, bio, gender, tags).joinToString("|")
    LaunchedEffect(draftLoaded) { if (draftLoaded && baseline == null) baseline = snapshot }
    val dirty = baseline != null && snapshot != baseline
    LaunchedEffect(snapshot) {
        if (draftLoaded && dirty) {
            draftStore.saveLookingDraft(LookingDraft(city = city, lookingIn = lookingIn, budget = budget, bio = bio, gender = gender, tags = tags))
        }
    }
    LaunchedEffect(focusTarget) {
        val target = focusTarget ?: return@LaunchedEffect
        delay(HqMotion.slow.toLong())
        runCatching { target.requestFocus() }
        focusTarget = null
    }

    val budgetValue = MoneyInput.parse(budget)
    val cityError = if (city.isBlank()) "Enter your city." else null
    val areasError = if (lookingIn.isBlank()) "Enter at least one area." else null
    val budgetError = if (budgetValue == null || budgetValue <= 0) "Enter your budget, like 9000 or 9k." else null
    fun firstError(s: Int): FocusRequester? = if (s == 0) when {
        cityError != null -> cityFocus
        areasError != null -> areasFocus
        budgetError != null -> budgetFocus
        else -> null
    } else null

    fun next() {
        val problem = firstError(step)
        if (problem != null) { errorsOnStep = step; focusTarget = problem; return }
        errorsOnStep = -1
        if (budget.isNotBlank()) budget = MoneyInput.normalise(budget)
        if (step < LOOKING_STEPS - 1) {
            step += 1
        } else {
            val bad = (0 until LOOKING_STEPS).firstOrNull { firstError(it) != null }
            if (bad != null) { step = bad; errorsOnStep = bad; focusTarget = firstError(bad); return }
            onPublish(city.trim(), lookingIn.trim(), budgetValue?.toDouble() ?: 0.0, bio.trim(), gender, tags.joinToString(","))
            scope.launch { draftStore.clearLookingDraft() }
        }
    }
    fun back() {
        when {
            step > 0 -> { step -= 1; errorsOnStep = -1 }
            dirty -> showDiscard = true
            else -> onExit()
        }
    }
    BackHandler(enabled = !showDiscard) { back() }

    DiscoverWizardScaffold(
        title = "I'm looking for a room",
        step = step,
        stepCount = LOOKING_STEPS,
        stepTitle = { lookingStepTitles[it] },
        stepSubtitle = {
            when (it) {
                0 -> "Flats with a room see this and can send you a request."
                1 -> "A little about you helps flats decide."
                else -> "This is how flats see your post."
            }
        },
        onBack = { back() },
        primaryLabel = if (step < LOOKING_STEPS - 1) "Continue" else "Publish post",
        onPrimary = { next() },
        loading = publishing,
        bottomNote = publishError?.let { { HqInlineError(it) } },
    ) { shown ->
        val showErrors = errorsOnStep == shown
        when (shown) {
            0 -> {
                HqTextField(
                    city, { city = it.take(80); onClearError() }, "City",
                    modifier = Modifier.focusRequester(cityFocus), placeholder = "e.g. Hyderabad",
                    errorText = if (showErrors) cityError else null,
                )
                QuestionGap()
                HqTextField(
                    lookingIn, { lookingIn = it.take(160); onClearError() }, "Areas you'd like",
                    modifier = Modifier.focusRequester(areasFocus), placeholder = "e.g. Gachibowli, Madhapur",
                    helperText = "Separate areas with commas.",
                    errorText = if (showErrors) areasError else null,
                )
                QuestionGap()
                HqTextField(
                    budget, { budget = it.take(14); onClearError() }, "Budget per month",
                    modifier = Modifier.focusRequester(budgetFocus), placeholder = null, prefix = "₹",
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Done, onImeAction = { next() },
                    helperText = "9000, 9,000 and 9k all work.",
                    errorText = if (showErrors) budgetError else null,
                )
            }
            1 -> {
                FormQuestion("You are") {
                    ChoiceChips(habitiq.app.ui.GenderOptions, { it == habitiq.app.ui.normalizeGender(gender) }, { gender = it })
                }
                QuestionGap()
                FormQuestion("Your lifestyle") {
                    TagPicker(
                        groups = listOf(TagGroup("Lifestyle", DiscoverFilterLogic.lifestyleTagOptions, tags) { t ->
                            tags = if (tags.any { it.equals(t, true) }) tags.filterNot { it.equals(t, true) } else tags + t
                        }),
                        max = ListingFacts.MAX_TAGS,
                        collapsedPerGroup = 8,
                    )
                }
                QuestionGap()
                HqTextField(
                    bio, { bio = it.take(BIO_MAX); onClearError() }, "About you (optional)",
                    placeholder = "e.g. Software engineer, early riser, loves cooking",
                    singleLine = false, minLines = 3,
                    helperText = "Work, routine, what you're like at home · ${bio.length}/$BIO_MAX",
                )
            }
            else -> {
                FlatmateCard(
                    seeker = SeekerProfile(
                        id = "preview", displayName = "You", city = city.trim(), lookingIn = lookingIn.trim(),
                        budget = budgetValue?.toDouble() ?: 0.0, bio = bio.trim(), gender = gender, lifestyleTags = tags.joinToString(", "),
                    ),
                    signals = emptyList(),
                    trust = TrustCopy.forSeeker(true),
                    onView = null,
                    onConnect = {},
                )
            }
        }
    }

    if (showDiscard) {
        HqDiscardChangesDialog(
            onKeepEditing = { showDiscard = false },
            onDiscard = {
                showDiscard = false
                scope.launch { draftStore.clearLookingDraft() }
                onExit()
            },
        )
    }
}
