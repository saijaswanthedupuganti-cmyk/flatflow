package habitiq.app.agent

import kotlin.math.max
import kotlin.math.min

/**
 * Meaning-based matching for flat searches, on the phone. Speech mangles area names ("punja gutta",
 * "kondapuram"), people say "veg" for vegetarian and "wifi" for internet, so exact substring matching
 * isn't enough. Gemini handles the bigger leaps (a misheard name to the right area, which areas are
 * near which); this keeps the final filtering and ranking on real listing data.
 */
object FlatSearch {

    /** Lowercase letters only, so "Punja-gutta" and "punjagutta" compare equal. */
    fun squash(s: String): String = s.lowercase().filter { it in 'a'..'z' }

    /** 0..1: how likely two place names are the same place. Handles spacing, typos and partial names. */
    fun placeSimilarity(a: String, b: String): Double {
        val x = squash(a)
        val y = squash(b)
        if (x.isEmpty() || y.isEmpty()) return 0.0
        if (x == y) return 1.0
        // "kondapur" inside "kondapur main road", or "hitech" inside "hitechcity".
        if (min(x.length, y.length) >= 4 && (x.contains(y) || y.contains(x))) return 0.95
        val d = levenshtein(x, y)
        return 1.0 - d.toDouble() / max(x.length, y.length)
    }

    private const val SAME_PLACE = 0.78

    /** True when [place] names this listing's area or city, allowing for speech typos. */
    fun inPlace(v: AgentVacancy, place: String): Boolean {
        if (placeSimilarity(v.area, place) >= SAME_PLACE) return true
        if (placeSimilarity(v.city, place) >= 0.9) return true
        // Multi-word areas: "kukatpally housing board" matches "kukatpally".
        return v.area.split(' ', ',', '-').any { it.length >= 4 && placeSimilarity(it, place) >= 0.85 }
    }

    /** True when any listing is in [place]; used to decide whether the rules can answer or Gemini should. */
    fun knows(state: HouseholdState, place: String): Boolean = state.vacancies.any { inPlace(it, place) }

    /** The listing's own spelling of [place], for friendly headlines ("Punjagutta", not "punja gutta"). */
    fun displayName(state: HouseholdState, place: String): String =
        state.vacancies.firstOrNull { placeSimilarity(it.area, place) >= SAME_PLACE }?.area?.trim()
            ?: place.trim().split(' ').joinToString(" ") { w -> w.replaceFirstChar(Char::uppercase) }

    /** Words people use for the same thing. Keys are what Gemini or the rules produce; values are what listings say. */
    private val SYNONYMS: Map<String, List<String>> = mapOf(
        "veg" to listOf("veg", "vegetarian", "pure veg"),
        "vegetarian" to listOf("veg", "vegetarian", "pure veg"),
        "non veg" to listOf("non veg", "nonveg", "non vegetarian"),
        "wifi" to listOf("wifi", "wi fi", "internet", "broadband"),
        "internet" to listOf("wifi", "wi fi", "internet", "broadband"),
        "ac" to listOf("ac", "air conditioned", "air conditioner", "air conditioning"),
        "furnished" to listOf("furnished", "fully furnished"),
        "semi furnished" to listOf("semi furnished", "semifurnished"),
        "parking" to listOf("parking", "bike parking", "car parking"),
        "pets" to listOf("pet", "pets", "pet friendly"),
        "food" to listOf("food", "meals", "cook", "tiffin"),
        "cook" to listOf("cook", "meals", "food"),
        "maid" to listOf("maid", "housekeeping", "cleaning"),
        "gym" to listOf("gym", "fitness"),
        "washing machine" to listOf("washing machine", "washer", "laundry"),
        "private room" to listOf("private", "single", "own room"),
        "single" to listOf("private", "single", "own room"),
        "sharing" to listOf("sharing", "shared", "twin", "double"),
        "non smoker" to listOf("non smoker", "no smoking", "nonsmoker"),
        "no drinking" to listOf("no drinking", "no alcohol", "non drinker"),
        "working professionals" to listOf("working", "professional", "professionals", "employee"),
        "students" to listOf("student", "students", "college"),
        "quiet" to listOf("quiet", "peaceful", "calm"),
        "metro" to listOf("metro"),
        "power backup" to listOf("power backup", "inverter", "generator"),
        "lift" to listOf("lift", "elevator"),
    )

    /** Everything searchable about a listing as one normalised string. */
    private fun text(v: AgentVacancy): String =
        (listOf(v.flatName, v.area, v.roomType, v.furnishing, v.flatType, v.about) + v.tags)
            .joinToString(" ") { normalizeUtterance(it) }

    /** How many of the person's wishes this listing meets. "unfurnished" never counts as "furnished". */
    fun wantScore(v: AgentVacancy, wants: List<String>): Int {
        if (wants.isEmpty()) return 0
        val body = " ${text(v)} "
        return wants.count { want ->
            val key = normalizeUtterance(want)
            (SYNONYMS[key] ?: listOf(key)).any { term ->
                val hit = Regex("""(?<![a-z])${Regex.escape(term)}(?![a-z])""").find(body) ?: return@any false
                // Negated forms: "unfurnished", "non veg" for "veg", "no pets" for "pets".
                val before = body.substring(0, hit.range.first)
                !(before.endsWith("un") || (term != "non veg" && before.endsWith("non ")) || before.endsWith("no "))
            }
        }
    }

    private fun levenshtein(a: String, b: String): Int {
        var prev = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            val cur = IntArray(b.length + 1)
            cur[0] = i
            for (j in 1..b.length) {
                cur[j] = minOf(cur[j - 1] + 1, prev[j] + 1, prev[j - 1] + if (a[i - 1] == b[j - 1]) 0 else 1)
            }
            prev = cur
        }
        return prev[b.length]
    }
}
