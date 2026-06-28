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
import com.example.notetakingapp.model.Note
import com.example.notetakingapp.viewmodel.NoteViewModel

class UpdateNoteFragment : Fragment(R.layout.fragment_update_note) {

    private var _binding : FragmentUpdateNoteBinding? = null
    private val binding get() = _binding!!

    private lateinit var currentNote : Note

    private lateinit var notesViewModel : NoteViewModel
    //argument is for passing data from one screen to another
    //here we pass the argument note to the UpdateNoteFragment from HomeFragment
    private val args : UpdateNoteFragmentArgs by navArgs()


    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notesViewModel = (activity as MainActivity).noteViewModel
        currentNote = args.note!!

        binding.etNoteTitleUpdate.setText(currentNote.noteTitle)
        binding.etNoteBodyUpdate.setText(currentNote.noteBody)

        binding.fabDone.setOnClickListener {
            val title = binding.etNoteTitleUpdate.text.toString().trim()
            val body = binding.etNoteBodyUpdate.text.toString().trim()
            if(title.isNotEmpty()){
                val note = Note(currentNote.id, title, body, currentNote.isPinned, currentNote.isArchived)
                notesViewModel.updateNote(note)
                view.findNavController().navigate(R.id.action_updateNoteFragment_to_homeFragment)
            }else{
                Toast.makeText(context, "Please Enter Note Title", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteNote(){
        println(AlertDialog.Builder(activity).apply {
            setTitle("Delete Note")
            setMessage("Are you sure you want to delete this note?")
            setPositiveButton("Delete") { _, _ ->
                notesViewModel.deleteNote(currentNote)
                view?.findNavController()?.navigate(R.id.action_updateNoteFragment_to_homeFragment)
            }
            setNegativeButton("Cancel", null)

        }.create().show())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_update_note, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.menu_delete ->
                deleteNote()
            R.id.menu_pin -> {
                togglePin(currentNote.id)
                if(currentNote.isPinned){
                    Toast.makeText(context, "Note Pinned", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, "Note Unpinned", Toast.LENGTH_SHORT).show()
                }
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

    private fun togglePin(noteId : Int){
        notesViewModel.togglePinnedStatus(noteId)
    }

}