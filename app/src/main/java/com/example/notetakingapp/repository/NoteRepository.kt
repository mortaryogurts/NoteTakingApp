package com.example.notetakingapp.repository

import androidx.lifecycle.LiveData
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
        db.getNoteDao().searchNotes(query!!)


    suspend fun togglePin(noteId : Int) =
        db.getNoteDao().togglePinnedStatus(noteId)

    suspend fun getNotesByIds(ids: List<Int>) =
        db.getNoteDao().getNotesByIds(ids)

    suspend fun setPinnedStatus(noteId: Int, isPinned: Boolean) =
        db.getNoteDao().setPinnedStatus(noteId, isPinned)

    suspend fun deleteNotesOlderThan(cutoff: Long) =
        db.getNoteDao().deleteNotesOlderThan(cutoff)

    fun getArchivedNotes(): LiveData<List<Note>> = db.getNoteDao().getArchivedNotes()

    suspend fun archiveNote(noteId: Int) = db.getNoteDao().archiveNote(noteId)

    suspend fun archiveNotes(noteIds: List<Int>) = db.getNoteDao().archiveNotes(noteIds)

    suspend fun unarchiveNote(noteId: Int) = db.getNoteDao().unArchiveNote(noteId)

    suspend fun unarchiveNotes(noteIds: List<Int>) = db.getNoteDao().unArchiveNotes(noteIds)

    fun getTrashedNotes(): LiveData<List<Note>> = db.getNoteDao().getTrashedNotes()

    suspend fun moveToTrash(noteId: Int) {
        db.getNoteDao().moveToTrash(noteId, System.currentTimeMillis())
    }

    suspend fun moveNotesToTrash(noteIds: List<Int>) {
        db.getNoteDao().moveNotesToTrash(noteIds, System.currentTimeMillis())
    }

    suspend fun restoreFromTrash(noteId: Int) = db.getNoteDao().restoreFromTrash(noteId)

    suspend fun restoreNotesFromTrash(noteIds: List<Int>) = db.getNoteDao().restoreNotesFromTrash(noteIds)

    suspend fun permanentlyDelete(noteId: Int) = db.getNoteDao().permanentlyDelete(noteId)

    suspend fun permanentlyDeleteNotes(noteIds: List<Int>) = db.getNoteDao().permanentlyDeleteNotes(noteIds)

    suspend fun deleteOldTrashedNotes() {
        val cutoff = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000) // 30 days in ms
        db.getNoteDao().deleteOldTrashedNotes(cutoff)
    }
}
