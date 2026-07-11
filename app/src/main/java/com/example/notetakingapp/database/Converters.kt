package com.example.notetakingapp.database

import androidx.room.TypeConverter
import com.example.notetakingapp.model.NoteBlock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromNoteBlockList(value: List<NoteBlock>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toNoteBlockList(value: String): List<NoteBlock> {
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            // Fallback for old notes that might be plain text
            listOf(NoteBlock.Text(value))
        }
    }
}
