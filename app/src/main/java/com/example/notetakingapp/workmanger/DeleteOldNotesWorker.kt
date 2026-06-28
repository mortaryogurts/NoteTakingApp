package com.example.notetakingapp.workmanger

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.notetakingapp.database.NoteDatabase
import com.example.notetakingapp.repository.NoteRepository

class DeleteOldNotesWorker(
    context : Context,
    workerParams : WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val database = NoteDatabase(applicationContext)
            val repository = NoteRepository(database)

            repository.deleteOldTrashedNotes()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}