package habitiq.app.discover

import habitiq.app.data.SeekerProfile
import habitiq.app.data.VacancyListing
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Filters for "Find a flat": vacancies with an open room. */
data class VacancyFilters(
    val cityArea: String = "",
    val rentMin: String = "",
    val rentMax: String = "",
    val genderPreference: String = "any",
    val flatType: String = "any",
    val roomType: String = "any",
    val availabilityDays: Int? = null,
    val lifestyleTags: Set<String> = emptySet(),
    /** "any" | "furnished" | "semi_furnished" | "unfurnished". */
    val furnishing: String = "any",
    /** When the room is free: "any" | "now" | "month" (within about 30 days) | "later". */
    val available: String = "any",
) {
    /** Filters set in the sheet. The typed search ([cityArea]) is not counted: it is visible in the search field. */
    val activeCount: Int
        get() = listOfNotNull(
            (rentMin.isNotBlank() || rentMax.isNotBlank()).takeIf { it },
            genderPreference.takeIf { it != "any" },
            flatType.takeIf { it != "any" },
            roomType.takeIf { it != "any" },
            availabilityDays?.toString(),
            furnishing.takeIf { it != "any" },
            available.takeIf { it != "any" },
        ).size + lifestyleTags.size
}

/** Filters for "Find a flatmate": seeker profiles. */
data class SeekerFilters(
    val cityArea: String = "",
    val budgetMin: String = "",
    val budgetMax: String = "",
    val gender: String = "any",
    val lifestyleTags: Set<String> = emptySet()
) {
    /** Filters set in the sheet; the typed search is visible in the field and not counted. */
    val activeCount: Int
        get() = listOfNotNull(
            (budgetMin.isNotBlank() || budgetMax.isNotBlank()).takeIf { it },
            gender.takeIf { it != "any" },
        ).size + lifestyleTags.size
}

/** One-tap budget range for the filter sheet (plain rupee strings; blank = open-ended). */
data class BudgetPreset(val label: String, val min: String, val max: String)

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

    // Filter sheet choices: most-used first, at most five per question.
    val budgetPresets = listOf(
        BudgetPreset("Under ₹8k", "", "8000"),
        BudgetPreset("₹8–12k", "8000", "12000"),
        BudgetPreset("₹12–18k", "12000", "18000"),
        BudgetPreset("₹18k+", "18000", ""),
    )
    val roomFilterOptions = listOf("any" to "Any", "private" to "Private", "shared" to "Shared")
    val suitsFilterOptions = listOf("any" to "Anyone", "female" to "Women", "male" to "Men")
    val availableOptions = listOf("any" to "Any time", "now" to "Now", "month" to "This month", "later" to "Later")
    val furnishingFilterOptions = listOf("any" to "Any") + ListingFacts.furnishingOptions

    /** "₹8k–12k", "Under ₹8k", "₹18k+": a short label for an applied budget. */
    fun budgetLabel(min: String, max: String): String {
        fun k(v: String): String {
            val n = MoneyInput.parse(v) ?: return v.trim()
            return when {
                n >= 1000 && n % 1000 == 0L -> "${n / 1000}k"
                n >= 1000 -> String.format(java.util.Locale.US, "%.1fk", n / 1000.0)
                else -> n.toString()
            }
        }
        return when {
            min.isNotBlank() && max.isNotBlank() -> "₹${k(min)}–${k(max)}"
            max.isNotBlank() -> "Under ₹${k(max)}"
            min.isNotBlank() -> "₹${k(min)}+"
            else -> "Any budget"
        }
    }

    fun applyVacancyFilters(listings: List<VacancyListing>, filters: VacancyFilters): List<VacancyListing> {
        val rentMin = MoneyInput.parse(filters.rentMin)?.toDouble()
        val rentMax = MoneyInput.parse(filters.rentMax)?.toDouble()
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
                matchesFurnishing(filters.furnishing, listing.furnishing) &&
                matchesAvailableWhen(filters.available, listing.availableFrom, listing.moveInTiming) &&
                matchesVacancyTags(filters.lifestyleTags, listing.displayTags() + listing.amenities)
        }
    }

    fun applySeekerFilters(
        seekers: List<SeekerProfile>,
        filters: SeekerFilters,
        currentUserId: String,
        blockedIds: Set<String> = emptySet()
    ): List<SeekerProfile> {
        val budgetMin = MoneyInput.parse(filters.budgetMin)?.toDouble()
        val budgetMax = MoneyInput.parse(filters.budgetMax)?.toDouble()

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
        val q = query.trim()
        return fields.any { it.contains(q, ignoreCase = true) }
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
        if (normalized == filter || normalized == "any" || normalized == "anyone" || normalized.isBlank()) return true
        // "Women only" listings are for women, so the Women filter finds them too.
        return filter == "female" && normalized == "women_only"
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

    private fun matchesFurnishing(filter: String, furnishing: String): Boolean {
        if (filter == "any") return true
        return furnishing.equals(filter, ignoreCase = true)
    }

    /** Listings with neither a date nor a timing match every option instead of silently disappearing. */
    internal fun matchesAvailableWhen(filter: String, availableFrom: String, moveInTiming: String, today: LocalDate = LocalDate.now()): Boolean {
        if (filter == "any") return true
        if (availableFrom.isBlank() && moveInTiming.isBlank()) return true
        val date = availableFrom.takeIf { it.length >= 10 }?.let { runCatching { LocalDate.parse(it.take(10)) }.getOrNull() }
        val now = availableFrom.equals(ListingFacts.IMMEDIATELY, ignoreCase = true) ||
            (date != null && !date.isAfter(today)) ||
            (date == null && moveInTiming == "immediate")
        val withinMonth = now ||
            (date != null && !date.isAfter(today.plusDays(31))) ||
            (date == null && moveInTiming == "within_1_month")
        return when (filter) {
            "now" -> now
            "month" -> withinMonth
            "later" -> !withinMonth || (date == null && moveInTiming == "flexible")
            else -> true
        }
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
