package com.example.notetakingapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Categories(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val name : String,
    val isDefault: Boolean = false
)
