package habitiq.app.discover

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.discoveryDraftDataStore: DataStore<Preferences> by preferencesDataStore(name = "habitiq_discovery_drafts")

data class VacancyDraft(
    val step: Int = 0,
    val city: String = "",
    val area: String = "",
    val rent: String = "",
    val beds: String = "1",
    val gender: String = "any",
    val about: String = "",
    val tags: List<String> = emptyList(),
    val roomType: String = "private",
    val deposit: String = "",
    val availableFrom: String = "",
    val noticePeriod: String = "any",
    val furnishing: String = "furnished",
    val amenities: List<String> = emptyList(),
    val occupation: String = "any",
    val budgetMin: String = "",
    val budgetMax: String = "",
    val moveInTiming: String = "within_1_month",
    val selectedPhotoUris: List<String> = emptyList(),
    val coverPhotoUri: String? = null
)

data class LookingDraft(
    val city: String = "",
    val lookingIn: String = "",
    val budget: String = "",
    val bio: String = "",
    val gender: String = "any",
    val tags: List<String> = emptyList()
)

class DiscoveryDraftStore(private val context: Context) {

    fun getVacancyDraft(flatId: String): Flow<VacancyDraft?> {
        val p = "vacancy_${flatId}_"
        return context.discoveryDraftDataStore.data.map { prefs ->
            val hasDraft = prefs[stringPreferencesKey("${p}has_draft")] == "true"
            if (!hasDraft) {
                null
            } else {
                val photoUrisStr = prefs[stringPreferencesKey("${p}photos")].orEmpty()
                val photoUris = if (photoUrisStr.isBlank()) emptyList() else photoUrisStr.split("|||")
                val tagsStr = prefs[stringPreferencesKey("${p}tags")].orEmpty()
                val tags = if (tagsStr.isBlank()) emptyList() else tagsStr.split("|||")
                val amenitiesStr = prefs[stringPreferencesKey("${p}amenities")].orEmpty()
                val amenities = if (amenitiesStr.isBlank()) emptyList() else amenitiesStr.split("|||")
                VacancyDraft(
                    step = prefs[intPreferencesKey("${p}step")] ?: 0,
                    city = prefs[stringPreferencesKey("${p}city")].orEmpty(),
                    area = prefs[stringPreferencesKey("${p}area")].orEmpty(),
                    rent = prefs[stringPreferencesKey("${p}rent")].orEmpty(),
                    beds = prefs[stringPreferencesKey("${p}beds")] ?: "1",
                    gender = prefs[stringPreferencesKey("${p}gender")] ?: "any",
                    about = prefs[stringPreferencesKey("${p}about")].orEmpty(),
                    tags = tags,
                    roomType = prefs[stringPreferencesKey("${p}room_type")] ?: "private",
                    deposit = prefs[stringPreferencesKey("${p}deposit")].orEmpty(),
                    availableFrom = prefs[stringPreferencesKey("${p}available_from")].orEmpty(),
                    noticePeriod = prefs[stringPreferencesKey("${p}notice_period")] ?: "any",
                    furnishing = prefs[stringPreferencesKey("${p}furnishing")] ?: "furnished",
                    amenities = amenities,
                    occupation = prefs[stringPreferencesKey("${p}occupation")] ?: "any",
                    budgetMin = prefs[stringPreferencesKey("${p}budget_min")].orEmpty(),
                    budgetMax = prefs[stringPreferencesKey("${p}budget_max")].orEmpty(),
                    moveInTiming = prefs[stringPreferencesKey("${p}move_in_timing")] ?: "within_1_month",
                    selectedPhotoUris = photoUris,
                    coverPhotoUri = prefs[stringPreferencesKey("${p}cover_photo")].takeIf { !it.isNullOrBlank() }
                )
            }
        }
    }

