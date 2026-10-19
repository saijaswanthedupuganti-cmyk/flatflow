package habitiq.app.discover

import habitiq.app.data.VacancyListing
import java.time.LocalDate

/**
 * Plain-language facts for a room listing, shared by the card, the detail screen and the wizard's
 * live preview so all three say the same thing. Pure functions: no Compose, easy to test.
 */
object ListingFacts {
    const val IMMEDIATELY = "Immediately"

    val roomTypeOptions = listOf("private" to "Private room", "shared" to "Shared room")
    val furnishingOptions = listOf("furnished" to "Furnished", "semi_furnished" to "Semi-furnished", "unfurnished" to "Unfurnished")
    val noticeOptions = listOf("any" to "Flexible", "1_month" to "1 month", "2_months" to "2 months", "3_months" to "3 months")
    val occupationOptions = listOf("any" to "Anyone", "working_professional" to "Working", "student" to "Student")

    /** "Who it suits": three plain choices. A stored "women_only" reads as Women and is kept as it is. */
    val suitsOptions = listOf("any" to "Anyone", "female" to "Women", "male" to "Men")

    /** Things in the flat (vacancy.amenities). The first four are the most common and show first. */
    val amenityOptions = listOf(
        "WiFi", "AC", "Attached Bathroom", "Washing Machine",
        "Geyser", "Cook / Maid", "Parking", "Power Backup", "Gym", "Balcony",
    )

    /** House rules and habits (vacancy.lifestyle). Furnished / AC / WiFi live in their own fields here. */
    val houseRuleOptions = listOf("Vegetarian", "No Smoking", "Work from Home", "Quiet", "No Alcohol", "Pet Friendly", "Social")

    /** At most this many tags per listing, so cards and detail stay calm. */
    const val MAX_TAGS = 6

    fun suitsKey(stored: String?): String = when (stored?.lowercase()?.replace(" ", "_")) {
        "female", "women_only", "women" -> "female"
        "male", "men" -> "male"
        else -> "any"
    }

    fun suitsLabel(stored: String?): String = when (suitsKey(stored)) {
        "female" -> "Women"
        "male" -> "Men"
        else -> "Anyone"
    }

    fun roomTypeLabel(roomType: String?, beds: Int = 1): String? = when (roomType?.lowercase()) {
        "private" -> "Private room"
        "shared" -> if (beds > 1) "Shared room · $beds beds" else "Shared room"
        else -> null
    }

    fun furnishingLabel(value: String?): String? = furnishingOptions.firstOrNull { it.first == value?.lowercase() }?.second

    fun noticeLabel(value: String?): String? = when (value?.lowercase()) {
        null, "" -> null
        else -> noticeOptions.firstOrNull { it.first == value.lowercase() }?.second
    }

    fun occupationLabel(value: String?): String? = when (value?.lowercase()) {
        "working_professional" -> "Working professional"
        "student" -> "Student"
        else -> null
    }

    /** "Available now", "Available from 3 Nov", "Available this month", or null when unknown. */
    fun availabilityLabel(availableFrom: String?, moveInTiming: String?, today: LocalDate = LocalDate.now()): String? {
        val date = parseDate(availableFrom)
        return when {
            availableFrom.equals(IMMEDIATELY, ignoreCase = true) -> "Available now"
            date != null && !date.isAfter(today) -> "Available now"
            date != null -> "Available from ${date.dayOfMonth} ${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }}"
            moveInTiming == "immediate" -> "Available now"
            moveInTiming == "within_1_month" -> "Available this month"
            else -> null
        }
    }

    /** Smart default for the stored move-in timing, derived from the date (no extra question). */
    fun moveInTimingFor(availableFrom: String?, today: LocalDate = LocalDate.now()): String {
        val date = parseDate(availableFrom)
        return when {
            availableFrom.equals(IMMEDIATELY, ignoreCase = true) -> "immediate"
            date == null -> "within_1_month"
            !date.isAfter(today) -> "immediate"
            !date.isAfter(today.plusDays(31)) -> "within_1_month"
            else -> "flexible"
        }
    }

    /** "Private room · Furnished · Available now": the one facts line on a card. */
    fun factsLine(listing: VacancyListing, today: LocalDate = LocalDate.now()): String = listOfNotNull(
        roomTypeLabel(listing.roomType, listing.bedsAvailable),
        furnishingLabel(listing.furnishing),
        availabilityLabel(listing.availableFrom, listing.moveInTiming, today),
    ).joinToString(" · ")

    fun areaLine(listing: VacancyListing): String =
        listOf(listing.area, listing.city).map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString(", ")

    /**
     * Tags worth showing on a card: what the poster picked (house rules, then things in the flat).
     * Older listings only have generated custom tags; those are used with the facts already shown elsewhere
     * (room type, furnishing, area, member count, timing) taken out so nothing is said twice.
     */
    fun cardTags(listing: VacancyListing): List<String> {
        val picked = (listing.lifestyle + listing.amenities).map { it.trim() }.filter { it.isNotEmpty() }
        val source = picked.ifEmpty { listing.customTags.map { it.trim() } }
        val noise = setOfNotNull(
            listing.area.trim().lowercase().takeIf { it.isNotEmpty() },
            listing.city.trim().lowercase().takeIf { it.isNotEmpty() },
        )
        return source.filter { tag ->
            val t = tag.lowercase()
            t.isNotEmpty() && t !in noise &&
                !Regex("""^\d+ (room|rooms|bed|beds|current member|current members)$""").matches(t) &&
                t !in setOf("private room", "shared room", "furnished", "semi_furnished", "semi-furnished", "unfurnished",
                    "immediate", "within 1 month", "flexible", "any gender")
        }.distinctBy { it.lowercase() }
    }

    private fun parseDate(value: String?): LocalDate? = value?.takeIf { it.length >= 10 }?.let { runCatching { LocalDate.parse(it.take(10)) }.getOrNull() }
}
