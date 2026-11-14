package com.example.bookshelf

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.bookshelf.data.UserPreferencesRepository

private const val PREFERENCES_NAME = "user_preferences"

// One DataStore instance per file for the whole process (creating two for the same file is an error).
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

class BookShelfApplication : Application() {
    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(dataStore)
    }
}