    suspend fun saveVacancyDraft(flatId: String, draft: VacancyDraft) {
        val p = "vacancy_${flatId}_"
        context.discoveryDraftDataStore.edit { prefs ->
            prefs[stringPreferencesKey("${p}has_draft")] = "true"
            prefs[intPreferencesKey("${p}step")] = draft.step
            prefs[stringPreferencesKey("${p}city")] = draft.city
            prefs[stringPreferencesKey("${p}area")] = draft.area
            prefs[stringPreferencesKey("${p}rent")] = draft.rent
            prefs[stringPreferencesKey("${p}beds")] = draft.beds
            prefs[stringPreferencesKey("${p}gender")] = draft.gender
            prefs[stringPreferencesKey("${p}about")] = draft.about
            prefs[stringPreferencesKey("${p}tags")] = draft.tags.joinToString("|||")
            prefs[stringPreferencesKey("${p}room_type")] = draft.roomType
            prefs[stringPreferencesKey("${p}deposit")] = draft.deposit
            prefs[stringPreferencesKey("${p}available_from")] = draft.availableFrom
            prefs[stringPreferencesKey("${p}notice_period")] = draft.noticePeriod
            prefs[stringPreferencesKey("${p}furnishing")] = draft.furnishing
            prefs[stringPreferencesKey("${p}amenities")] = draft.amenities.joinToString("|||")
            prefs[stringPreferencesKey("${p}occupation")] = draft.occupation
            prefs[stringPreferencesKey("${p}budget_min")] = draft.budgetMin
            prefs[stringPreferencesKey("${p}budget_max")] = draft.budgetMax
            prefs[stringPreferencesKey("${p}move_in_timing")] = draft.moveInTiming
            prefs[stringPreferencesKey("${p}photos")] = draft.selectedPhotoUris.joinToString("|||")
            prefs[stringPreferencesKey("${p}cover_photo")] = draft.coverPhotoUri.orEmpty()
        }
    }

    suspend fun clearVacancyDraft(flatId: String) {
        val p = "vacancy_${flatId}_"
        context.discoveryDraftDataStore.edit { prefs ->
            val keysToRemove = prefs.asMap().keys.filter { it.name.startsWith(p) }
            keysToRemove.forEach { key ->
                prefs.remove(key)
            }
        }
    }

    fun getLookingDraft(): Flow<LookingDraft?> {
        val p = "looking_"
        return context.discoveryDraftDataStore.data.map { prefs ->
            val hasDraft = prefs[stringPreferencesKey("${p}has_draft")] == "true"
            if (!hasDraft) {
                null
            } else {
                val tagsStr = prefs[stringPreferencesKey("${p}tags")].orEmpty()
                val tags = if (tagsStr.isBlank()) emptyList() else tagsStr.split("|||")
                LookingDraft(
                    city = prefs[stringPreferencesKey("${p}city")].orEmpty(),
                    lookingIn = prefs[stringPreferencesKey("${p}looking_in")].orEmpty(),
                    budget = prefs[stringPreferencesKey("${p}budget")].orEmpty(),
                    bio = prefs[stringPreferencesKey("${p}bio")].orEmpty(),
                    gender = prefs[stringPreferencesKey("${p}gender")] ?: "any",
                    tags = tags
                )
            }
        }
    }

    suspend fun saveLookingDraft(draft: LookingDraft) {
        val p = "looking_"
        context.discoveryDraftDataStore.edit { prefs ->
            prefs[stringPreferencesKey("${p}has_draft")] = "true"
            prefs[stringPreferencesKey("${p}city")] = draft.city
            prefs[stringPreferencesKey("${p}looking_in")] = draft.lookingIn
            prefs[stringPreferencesKey("${p}budget")] = draft.budget
            prefs[stringPreferencesKey("${p}bio")] = draft.bio
            prefs[stringPreferencesKey("${p}gender")] = draft.gender
            prefs[stringPreferencesKey("${p}tags")] = draft.tags.joinToString("|||")
        }
    }

    suspend fun clearLookingDraft() {
        val p = "looking_"
        context.discoveryDraftDataStore.edit { prefs ->
            val keysToRemove = prefs.asMap().keys.filter { it.name.startsWith(p) }
            keysToRemove.forEach { key ->
                prefs.remove(key)
            }
        }
    }
}
