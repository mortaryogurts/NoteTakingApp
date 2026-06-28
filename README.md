# NoteTakingApp

A modern, feature-rich Android note-taking application built with Kotlin and Jetpack components.

## 🚀 Features

-   **Note Management**: Create, update, and delete notes with ease.
-   **Pinning**: Pin important notes to the top of your list.
-   **Archiving**: Keep your main view clean by moving notes to the archive.
-   **Trash & Auto-Delete**: Deleted notes move to the trash and are automatically permanently deleted after a set period using WorkManager.
-   **Flexible Layouts**: Toggle between Grid (2-column) and List (1-column) views. Your preference is persisted across app restarts.
-   **Search**: Quickly find notes with real-time search functionality.
-   **Selection Mode**: Perform bulk actions (archive, delete, pin) on multiple notes at once.
-   **Undo Actions**: Easily undo archiving or trashing notes via snackbars.
-   **Edge-to-Edge**: Modern UI implementation with full edge-to-edge support.

## 🛠 Tech Stack

-   **Language**: Kotlin
-   **Architecture**: MVVM (Model-View-ViewModel)
-   **Database**: Room Persistence Library
-   **Navigation**: Jetpack Navigation Component with Single Activity Architecture
-   **UI**: 
    -   View Binding & Data Binding
    -   Material Design 3
    -   Recycler View with Staggered Grid Layout
-   **Background Tasks**: WorkManager (for auto-deleting old trashed notes)
-   **Lifecycle**: ViewModel, LiveData
-   **Asynchronous**: Kotlin Coroutines

## 📂 Project Structure

-   `model`: Contains the `Note` entity and data models.
-   `database`: Room database configuration, including DAOs and type converters.
-   `repository`: Handles data operations and provides a clean API to the rest of the app.
-   `viewmodel`: Contains `NoteViewModel` which manages UI-related data and persists settings like layout state.
-   `views`: Fragments and Activities (`MainActivity`, `HomeFragment`, `ArchiveFragment`, `TrashFragment`, etc.).
-   `adapter`: RecyclerView adapters for displaying notes.
-   `workmanager`: Background workers like `DeleteOldNotesWorker`.

## ⚙️ Installation

1.  Clone the repository.
2.  Open the project in Android Studio.
3.  Build and run on an Android device or emulator (API 24+).

## 📄 License

This project is for educational purposes.
