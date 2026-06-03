package dev.mkao.weaver.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Flow-based replacement for the legacy synchronous `AppPreferencesManager`
 * (SharedPreferences). Persists the user's news edition (language + country)
 * transactionally via DataStore.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val COUNTRY_CODE = stringPreferencesKey("country_code")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val PREFERRED_TOPICS = stringSetPreferencesKey("preferred_topics")
        val PREFERRED_SOURCES = stringSetPreferencesKey("preferred_sources")
        val PREFERRED_LAYOUT = stringPreferencesKey("preferred_layout")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val PUSH_NOTIFICATIONS = booleanPreferencesKey("push_notifications")
        val BLOCKED_SOURCES = stringSetPreferencesKey("blocked_sources")
        val FAVORITE_VIDEO_IDS = stringSetPreferencesKey("favorite_video_ids")
    }

    private val legacyPrefs =
        context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)

    val languageCode: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.LANGUAGE_CODE] ?: migrateLegacyLanguage() ?: DEFAULT_LANGUAGE
    }

    val countryCode: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.COUNTRY_CODE] ?: migrateLegacyCountry() ?: DEFAULT_COUNTRY
    }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }

    val preferredTopics: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[Keys.PREFERRED_TOPICS] ?: emptySet()
    }

    val preferredSources: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[Keys.PREFERRED_SOURCES] ?: emptySet()
    }

    val preferredLayout: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.PREFERRED_LAYOUT] ?: "default"
    }

    val isDarkMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.DARK_MODE] ?: true
    }

    val isPushNotificationsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.PUSH_NOTIFICATIONS] ?: true
    }

    val blockedSources: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[Keys.BLOCKED_SOURCES] ?: emptySet()
    }

    val favoriteVideoIds: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[Keys.FAVORITE_VIDEO_IDS] ?: emptySet()
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setPreferredTopics(topics: Set<String>) {
        dataStore.edit { it[Keys.PREFERRED_TOPICS] = topics }
    }

    suspend fun setPreferredSources(sources: Set<String>) {
        dataStore.edit { it[Keys.PREFERRED_SOURCES] = sources }
    }

    suspend fun setPreferredLayout(layout: String) {
        dataStore.edit { it[Keys.PREFERRED_LAYOUT] = layout }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setPushNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.PUSH_NOTIFICATIONS] = enabled }
    }

    suspend fun blockSource(sourceName: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.BLOCKED_SOURCES] ?: emptySet()
            prefs[Keys.BLOCKED_SOURCES] = current + sourceName
        }
    }

    suspend fun unblockSource(sourceName: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.BLOCKED_SOURCES] ?: emptySet()
            prefs[Keys.BLOCKED_SOURCES] = current - sourceName
        }
    }

    suspend fun toggleFavoriteVideo(videoId: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_VIDEO_IDS] ?: emptySet()
            if (current.contains(videoId)) {
                prefs[Keys.FAVORITE_VIDEO_IDS] = current - videoId
            } else {
                prefs[Keys.FAVORITE_VIDEO_IDS] = current + videoId
            }
        }
    }

    suspend fun setEdition(languageCode: String, countryCode: String) {
        dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE_CODE] = languageCode
            prefs[Keys.COUNTRY_CODE] = countryCode
        }
    }

    suspend fun setLanguageCode(languageCode: String) {
        dataStore.edit { it[Keys.LANGUAGE_CODE] = languageCode }
    }

    suspend fun setCountryCode(countryCode: String) {
        dataStore.edit { it[Keys.COUNTRY_CODE] = countryCode }
    }

    // One-shot migration from the old SharedPreferences file so existing users
    // keep their edition across the upgrade.
    private fun migrateLegacyLanguage(): String? =
        legacyPrefs.getString("language_code", null)

    private fun migrateLegacyCountry(): String? =
        legacyPrefs.getString("country_code", null)

    companion object {
        private const val LEGACY_PREFS_NAME = "user_preferences"
        private const val DEFAULT_LANGUAGE = "en"
        private const val DEFAULT_COUNTRY = "us"
    }
}
