package habitiq.app.lib

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

/** Formats stored INR amounts without silently discarding paise. */
fun formatInr(amount: Double): String {
    val decimal = BigDecimal.valueOf(abs(amount)).setScale(2, RoundingMode.HALF_UP)
    val parts = decimal.toPlainString().split('.')
    val whole = parts[0]
    val grouped = if (whole.length <= 3) {
        whole
    } else {
        val tail = whole.takeLast(3)
        val head = whole.dropLast(3)
            .reversed()
            .chunked(2)
            .joinToString(",")
            .reversed()
        "$head,$tail"
    }
    val fraction = parts.getOrNull(1).orEmpty()
    return "₹$grouped" + if (fraction == "00") "" else ".$fraction"
}

/**
 * Parses user-typed money into whole paise so split totals compare exactly, with no floating-point
 * drift. Returns null for blank, negative or non-numeric input; extra decimals round half-up.
 */
fun parsePaise(text: String): Long? =
    text.trim().replace(",", "").toBigDecimalOrNull()
        ?.takeIf { it.signum() >= 0 }
        ?.setScale(2, RoundingMode.HALF_UP)
        ?.movePointRight(2)
        ?.toLong()
