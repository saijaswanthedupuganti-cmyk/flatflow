package habitiq.app.flats

import kotlin.random.Random

/**
 * Invite codes are separate from flat ids (security audit 18 Oct). A flat id is visible to anyone who
 * opens a Discover listing, so it can't be the secret that lets someone in. Each flat gets a random code
 * at `inviteCodes/{code}`; joining directly needs that code, and firestore.rules checks it.
 *
 * Same alphabet as flat ids (no 0/O/1/I), no prefix, so a code is six characters a person can read out.
 */
private const val INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
const val INVITE_CODE_LENGTH = 6
private val INVITE_CODE_PATTERN = Regex("\\A[$INVITE_ALPHABET]{$INVITE_CODE_LENGTH}\\z")

fun generateInviteCode(random: Random = Random.Default): String =
    (1..INVITE_CODE_LENGTH).map { INVITE_ALPHABET[random.nextInt(INVITE_ALPHABET.length)] }.joinToString("")

/** What a person typed into "Join a flat". */
sealed interface InviteInput {
    data class Code(val code: String) : InviteInput
    /** An old `FLAT-XXXX` code (the flat id). Still accepted, but only as a request the admin approves. */
    data class LegacyFlatId(val flatId: String) : InviteInput
    data object Invalid : InviteInput
}

/** Forgiving about case, spaces and dashes inside a code ("k7q-m2p"), strict about the characters. */
fun parseInviteInput(raw: String): InviteInput {
    val upper = raw.trim().uppercase()
    if (isValidFlatIdFormat(upper)) return InviteInput.LegacyFlatId(upper)
    val compact = upper.filterNot { it == ' ' || it == '-' }
    return if (INVITE_CODE_PATTERN.matches(compact)) InviteInput.Code(compact) else InviteInput.Invalid
}
