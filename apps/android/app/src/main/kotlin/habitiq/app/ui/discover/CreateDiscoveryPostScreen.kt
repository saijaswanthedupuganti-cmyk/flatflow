package habitiq.app.ui.discover

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import habitiq.app.data.VacancyData
import habitiq.app.discover.DiscoverFilterLogic
import habitiq.app.discover.DiscoverFlags
import habitiq.app.discover.DiscoveryDraftStore
import habitiq.app.discover.DiscoveryPostType
import habitiq.app.discover.LookingDraft
import habitiq.app.discover.VacancyDraft
import habitiq.app.flats.FlatInfo
import habitiq.app.ui.components.HqButton
import habitiq.app.ui.components.HqDateField
import habitiq.app.ui.components.HqButtonVariant
import habitiq.app.ui.components.HqCard
import habitiq.app.ui.components.HqChip
import habitiq.app.ui.components.HqConfirmDialog
import habitiq.app.ui.components.HqTextButton
import habitiq.app.ui.components.HqTextField
import habitiq.app.ui.theme.HqSpacing
import habitiq.app.ui.theme.HqType
import habitiq.app.ui.theme.LocalHqColors
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

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
    initialType: String? = null
) {
    val c = LocalHqColors.current
    val context = LocalContext.current
    val draftStore = remember { DiscoveryDraftStore(context) }
    val scope = rememberCoroutineScope()
    var typeName by rememberSaveable { mutableStateOf(initialType) }
    val type = typeName?.let { runCatching { DiscoveryPostType.valueOf(it) }.getOrNull() }
    var showDiscard by remember { mutableStateOf(false) }

    BackHandler(enabled = type != null || showDiscard) {
        if (showDiscard) showDiscard = false else showDiscard = true
    }

    Column(Modifier.fillMaxSize().background(c.canvas)) {
        HqTextButton(text = "← Back", onClick = { if (type == null) onBack() else showDiscard = true }, modifier = Modifier.padding(start = HqSpacing.sm, top = HqSpacing.sm))
        when (type) {
            null -> PostTypeSelection(
                lookingEnabled = DiscoverFlags.LOOKING_POSTS,
                onVacancy = { typeName = DiscoveryPostType.VACANCY.name },
                onLooking = { typeName = DiscoveryPostType.LOOKING.name }
            )
            DiscoveryPostType.VACANCY -> VacancyWizard(
                isAdmin = isAdmin,
                flat = flat,
                existing = existingVacancy,
                draftStore = draftStore,
                onPublish = onPublishVacancy,
                onCancel = { showDiscard = true },
                publishing = publishing,
                uploadProgress = uploadProgress,
                publishError = publishError,
                onClearError = onClearError
            )
            DiscoveryPostType.LOOKING -> LookingWizard(
                draftStore = draftStore,
                onPublish = onPublishLooking,
                onCancel = { showDiscard = true },
                publishing = publishing,
                publishError = publishError,
                onClearError = onClearError
            )
        }
    }
    if (showDiscard) {
        HqConfirmDialog(
            title = "Discard changes?",
            message = "Your unfinished post will not be saved.",
            confirmLabel = "Discard",
            onConfirm = {
                if (type == DiscoveryPostType.VACANCY && flat != null) {
                    scope.launch { draftStore.clearVacancyDraft(flat.id) }
                } else if (type == DiscoveryPostType.LOOKING) {
                    scope.launch { draftStore.clearLookingDraft() }
                }
                showDiscard = false
                typeName = null
            },
            onDismiss = { showDiscard = false }
        )
    }
}

@Composable
private fun PostTypeSelection(
    lookingEnabled: Boolean,
    onVacancy: () -> Unit,
    onLooking: () -> Unit
) {
    val c = LocalHqColors.current
    Column(Modifier.padding(HqSpacing.xl), verticalArrangement = Arrangement.spacedBy(HqSpacing.md)) {
        Text("Create a Discovery post", style = HqType.headlineMedium, color = c.textPrimary)
        Text("Vacancy and looking posts are different. Choose one.", style = HqType.bodyMedium, color = c.textSecondary)
        HqButton(text = "I have a vacancy", onClick = onVacancy)
        if (lookingEnabled) {
            HqButton(text = "I'm looking for a flat", onClick = onLooking, variant = HqButtonVariant.Secondary)
        } else {
            Text("Looking posts are not enabled in this build.", style = HqType.bodySmall, color = c.textMuted)
        }
    }
}

