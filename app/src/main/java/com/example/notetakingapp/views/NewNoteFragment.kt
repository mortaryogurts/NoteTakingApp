package com.example.notetakingapp.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.navigation.findNavController
import coil.load
import com.example.notetakingapp.R
import com.example.notetakingapp.databinding.FragmentNewNoteBinding
import com.example.notetakingapp.model.Categories
import com.example.notetakingapp.model.Note
import com.example.notetakingapp.model.NoteBlock
import com.example.notetakingapp.viewmodel.CategoryViewModel
import com.example.notetakingapp.viewmodel.NoteViewModel
import com.google.android.material.snackbar.Snackbar

class NewNoteFragment : Fragment(R.layout.fragment_new_note) {

    private var _binding: FragmentNewNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var notesViewModel: NoteViewModel
    private lateinit var mView: View
    private lateinit var categoryViewmodel : CategoryViewModel

    private var selectedCategoryId : Int? = null
    private var currentCategories : List<Categories> = emptyList()

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            addImageBlock(uri.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notesViewModel = (activity as MainActivity).noteViewModel
        categoryViewmodel = (activity as MainActivity).categoryViewModel
        mView = view

        observeCategories()
        updateCategoryTextView()

        binding.fabSave.setOnClickListener {
            saveNote(mView)
        }
        binding.tvCategory.setOnClickListener {
            showCategoryDialog()
        }

        binding.btnAddImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Initialize with one text block
        addTextBlock("")

        binding.tvCategory.text = "No Category"
    }

    private fun addTextBlock(content: String) {
        val editText = EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            hint = getString(R.string.type_something)
            background = null // transparent background
            setText(content)
        }
        binding.blockContainer.addView(editText)
        editText.requestFocus()
    }

    private fun addImageBlock(uri: String) {
        val imageView = ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                800 // height
            ).also { it.setMargins(0, 16, 0, 16) }
            scaleType = ImageView.ScaleType.CENTER_CROP
            load(uri)
            tag = uri // Store URI in tag
        }
        binding.blockContainer.addView(imageView)
        // Add another text block after the image
        addTextBlock("")
    }

    private fun getNoteBlocks(): List<NoteBlock> {
        val blocks = mutableListOf<NoteBlock>()
        binding.blockContainer.children.forEach { view ->
            when (view) {
                is EditText -> {
                    val text = view.text.toString().trim()
                    if (text.isNotEmpty()) {
                        blocks.add(NoteBlock.Text(text))
                    }
                }
                is ImageView -> {
                    val uri = view.tag as? String
                    if (uri != null) {
                        blocks.add(NoteBlock.Image(uri))
                    }
                }
            }
        }
        return blocks
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
        categoryViewmodel.allCategories.observe(viewLifecycleOwner) { categories ->
            currentCategories = categories
        }
    }

    private fun showCategoryDialog() {
        val categoryNames = arrayOf("No Category") + currentCategories.map { it.name }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Select Category")
            .setItems(categoryNames) { _, which ->
                if (which == 0) {
                    selectedCategoryId = null
                    binding.tvCategory.text = "No Category"
                } else {
                    val selected = currentCategories[which - 1]
                    selectedCategoryId = selected.id
                    binding.tvCategory.text = selected.name
                }
            }
            .show()
    }

    private fun saveNote(view: View) {
        val noteTitle = binding.etNoteTitle.text.toString().trim()
        val noteBlocks = getNoteBlocks()

        if (noteTitle.isNotEmpty()) {
            val note = Note(
                id = 0, 
                noteTitle = noteTitle, 
                noteBody = noteBlocks,
                isPinned = false, 
                isArchived = false,
                categoryId = selectedCategoryId
            )
            notesViewModel.addNote(note)
            Snackbar.make(view, "Note Saved Successfully", Snackbar.LENGTH_SHORT).show()
            view.findNavController().navigate(R.id.action_newNoteFragment_to_homeFragment)
        } else {
            Snackbar.make(view, "Please Enter Note Title", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_new_note, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                saveNote(mView)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}