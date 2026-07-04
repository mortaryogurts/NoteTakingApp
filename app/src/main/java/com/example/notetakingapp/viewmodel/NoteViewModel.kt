package com.example.notetakingapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.room.util.query
import com.example.notetakingapp.model.Note
import com.example.notetakingapp.repository.NoteRepository
import kotlinx.coroutines.launch


enum class SortOrder {
    DATE_CREATED_ASC,
    DATE_CREATED_DESC,
    DATE_UPDATED_ASC,
    DATE_UPDATED_DESC,
    TITLE_ASC,
    TITLE_DESC
}

class NoteViewModel(private val repo: NoteRepository, app: Application) : AndroidViewModel(app) {


    private val sharedPrefs =
        app.getSharedPreferences("note_prefs", android.content.Context.MODE_PRIVATE)

    private val _spanCount = MutableLiveData(sharedPrefs.getInt("span_count", 2))
    val spanCount: LiveData<Int> = _spanCount

    fun toggleSpanCount() {
        val newCount = if (_spanCount.value == 2) 1 else 2
        _spanCount.value = newCount
        sharedPrefs.edit().putInt("span_count", newCount).apply()
    }

    private val _sortOrder = MutableLiveData<SortOrder>(SortOrder.DATE_CREATED_DESC)
    val sortOrder: LiveData<SortOrder> = _sortOrder

    private val _selectedCategory = MutableLiveData<Int?>(null)
    val selectedCategory: LiveData<Int?> = _selectedCategory

    fun setSelectedCategory(categoryId: Int?) {
        _selectedCategory.value = categoryId
    }

    private val searchQuery: MutableLiveData<String> = MutableLiveData<String>("")
    val notes: LiveData<List<Note>> = searchQuery.switchMap { query ->
        if (query.isNullOrEmpty()) {
            selectedCategory.switchMap { categoryId ->
                if (categoryId == null) {
                    _sortOrder.switchMap { order ->
                        when (order) {
                            SortOrder.DATE_CREATED_ASC -> repo.getNotesSortedByCreatedAsc()
                            SortOrder.DATE_CREATED_DESC -> repo.getNotesSortedByCreatedDesc()
                            SortOrder.DATE_UPDATED_ASC -> repo.getNotesSortedByUpdatedAsc()
                            SortOrder.DATE_UPDATED_DESC -> repo.getNotesSortedByUpdatedDesc()
                            SortOrder.TITLE_ASC -> repo.getNotesSortedByTitleAsc()
                            SortOrder.TITLE_DESC -> repo.getNotesSortedByTitleDesc()
                        }
                    }
                } else {
                    repo.getNotesByCategory(categoryId)
                }
            }
        } else {
            repo.searchNotes(query)
        }
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun addNote(note: Note) =
        viewModelScope.launch {
            repo.insertNote(note)
        }

    fun deleteNote(note: Note) =
        viewModelScope.launch {
            repo.deleteNote(note)
        }

    fun updateNote(note: Note) =
        viewModelScope.launch {
            repo.updateNote(note)
        }

    fun getAllNotes() = repo.getAllNotes()
    fun searchNotes(query: String) {
        searchQuery.value = query
    }


    fun togglePinnedStatus(noteId: Int) = viewModelScope.launch {
        repo.togglePin(noteId)
    }

    fun pinNotes(noteIds: Set<Int>) {
        viewModelScope.launch {
            val selectedNotes = repo.getNotesByIds(noteIds.toList())
            val hasUnpinned = selectedNotes.any { !it.isPinned }

            if (hasUnpinned) {
                // If there's at least one unpinned note (mix or all unpinned), pin the unpinned ones
                selectedNotes.filter { !it.isPinned }.forEach { note ->
                    repo.setPinnedStatus(note.id, true)
                }
            } else {
                // If all selected notes are pinned, unpin them all
                selectedNotes.forEach { note ->
                    repo.setPinnedStatus(note.id, false)
                }
            }
        }
    }

    fun deleteNotes(noteIds: Set<Int>) {
        viewModelScope.launch {
            val selectedNotes = repo.getNotesByIds(noteIds.toList())
            selectedNotes.forEach { note ->
                repo.deleteNote(note)
            }
        }
    }

    fun deleteNotesOlderThan(cutoff: Long) {
        viewModelScope.launch {
            repo.deleteNotesOlderThan(cutoff)
        }
    }

    val archivedNotes: LiveData<List<Note>> = repo.getArchivedNotes()

    fun archiveNote(noteId: Int) {
        viewModelScope.launch {
            repo.archiveNote(noteId)
        }
    }

    fun archiveNotes(noteIds: Set<Int>) {
        viewModelScope.launch {
            repo.archiveNotes(noteIds.toList())
        }
    }

    fun unArchiveNote(noteId: Int) {
        viewModelScope.launch {
            repo.unarchiveNote(noteId)
        }
    }

    fun unArchiveNotes(noteIds: Set<Int>) {
        viewModelScope.launch {
            repo.unarchiveNotes(noteIds.toList())
        }
    }

    val trashedNotes: LiveData<List<Note>> = repo.getTrashedNotes()

    fun moveToTrash(noteId: Int) {
        viewModelScope.launch {
            repo.moveToTrash(noteId)
        }
    }

    fun moveNotesToTrash(noteIds: Set<Int>) {
        viewModelScope.launch {
            repo.moveNotesToTrash(noteIds.toList())
        }
    }

    fun restoreFromTrash(noteId: Int) {
        viewModelScope.launch {
            repo.restoreFromTrash(noteId)
        }
    }

    fun restoreNotesFromTrash(noteIds: Set<Int>) {
        viewModelScope.launch {
            repo.restoreNotesFromTrash(noteIds.toList())
        }
    }

    fun permanentlyDelete(noteId: Int) {
        viewModelScope.launch {
            repo.permanentlyDelete(noteId)
        }
    }

    fun permanentlyDeleteNotes(noteIds: Set<Int>) {
        viewModelScope.launch {
            repo.permanentlyDeleteNotes(noteIds.toList())
        }
    }

    fun deleteOldTrashedNotes() {
        viewModelScope.launch {
            repo.deleteOldTrashedNotes()
        }
    }

}
