package com.example.notetakingapp.repository

import androidx.lifecycle.LiveData
import com.example.notetakingapp.database.CategoryDao
import com.example.notetakingapp.model.Categories
import com.example.notetakingapp.model.Note

class CategoryRepository(private val categoryDao : CategoryDao) {

    fun getAllCategories() : LiveData<List<Categories>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: Categories) = categoryDao.insertCategory(category)

    suspend fun deleteCategory(category: Categories) = categoryDao.deleteCategory(category)

    suspend fun getCategoryById(categoryId: Int): Categories? = categoryDao.getCategoryById(categoryId)

    suspend fun renameCategory(categoryId: Int, newName: String) = categoryDao.renameCategory(categoryId, newName)

}