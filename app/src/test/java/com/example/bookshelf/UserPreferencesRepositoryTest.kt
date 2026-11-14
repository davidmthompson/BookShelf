package com.example.bookshelf

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.example.bookshelf.data.ReadFilter
import com.example.bookshelf.data.SortOrder
import com.example.bookshelf.data.UserPreferences
import com.example.bookshelf.data.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException

class UserPreferencesRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun dataStore(scope: CoroutineScope, file: File = File(tmp.root, "test.preferences_pb")) =
        PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })

    @Test
    fun freshInstall_emitsDefaults() = runTest {
        val repo = UserPreferencesRepository(dataStore(backgroundScope))
        assertEquals(UserPreferences(), repo.userPreferences.first())
    }

    @Test
    fun updates_areEmittedByTheFlow() = runTest {
        val repo = UserPreferencesRepository(dataStore(backgroundScope))
        repo.setReadFilter(ReadFilter.UNREAD)
        repo.setSortOrder(SortOrder.RATING)
        repo.setHideLowRated(true)
        repo.setDarkTheme(true)
        assertEquals(
            UserPreferences(ReadFilter.UNREAD, SortOrder.RATING, hideLowRated = true, darkTheme = true),
            repo.userPreferences.first(),
        )
    }

    @Test
    fun savedValues_survive_aNewDataStoreOnTheSameFile() = runTest {
        val file = File(tmp.root, "persist.preferences_pb")
        // "First app launch": write, then let that DataStore's scope end (like the process dying).
        val firstLaunch = CoroutineScope(Dispatchers.IO + Job())
        val first = UserPreferencesRepository(dataStore(firstLaunch, file))
        first.setSortOrder(SortOrder.DATE_ADDED)
        first.setReadFilter(ReadFilter.READ)
        firstLaunch.cancel()
        // "Second launch": a brand-new DataStore reads the same file from disk.
        val second = UserPreferencesRepository(dataStore(backgroundScope, file))
        val prefs = second.userPreferences.first()
        assertEquals(SortOrder.DATE_ADDED, prefs.sortOrder)
        assertEquals(ReadFilter.READ, prefs.readFilter)
    }

    @Test
    fun ioException_fallsBackToDefaults() = runTest {
        val repo = UserPreferencesRepository(ThrowingDataStore(IOException("disk error")))
        assertEquals(UserPreferences(), repo.userPreferences.first())
    }

    @Test(expected = IllegalStateException::class)
    fun otherExceptions_areRethrown() = runTest {
        val repo = UserPreferencesRepository(ThrowingDataStore(IllegalStateException("bug")))
        repo.userPreferences.first()
    }

    private class ThrowingDataStore(private val error: Throwable) : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw error }
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            throw error
    }
}
