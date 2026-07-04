package com.example.notetakingapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.notetakingapp.model.Categories


@Dao
interface CategoryDao {

    @Query("SELECT * FROM CATEGORIES ORDER BY isDefault DESC, name ASC")
    fun getAllCategories() : LiveData<List<Categories>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Categories)

    @Delete
    suspend fun deleteCategory(category: Categories)

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): Categories?

    @Query("UPDATE categories SET name = :newName WHERE id = :categoryId")
    suspend fun renameCategory(categoryId: Int, newName: String)
}