package com.example.notetakingapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.notetakingapp.R
import com.example.notetakingapp.views.HomeFragmentDirections
import com.example.notetakingapp.databinding.NoteLayoutBinding
import com.example.notetakingapp.model.Note
import kotlin.random.Random




class NoteAdapter : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {


    inner class NoteViewHolder(val itemBinding : NoteLayoutBinding) : RecyclerView.ViewHolder(itemBinding.root)
    var onSelectionChanged: (() -> Unit)? = null
    private val selectedNotes = mutableSetOf<Int>()

    private fun toggleSelection(noteId: Int) {
        if (selectedNotes.contains(noteId)) {
            selectedNotes.remove(noteId)
        } else {
            selectedNotes.add(noteId)
        }
        notifyDataSetChanged()

    }

    fun getSelectedNotes(): Set<Int> = selectedNotes.toSet()

    fun clearSelection() {
        selectedNotes.clear()
        notifyDataSetChanged()
    }

    fun isInSelectionMode() = selectedNotes.isNotEmpty()


    //only updates those items in the recycler view that have changed and not all the items
//using notifyDataSetChanged() makes the app redrqaw the list every time the list changes
//this checks whether the items have changed and only updates those items in the list which is better for the ui
    private val differCallback = object : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.noteBody == newItem.noteBody &&
                    oldItem.noteTitle == newItem.noteTitle
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallback)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NoteViewHolder {
        return NoteViewHolder(
            NoteLayoutBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(
        holder: NoteViewHolder,
        position: Int
    ) {
        val currentNote = differ.currentList[position]
        holder.itemBinding.tvNoteTitle.text = currentNote.noteTitle
        holder.itemBinding.tvNoteBody.text = currentNote.noteBody
        if (currentNote.isPinned == true) {
            holder.itemBinding.isPinned.visibility = View.VISIBLE
        } else {
            holder.itemBinding.isPinned.visibility = View.GONE
        }
        val random = Random
        val color = Color.argb(
            255,
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)

        )
        holder.itemBinding.ibColor.setBackgroundColor(color)

        val isSelected = selectedNotes.contains(currentNote.id)
        holder.itemView.isActivated = isSelected
        holder.itemBinding.cbSelected.apply {
            isVisible = isInSelectionMode()
            isChecked = isSelected
        }

        holder.itemView.setOnClickListener {
            if (isInSelectionMode()) {
                toggleSelection(currentNote.id)
                onSelectionChanged?.invoke()
            } else {
                val directions =
                    HomeFragmentDirections.actionHomeFragmentToUpdateNoteFragment(currentNote)

                it.findNavController().navigate(directions)
            }
        }

        holder.itemView.setOnLongClickListener {
            toggleSelection(currentNote.id)
            onSelectionChanged?.invoke()
            true
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}