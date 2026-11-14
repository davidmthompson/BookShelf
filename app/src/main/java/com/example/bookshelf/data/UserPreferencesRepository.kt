package com.example.bookshelf.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * The only class that touches DataStore. The rest of the app reads [userPreferences]
 * and calls the suspend update functions.
 */
class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val READ_FILTER = stringPreferencesKey("read_filter")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val HIDE_LOW_RATED = booleanPreferencesKey("hide_low_rated")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    /**
     * Emits the current preferences, then a new value every time they change.
     * On a fresh install no keys exist, so every field falls back to its default.
     */
    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { e ->
            // A disk read problem shouldn't crash the app: show the defaults instead.
            // Anything else is a real bug, so let it propagate.
            if (e is IOException) {
                Log.e(TAG, "Error reading preferences, using defaults", e)
                emit(emptyPreferences())
            } else {
                throw e
            }
        }
        .map(::toUserPreferences)

    suspend fun setReadFilter(filter: ReadFilter) {
        dataStore.edit { it[Keys.READ_FILTER] = filter.name }
    }

    suspend fun setSortOrder(order: SortOrder) {
        dataStore.edit { it[Keys.SORT_ORDER] = order.name }
    }

    suspend fun setHideLowRated(hide: Boolean) {
        dataStore.edit { it[Keys.HIDE_LOW_RATED] = hide }
    }

    suspend fun setDarkTheme(dark: Boolean) {
        dataStore.edit { it[Keys.DARK_THEME] = dark }
    }

    private fun toUserPreferences(prefs: Preferences): UserPreferences {
        val defaults = UserPreferences()
        return UserPreferences(
            readFilter = prefs[Keys.READ_FILTER].toEnumOr(defaults.readFilter),
            sortOrder = prefs[Keys.SORT_ORDER].toEnumOr(defaults.sortOrder),
            hideLowRated = prefs[Keys.HIDE_LOW_RATED] ?: defaults.hideLowRated,
            darkTheme = prefs[Keys.DARK_THEME] ?: defaults.darkTheme,
        )
    }

    private companion object {
        const val TAG = "UserPrefsRepo"
    }
}

/** Unknown or missing names (e.g. from an older app version) fall back to the default. */
private inline fun <reified T : Enum<T>> String?.toEnumOr(default: T): T =
    enumValues<T>().firstOrNull { it.name == this } ?: default
