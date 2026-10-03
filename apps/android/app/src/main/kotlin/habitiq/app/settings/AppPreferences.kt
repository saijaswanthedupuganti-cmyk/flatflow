package habitiq.app.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habitiq_prefs")

class AppPreferences(private val context: Context) {
    private val biometricKey = booleanPreferencesKey("biometric_lock_enabled")
    private val discoveryTrustConsentKey = booleanPreferencesKey("discovery_trust_consent")
    private val discoveryTrustPromptedKey = booleanPreferencesKey("discovery_trust_prompted")

    private val welcomeSeenKey = booleanPreferencesKey("welcome_slides_seen")

    /** The three welcome slides show once per install; after that, signed-out launches go straight to login. */
    val welcomeSeen: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[welcomeSeenKey] ?: false }

    suspend fun setWelcomeSeen() {
        context.dataStore.edit { prefs -> prefs[welcomeSeenKey] = true }
    }

    val isBiometricLockEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[biometricKey] ?: false }

    val discoveryTrustConsent: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[discoveryTrustConsentKey] ?: false }

    val discoveryTrustPrompted: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[discoveryTrustPromptedKey] ?: false }

    suspend fun setBiometricLockEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[biometricKey] = enabled }
    }

    suspend fun setDiscoveryTrustConsent(allowed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[discoveryTrustConsentKey] = allowed
            prefs[discoveryTrustPromptedKey] = true
        }
    }

    /** Flats this person bookmarked in Discover. Kept on the device, per account, never uploaded. */
    fun savedFlats(uid: String): Flow<Set<String>> = context.dataStore.data
        .map { prefs -> prefs[stringSetPreferencesKey("saved_flats_$uid")] ?: emptySet() }

    suspend fun setFlatSaved(uid: String, flatId: String, saved: Boolean) {
        if (uid.isBlank()) return
        context.dataStore.edit { prefs ->
            val key = stringSetPreferencesKey("saved_flats_$uid")
            val current = prefs[key] ?: emptySet()
            prefs[key] = if (saved) current + flatId else current - flatId
        }
    }

    /**
     * Which activity entries this person has seen: everything at or before [ActivitySeen.seenUpToMillis], plus
     * single entries opened since. Kept on the device per account; there is no server-side unread state.
     */
    fun activitySeen(uid: String): Flow<ActivitySeen> = context.dataStore.data.map { prefs ->
        ActivitySeen(
            seenUpToMillis = prefs[longPreferencesKey("activity_seen_$uid")] ?: 0L,
            readIds = prefs[stringSetPreferencesKey("activity_read_$uid")] ?: emptySet(),
        )
    }

    suspend fun markActivityRead(uid: String, id: String) {
        if (uid.isBlank()) return
        context.dataStore.edit { prefs ->
            val key = stringSetPreferencesKey("activity_read_$uid")
            // Bounded so the set cannot grow forever; the seen-up-to mark covers anything older.
            prefs[key] = ((prefs[key] ?: emptySet()) + id).toList().takeLast(200).toSet()
        }
    }

    suspend fun markAllActivityRead(uid: String, nowMillis: Long) {
        if (uid.isBlank()) return
        context.dataStore.edit { prefs ->
            prefs[longPreferencesKey("activity_seen_$uid")] = nowMillis
            prefs[stringSetPreferencesKey("activity_read_$uid")] = emptySet()
        }
    }
}

data class ActivitySeen(val seenUpToMillis: Long, val readIds: Set<String>)
