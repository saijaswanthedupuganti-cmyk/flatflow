package habitiq.app.flats

import kotlin.random.Random

private const val FLAT_ID_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
private const val FLAT_ID_CODE_LENGTH = 4
private const val FLAT_ID_PREFIX = "FLAT-"
// \A/\z (not ^/$) -- in JVM regex, $ matches just before a trailing line terminator even
// without MULTILINE, so "FLAT-A3B9\n" would otherwise incorrectly pass validation.
private val FLAT_ID_PATTERN = Regex("\\A$FLAT_ID_PREFIX[$FLAT_ID_ALPHABET]{$FLAT_ID_CODE_LENGTH}\\z")

fun generateFlatId(random: Random = Random.Default): String {
    val code = (1..FLAT_ID_CODE_LENGTH)
        .map { FLAT_ID_ALPHABET[random.nextInt(FLAT_ID_ALPHABET.length)] }
        .joinToString("")
    return "$FLAT_ID_PREFIX$code"
}

fun isValidFlatIdFormat(flatId: String): Boolean = FLAT_ID_PATTERN.matches(flatId)
