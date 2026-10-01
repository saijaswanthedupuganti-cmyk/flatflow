package habitiq.app.discover

import habitiq.app.data.SeekerProfile
import habitiq.app.data.VacancyListing
import java.time.Instant
import java.time.temporal.ChronoUnit

/** Filters for "Use a flat" — vacancies with an open room. */
data class VacancyFilters(
    val cityArea: String = "",
    val rentMin: String = "",
    val rentMax: String = "",
    val genderPreference: String = "any",
    val flatType: String = "any",
    val roomType: String = "any",
    val availabilityDays: Int? = null,
    val lifestyleTags: Set<String> = emptySet()
) {
    val activeCount: Int
        get() = listOfNotNull(
            cityArea.takeIf { it.isNotBlank() },
            rentMin.takeIf { it.isNotBlank() },
            rentMax.takeIf { it.isNotBlank() },
            genderPreference.takeIf { it != "any" },
            flatType.takeIf { it != "any" },
            roomType.takeIf { it != "any" },
            availabilityDays?.toString(),
            lifestyleTags.takeIf { it.isNotEmpty() }?.size?.toString()
        ).size
}

/** Filters for "Find a person" — seeker profiles. */
data class SeekerFilters(
    val cityArea: String = "",
    val budgetMin: String = "",
    val budgetMax: String = "",
    val gender: String = "any",
    val lifestyleTags: Set<String> = emptySet()
) {
    val activeCount: Int
        get() = listOfNotNull(
            cityArea.takeIf { it.isNotBlank() },
            budgetMin.takeIf { it.isNotBlank() },
            budgetMax.takeIf { it.isNotBlank() },
            gender.takeIf { it != "any" },
            lifestyleTags.takeIf { it.isNotEmpty() }?.size?.toString()
        ).size
}

object DiscoverFilterLogic {
    val vacancyGenderOptions = listOf("any" to "Anyone", "male" to "Male", "female" to "Female", "women_only" to "Women only")
    /** Matches create-flat wizard: apartment | house (flatType on flat doc). */
    val flatTypeOptions = listOf("any" to "Any type", "apartment" to "Apartment", "house" to "Independent house")
    val roomTypeOptions = listOf("any" to "Any", "private" to "Private room", "shared" to "Shared room")
    val availabilityOptions = listOf(null to "Any time", 7 to "Last 7 days", 30 to "Last 30 days", 90 to "Last 90 days")
    val seekerGenderOptions = listOf("any" to "Anyone", "male" to "Male", "female" to "Female", "other" to "Other")
    /** Common vacancy.lifestyle / customTags values (web VacancyListing schema). */
    val lifestyleTagOptions = listOf(
        "Vegetarian", "No Smoking", "No Alcohol", "Pet Friendly",
        "Work from Home", "Quiet", "Social", "Furnished", "AC", "WiFi"
    )

    fun applyVacancyFilters(listings: List<VacancyListing>, filters: VacancyFilters): List<VacancyListing> {
        val rentMin = filters.rentMin.toDoubleOrNull()
        val rentMax = filters.rentMax.toDoubleOrNull()
        val cutoff = filters.availabilityDays?.let { days ->
            Instant.now().minus(days.toLong(), ChronoUnit.DAYS)
        }

        return listings.filter { listing ->
            matchesCityArea(filters.cityArea, listing.city, listing.area) &&
                matchesRent(listing.rentPerHead, rentMin, rentMax) &&
                matchesGenderPreference(filters.genderPreference, listing.preferredGender) &&
                matchesFlatType(filters.flatType, listing.flatType) &&
                matchesRoomType(filters.roomType, listing.roomType) &&
                matchesAvailability(cutoff, listing.updatedAt) &&
                matchesVacancyTags(filters.lifestyleTags, listing.displayTags())
        }
    }

    fun applySeekerFilters(
        seekers: List<SeekerProfile>,
        filters: SeekerFilters,
        currentUserId: String,
        blockedIds: Set<String> = emptySet()
    ): List<SeekerProfile> {
        val budgetMin = filters.budgetMin.toDoubleOrNull()
        val budgetMax = filters.budgetMax.toDoubleOrNull()

        return seekers.filter { seeker ->
            seeker.id != currentUserId &&
                seeker.id !in blockedIds &&
                seeker.active &&
                matchesCityArea(filters.cityArea, seeker.city, seeker.lookingIn) &&
                matchesBudget(seeker.budget, budgetMin, budgetMax) &&
                matchesSeekerGender(filters.gender, seeker.gender) &&
                matchesLifestyleTags(filters.lifestyleTags, seeker.lifestyleTags)
        }
    }

    private fun matchesCityArea(query: String, vararg fields: String): Boolean {
        if (query.isBlank()) return true
        return fields.any { it.contains(query, ignoreCase = true) }
    }

    private fun matchesRent(rent: Double?, min: Double?, max: Double?): Boolean {
        if (rent == null) return min == null
        if (min != null && rent < min) return false
        if (max != null && rent > max) return false
        return true
    }

    private fun matchesBudget(budget: Double, min: Double?, max: Double?): Boolean {
        if (min != null && budget < min) return false
        if (max != null && budget > max) return false
        return true
    }

    private fun matchesGenderPreference(filter: String, listingGender: String): Boolean {
        if (filter == "any") return true
        val normalized = listingGender.lowercase().replace(" ", "_")
        return normalized == filter || normalized == "any" || normalized == "anyone"
    }

    private fun matchesSeekerGender(filter: String, seekerGender: String): Boolean {
        if (filter == "any") return true
        if (seekerGender.isBlank()) return true
        return seekerGender.lowercase() == filter
    }

    private fun matchesFlatType(filter: String, flatType: String): Boolean {
        if (filter == "any") return true
        if (flatType.isBlank()) return true
        return flatType.equals(filter, ignoreCase = true)
    }

    private fun matchesRoomType(filter: String, roomType: String?): Boolean {
        if (filter == "any") return true
        return roomType?.equals(filter, ignoreCase = true) == true
    }

    private fun matchesAvailability(cutoff: Instant?, updatedAt: String): Boolean {
        if (cutoff == null) return true
        if (updatedAt.isBlank()) return true
        return runCatching { Instant.parse(updatedAt) }.getOrNull()?.isAfter(cutoff) != false
    }

    private fun matchesLifestyleTags(required: Set<String>, lifestyleTags: String): Boolean {
        if (required.isEmpty()) return true
        val tags = lifestyleTags.split(",", ";").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        return required.all { req -> tags.any { it.contains(req.lowercase()) } }
    }

    private fun matchesVacancyTags(required: Set<String>, listingTags: List<String>): Boolean {
        if (required.isEmpty()) return true
        val normalized = listingTags.map { it.lowercase() }
        return required.all { req -> normalized.any { it.contains(req.lowercase()) } }
    }

    fun formatFlatType(value: String): String = when (value.lowercase()) {
        "apartment" -> "Apartment"
        "house" -> "Independent house"
        else -> value.replaceFirstChar { it.uppercase() }.ifBlank { "Flat" }
    }

    fun formatGenderPreference(value: String): String = when (value.lowercase().replace(" ", "_")) {
        "male" -> "Male preferred"
        "female", "women_only" -> "Women only"
        else -> "Any gender"
    }

    fun formatRoomType(roomType: String?, beds: Int): String = when (roomType?.lowercase()) {
        "private" -> "Private room"
        "shared" -> if (beds > 1) "Shared · $beds beds" else "Shared room"
        else -> "Room type not specified"
    }
}
