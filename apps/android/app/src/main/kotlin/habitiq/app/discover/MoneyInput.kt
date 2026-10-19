package habitiq.app.discover

/**
 * Forgiving rupee input (Postel's law): people type rent the way they say it.
 * "8000", "8,000", "₹8,000", "8k", "8.5K", "1.2L", "1 lakh", "8000/month" all parse.
 * Returns whole rupees, or null when there is no usable number.
 */
object MoneyInput {
    private val pattern = Regex("""^(\d+(?:\.\d+)?)\s*(k|thousand|l|lac|lakh|lakhs)?$""")

    fun parse(raw: String): Long? {
        val cleaned = raw.lowercase()
            .replace("₹", "")
            .replace("rs.", "")
            .replace("rs", "")
            .replace("inr", "")
            .replace(",", "")
            .replace("_", "")
            .substringBefore("/")
            .replace("per month", "")
            .replace("a month", "")
            .replace("pm", "")
            .trim()
        if (cleaned.isEmpty()) return null
        val match = pattern.matchEntire(cleaned) ?: return null
        val number = match.groupValues[1].toDoubleOrNull() ?: return null
        val multiplier = when (match.groupValues[2]) {
            "k", "thousand" -> 1_000.0
            "l", "lac", "lakh", "lakhs" -> 100_000.0
            else -> 1.0
        }
        val value = Math.round(number * multiplier)
        return value.takeIf { it in 0..99_999_999 }
    }

    /** Normalises what was typed to plain digits ("8k" -> "8000"); leaves unparseable text as it is. */
    fun normalise(raw: String): String = parse(raw)?.toString() ?: raw.trim()
}
