package com.example.notetakingapp.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notetakingapp.R
import com.example.notetakingapp.adapter.NoteAdapter
import com.example.notetakingapp.databinding.FragmentHomeBinding
import com.example.notetakingapp.model.Note
import com.example.notetakingapp.viewmodel.NoteViewModel
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment(R.layout.fragment_home), SearchView.OnQueryTextListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var notesViewModel: NoteViewModel
    private lateinit var noteAdapter: NoteAdapter

    // Removed local col variable



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notesViewModel = (activity as MainActivity).noteViewModel


        setUpRecyclerView()
        binding.fabAddButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_homeFragment_to_newNoteFragment)
        }

        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.title = "Selected: ${noteAdapter.getSelectedNotes().size}"

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
                        // swipe right → archive
                        notesViewModel.archiveNote(note.id)
                        showUndoSnackbar("Note archived") {
                            notesViewModel.unArchiveNote(note.id)
                        }
                    }
                    ItemTouchHelper.LEFT -> {
                        // swipe left → trash
                        notesViewModel.moveToTrash(note.id)
                        showUndoSnackbar("Note moved to trash") {
                            notesViewModel.restoreFromTrash(note.id)
                        }
                    }
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.recyclerView)

    }

    private fun setUpRecyclerView() {
        noteAdapter = NoteAdapter()
        noteAdapter.onSelectionChanged = {
            onSelectionChanged()
        }
        binding.recyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL
            )
            setHasFixedSize(true)
            adapter = noteAdapter
        }

        notesViewModel.spanCount.observe(viewLifecycleOwner) { spanCount ->
            (binding.recyclerView.layoutManager as? StaggeredGridLayoutManager)?.spanCount = spanCount
            requireActivity().invalidateOptionsMenu()
        }

        activity?.let {

            notesViewModel.notes.observe(viewLifecycleOwner) { note ->
                noteAdapter.differ.submitList(note)
                updateUI(note)
            }
        }

    }

    private fun updateUI(notes: List<Note>) {
        if (notes.isNotEmpty()) {
            binding.cardView.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        } else {
            binding.cardView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        
        val isSelectionMode = noteAdapter.isInSelectionMode()
        
        if (isSelectionMode) {
            inflater.inflate(R.menu.menu_home_selection, menu)
            // Add Pin and Delete to selection menu if they are not already there
            if (menu.findItem(R.id.menu_pin) == null) {
                inflater.inflate(R.menu.home_menu, menu)
                menu.findItem(R.id.menu_search).isVisible = false
                menu.findItem(R.id.action_archive).isVisible = false
                menu.findItem(R.id.action_trash).isVisible = false
                menu.findItem(R.id.menu_pin).isVisible = true
                menu.findItem(R.id.menu_delete).isVisible = true
            }
        } else {
            inflater.inflate(R.menu.home_menu, menu)
            val menuSearch = menu.findItem(R.id.menu_search).actionView as SearchView
            menuSearch.isSubmitButtonEnabled = false
            menuSearch.setOnQueryTextListener(this)

            val toggleItem = menu.findItem(R.id.menu_toggle_list_view)
            toggleItem.title = if (notesViewModel.spanCount.value == 2) "List View" else "Grid View"
        }

        val mainActivity = activity as? MainActivity
        val toolbar = mainActivity?.binding?.toolbar
        if (isSelectionMode) {
            val count = noteAdapter.getSelectedNotes().size
            toolbar?.title = count.toString()
            toolbar?.setNavigationIcon(R.drawable.ic_cancel)
            toolbar?.setNavigationOnClickListener {
                noteAdapter.clearSelection()
                onSelectionChanged()
            }
            toolbar?.setBackgroundColor(resources.getColor(R.color.selectionColor, null))
        } else {
            toolbar?.title = "Notes"
            toolbar?.navigationIcon = null
            toolbar?.setBackgroundColor(resources.getColor(R.color.background_light, null))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_pin -> {
                notesViewModel.pinNotes(noteAdapter.getSelectedNotes())
                noteAdapter.clearSelection()
                onSelectionChanged()
                return true
            }
            R.id.menu_delete -> {
                notesViewModel.moveNotesToTrash(noteAdapter.getSelectedNotes())
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
            R.id.menu_archive -> {
                notesViewModel.archiveNotes(noteAdapter.getSelectedNotes())
                noteAdapter.clearSelection()
                onSelectionChanged()
                return true
            }
            R.id.action_archive -> {
                findNavController().navigate(R.id.action_homeFragment_to_archiveFragment)
                return true
            }
            R.id.action_trash -> {
                findNavController().navigate(R.id.action_homeFragment_to_trashFragment)
                return true
            }
            R.id.menu_toggle_list_view -> {
                notesViewModel.toggleSpanCount()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextChange(p0: String?): Boolean {
        notesViewModel.searchNotes(p0 ?: "")
        return true

    }

    override fun onQueryTextSubmit(p0: String?): Boolean {
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun onSelectionChanged(){
        requireActivity().invalidateOptionsMenu() //triggers onCreateOptionsMenu() again
    }

    private fun showUndoSnackbar(message: String, onUndo: () -> Unit) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Undo") { onUndo() }
            .show()
    }

}