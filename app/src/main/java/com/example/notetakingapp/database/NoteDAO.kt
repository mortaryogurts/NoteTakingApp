package com.example.notetakingapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.notetakingapp.model.Note

@Dao
interface NoteDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note : Note)

    @Update
    suspend fun updateNote(note : Note)

    @Delete
    suspend fun deleteNote(note : Note)

    @Query("SELECT * FROM notes WHERE isArchived = 0 AND deletedAt IS NULL ORDER BY isPinned DESC, id DESC")
    fun getAllNotes() : LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE (noteTitle LIKE '%' || :query || '%' OR noteBody LIKE '%' || :query || '%') AND isArchived = 0 AND deletedAt IS NULL ORDER BY isPinned DESC, id DESC")
    fun searchNotes(query: String): LiveData<List<Note>>

    @Query("UPDATE notes SET isPinned = NOT isPinned WHERE id = :noteId")
    suspend fun togglePinnedStatus(noteId: Int)

    @Query("SELECT * FROM notes WHERE id IN (:ids)")
    suspend fun getNotesByIds(ids: List<Int>): List<Note>

    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :noteId")
    suspend fun setPinnedStatus(noteId: Int, isPinned: Boolean)

    @Query("DELETE FROM notes WHERE deletedAt IS NOT NULL AND deletedAt < :cutoff")
    suspend fun deleteNotesOlderThan(cutoff: Long)

    @Query("SELECT * FROM notes WHERE isArchived = 1 AND deletedAt IS NULL ORDER BY id DESC")
    fun getArchivedNotes() : LiveData<List<Note>>

    @Query("UPDATE notes SET isArchived = 1 WHERE id = :noteId")
    suspend fun archiveNote(noteId: Int)
    @Query("UPDATE notes SET isArchived = 0 WHERE id = :noteId")
    suspend fun unArchiveNote(noteId : Int)

    @Query("SELECT * FROM notes WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getTrashedNotes() : LiveData<List<Note>>

    @Query("UPDATE notes SET deletedAt = :timeStamp WHERE id = :noteId")
    suspend fun moveToTrash(noteId : Int, timeStamp : Long)

    @Query("UPDATE notes SET deletedAt = NULL WHERE id = :noteId")
    suspend fun restoreFromTrash(noteId: Int)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun permanentlyDelete(noteId: Int)

    @Query("DELETE FROM notes WHERE deletedAt IS NOT NULL AND deletedAt < :cutoff")
    suspend fun deleteOldTrashedNotes(cutoff: Long)
    @Query("UPDATE notes SET isArchived = 1 WHERE id IN (:noteIds)")
    suspend fun archiveNotes(noteIds: List<Int>)

    @Query("UPDATE notes SET isArchived = 0 WHERE id IN (:noteIds)")
    suspend fun unArchiveNotes(noteIds: List<Int>)

    @Query("UPDATE notes SET deletedAt = :timeStamp WHERE id IN (:noteIds)")
    suspend fun moveNotesToTrash(noteIds: List<Int>, timeStamp: Long)

    @Query("UPDATE notes SET deletedAt = NULL WHERE id IN (:noteIds)")
    suspend fun restoreNotesFromTrash(noteIds: List<Int>)

    @Query("DELETE FROM notes WHERE id IN (:noteIds)")
    suspend fun permanentlyDeleteNotes(noteIds: List<Int>)

    // Sort by date created
    @Query("SELECT * FROM notes WHERE isArchived = 0 AND deletedAt IS NULL ORDER BY isPinned DESC, createdAt DESC")
    fun getNotesSortedByCreatedDesc(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE isArchived = 0 AND deletedAt IS NULL ORDER BY isPinned DESC, createdAt ASC")
    fun getNotesSortedByCreatedAsc(): LiveData<List<Note>>

    // Sort by date modified
    @Query("SELECT * FROM notes WHERE isArchived = 0 AND deletedAt IS NULL ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesSortedByUpdatedDesc(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE isArchived = 0 AND deletedAt IS NULL ORDER BY isPinned DESC, updatedAt ASC")
    fun getNotesSortedByUpdatedAsc(): LiveData<List<Note>>

    // Sort alphabetically
    @Query("SELECT * FROM notes WHERE isArchived = 0 AND deletedAt IS NULL ORDER BY isPinned DESC, noteTitle ASC")
    fun getNotesSortedByTitleAsc(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE isArchived = 0 AND deletedAt IS NULL ORDER BY isPinned DESC, noteTitle DESC")
    fun getNotesSortedByTitleDesc(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE categoryId = :categoryId AND isArchived = 0 AND deletedAt IS NULL ORDER BY isPinned DESC, createdAt DESC")
    fun getNotesByCategory(categoryId: Int): LiveData<List<Note>>
}