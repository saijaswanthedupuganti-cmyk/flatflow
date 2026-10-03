package habitiq.app.agent

fun editDistance(a: String, b: String): Int {
    var prev = IntArray(b.length + 1) { it }
    for (i in 1..a.length) {
        val cur = IntArray(b.length + 1)
        cur[0] = i
        for (j in 1..b.length) {
            val cost = if (a[i - 1] == b[j - 1]) 0 else 1
            cur[j] = minOf(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + cost)
        }
        prev = cur
    }
    return prev[b.length]
}

/**
 * Members whose first name is [word]. Exact matches win. With [fuzzy], names of 4+ letters also
 * match at edit distance 1. Callers pass fuzzy = false for the parser's own vocabulary, so "gave"
 * never turns into Dave.
 */
fun matchMembers(word: String, members: List<AgentMember>, fuzzy: Boolean): List<AgentMember> {
    val w = word.lowercase()
    val exact = members.filter { it.firstName.lowercase() == w }
    if (exact.isNotEmpty() || !fuzzy || w.length < 4) return exact
    return members.filter { m -> m.firstName.lowercase().let { it.length >= 4 && editDistance(it, w) <= 1 } }
}