@Composable
private fun VacancyWizard(
    isAdmin: Boolean,
    flat: FlatInfo?,
    existing: VacancyData?,
    draftStore: DiscoveryDraftStore,
    onPublish: (VacancyData, List<Uri>) -> Unit,
    onCancel: () -> Unit,
    publishing: Boolean,
    uploadProgress: Float?,
    publishError: String?,
    onClearError: () -> Unit
) {
    val c = LocalHqColors.current
    val scope = rememberCoroutineScope()
    var step by rememberSaveable { mutableIntStateOf(0) }
    // Registered after the screen's discard handler, so back walks to the previous step before offering discard.
    BackHandler(enabled = step > 0) { step -= 1 }
    var city by rememberSaveable { mutableStateOf(existing?.city.orEmpty()) }
    var area by rememberSaveable { mutableStateOf(existing?.area.orEmpty()) }
    var rent by rememberSaveable { mutableStateOf(existing?.rentPerHead?.toInt()?.toString().orEmpty()) }
    var beds by rememberSaveable { mutableStateOf((existing?.bedsAvailable ?: 1).toString()) }
    var gender by rememberSaveable { mutableStateOf(existing?.preferredGender ?: "any") }
    var about by rememberSaveable { mutableStateOf(existing?.about.orEmpty()) }
    var tags by rememberSaveable { mutableStateOf(existing?.lifestyle.orEmpty()) }
    var roomType by rememberSaveable { mutableStateOf(existing?.roomType ?: "private") }
    var deposit by rememberSaveable { mutableStateOf(existing?.securityDeposit?.toInt()?.toString().orEmpty()) }
    var availableFrom by rememberSaveable { mutableStateOf(existing?.availableFrom.orEmpty()) }
    var noticePeriod by rememberSaveable { mutableStateOf(existing?.noticePeriod ?: "any") }
    var furnishing by rememberSaveable { mutableStateOf(existing?.furnishing ?: "furnished") }
    var amenities by rememberSaveable { mutableStateOf(existing?.amenities.orEmpty()) }
    var occupation by rememberSaveable { mutableStateOf(existing?.preferredOccupation ?: "any") }
    var budgetMin by rememberSaveable { mutableStateOf(existing?.preferredBudgetMin?.toInt()?.toString().orEmpty()) }
    var budgetMax by rememberSaveable { mutableStateOf(existing?.preferredBudgetMax?.toInt()?.toString().orEmpty()) }
    var moveInTiming by rememberSaveable { mutableStateOf(existing?.moveInTiming ?: "within_1_month") }
    var selectedPhotoStrings by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var coverPhotoString by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedPhotos = selectedPhotoStrings.map(Uri::parse)
    val coverPhoto = coverPhotoString?.let(Uri::parse)
    var showErrors by remember { mutableStateOf(false) }
    var draftLoaded by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        }
        val combined = (selectedPhotoStrings + uris.map(Uri::toString)).distinct().take(8)
        selectedPhotoStrings = combined
        if (coverPhotoString == null) coverPhotoString = combined.firstOrNull()
        showErrors = false
        onClearError()
    }

    if (flat == null) {
        Column(Modifier.padding(HqSpacing.xl), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            Text("Join a flat to post a vacancy", style = HqType.titleMedium, color = c.textPrimary)
            Text("Vacancies are listed for the flat you live in.", style = HqType.bodyMedium, color = c.textSecondary)
            HqTextButton(text = "Choose another type", onClick = onCancel)
        }
        return
    }

    LaunchedEffect(flat.id) {
        if (!draftLoaded && existing == null) {
            val savedDraft = draftStore.getVacancyDraft(flat.id).firstOrNull()
            if (savedDraft != null) {
                step = savedDraft.step
                city = savedDraft.city
                area = savedDraft.area
                rent = savedDraft.rent
                beds = savedDraft.beds
                gender = savedDraft.gender
                about = savedDraft.about
                tags = savedDraft.tags
                roomType = savedDraft.roomType
                deposit = savedDraft.deposit
                availableFrom = savedDraft.availableFrom
                noticePeriod = savedDraft.noticePeriod
                furnishing = savedDraft.furnishing
                amenities = savedDraft.amenities
                occupation = savedDraft.occupation
                budgetMin = savedDraft.budgetMin
                budgetMax = savedDraft.budgetMax
                moveInTiming = savedDraft.moveInTiming
                selectedPhotoStrings = savedDraft.selectedPhotoUris
                coverPhotoString = savedDraft.coverPhotoUri
            }
            draftLoaded = true
        }
    }

    LaunchedEffect(step, city, area, rent, beds, gender, about, tags, roomType, deposit, availableFrom, noticePeriod, furnishing, amenities, occupation, budgetMin, budgetMax, moveInTiming, selectedPhotoStrings, coverPhotoString) {
        if (draftLoaded || existing != null) {
            draftStore.saveVacancyDraft(
                flat.id,
                VacancyDraft(
                    step = step,
                    city = city,
                    area = area,
                    rent = rent,
                    beds = beds,
                    gender = gender,
                    about = about,
                    tags = tags,
                    roomType = roomType,
                    deposit = deposit,
                    availableFrom = availableFrom,
                    noticePeriod = noticePeriod,
                    furnishing = furnishing,
                    amenities = amenities,
                    occupation = occupation,
                    budgetMin = budgetMin,
                    budgetMax = budgetMax,
                    moveInTiming = moveInTiming,
                    selectedPhotoUris = selectedPhotoStrings,
                    coverPhotoUri = coverPhotoString
                )
            )
        }
    }

    val stepValid = when (step) {
        0 -> city.isNotBlank() && area.isNotBlank() && (beds.toIntOrNull() ?: 0) in 1..8
        3 -> (rent.toDoubleOrNull() ?: 0.0) > 0
        else -> true
    }
    fun goNext() {
        if (stepValid) {
            showErrors = false
            step += 1
        } else {
            showErrors = true
        }
    }
    // Done on a step's last field advances only when the step is complete; otherwise it just closes the keyboard.
    val imeNext: (() -> Unit)? = if (stepValid && step < 8) ({ goNext() }) else null

    Column(Modifier.padding(HqSpacing.xl).verticalScroll(rememberScrollState())) {
        if (!isAdmin) {
            Text(
                "Your flat admin will review this before it goes live on Discover.",
                style = HqType.bodyMedium, color = c.statusInfoFg,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.statusInfoBg).padding(12.dp),
            )
            Spacer(Modifier.height(HqSpacing.md))
        }
        Text("Post a vacancy", style = HqType.headlineMedium, color = c.textPrimary)
        Text("Step ${step + 1} of 9", style = HqType.labelMedium, color = c.textSecondary)
        Spacer(Modifier.height(HqSpacing.sm))
        LinearProgressIndicator(
            progress = { (step + 1) / 9f },
            modifier = Modifier.fillMaxWidth(),
            color = c.textBrand,
            trackColor = c.surfaceSubtle,
        )
        Spacer(Modifier.height(HqSpacing.lg))
        when (step) {
            0 -> {
                Text("Basic flat details", style = HqType.headlineSmall, color = c.textPrimary)
                Spacer(Modifier.height(HqSpacing.sm))
                HqCard {
                    Text(flat.name, style = HqType.titleLarge, color = c.textPrimary)
                    Text("This is the flat you manage · ${flat.memberCount} current member${if (flat.memberCount == 1) "" else "s"}", style = HqType.bodySmall, color = c.textSecondary)
                }
                Spacer(Modifier.height(HqSpacing.md))
                HqTextField(city, { city = it.take(80); showErrors = false; onClearError() }, "City", modifier = Modifier.fillMaxWidth(), placeholder = "e.g. Hyderabad", errorText = if (showErrors && city.isBlank()) "Enter a city." else null)
                Spacer(Modifier.height(HqSpacing.sm))
                HqTextField(area, { area = it.take(120); showErrors = false; onClearError() }, "Area / neighbourhood", modifier = Modifier.fillMaxWidth(), placeholder = "e.g. Gachibowli", errorText = if (showErrors && area.isBlank()) "Enter an approximate area." else null)
                Spacer(Modifier.height(HqSpacing.sm))
                HqTextField(beds, { beds = it.filter(Char::isDigit).take(2); showErrors = false }, "Rooms available", modifier = Modifier.fillMaxWidth(), placeholder = "e.g. 1", keyboardType = KeyboardType.Number, imeAction = ImeAction.Done, onImeAction = imeNext, errorText = if (showErrors && (beds.toIntOrNull() ?: 0) !in 1..8) "Enter between 1 and 8 rooms." else null)
            }
            1 -> {
                Text("Add clear photos", style = HqType.headlineSmall, color = c.textPrimary)
                Text("Show the living room, bedroom and kitchen. Photos upload securely when you publish.", style = HqType.bodyMedium, color = c.textSecondary)
                Spacer(Modifier.height(HqSpacing.md))
                HqCard {
                    Text(if (selectedPhotos.isEmpty()) "No new photos selected" else "${selectedPhotos.size} photo${if (selectedPhotos.size == 1) "" else "s"} ready", style = HqType.titleMedium, color = c.textPrimary)
                    Text(if (existing?.photoUrls.isNullOrEmpty()) "Add at least 3 photos for a stronger listing." else "${existing?.photoUrls?.size} published photo${if (existing?.photoUrls?.size == 1) "" else "s"} already saved.", style = HqType.bodySmall, color = c.textSecondary)
                    Spacer(Modifier.height(HqSpacing.md))
                    Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
                        HqButton(text = "Choose photos", onClick = { photoPicker.launch(arrayOf("image/*")) }, variant = HqButtonVariant.Secondary, fullWidth = false)
                    }
                }
                if (selectedPhotos.isEmpty() && existing?.photoUrls.isNullOrEmpty()) {
                    Spacer(Modifier.height(HqSpacing.sm))
                    Text("You can publish without a photo, but clear photos help people understand the home.", style = HqType.bodySmall, color = c.textSecondary)
                }
                selectedPhotos.forEachIndexed { index, uri ->
                    Spacer(Modifier.height(HqSpacing.sm))
                    HqCard {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected listing photo ${index + 1}",
                            modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                            placeholder = ColorPainter(c.surfaceSubtle),
                            error = ColorPainter(c.statusDangerBg)
                        )
                        Spacer(Modifier.height(HqSpacing.sm))
                        Text("Photo ${index + 1}${if (coverPhoto == uri) " · Cover" else ""}", style = HqType.titleSmall, color = c.textPrimary)
                        Text(uri.lastPathSegment ?: "Selected image", style = HqType.caption, color = c.textMuted)
                        Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                            if (coverPhoto != uri) HqTextButton(text = "Set cover", onClick = { coverPhotoString = uri.toString() })
                            if (index > 0) HqTextButton(text = "Move up", onClick = {
                                val changed = selectedPhotos.toMutableList()
                                val item = changed.removeAt(index)
                                changed.add(index - 1, item)
                                selectedPhotoStrings = changed.map(Uri::toString)
                            })
                            HqTextButton(text = "Remove", onClick = {
                                val changed = selectedPhotos.toMutableList()
                                changed.removeAt(index)
                                selectedPhotoStrings = changed.map(Uri::toString)
                                if (coverPhoto == uri) coverPhotoString = changed.firstOrNull()?.toString()
                            })
                        }
                    }
                }
            }
            2 -> {
                Text("Room type & furnishing", style = HqType.headlineSmall, color = c.textPrimary)
                Spacer(Modifier.height(HqSpacing.sm))
                ChoiceRow(
                    title = "Room type",
                    options = listOf("private" to "Private room", "shared" to "Shared room"),
                    selected = roomType,
                    onSelect = { roomType = it }
                )
                Spacer(Modifier.height(HqSpacing.md))
                ChoiceRow(
                    title = "Furnishing",
                    options = listOf("furnished" to "Furnished", "semi_furnished" to "Semi-furnished", "unfurnished" to "Unfurnished"),
                    selected = furnishing,
                    onSelect = { furnishing = it }
                )
            }
            3 -> {
                Text("Rent & security deposit", style = HqType.headlineSmall, color = c.textPrimary)
                Spacer(Modifier.height(HqSpacing.sm))
                HqTextField(rent, { rent = it.filter(Char::isDigit).take(9); showErrors = false }, "Monthly rent per head (₹)", modifier = Modifier.fillMaxWidth(), placeholder = "e.g. 12000", keyboardType = KeyboardType.Number, errorText = if (showErrors && (rent.toDoubleOrNull() ?: 0.0) <= 0) "Enter monthly rent." else null)
                Spacer(Modifier.height(HqSpacing.sm))
                HqTextField(deposit, { deposit = it.filter(Char::isDigit).take(9) }, "Security deposit (₹, optional)", modifier = Modifier.fillMaxWidth(), placeholder = "e.g. 25000", keyboardType = KeyboardType.Number, imeAction = ImeAction.Done, onImeAction = imeNext)
                Spacer(Modifier.height(HqSpacing.md))
                ChoiceRow(
                    title = "Notice period",
                    options = listOf("any" to "Flexible / any", "1_month" to "1 month", "2_months" to "2 months", "3_months" to "3 months"),
                    selected = noticePeriod,
                    onSelect = { noticePeriod = it }
                )
            }
            4 -> {
                Text("Dates & timing", style = HqType.headlineSmall, color = c.textPrimary)
                Spacer(Modifier.height(HqSpacing.sm))
                HqDateField(availableFrom, { availableFrom = it }, "Available from", modifier = Modifier.fillMaxWidth(), placeholder = "Pick a move-in date", immediateLabel = "Immediately")
                Spacer(Modifier.height(HqSpacing.md))
                ChoiceRow(
                    title = "Move-in timing",
                    options = listOf("immediate" to "Immediate", "within_1_month" to "Within 1 month", "flexible" to "Flexible"),
                    selected = moveInTiming,
                    onSelect = { moveInTiming = it }
                )
            }
            5 -> {
                Text("Preferred flatmate", style = HqType.headlineSmall, color = c.textPrimary)
                Spacer(Modifier.height(HqSpacing.sm))
                ChoiceRow(
                    title = "Preferred gender",
                    options = DiscoverFilterLogic.vacancyGenderOptions,
                    selected = gender,
                    onSelect = { gender = it }
                )
                Spacer(Modifier.height(HqSpacing.md))
                ChoiceRow(
                    title = "Preferred occupation",
                    options = listOf("any" to "Any", "working_professional" to "Working professional", "student" to "Student"),
                    selected = occupation,
                    onSelect = { occupation = it }
                )
            }
            6 -> {
                Text("Amenities & lifestyle", style = HqType.headlineSmall, color = c.textPrimary)
                Spacer(Modifier.height(HqSpacing.sm))
                Text("Select amenities available in the flat:", style = HqType.bodySmall, color = c.textSecondary)
                Spacer(Modifier.height(HqSpacing.xs))
                Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                    listOf("WiFi", "AC", "Attached Bathroom", "Washing Machine", "Geyser", "Cook / Maid", "Parking", "Power Backup", "Gym", "Balcony").forEach { amenity ->
                        HqChip(
                            label = amenity,
                            selected = amenity in amenities,
                            onClick = { amenities = if (amenity in amenities) amenities - amenity else amenities + amenity }
                        )
                    }
                }
                Spacer(Modifier.height(HqSpacing.md))
                Text("Select lifestyle tags:", style = HqType.bodySmall, color = c.textSecondary)
                Spacer(Modifier.height(HqSpacing.xs))
                Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
                    DiscoverFilterLogic.lifestyleTagOptions.forEach { tag ->
                        HqChip(
                            label = tag,
                            selected = tag in tags,
                            onClick = { tags = if (tag in tags) tags - tag else tags + tag }
                        )
                    }
                }
            }
            7 -> {
                Text("Budget range for flatmates (optional)", style = HqType.headlineSmall, color = c.textPrimary)
                Spacer(Modifier.height(HqSpacing.sm))
                Text("Helps people who are looking find this listing by budget.", style = HqType.bodyMedium, color = c.textSecondary)
                Spacer(Modifier.height(HqSpacing.sm))
                HqTextField(budgetMin, { budgetMin = it.filter(Char::isDigit).take(9) }, "Min budget (₹, optional)", modifier = Modifier.fillMaxWidth(), placeholder = "Any", keyboardType = KeyboardType.Number)
                Spacer(Modifier.height(HqSpacing.sm))
                HqTextField(budgetMax, { budgetMax = it.filter(Char::isDigit).take(9) }, "Max budget (₹, optional)", modifier = Modifier.fillMaxWidth(), placeholder = "Any", keyboardType = KeyboardType.Number, imeAction = ImeAction.Done, onImeAction = imeNext)
            }
            8 -> {
                Text("About the home & review", style = HqType.headlineSmall, color = c.textPrimary)
                Spacer(Modifier.height(HqSpacing.sm))
                HqTextField(about, { about = it.take(800) }, "Tell prospective flatmates about the home", modifier = Modifier.fillMaxWidth(), placeholder = "e.g. Sunny 3BHK, 5 min to metro, quiet building", singleLine = false, minLines = 4, helperText = "${about.length}/800")
                Spacer(Modifier.height(HqSpacing.md))
                HqCard {
                    Text("Summary preview", style = HqType.titleMedium, color = c.textPrimary)
                    Text("$city · $area", style = HqType.bodyMedium, color = c.textSecondary)
                    Text("${beds.toIntOrNull() ?: 1} room${if ((beds.toIntOrNull() ?: 1) == 1) "" else "s"} available · ₹$rent/mo", style = HqType.bodyMedium, color = c.textSecondary)
                    Text("$roomType · $furnishing", style = HqType.bodySmall, color = c.textMuted)
                    if (selectedPhotos.isNotEmpty() || !existing?.photoUrls.isNullOrEmpty()) {
                        Text("${selectedPhotos.size + (existing?.photoUrls?.size ?: 0)} total photos", style = HqType.bodySmall, color = c.textMuted)
                    }
                }
            }
        }
        if (publishError != null) {
            Spacer(Modifier.height(HqSpacing.sm))
            Text(publishError, style = HqType.bodySmall, color = c.statusDangerFg)
        }
        if (uploadProgress != null && uploadProgress > 0f) {
            Spacer(Modifier.height(HqSpacing.sm))
            LinearProgressIndicator(
                progress = { uploadProgress },
                modifier = Modifier.fillMaxWidth(),
                color = c.textBrand,
                trackColor = c.surfaceSubtle,
            )
            Text("Uploading photos… ${(uploadProgress * 100).toInt()}%", style = HqType.caption, color = c.textSecondary)
        }
        Spacer(Modifier.height(HqSpacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            if (step > 0) {
                HqButton(text = "Previous", onClick = { step -= 1 }, variant = HqButtonVariant.Secondary, modifier = Modifier.weight(1f))
            } else {
                HqButton(text = "Cancel", onClick = onCancel, variant = HqButtonVariant.Secondary, modifier = Modifier.weight(1f))
            }
            if (step < 8) {
                HqButton(
                    text = "Next",
                    onClick = { goNext() },
                    modifier = Modifier.weight(1f)
                )
            } else {
                HqButton(
                    text = when {
                        !isAdmin -> "Send for approval"
                        existing != null -> "Update vacancy"
                        else -> "Publish vacancy"
                    },
                    onClick = {
                        val valid = city.isNotBlank() && area.isNotBlank() && (rent.toDoubleOrNull() ?: 0.0) > 0
                        if (!valid) {
                            showErrors = true
                            return@HqButton
                        }
                        onPublish(
                            VacancyData(
                                active = true,
                                city = city.trim(),
                                area = area.trim(),
                                rentPerHead = rent.toDoubleOrNull(),
                                bedsAvailable = beds.toIntOrNull() ?: 1,
                                preferredGender = gender,
                                about = about.trim(),
                                lifestyle = tags,
                                customTags = smartTags(flat, beds, area, roomType, furnishing, tags, amenities, gender, occupation, moveInTiming),
                                roomType = roomType,
                                securityDeposit = deposit.toDoubleOrNull(),
                                availableFrom = availableFrom.trim(),
                                noticePeriod = noticePeriod,
                                furnishing = furnishing,
                                amenities = amenities,
                                preferredOccupation = occupation,
                                preferredBudgetMin = budgetMin.toDoubleOrNull(),
                                preferredBudgetMax = budgetMax.toDoubleOrNull(),
                                moveInTiming = moveInTiming,
                                approximateLocationOnly = true,
                                photoUrls = existing?.photoUrls.orEmpty(),
                                postStatus = "PUBLISHED"
                            ), selectedPhotos.sortedBy { if (it == coverPhoto) 0 else 1 }
                        )
                        scope.launch { draftStore.clearVacancyDraft(flat.id) }
                    },
                    enabled = !publishing,
                    loading = publishing,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ChoiceRow(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val c = LocalHqColors.current
    Text(title, style = HqType.titleMedium, color = c.textPrimary)
    Column(verticalArrangement = Arrangement.spacedBy(HqSpacing.xs)) {
        options.forEach { (value, label) -> HqChip(label = label, selected = selected == value, onClick = { onSelect(value) }) }
    }
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

@Composable
private fun LookingWizard(
    draftStore: DiscoveryDraftStore,
    onPublish: (String, String, Double, String, String, String) -> Unit,
    onCancel: () -> Unit,
    publishing: Boolean,
    publishError: String?,
    onClearError: () -> Unit
) {
    val c = LocalHqColors.current
    val scope = rememberCoroutineScope()
    var city by rememberSaveable { mutableStateOf("") }
    var lookingIn by rememberSaveable { mutableStateOf("") }
    var budget by rememberSaveable { mutableStateOf("") }
    var bio by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf("any") }
    var tags by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    var draftLoaded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!draftLoaded) {
            val savedDraft = draftStore.getLookingDraft().firstOrNull()
            if (savedDraft != null) {
                city = savedDraft.city
                lookingIn = savedDraft.lookingIn
                budget = savedDraft.budget
                bio = savedDraft.bio
                gender = savedDraft.gender
                tags = savedDraft.tags
            }
            draftLoaded = true
        }
    }

    LaunchedEffect(city, lookingIn, budget, bio, gender, tags) {
        if (draftLoaded) {
            draftStore.saveLookingDraft(
                LookingDraft(
                    city = city,
                    lookingIn = lookingIn,
                    budget = budget,
                    bio = bio,
                    gender = gender,
                    tags = tags
                )
            )
        }
    }

    Column(Modifier.padding(HqSpacing.xl).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
        Text("Looking for a flat", style = HqType.headlineSmall, color = c.textPrimary)
        Text("This is a looking post, not a vacancy.", style = HqType.bodySmall, color = c.textSecondary)
        HqTextField(city, { city = it.take(80); showErrors = false; onClearError() }, "Your city", modifier = Modifier.fillMaxWidth(), placeholder = "e.g. Hyderabad", errorText = if (showErrors && city.isBlank()) "Enter your city." else null)
        HqTextField(lookingIn, { lookingIn = it.take(160); showErrors = false; onClearError() }, "Areas you want", modifier = Modifier.fillMaxWidth(), placeholder = "e.g. Gachibowli, Madhapur", errorText = if (showErrors && lookingIn.isBlank()) "Enter at least one preferred area." else null)
        HqTextField(budget, { budget = it.filter { c2 -> c2.isDigit() }.take(9); showErrors = false; onClearError() }, "Budget (₹)", modifier = Modifier.fillMaxWidth(), placeholder = "e.g. 12000", keyboardType = KeyboardType.Number, imeAction = ImeAction.Done, errorText = if (showErrors && (budget.toDoubleOrNull() ?: 0.0) <= 0) "Enter a budget greater than zero." else null)
        Text("Gender (optional, structured)", style = HqType.bodySmall, color = c.textPrimary)
        DiscoverFilterLogic.seekerGenderOptions.forEach { (value, label) ->
            HqChip(label = label, selected = gender == value, onClick = { gender = value })
        }
        Text("Lifestyle", style = HqType.titleMedium, color = c.textPrimary)
        DiscoverFilterLogic.lifestyleTagOptions.forEach { tag ->
            HqChip(label = tag, selected = tag in tags, onClick = { tags = if (tag in tags) tags - tag else tags + tag })
        }
        HqTextField(bio, { bio = it.take(600); onClearError() }, "About you", modifier = Modifier.fillMaxWidth(), placeholder = "e.g. Software engineer, early riser, loves cooking", helperText = "${bio.length}/600", singleLine = false, minLines = 3)
        if (publishError != null) Text(publishError, style = HqType.bodySmall, color = c.statusDangerFg)
        Row(horizontalArrangement = Arrangement.spacedBy(HqSpacing.sm)) {
            HqButton(text = "Cancel", onClick = onCancel, variant = HqButtonVariant.Secondary, modifier = Modifier.weight(1f))
            HqButton(
                text = "Publish looking post",
                onClick = {
                    val valid = city.isNotBlank() && lookingIn.isNotBlank() && (budget.toDoubleOrNull() ?: 0.0) > 0
                    if (!valid) showErrors = true
                    else {
                        onPublish(city, lookingIn, budget.toDoubleOrNull() ?: 0.0, bio, gender, tags.joinToString(","))
                        scope.launch { draftStore.clearLookingDraft() }
                    }
                },
                enabled = !publishing,
                loading = publishing,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
