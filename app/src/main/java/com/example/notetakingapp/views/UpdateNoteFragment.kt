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
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.children
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.notetakingapp.R
import com.example.notetakingapp.databinding.FragmentUpdateNoteBinding
import com.example.notetakingapp.model.Categories
import com.example.notetakingapp.model.Note
import com.example.notetakingapp.model.NoteBlock
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
        _binding = FragmentUpdateNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notesViewModel = (activity as MainActivity).noteViewModel
        categoriesViewModel = (activity as MainActivity).categoryViewModel
        currentNote = args.note!!

        binding.etNoteTitleUpdate.setText(currentNote.noteTitle)
        
        // Render existing blocks
        if (currentNote.noteBody.isEmpty()) {
            addTextBlock("")
        } else {
            currentNote.noteBody.forEach { block ->
                when (block) {
                    is NoteBlock.Text -> addTextBlock(block.content)
                    is NoteBlock.Image -> addImageBlock(block.contentUri)
                }
            }
        }
        
        selectedCategoryId = currentNote.categoryId
        updateCategoryTextView()

        observeCategories()

        binding.fabDone.setOnClickListener {
            updateNote()
        }
        
        binding.tvCategory.setOnClickListener {
            showCategoryPickerDialog()
        }

        binding.btnAddImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun addTextBlock(content: String) {
        val editText = EditText(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            hint = getString(R.string.type_something)
            background = null
            setText(content)
        }
        binding.blockContainer.addView(editText)
    }

    private fun addImageBlock(uri: String) {
        val imageView = ImageView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                800
            ).also { it.setMargins(0, 16, 0, 16) }
            scaleType = ImageView.ScaleType.CENTER_CROP
            load(uri)
            tag = uri
        }
        binding.blockContainer.addView(imageView)
        // Only add a text block if the last block wasn't a text block
        val lastChild = binding.blockContainer.getChildAt(binding.blockContainer.childCount - 1)
        if (lastChild !is EditText) {
            addTextBlock("")
        }
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
        val blocks = getNoteBlocks()
        
        if (title.isNotEmpty()) {
            val note = currentNote.copy(
                noteTitle = title, 
                noteBody = blocks,
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