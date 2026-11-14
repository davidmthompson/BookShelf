package com.example.bookshelf.data

import java.time.LocalDate

data class Book(
    val id: Int,
    val title: String,
    val author: String,
    /** Rating out of 5. */
    val rating: Double,
    val dateAdded: LocalDate,
    val isRead: Boolean,
)

/** The books shown in the app. Hard-coded so the assignment focuses on preferences. */
object BookSource {
    val books: List<Book> = listOf(
        Book(1, "Project Hail Mary", "Andy Weir", 4.7, LocalDate.of(2026, 9, 2), isRead = true),
        Book(2, "The Midnight Library", "Matt Haig", 3.9, LocalDate.of(2026, 10, 18), isRead = false),
        Book(3, "Dune", "Frank Herbert", 4.5, LocalDate.of(2026, 6, 11), isRead = true),
        Book(4, "Atomic Habits", "James Clear", 4.2, LocalDate.of(2026, 11, 3), isRead = false),
        Book(5, "The Alchemist", "Paulo Coelho", 3.2, LocalDate.of(2026, 3, 27), isRead = true),
        Book(6, "Educated", "Tara Westover", 4.4, LocalDate.of(2026, 8, 15), isRead = false),
        Book(7, "Twilight", "Stephenie Meyer", 2.8, LocalDate.of(2026, 1, 9), isRead = true),
        Book(8, "Klara and the Sun", "Kazuo Ishiguro", 3.6, LocalDate.of(2026, 10, 30), isRead = false),
    )
}
