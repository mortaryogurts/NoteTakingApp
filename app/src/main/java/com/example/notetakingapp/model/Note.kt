package com.example.notetakingapp.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "notes",
    foreignKeys = [ForeignKey(
        entity = Categories::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id : Int,
    @ColumnInfo(defaultValue = "undefined")
    val noteTitle : String,
    val noteBody : String,
    val isPinned : Boolean,
    val isArchived : Boolean,
    val categoryId : Int? = null,
    val deletedAt : Long? = null,
    val createdAt : Long = System.currentTimeMillis(),
    val updatedAt : Long = System.currentTimeMillis()
): Parcelable
