package habitiq.app.discover

/**
 * Feature flags for Discovery. Keep Discovery additive and reversible:
 * turning a flag off hides the surface without touching Tasks / Expenses / Settlements.
 *
 * Looking posts and Find Flatmate are on because current product direction
 * ships them in the APK; they remain switchable.
 */
object DiscoverFlags {
    const val ENABLED = true
    const val FIND_FLATMATE = true
    const val LOOKING_POSTS = true
    const val CONNECTION_REQUESTS = true
    const val QUALITATIVE_TRUST = true
    /** Phase 2 — Google Maps / Places / routing are not in this APK. Keep false. */
    const val DISTANCE_INTELLIGENCE = false
}
