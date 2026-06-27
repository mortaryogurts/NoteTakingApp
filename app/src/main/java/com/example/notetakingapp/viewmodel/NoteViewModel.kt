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

class NoteViewModel(private val repo: NoteRepository, app: Application) : AndroidViewModel(app) {


    private val searchQuery : MutableLiveData<String> = MutableLiveData<String>("")

    val notes : LiveData<List<Note>> = searchQuery.switchMap { query ->
        if(query.isNullOrEmpty()){
            repo.getAllNotes()
        }else{
            repo.searchNotes(query)
        }
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
    fun searchNotes(query: String){
        searchQuery.value = query
    }


    fun togglePinnedStatus(noteId: Int) = viewModelScope.launch {
        repo.togglePin(noteId)
    }

    fun pinNotes(noteIds : Set<Int>){
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
}
