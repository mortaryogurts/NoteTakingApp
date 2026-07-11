package com.example.notetakingapp.views

import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.notetakingapp.R
import com.example.notetakingapp.database.NoteDatabase
import com.example.notetakingapp.databinding.ActivityMainBinding
import com.example.notetakingapp.model.Categories
import com.example.notetakingapp.repository.CategoryRepository
import com.example.notetakingapp.repository.NoteRepository
import com.example.notetakingapp.viewmodel.CategoryViewModel
import com.example.notetakingapp.viewmodel.CategoryViewModelFactory
import com.example.notetakingapp.viewmodel.NoteViewModel
import com.example.notetakingapp.viewmodel.NoteViewModelFactory
import com.example.notetakingapp.workmanger.DeleteOldNotesWorker
import android.widget.EditText
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    lateinit var noteViewModel: NoteViewModel
    lateinit var categoryViewModel : CategoryViewModel
    lateinit var binding: ActivityMainBinding
    lateinit var drawerToggle : ActionBarDrawerToggle
    private lateinit var navController: NavController
    private var currentCategories: List<Categories> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduleAutoDelete()
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setUpViewModel()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        setUpDrawer()
        observeCategories()

        // Windows insets are now handled via fitsSystemWindows="true" in XML
    }

    private fun setUpDrawer() {
        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.drawer_open,
            R.string.drawer_close)

        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_all_notes -> {
                    noteViewModel.setSelectedCategory(null)
                    navController.navigate(R.id.homeFragment)
                    menuItem.isChecked = true
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_archive -> {
                    navController.navigate(R.id.archiveFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_trash -> {
                    navController.navigate(R.id.trashFragment)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_add_category -> {
                    showAddCategoryDialog()
                    binding.drawerLayout.closeDrawers()
                    true
                }
                else -> {
                    val category = currentCategories.find { it.id == menuItem.itemId }
                    category?.let {
                        showCategoryOptionsDialog(it)
                    }
                    true
                }
            }
        }
        
        // Handle drawer lock based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    binding.navigationView.setCheckedItem(R.id.nav_all_notes)
                }
                R.id.archiveFragment -> {
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    binding.navigationView.setCheckedItem(R.id.nav_archive)
                }
                R.id.trashFragment -> {
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    binding.navigationView.setCheckedItem(R.id.nav_trash)
                }
                else -> {
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
            }
        }
    }

    private fun observeCategories() {
        categoryViewModel.allCategories.observe(this) { categories ->
            val menu = binding.navigationView.menu
            currentCategories = categories

            menu.removeGroup(R.id.group_categories)

            categories.forEach { category ->
                menu.add(R.id.group_categories, category.id, Menu.NONE, category.name)
                    .setCheckable(true)
                    .setIcon(R.drawable.ic_folder)
            }

            menu.add(R.id.group_categories, R.id.nav_add_category, Menu.NONE, "New Category")
                .setIcon(R.drawable.ic_add)
        }
    }

    private fun showCategoryOptionsDialog(category: Categories) {
        val options = arrayOf("Open", "Rename", "Delete")

        AlertDialog.Builder(this)
            .setTitle(category.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        noteViewModel.setSelectedCategory(category.id)
                        navController.navigate(R.id.homeFragment)
                        binding.navigationView.setCheckedItem(category.id)
                        binding.drawerLayout.closeDrawers()
                    }
                    1 -> showRenameCategoryDialog(category)
                    2 -> showDeleteCategoryDialog(category)
                }
            }
            .show()
    }

    private fun showAddCategoryDialog() {
        val editText = EditText(this).apply {
            hint = "Category name"
            inputType = InputType.TYPE_CLASS_TEXT
            setPadding(48, 24, 48, 24)
        }

        AlertDialog.Builder(this)
            .setTitle("New Category")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    categoryViewModel.insertCategory(Categories(0, name, false))
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRenameCategoryDialog(category: Categories) {
        val editText = EditText(this).apply {
            hint = "New name"
            setText(category.name)
            inputType = InputType.TYPE_CLASS_TEXT
            setPadding(48, 24, 48, 24)
        }

        AlertDialog.Builder(this)
            .setTitle("Rename Category")
            .setView(editText)
            .setPositiveButton("Rename") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    categoryViewModel.renameCategory(category.id, newName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteCategoryDialog(category: Categories) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("\"${category.name}\" will be deleted. Notes in this category will become uncategorized.")
            .setPositiveButton("Delete") { _, _ ->
                categoryViewModel.deleteCategory(category)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setUpViewModel() {
        val noteRepository = NoteRepository(NoteDatabase(this))
        val categoryRepository = CategoryRepository(NoteDatabase(this).getCategoryDao())
        val viewModelProviderFactory = NoteViewModelFactory(application, noteRepository)
        val categoryViewModelProviderFactory = CategoryViewModelFactory(categoryRepository)
        noteViewModel = ViewModelProvider(
            this,
            viewModelProviderFactory
        ).get(NoteViewModel::class.java)
        categoryViewModel = ViewModelProvider(
            this,
            categoryViewModelProviderFactory
        ).get(CategoryViewModel::class.java)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun scheduleAutoDelete() {
        val deleteRequest = PeriodicWorkRequestBuilder<DeleteOldNotesWorker>(
            1, TimeUnit.DAYS // runs once every day
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true) // don't run on low battery
                    .build()
            )
            .build()


        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "delete_old_trashed_notes",        // unique name prevents duplicate workers
            ExistingPeriodicWorkPolicy.KEEP,   // if already scheduled, keep existing
            deleteRequest
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}