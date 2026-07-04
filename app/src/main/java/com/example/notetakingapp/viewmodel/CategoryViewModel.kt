package com.example.notetakingapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notetakingapp.model.Categories
import com.example.notetakingapp.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel(private val categoryRepository: CategoryRepository) : ViewModel() {

    val allCategories : LiveData<List<Categories>> =  categoryRepository.getAllCategories()

    private val _selectedCategory = MutableLiveData<Int?>(null)
    val selectedCategory : LiveData<Int?> = _selectedCategory

    fun insertCategory(category: Categories){
        viewModelScope.launch {
            categoryRepository.insertCategory(category)
        }
    }

    fun deleteCategory(category : Categories){
        viewModelScope.launch {
            categoryRepository.deleteCategory(category)
        }
    }

    fun renameCategory(categoryId : Int, newName : String) {
        viewModelScope.launch {
            categoryRepository.renameCategory(categoryId, newName)
        }
    }

    fun selectCategory(categoryId : Int?){
        _selectedCategory.value = categoryId
    }
}