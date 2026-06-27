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

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, id DESC")
    fun getAllNotes() : LiveData<List<Note>>

    @Query("SELECT * FROM notes where noteTitle like '%' || :query || '%' OR noteBody LIKE '%' || :query || '%'")
    fun searchNote(query: String?) : LiveData<List<Note>>

    @Query("UPDATE notes SET isPinned = NOT isPinned WHERE id = :noteId")
    suspend fun togglePinnedStatus(noteId: Int)

    @Query("SELECT * FROM notes WHERE id IN (:ids)")
    suspend fun getNotesByIds(ids: List<Int>): List<Note>

    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :noteId")
    suspend fun setPinnedStatus(noteId: Int, isPinned: Boolean)
}