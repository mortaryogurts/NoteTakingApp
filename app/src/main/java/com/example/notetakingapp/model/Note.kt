package com.example.notetakingapp.model

import android.os.Parcelable
import androidx.appcompat.widget.DialogTitle
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id : Int,
    val noteTitle : String,
    val noteBody : String,
    val isPinned : Boolean,
    val isArchived : Boolean,
    val deletedAt : Long? = null
): Parcelable
