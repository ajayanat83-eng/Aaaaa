package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM cached_categories ORDER BY sortOrder ASC")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM cached_categories ORDER BY sortOrder ASC")
    suspend fun getAllCategoriesList(): List<CategoryEntity>

    @Query("SELECT * FROM cached_categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("DELETE FROM cached_categories WHERE id = :id")
    suspend fun deleteCategoryById(id: String)

    @Query("DELETE FROM cached_categories")
    suspend fun clearAllCategories()
}
