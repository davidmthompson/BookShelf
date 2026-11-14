package com.example.bookshelf.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bookshelf.data.Book
import com.example.bookshelf.data.LOW_RATING_THRESHOLD
import com.example.bookshelf.data.ReadFilter
import com.example.bookshelf.data.SortOrder
import com.example.bookshelf.data.UserPreferences
import java.time.format.DateTimeFormatter

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    state: BooksUiState,
    onReadFilterChange: (ReadFilter) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onHideLowRatedChange: (Boolean) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text("BookShelf") }) }) { padding ->
        when (state) {
            BooksUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is BooksUiState.Ready -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.TopCenter,
            ) {
                LazyColumn(
                    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        SettingsCard(
                            prefs = state.preferences,
                            onReadFilterChange = onReadFilterChange,
                            onSortOrderChange = onSortOrderChange,
                            onHideLowRatedChange = onHideLowRatedChange,
                            onDarkThemeChange = onDarkThemeChange,
                        )
                    }
                    item {
                        Text(
                            "Showing ${state.books.size} of ${state.totalBooks} books",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    if (state.books.isEmpty()) {
                        item {
                            Text(
                                "No books match these settings.",
                                modifier = Modifier.padding(vertical = 24.dp),
                            )
                        }
                    }
                    items(state.books, key = { it.id }) { book -> BookRow(book) }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(
    prefs: UserPreferences,
    onReadFilterChange: (ReadFilter) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onHideLowRatedChange: (Boolean) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Saved settings", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            Text("Show", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                val options = ReadFilter.entries
                options.forEachIndexed { index, filter ->
                    SegmentedButton(
                        selected = prefs.readFilter == filter,
                        onClick = { onReadFilterChange(filter) },
                        shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    ) { Text(filter.label) }
                }
            }
            Spacer(Modifier.height(12.dp))

            Text("Sort by", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortOrder.entries.forEach { order ->
                    FilterChip(
                        selected = prefs.sortOrder == order,
                        onClick = { onSortOrderChange(order) },
                        label = { Text(order.label) },
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            SwitchRow(
                label = "Hide books rated below $LOW_RATING_THRESHOLD★",
                checked = prefs.hideLowRated,
                onCheckedChange = onHideLowRatedChange,
            )
            SwitchRow(
                label = "Dark theme",
                checked = prefs.darkTheme,
                onCheckedChange = onDarkThemeChange,
            )
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun BookRow(book: Book) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(book.author, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "★ ${book.rating}  ·  Added ${book.dateAdded.format(DATE_FORMAT)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (book.isRead) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    if (book.isRead) "Read" else "Unread",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}
