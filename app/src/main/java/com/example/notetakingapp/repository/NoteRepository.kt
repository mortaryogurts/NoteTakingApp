package com.example.notetakingapp.repository

import com.example.notetakingapp.database.NoteDatabase
import com.example.notetakingapp.model.Note


class NoteRepository(private val db : NoteDatabase) {

    suspend fun insertNote(note : Note) =
        db.getNoteDao().insertNote(note)

    suspend fun deleteNote(note : Note) =
        db.getNoteDao().deleteNote(note)

    suspend fun updateNote(note : Note) =
        db.getNoteDao().updateNote(note)

    fun getAllNotes() =
        db.getNoteDao().getAllNotes()

    fun searchNotes(query: String?) =
        db.getNoteDao().searchNote(query)


    suspend fun togglePin(noteId : Int) =
        db.getNoteDao().togglePinnedStatus(noteId)

    suspend fun getNotesByIds(ids: List<Int>) =
        db.getNoteDao().getNotesByIds(ids)

    suspend fun setPinnedStatus(noteId: Int, isPinned: Boolean) =
        db.getNoteDao().setPinnedStatus(noteId, isPinned)
}
