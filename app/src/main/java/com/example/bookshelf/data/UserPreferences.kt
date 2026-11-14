package com.example.bookshelf.data

enum class ReadFilter(val label: String) { ALL("All"), READ("Read"), UNREAD("Unread") }

enum class SortOrder(val label: String) { TITLE("Title"), RATING("Rating"), DATE_ADDED("Date added") }

/** Books rated below this are hidden when [UserPreferences.hideLowRated] is on. */
const val LOW_RATING_THRESHOLD = 3.5

/**
 * Everything the user can choose. The default values here are what a fresh install shows,
 * and what the repository falls back to when preferences can't be read.
 */
data class UserPreferences(
    val readFilter: ReadFilter = ReadFilter.ALL,
    val sortOrder: SortOrder = SortOrder.TITLE,
    val hideLowRated: Boolean = false,
    val darkTheme: Boolean = false,
)

/** Applies the saved preferences to a list of books. Pure, so it is easy to unit-test. */
fun List<Book>.applyPreferences(prefs: UserPreferences): List<Book> {
    val filtered = filter { book ->
        val matchesRead = when (prefs.readFilter) {
            ReadFilter.ALL -> true
            ReadFilter.READ -> book.isRead
            ReadFilter.UNREAD -> !book.isRead
        }
        val matchesRating = !prefs.hideLowRated || book.rating >= LOW_RATING_THRESHOLD
        matchesRead && matchesRating
    }
    return when (prefs.sortOrder) {
        SortOrder.TITLE -> filtered.sortedBy { it.title.lowercase() }
        SortOrder.RATING -> filtered.sortedByDescending { it.rating }
        SortOrder.DATE_ADDED -> filtered.sortedByDescending { it.dateAdded }
    }
}
