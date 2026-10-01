package habitiq.app.discover

import habitiq.app.data.SeekerProfile
import habitiq.app.data.VacancyListing

/**
 * Explainable relevance order. Not an AI ranker.
 * Trust never excludes a listing by itself.
 */
object DiscoverRanking {
    fun sortVacancies(listings: List<VacancyListing>, filters: VacancyFilters, viewer: SeekerProfile?): List<VacancyListing> {
        return listings.sortedByDescending { listing ->
            scoreVacancy(listing, filters, viewer)
        }
    }

    fun sortSeekers(seekers: List<SeekerProfile>, filters: SeekerFilters, viewerCity: String?): List<SeekerProfile> {
        return seekers.sortedByDescending { seeker ->
            scoreSeeker(seeker, filters, viewerCity)
        }
    }

    private fun scoreVacancy(listing: VacancyListing, filters: VacancyFilters, viewer: SeekerProfile?): Int {
        var score = 0
        val query = filters.cityArea.trim()
        if (query.isNotBlank()) {
            if (listing.area.contains(query, true)) score += 40
            else if (listing.city.contains(query, true)) score += 28
        }
        val rent = listing.rentPerHead
        val min = filters.rentMin.toDoubleOrNull()
        val max = filters.rentMax.toDoubleOrNull()
        if (rent != null && min != null && max != null && rent in min..max) score += 22
        else if (rent != null && viewer != null && viewer.budget > 0 && rent <= viewer.budget) score += 16
        if (filters.roomType != "any" && listing.roomType == filters.roomType) score += 12
        score += Compatibility.vacancySignals(listing, viewer, filters).size * 4
        if (listing.memberCount > 0) score += 2
        return score
    }

    private fun scoreSeeker(seeker: SeekerProfile, filters: SeekerFilters, viewerCity: String?): Int {
        var score = 0
        val query = filters.cityArea.trim()
        if (query.isNotBlank()) {
            if (seeker.lookingIn.contains(query, true)) score += 40
            else if (seeker.city.contains(query, true)) score += 28
        }
        val min = filters.budgetMin.toDoubleOrNull()
        val max = filters.budgetMax.toDoubleOrNull()
        if (seeker.budget > 0 && (min == null || seeker.budget >= min) && (max == null || seeker.budget <= max)) {
            if (min != null || max != null) score += 18
        }
        score += Compatibility.seekerSignals(seeker, filters, viewerCity).size * 4
        return score
    }
}
