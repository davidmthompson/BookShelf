package com.example.bookshelf

import com.example.bookshelf.data.BookSource
import com.example.bookshelf.data.LOW_RATING_THRESHOLD
import com.example.bookshelf.data.ReadFilter
import com.example.bookshelf.data.SortOrder
import com.example.bookshelf.data.UserPreferences
import com.example.bookshelf.data.applyPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplyPreferencesTest {
    private val books = BookSource.books

    @Test
    fun defaults_showAllBooks_sortedByTitle() {
        val result = books.applyPreferences(UserPreferences())
        assertEquals(books.size, result.size)
        assertEquals(books.map { it.title }.sortedBy { it.lowercase() }, result.map { it.title })
    }

    @Test
    fun readFilter_keepsOnlyMatchingBooks() {
        assertTrue(books.applyPreferences(UserPreferences(readFilter = ReadFilter.READ)).all { it.isRead })
        assertTrue(books.applyPreferences(UserPreferences(readFilter = ReadFilter.UNREAD)).none { it.isRead })
    }

    @Test
    fun sortByRating_isHighestFirst() {
        val ratings = books.applyPreferences(UserPreferences(sortOrder = SortOrder.RATING)).map { it.rating }
        assertEquals(ratings.sortedDescending(), ratings)
    }

    @Test
    fun sortByDate_isNewestFirst() {
        val dates = books.applyPreferences(UserPreferences(sortOrder = SortOrder.DATE_ADDED)).map { it.dateAdded }
        assertEquals(dates.sortedDescending(), dates)
    }

    @Test
    fun hideLowRated_removesBooksBelowThreshold() {
        val result = books.applyPreferences(UserPreferences(hideLowRated = true))
        assertTrue(result.all { it.rating >= LOW_RATING_THRESHOLD })
        assertTrue(result.size < books.size)
    }
}
