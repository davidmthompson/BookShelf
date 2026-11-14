package com.example.bookshelf.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookshelf.BookShelfApplication
import com.example.bookshelf.data.Book
import com.example.bookshelf.data.BookSource
import com.example.bookshelf.data.ReadFilter
import com.example.bookshelf.data.SortOrder
import com.example.bookshelf.data.UserPreferences
import com.example.bookshelf.data.UserPreferencesRepository
import com.example.bookshelf.data.applyPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface BooksUiState {
    /** Before DataStore's first emission, so the UI never flashes the wrong settings. */
    data object Loading : BooksUiState

    data class Ready(
        val preferences: UserPreferences,
        val books: List<Book>,
        val totalBooks: Int,
    ) : BooksUiState
}

class BooksViewModel(
    private val repository: UserPreferencesRepository,
    private val allBooks: List<Book> = BookSource.books,
) : ViewModel() {

    /**
     * Every time DataStore saves a change, the repository's Flow emits the new preferences,
     * this map re-filters and re-sorts the list, and Compose redraws.
     */
    val uiState: StateFlow<BooksUiState> = repository.userPreferences
        .map { prefs ->
            BooksUiState.Ready(
                preferences = prefs,
                books = allBooks.applyPreferences(prefs),
                totalBooks = allBooks.size,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BooksUiState.Loading,
        )

    // The UI only writes. It never updates its own state directly; the new value comes back
    // through the Flow above, so the screen always shows what is actually saved.
    fun setReadFilter(filter: ReadFilter) = viewModelScope.launch { repository.setReadFilter(filter) }
    fun setSortOrder(order: SortOrder) = viewModelScope.launch { repository.setSortOrder(order) }
    fun setHideLowRated(hide: Boolean) = viewModelScope.launch { repository.setHideLowRated(hide) }
    fun setDarkTheme(dark: Boolean) = viewModelScope.launch { repository.setDarkTheme(dark) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as BookShelfApplication
                BooksViewModel(app.userPreferencesRepository)
            }
        }
    }
}
