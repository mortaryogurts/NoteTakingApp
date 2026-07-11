package com.example.notetakingapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
sealed class NoteBlock : Parcelable {
    @Serializable
    @Parcelize
    data class Text(val content: String) : NoteBlock()

    @Serializable
    @Parcelize
    data class Image(val contentUri: String) : NoteBlock()
}

