package com.example.notetakingapp.views

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.notetakingapp.R
import com.example.notetakingapp.databinding.FragmentUpdateNoteBinding
import com.example.notetakingapp.model.Categories
import com.example.notetakingapp.model.Note
import com.example.notetakingapp.viewmodel.CategoryViewModel
import com.example.notetakingapp.viewmodel.NoteViewModel

class UpdateNoteFragment : Fragment(R.layout.fragment_update_note) {

    private var _binding : FragmentUpdateNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentNote : Note
    private lateinit var notesViewModel : NoteViewModel
    private lateinit var categoriesViewModel : CategoryViewModel
    private val args : UpdateNoteFragmentArgs by navArgs()

    private var selectedCategoryId : Int? = null
    private var currentCategories: List<Categories> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notesViewModel = (activity as MainActivity).noteViewModel
        categoriesViewModel = (activity as MainActivity).categoryViewModel
        currentNote = args.note!!

        binding.etNoteTitleUpdate.setText(currentNote.noteTitle)
        binding.etNoteBodyUpdate.setText(currentNote.noteBody)
        
        selectedCategoryId = currentNote.categoryId
        updateCategoryTextView()

        observeCategories()

        binding.fabDone.setOnClickListener {
            updateNote()
        }
        
        binding.tvCategory.setOnClickListener {
            showCategoryPickerDialog()
        }
    }

    private fun updateCategoryTextView() {
        if (selectedCategoryId == null) {
            binding.tvCategory.text = "No Category"
        } else {
            val category = currentCategories.find { it.id == selectedCategoryId }
            binding.tvCategory.text = category?.name ?: "Loading..."
        }
    }

    private fun observeCategories() {
        categoriesViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            currentCategories = categories
            updateCategoryTextView()
        }
    }

    private fun showCategoryPickerDialog() {
        val categoryNames =
            arrayOf("No Category") + currentCategories.map { it.name }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Pick Category")
            .setItems(categoryNames) { _, which ->
                if (which == 0) {
                    selectedCategoryId = null
                } else {
                    selectedCategoryId = currentCategories[which - 1].id
                }
                updateCategoryTextView()
            }
            .show()
    }

    private fun updateNote() {
        val title = binding.etNoteTitleUpdate.text.toString().trim()
        val body = binding.etNoteBodyUpdate.text.toString().trim()
        
        if (title.isNotEmpty()) {
            val note = currentNote.copy(
                noteTitle = title, 
                noteBody = body, 
                categoryId = selectedCategoryId,
                updatedAt = System.currentTimeMillis()
            )
            notesViewModel.updateNote(note)
            view?.findNavController()?.navigate(R.id.action_updateNoteFragment_to_homeFragment)
        } else {
            Toast.makeText(context, "Please Enter Note Title", Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteNote(){
        AlertDialog.Builder(activity).apply {
            setTitle("Delete Note")
            setMessage("Are you sure you want to delete this note?")
            setPositiveButton("Delete") { _, _ ->
                notesViewModel.deleteNote(currentNote)
                view?.findNavController()?.navigate(R.id.action_updateNoteFragment_to_homeFragment)
            }
            setNegativeButton("Cancel", null)

        }.create().show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_update_note, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_delete -> deleteNote()
            R.id.menu_pin -> {
                notesViewModel.togglePinnedStatus(currentNote.id)
                // Note: The Toast might show the old status since currentNote is not updated in-place
                // But it's better than nothing.
            }
            R.id.menu_archive -> {
                notesViewModel.archiveNote(currentNote.id)
                findNavController().navigate(R.id.action_updateNoteFragment_to_homeFragment)
                Toast.makeText(context, "Note Archived", Toast.LENGTH_SHORT).show()
            }
            R.id.menu_move_to_trash -> {
                notesViewModel.moveToTrash(currentNote.id)
                findNavController().navigate(R.id.action_updateNoteFragment_to_homeFragment)
                Toast.makeText(context, "Note Moved to Trash", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}