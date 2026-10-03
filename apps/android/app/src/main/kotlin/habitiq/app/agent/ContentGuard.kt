package habitiq.app.agent

/**
 * Acceptable-use gate for the agent. Anything the agent records lands in shared flat data, so it
 * refuses requests that log illegal goods or services, or that carry abuse or threats. Matching is
 * whole-word on normalised text, so "weeding" and "coke" stay fine.
 *
 * Keep the lists short and specific: a false refusal of a real chore costs more trust than it saves.
 * Hindi/Telugu abuse terms are maintained by Sai; add them to ABUSE_WORDS (lowercase, romanised).
 */
object ContentGuard {
    const val REFUSAL = "I can't help with that one. Oddroof is for shared home stuff like groceries, bills and chores."

    private val ILLEGAL_WORDS = setOf(
        "ganja", "weed", "charas", "hash", "cocaine", "mdma", "ecstasy", "lsd", "heroin", "meth", "drugs",
        "pistol", "revolver", "gun", "bullets", "ammo",
        "bribe", "hawala", "satta", "matka", "escort", "escorts",
    )
    private val ABUSE_WORDS = setOf(
        "idiot", "stupid", "moron", "bastard", "bitch", "slut", "whore",
    )
    private val THREAT_PHRASES = listOf(
        Regex("""\bwill (?:kill|hurt|beat|slap)\b"""),
        Regex("""\b(?:kill|hurt|beat|slap) (?:him|her|you|them)\b"""),
        Regex("""\bbeat (?:him|her|them) up\b"""),
    )

    fun reasonToBlock(raw: String): String? {
        val text = normalizeUtterance(raw)
        val words = text.split(' ').toSet()
        return when {
            words.any { it in ILLEGAL_WORDS } -> REFUSAL
            words.any { it in ABUSE_WORDS } -> REFUSAL
            THREAT_PHRASES.any { it.containsMatchIn(text) } -> REFUSAL
            else -> null
        }
    }
}
