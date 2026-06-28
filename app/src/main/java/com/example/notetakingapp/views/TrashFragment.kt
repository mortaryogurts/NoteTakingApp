package com.example.notetakingapp.views

import android.app.AlertDialog
import android.os.Bundle
import android.text.Layout
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notetakingapp.R
import com.example.notetakingapp.adapter.NoteAdapter
import com.example.notetakingapp.databinding.FragmentTrashBinding
import com.example.notetakingapp.model.Note
import com.example.notetakingapp.viewmodel.NoteViewModel
import com.google.android.material.snackbar.Snackbar


class TrashFragment : Fragment(R.layout.fragment_trash) {

    private var _binding : FragmentTrashBinding? = null
    private val binding get() = _binding!!

    private lateinit var notesViewModel : NoteViewModel
    private lateinit var noteAdapter: NoteAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTrashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notesViewModel = (activity as MainActivity).noteViewModel
        setupRecyclerView()
        observeTrashedNotes()
        setupSwipeActions()
        setupEmptyTrashMenu()
    }


    private fun setupSwipeActions() {
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
                        // swipe right → restore to home
                        notesViewModel.restoreFromTrash(note.id)
                        showUndoSnackbar("Note restored") {
                            notesViewModel.moveToTrash(note.id)
                        }
                    }
                    ItemTouchHelper.LEFT -> {
                        // swipe left → permanently delete, no undo
                        showPermanentDeleteDialog(note)
                    }
                }
            }


        })

        itemTouchHelper.attachToRecyclerView(binding.trashedRecyclerView)
    }

    private fun observeTrashedNotes() {
        notesViewModel.trashedNotes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.differ.submitList(notes)
        }
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter()
        noteAdapter.onSelectionChanged = {
            onSelectionChanged()
        }
        binding.trashedRecyclerView.apply {
            adapter = noteAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

        notesViewModel.spanCount.observe(viewLifecycleOwner) { spanCount ->
            (binding.trashedRecyclerView.layoutManager as? StaggeredGridLayoutManager)?.spanCount = spanCount
            requireActivity().invalidateOptionsMenu()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showPermanentDeleteDialog(note: Note) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete forever?")
            .setMessage("This note will be permanently deleted and cannot be recovered.")
            .setPositiveButton("Delete") { _, _ ->
                notesViewModel.permanentlyDelete(note.id)
            }
            .setNegativeButton("Cancel") { _, _ ->
                // refresh adapter to snap note back
                noteAdapter.notifyDataSetChanged()
            }
            .show()
    }

    private fun showUndoSnackbar(string: String, function: () -> Unit) {
        Snackbar.make(binding.root, string, Snackbar.LENGTH_LONG)
            .setAction("Undo") { function() }
            .show()
    }

    private fun setupEmptyTrashMenu() {
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        val isSelectionMode = noteAdapter.isInSelectionMode()

        val mainActivity = activity as? MainActivity
        val toolbar = mainActivity?.binding?.toolbar

        if (isSelectionMode) {
            inflater.inflate(R.menu.menu_trash_selection, menu)
            val count = noteAdapter.getSelectedNotes().size
            toolbar?.title = count.toString()
            toolbar?.setNavigationIcon(R.drawable.ic_cancel)
            toolbar?.setNavigationOnClickListener {
                noteAdapter.clearSelection()
                onSelectionChanged()
            }
            toolbar?.setBackgroundColor(resources.getColor(R.color.selectionColor, null))
        } else {
            inflater.inflate(R.menu.trash_menu, menu)
            toolbar?.title = "Trash"
            toolbar?.navigationIcon = null
            toolbar?.setBackgroundColor(resources.getColor(R.color.background_light, null))

            val toggleItem = menu.findItem(R.id.menu_toggle_list_view)
            toggleItem.title = if (notesViewModel.spanCount.value == 2) "List View" else "Grid View"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_empty_trash -> {
                showEmptyTrashDialog()
                true
            }
            R.id.menu_restore -> {
                notesViewModel.restoreNotesFromTrash(noteAdapter.getSelectedNotes())
                noteAdapter.clearSelection()
                onSelectionChanged()
                true
            }
            R.id.menu_permanently_delete -> {
                showBulkPermanentDeleteDialog()
                true
            }
            R.id.menu_archive -> {
                notesViewModel.archiveNotes(noteAdapter.getSelectedNotes())
                noteAdapter.clearSelection()
                onSelectionChanged()
                true
            }
            R.id.menu_toggle_list_view -> {
                notesViewModel.toggleSpanCount()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showBulkPermanentDeleteDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete selected forever?")
            .setMessage("Selected notes will be permanently deleted and cannot be recovered.")
            .setPositiveButton("Delete") { _, _ ->
                notesViewModel.permanentlyDeleteNotes(noteAdapter.getSelectedNotes())
                noteAdapter.clearSelection()
                onSelectionChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun onSelectionChanged() {
        requireActivity().invalidateOptionsMenu()
    }

    private fun showEmptyTrashDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Empty trash?")
            .setMessage("All notes in trash will be permanently deleted.")
            .setPositiveButton("Empty") { _, _ ->
                noteAdapter.differ.currentList.forEach { note ->
                    notesViewModel.permanentlyDelete(note.id)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}