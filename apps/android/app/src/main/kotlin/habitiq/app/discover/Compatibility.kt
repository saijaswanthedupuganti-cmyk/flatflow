package habitiq.app.discover

import habitiq.app.data.SeekerProfile
import habitiq.app.data.VacancyListing

/**
 * Compatibility is "why you may fit" from real overlapping fields.
 * Does not use company, college, village, or any field that is not on the document.
 * Never mixed with trust.
 */
object Compatibility {
    fun vacancySignals(listing: VacancyListing, viewer: SeekerProfile?, filters: VacancyFilters): List<CompatibilitySignal> {
        val out = mutableListOf<CompatibilitySignal>()
        val query = filters.cityArea.trim()
        if (query.isNotBlank() && listOf(listing.city, listing.area).any { it.contains(query, ignoreCase = true) }) {
            out += CompatibilitySignal("location", "In ${listing.area.ifBlank { listing.city }.ifBlank { "your search area" }}")
        } else if (viewer != null && citiesOverlap(viewer, listing.city, listing.area)) {
            out += CompatibilitySignal("location", "Same city / area as your looking post")
        }

        val rent = listing.rentPerHead
        if (rent != null) {
            val min = filters.rentMin.toDoubleOrNull() ?: viewer?.budget
            val max = filters.rentMax.toDoubleOrNull() ?: viewer?.budget
            if (min != null && max != null && rent in min.coerceAtMost(max)..max.coerceAtLeast(min)) {
                out += CompatibilitySignal("budget", "Within your budget")
            } else if (viewer != null && viewer.budget > 0 && rent <= viewer.budget) {
                out += CompatibilitySignal("budget", "Rent is within your looking budget")
            }
        }

        if (filters.roomType == "private" && listing.roomType == "private") {
            out += CompatibilitySignal("room", "Private room")
        } else if (filters.roomType == "shared" && listing.roomType == "shared") {
            out += CompatibilitySignal("room", "Shared room")
        }

        val listingTags = listing.displayTags().map { it.lowercase() }
        val wanted = filters.lifestyleTags.ifEmpty {
            viewer?.lifestyleTags.orEmpty().split(",", ";").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        }
        val matchedTags = wanted.filter { req -> listingTags.any { it.contains(req.lowercase()) } }
        matchedTags.take(3).forEach { tag ->
            out += CompatibilitySignal("tag_$tag", tag)
        }
        return out.distinctBy { it.id }
    }

    fun seekerSignals(seeker: SeekerProfile, filters: SeekerFilters, viewerFlatCity: String?): List<CompatibilitySignal> {
        val out = mutableListOf<CompatibilitySignal>()
        val query = filters.cityArea.trim()
        if (query.isNotBlank() && listOf(seeker.city, seeker.lookingIn).any { it.contains(query, ignoreCase = true) }) {
            out += CompatibilitySignal("location", "Looking in ${seeker.lookingIn.ifBlank { seeker.city }}")
        } else if (!viewerFlatCity.isNullOrBlank() &&
            (seeker.city.contains(viewerFlatCity, true) || seeker.lookingIn.contains(viewerFlatCity, true))
        ) {
            out += CompatibilitySignal("location", "Interested in your city")
        }

        val min = filters.budgetMin.toDoubleOrNull()
        val max = filters.budgetMax.toDoubleOrNull()
        if (seeker.budget > 0 && (min == null || seeker.budget >= min) && (max == null || seeker.budget <= max) && (min != null || max != null)) {
            out += CompatibilitySignal("budget", "Budget fits your range")
        }

        val seekerTags = seeker.lifestyleTags.split(",", ";").map { it.trim() }.filter { it.isNotEmpty() }
        filters.lifestyleTags.forEach { req ->
            if (seekerTags.any { it.contains(req, ignoreCase = true) }) {
                out += CompatibilitySignal("tag_$req", req)
            }
        }
        return out.distinctBy { it.id }
    }

    private fun citiesOverlap(viewer: SeekerProfile, city: String, area: String): Boolean {
        val fields = listOf(viewer.city, viewer.lookingIn).filter { it.isNotBlank() }
        return fields.any { f ->
            city.contains(f, true) || area.contains(f, true) || f.contains(city, true) || f.contains(area, true)
        }
    }
}
