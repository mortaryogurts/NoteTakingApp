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
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.notetakingapp.R
import com.example.notetakingapp.adapter.NoteAdapter
import com.example.notetakingapp.databinding.FragmentHomeBinding
import com.example.notetakingapp.model.Note
import com.example.notetakingapp.viewmodel.NoteViewModel

class HomeFragment : Fragment(R.layout.fragment_home), SearchView.OnQueryTextListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var notesViewModel: NoteViewModel
    private lateinit var noteAdapter: NoteAdapter


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
        inflater.inflate(R.menu.home_menu, menu)

        val menuSearch = menu.findItem(R.id.menu_search).actionView as SearchView
        menuSearch.isSubmitButtonEnabled = false

        val isSelectionMode = noteAdapter.isInSelectionMode()
        menu.findItem(R.id.menu_pin).isVisible = isSelectionMode
        menu.findItem(R.id.menu_delete).isVisible = isSelectionMode

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
            menu.findItem(R.id.menu_search).isVisible = false
        } else {
            toolbar?.title = "Notes"
            toolbar?.navigationIcon = null
            toolbar?.setBackgroundColor(resources.getColor(R.color.background_light, null))
            menu.findItem(R.id.menu_search).isVisible = true
        }

        menuSearch.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_pin -> {
                notesViewModel.pinNotes(noteAdapter.getSelectedNotes())
                noteAdapter.clearSelection()
                requireActivity().invalidateOptionsMenu()
                true
            }
            R.id.menu_delete -> {
                notesViewModel.deleteNotes(noteAdapter.getSelectedNotes())
                noteAdapter.clearSelection()
                requireActivity().invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
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

}