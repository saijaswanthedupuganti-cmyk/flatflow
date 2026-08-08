package habitiq.app.flats

import kotlin.random.Random

private const val FLAT_ID_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
private const val FLAT_ID_CODE_LENGTH = 6
private const val LEGACY_FLAT_ID_CODE_LENGTH = 4 // pre-2026-08 flats; still valid, never regenerated
private const val FLAT_ID_PREFIX = "FLAT-"
// \A/\z (not ^/$) -- in JVM regex, $ matches just before a trailing line terminator even
// without MULTILINE, so "FLAT-A3B9\n" would otherwise incorrectly pass validation.
// Accepts both lengths: 4-char codes predate the 2026-08 entropy bump (32^4 =~ 1.05M
// combinations was cheaply enumerable) and remain valid forever since flat IDs never change;
// new flats generate 6-char codes (32^6 =~ 1.07B) going forward.
private val FLAT_ID_PATTERN = Regex(
    "\\A$FLAT_ID_PREFIX[$FLAT_ID_ALPHABET]{$LEGACY_FLAT_ID_CODE_LENGTH}\\z" +
        "|\\A$FLAT_ID_PREFIX[$FLAT_ID_ALPHABET]{$FLAT_ID_CODE_LENGTH}\\z"
)

fun generateFlatId(random: Random = Random.Default): String {
    val code = (1..FLAT_ID_CODE_LENGTH)
        .map { FLAT_ID_ALPHABET[random.nextInt(FLAT_ID_ALPHABET.length)] }
        .joinToString("")
    return "$FLAT_ID_PREFIX$code"
}

fun isValidFlatIdFormat(flatId: String): Boolean = FLAT_ID_PATTERN.matches(flatId)
