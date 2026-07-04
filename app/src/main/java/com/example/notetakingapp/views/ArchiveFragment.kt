package com.example.notetakingapp.views

import android.R.id.message
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notetakingapp.R
import com.example.notetakingapp.adapter.NoteAdapter
import com.example.notetakingapp.databinding.FragmentArchiveBinding
import com.example.notetakingapp.viewmodel.NoteViewModel
import com.google.android.material.snackbar.Snackbar

class ArchiveFragment : Fragment(R.layout.fragment_archive) {

    private var _binding : FragmentArchiveBinding? = null
    private val binding get() = _binding!!
    private lateinit var notesViewModel : NoteViewModel
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentArchiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notesViewModel = (activity as MainActivity).noteViewModel
        setupRecyclerView()
        observeArchivedNotes()
        setupSwipeToUnarchiveOrTrash()
    }

    private fun setupSwipeToUnarchiveOrTrash() {
        noteAdapter = NoteAdapter()
        noteAdapter.onSelectionChanged = {
            onSelectionChanged()
        }
        binding.archivedRecyclerView.apply {
            adapter = noteAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

        notesViewModel.spanCount.observe(viewLifecycleOwner) { spanCount ->
            (binding.archivedRecyclerView.layoutManager as? StaggeredGridLayoutManager)?.spanCount = spanCount
            requireActivity().invalidateOptionsMenu()
        }
        
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val note = noteAdapter.differ.currentList[position]

                when (direction) {
                    ItemTouchHelper.RIGHT -> {
                        // swipe right → unarchive back to home
                        notesViewModel.unArchiveNote(note.id)
                        showUndoSnackbar("Note unarchived") {
                            notesViewModel.archiveNote(note.id)
                        }
                    }

                    ItemTouchHelper.LEFT -> {
                        // swipe left → move to trash
                        notesViewModel.moveToTrash(note.id)
                        showUndoSnackbar("Note moved to trash") {
                            notesViewModel.restoreFromTrash(note.id)
                        }
                    }
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.archivedRecyclerView)
    }

    private fun observeArchivedNotes() {
        notesViewModel.archivedNotes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.differ.submitList(notes)
        }
    }

    private fun setupRecyclerView() {
        // Handled in setupSwipeToUnarchiveOrTrash now
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        val isSelectionMode = noteAdapter.isInSelectionMode()

        val mainActivity = activity as? MainActivity
        val toolbar = mainActivity?.binding?.toolbar

        if (isSelectionMode) {
            inflater.inflate(R.menu.menu_archive_selection, menu)
            val count = noteAdapter.getSelectedNotes().size
            toolbar?.title = count.toString()
            toolbar?.setNavigationIcon(R.drawable.ic_cancel)
            toolbar?.setNavigationOnClickListener {
                noteAdapter.clearSelection()
                onSelectionChanged()
            }
            toolbar?.setBackgroundColor(resources.getColor(R.color.selectionColor, null))
        } else {
            inflater.inflate(R.menu.menu_archive, menu)
            toolbar?.title = "Archive"
            toolbar?.setNavigationIcon(R.drawable.ic_note) // Using ic_note as drawer icon
            toolbar?.setNavigationOnClickListener {
                mainActivity?.binding?.drawerLayout?.openDrawer(androidx.core.view.GravityCompat.START)
            }
            toolbar?.setBackgroundColor(resources.getColor(R.color.background_light, null))

            val toggleItem = menu.findItem(R.id.menu_toggle_list_view)
            toggleItem.title = if (notesViewModel.spanCount.value == 2) "List View" else "Grid View"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_unarchive -> {
                notesViewModel.unArchiveNotes(noteAdapter.getSelectedNotes())
                noteAdapter.clearSelection()
                onSelectionChanged()
                return true
            }
            R.id.menu_move_to_trash -> {
                notesViewModel.moveNotesToTrash(noteAdapter.getSelectedNotes())
                noteAdapter.clearSelection()
                onSelectionChanged()
                return true
            }
            R.id.menu_toggle_list_view -> {
                notesViewModel.toggleSpanCount()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onSelectionChanged() {
        requireActivity().invalidateOptionsMenu()
    }

    private fun showUndoSnackbar(string: String, function: () -> Unit) {
        Snackbar.make(binding.root, string, Snackbar.LENGTH_LONG)
            .setAction("Undo") { function() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}