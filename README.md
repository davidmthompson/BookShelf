# BookShelf: Book List with Saved Preferences

A small Jetpack Compose app that shows 8 books. You can filter and sort the list, and the app remembers
your choices with **Preferences DataStore**, so they're still there after the app closes.

## Features
| Requirement | Where |
|---|---|
| Filter by All / Read / Unread | segmented buttons under **Show** |
| Sort by Title, Rating, or Date added | chips under **Sort by** (rating and date are highest/newest first) |
| Original preference #1: **Hide books rated below 3.5★** | switch in the settings card |
| Original preference #2: **Dark theme** | switch in the settings card (also changes the status bar icons) |
| All settings saved in Preferences DataStore and applied to the list | `UserPreferencesRepository` + `applyPreferences()` |
| Saved settings shown on open, defaults on a fresh install | `UserPreferences()` default values; the card is titled "Saved settings" |
| DataStore only accessed in a repository, read as a `Flow`, written by `suspend` functions | `data/UserPreferencesRepository.kt` |
| `IOException` while reading → defaults; other exceptions rethrown | `.catch { }` in `userPreferences` |

## Project structure
```
app/src/main/java/com/example/bookshelf/
├── BookShelfApplication.kt        creates the single DataStore + repository
├── MainActivity.kt                collects UI state, applies the saved theme
├── data/
│   ├── Book.kt                    Book model + the 8 sample books
│   ├── UserPreferences.kt         ReadFilter, SortOrder, defaults, applyPreferences()
│   └── UserPreferencesRepository.kt   all DataStore reads/writes
└── ui/
    ├── BooksViewModel.kt          combines preferences + books into BooksUiState
    ├── BooksScreen.kt             settings card + book list
    └── theme/Theme.kt             light and dark color schemes
app/src/test/java/com/example/bookshelf/
├── UserPreferencesRepositoryTest.kt   defaults, updates, persistence, IOException, rethrow
└── ApplyPreferencesTest.kt            filter, sort, hide-low-rated logic
```

## How the UI receives preference changes
1. `UserPreferencesRepository.userPreferences` is `dataStore.data` mapped to a `UserPreferences` object.
   `dataStore.data` is a `Flow`: it emits the stored values once when collected, then again **every time
   they change**.
2. `BooksViewModel` maps that Flow into a `BooksUiState` (the preferences plus the filtered and sorted
   book list) and turns it into a `StateFlow` with `stateIn(viewModelScope, WhileSubscribed(5000), Loading)`.
3. `MainActivity` collects it with `collectAsStateWithLifecycle()`. When a new state arrives, Compose
   recomposes the screen and the theme.
4. When you tap a control, the UI calls a ViewModel function, which launches a coroutine and calls a
   `suspend` setter (for example `setSortOrder`) in the repository. The UI **never changes its own state
   directly**. The new value comes back through the same Flow after it has been saved, so what you see is
   always what's on disk. That's also why the settings reappear after a restart: on launch, the Flow's
   first emission is whatever was saved last time.

Until DataStore's first emission, the state is `Loading` (a spinner), so the screen never briefly shows the
wrong settings.

## Why DataStore updates are transactional
Every setter uses `dataStore.edit { prefs -> ... }`. `edit` is a suspend function that runs a
**read-modify-write as one atomic transaction**:
- DataStore runs updates **one at a time** (they're serialized), so two quick taps can't overwrite each
  other's changes.
- The block gets the **latest** saved values, changes them, and DataStore writes the whole result to a
  temporary file and then renames it over the old one. If the app crashes or the block throws, the old
  file is untouched: either all of the changes are saved or none are.
- `edit` only returns after the data is on disk, and only then does `data` emit the new value. Readers
  never see a half-written state.

This is the main improvement over `SharedPreferences`, where `apply()` writes in the background with no
error reporting and `commit()` blocks the main thread.

## Error handling
```kotlin
dataStore.data
    .catch { e ->
        if (e is IOException) emit(emptyPreferences())   // disk read failed: use defaults
        else throw e                                     // a real bug: don't hide it
    }
```
An unreadable file shouldn't crash the app, so the user gets the defaults instead. Any other exception means
something is actually wrong in the code, so it's rethrown. Both paths are covered by unit tests using a fake
`DataStore` that throws.

## Building and running
Open the folder in Android Studio and run the `app` configuration, or from a terminal:
```bash
./gradlew testDebugUnitTest      # 10 unit tests
./gradlew installDebug           # install on a running emulator/device
```
Requirements: Android Studio with SDK 37, min SDK 26.

## Demo script (for the screen recording)
`demo-silent.mp4` is a silent recording of cases 1–3 that you can talk over. To record it yourself:

**3. Start with no saved data.** Clear the app's data (Settings → Apps → BookShelf → Storage → Clear
storage, or `adb shell pm clear com.example.bookshelf`), or uninstall and reinstall. Open the app:
*Show: All*, *Sort by: Title*, both switches off, "Showing 8 of 8 books".

**1. Change the settings.** Tap *Read*, then *Unread*: the list and count change. Tap *Rating* and
*Date added*: the order changes. Tap *All* again, then turn on *Hide books rated below 3.5★*: Twilight and
The Alchemist disappear (8 → 6 books). Both are "Read", so the switch has no visible effect while
*Unread* is selected.
Turn on *Dark theme*: the whole app switches.

**2. Close and reopen.** Swipe the app away from Recents (or force-stop it) and open it again. The same
filter, sort, and switches are selected and the list matches.

**4. Explain.** Use the two sections above: the Flow from the repository → ViewModel `StateFlow` →
Compose, and `edit {}` being an atomic, serialized read-modify-write.
