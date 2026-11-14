package com.example.bookshelf

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bookshelf.ui.BooksScreen
import com.example.bookshelf.ui.BooksUiState
import com.example.bookshelf.ui.BooksViewModel
import com.example.bookshelf.ui.theme.BookShelfTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: BooksViewModel = viewModel(factory = BooksViewModel.Factory)
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            val dark = (state as? BooksUiState.Ready)?.preferences?.darkTheme ?: false

            // Match the status/navigation bar icons to the saved theme, not the system one.
            LaunchedEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            BookShelfTheme(darkTheme = dark) {
                BooksScreen(
                    state = state,
                    onReadFilterChange = viewModel::setReadFilter,
                    onSortOrderChange = viewModel::setSortOrder,
                    onHideLowRatedChange = viewModel::setHideLowRated,
                    onDarkThemeChange = viewModel::setDarkTheme,
                )
            }
        }
    }
}
