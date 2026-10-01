package habitiq.app.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "habitiq_prefs")

class AppPreferences(private val context: Context) {
    private val biometricKey = booleanPreferencesKey("biometric_lock_enabled")
    private val discoveryTrustConsentKey = booleanPreferencesKey("discovery_trust_consent")
    private val discoveryTrustPromptedKey = booleanPreferencesKey("discovery_trust_prompted")

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
}
