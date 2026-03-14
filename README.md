# Quick Notes

A modern Android note-taking app built with **Kotlin**, **Jetpack Compose**, **Room**, and **Hilt**, following **MVVM** and **Material Design 3**.

## Tech Stack

- **Language:** Kotlin  
- **UI:** Jetpack Compose  
- **Architecture:** MVVM  
- **Database:** Room  
- **Dependency Injection:** Hilt  
- **Min SDK:** 24  
- **Target SDK:** 34  

## Features

- **Notes management:** Add, edit, and delete notes (id, title, content, timestamp, isPinned).
- **Search:** Real-time filter by title or content via the top search bar.
- **Pin notes:** Pin/unpin notes; pinned notes appear at the top and show a pin icon.
- **Sorting:** Pinned first, then by latest timestamp.
- **UI:** Card-based list, FAB to add a note, empty state, and smooth Compose animations.

## Project Structure

```
app/src/main/java/com/quicknotes/
├── data/
│   ├── local/
│   │   ├── entity/Note.kt       # Room entity
│   │   ├── dao/NoteDao.kt       # CRUD + search Flow
│   │   └── AppDatabase.kt       # Room database
│   └── repository/
│       └── NoteRepository.kt    # Repository over DAO
├── di/
│   └── DatabaseModule.kt        # Hilt Room providers
├── navigation/
│   └── AppNavigation.kt        # NavHost, routes, ViewModels
├── ui/
│   ├── noteslist/
│   │   ├── NotesListScreen.kt   # List + search + FAB
│   │   └── NotesListViewModel.kt
│   ├── addeditnote/
│   │   ├── AddEditNoteScreen.kt # Add/Edit form
│   │   └── AddEditNoteViewModel.kt
│   └── theme/
│       ├── Theme.kt
│       ├── Color.kt
│       └── Type.kt
├── QuickNotesApplication.kt     # @HiltAndroidApp
└── MainActivity.kt             # Compose + AppNavigation
```

## How to Run

1. **Open in Android Studio**  
   Open the `QuickNotes` folder as the project (use **File → Open** and select the folder).

2. **Sync Gradle**  
   Let Android Studio sync the Gradle project (or use **File → Sync Project with Gradle Files**).

3. **Run on device or emulator**  
   - Connect a device with USB debugging enabled, or start an emulator (API 24+).  
   - Select the `app` run configuration and click **Run** (or press **Shift+F10**).

### From command line

```bash
cd QuickNotes
./gradlew assembleDebug
# Install on connected device:
./gradlew installDebug
```

On Windows:

```powershell
cd QuickNotes
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

## Screens

1. **NotesListScreen** – List of notes, search bar, FAB. Tap a note to edit; use pin/delete on each card.  
2. **AddEditNoteScreen** – Title and content fields; pin, save, and (when editing) delete in the top bar.

## Architecture Notes

- **ViewModel** exposes `StateFlow` (or the state is collected from it) for UI state; repository is used for all DB operations.
- **Repository** is the single source of truth over `NoteDao` (Room).
- **Room** provides `Flow<List<Note>>` so the list updates automatically when data changes.
- **Hilt** provides `AppDatabase`, `NoteDao`, and `NoteRepository`; ViewModels are created with `hiltViewModel()` in navigation.

## License

This project is for demonstration purposes.
