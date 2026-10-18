package habitiq.app.agent

/**
 * Acceptable-use gate for the agent. Anything the agent records lands in shared flat data, so it
 * refuses requests that log illegal goods or services, or that carry abuse or threats. Matching is
 * whole-word on normalised text, so "weeding" and "coke" stay fine.
 *
 * Keep the lists short and specific: a false refusal of a real chore costs more trust than it saves.
 * Ordinary household phrases that happen to contain a blocked word ("hash browns", "glue gun") are
 * removed before matching, so the rest of the sentence is still checked.
 * Hindi/Telugu abuse terms are maintained by Sai; add them to ABUSE_WORDS (lowercase, romanised).
 */
object ContentGuard {
    const val REFUSAL = "I can't help with that one. I'm here for flat stuff like groceries, bills and chores."

    private val ILLEGAL_WORDS = setOf(
        "ganja", "weed", "charas", "hash", "cocaine", "mdma", "ecstasy", "lsd", "heroin", "meth", "drugs",
        "pistol", "revolver", "gun", "bullets", "ammo",
        "bribe", "hawala", "satta", "matka", "escort", "escorts",
    )
    private val ABUSE_WORDS = setOf(
        "idiot", "stupid", "moron", "bastard", "bitch", "slut", "whore",
    )
    private const val PESTS = "cockroach(?:es)?|mosquito(?:es|s)?|rats?|mice|mouse|ants?|bugs?|bed ?bugs?|lizards?|flies|fly|pests?|termites?|germs?|weeds?"
    private val THREAT_PHRASES = listOf(
        Regex("""\bwill (?:kill|hurt|beat|slap)\b"""),
        Regex("""\b(?:kill|hurt|beat|slap) (?:him|her|you|them)\b"""),
        Regex("""\bbeat (?:him|her|them) up\b"""),
    )
    /** Everyday phrases that contain a blocked word. Stripped out before the checks above run. */
    private val HOUSEHOLD_PHRASES = listOf(
        Regex("""\bhash ?browns?\b"""),
        Regex("""\bweed (?:killer|remover|spray|cutter|trimmer)s?\b"""),
        Regex("""\b(?:glue|water|nerf|toy|massage|nail|staple|spray|heat|grease|soldering) guns?\b"""),
        Regex("""\bdrugs? (?:from|at) (?:the )?(?:pharmacy|chemist|medical(?: store| shop)?|apollo|medplus|hospital)\b"""),
        Regex("""\b(?:pharmacy|chemist|medical) drugs?\b"""),
        Regex("""\b(?:will )?(?:kill|killed|killing) (?:all |the |those |these |some )*(?:$PESTS)\b"""),
    )

    fun reasonToBlock(raw: String): String? {
        var text = normalizeUtterance(raw)
        HOUSEHOLD_PHRASES.forEach { text = it.replace(text, " ") }
        text = text.replace(Regex("""\s+"""), " ").trim()
        val words = text.split(' ').toSet()
        return when {
            words.any { it in ILLEGAL_WORDS } -> REFUSAL
            words.any { it in ABUSE_WORDS } -> REFUSAL
            THREAT_PHRASES.any { it.containsMatchIn(text) } -> REFUSAL
            else -> null
        }
    }
}
