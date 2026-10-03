package habitiq.app.agent

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Lowercases and strips punctuation while keeping ₹, decimal points and thousands commas that sit
 * between digits. Apostrophes are dropped so "what's" becomes "whats".
 */
fun normalizeUtterance(raw: String): String = raw.lowercase()
    .replace("₹", " ₹")
    .replace("'", "").replace("’", "")
    .replace(Regex("""(?<!\d)\.|\.(?!\d)"""), " ")
    .replace(Regex("""(?<!\d),|,(?!\d)"""), " ")
    .replace(Regex("""[^a-z0-9₹., ]"""), " ")
    .replace(Regex("""\s+"""), " ")
    .trim()

data class AmountMatch(val paise: Long, val range: IntRange)

private val NUMERIC = Regex("""(?:₹\s*|\b(?:rs|inr)\s*)?(\d[\d,]*(?:\.\d+)?)(?:\s*(k|lakhs?|lacs?|rupees|rs|bucks)\b)?""")

private val MONTHS = setOf(
    "jan", "january", "feb", "february", "mar", "march", "apr", "april", "may", "jun", "june", "jul", "july",
    "aug", "august", "sep", "sept", "september", "oct", "october", "nov", "november", "dec", "december",
)

/** Words after a number that mean it isn't money. */
private val NON_MONEY_UNITS = MONTHS + setOf(
    "am", "pm", "st", "nd", "rd", "th", "day", "days", "week", "weeks", "month", "months", "year", "years",
    "people", "persons", "members", "times", "hrs", "hours", "mins", "minutes", "bhk", "sharing", "beds", "bed",
)

private val UNITS = mapOf(
    "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5, "six" to 6, "seven" to 7, "eight" to 8,
    "nine" to 9, "ten" to 10, "eleven" to 11, "twelve" to 12, "thirteen" to 13, "fourteen" to 14,
    "fifteen" to 15, "sixteen" to 16, "seventeen" to 17, "eighteen" to 18, "nineteen" to 19,
    "twenty" to 20, "thirty" to 30, "forty" to 40, "fifty" to 50, "sixty" to 60, "seventy" to 70,
    "eighty" to 80, "ninety" to 90,
)
private val SCALES = setOf("hundred", "thousand", "lakh", "lakhs")
private val CURRENCY_WORDS = setOf("rupees", "rs", "bucks")

private fun multiplier(suffix: String?): BigDecimal = when {
    suffix == null -> BigDecimal.ONE
    suffix == "k" -> BigDecimal(1_000)
    suffix.startsWith("lakh") || suffix.startsWith("lac") -> BigDecimal(100_000)
    else -> BigDecimal.ONE
}

private fun toPaise(value: BigDecimal): Long = value.movePointRight(2).setScale(0, RoundingMode.HALF_UP).toLong()

private fun wordAfter(text: String, end: Int): String? =
    Regex("""[a-z]+""").find(text, end + 1)?.takeIf { text.substring(end + 1, it.range.first).isBlank() }?.value

private fun wordBefore(text: String, start: Int): String? =
    Regex("""[a-z]+""").findAll(text.substring(0, start)).lastOrNull()
        ?.takeIf { text.substring(it.range.last + 1, start).isBlank() }?.value

/** Every money amount in [text] (already passed through [normalizeUtterance]), in reading order. */
fun findAmounts(text: String): List<AmountMatch> {
    val found = mutableListOf<AmountMatch>()
    for (m in NUMERIC.findAll(text)) {
        val digits = m.groupValues[1].replace(",", "")
        val suffix = m.groupValues[2].ifEmpty { null }
        if (suffix == null && (wordAfter(text, m.range.last) in NON_MONEY_UNITS || wordBefore(text, m.range.first) in MONTHS)) continue
        val value = digits.toBigDecimalOrNull() ?: continue
        found += AmountMatch(toPaise(value * multiplier(suffix)), m.range)
    }
    found += wordAmounts(text)
    return found.sortedBy { it.range.first }
}

private fun wordAmounts(text: String): List<AmountMatch> {
    val tokens = Regex("""[a-z]+""").findAll(text).toList()
    val out = mutableListOf<AmountMatch>()
    var i = 0
    while (i < tokens.size) {
        // A spoken number starts on a unit word; a bare scale ("lakh" in "1.2 lakh") belongs to the digits before it.
        if (tokens[i].value !in UNITS) { i++; continue }
        var total = 0L
        var current = 0L
        var j = i
        var last = tokens[i]
        while (j < tokens.size) {
            val w = tokens[j].value
            val adjacent = j == i || text.substring(tokens[j - 1].range.last + 1, tokens[j].range.first).isBlank()
            if (!adjacent) break
            when {
                w in UNITS -> current += UNITS.getValue(w)
                w == "hundred" -> current = (if (current == 0L) 1 else current) * 100
                w == "thousand" -> { total += (if (current == 0L) 1 else current) * 1_000; current = 0 }
                w == "lakh" || w == "lakhs" -> { total += (if (current == 0L) 1 else current) * 100_000; current = 0 }
                w == "and" && j + 1 < tokens.size && tokens[j + 1].value in UNITS -> Unit
                else -> break
            }
            last = tokens[j]
            j++
        }
        var end = last.range.last
        if (j < tokens.size && tokens[j].value in CURRENCY_WORDS &&
            text.substring(last.range.last + 1, tokens[j].range.first).isBlank()) end = tokens[j].range.last
        val value = total + current
        if (value >= 10) out += AmountMatch(value * 100, tokens[i].range.first..end)
        i = j.coerceAtLeast(i + 1)
    }
    return out
}
