package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM cached_products ORDER BY sortOrder ASC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM cached_products ORDER BY sortOrder ASC")
    suspend fun getAllProductsList(): List<ProductEntity>

    @Query("SELECT * FROM cached_products WHERE categoryId = :categoryId ORDER BY sortOrder ASC")
    fun getProductsByCategoryFlow(categoryId: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM cached_products WHERE productId = :productId LIMIT 1")
    suspend fun getProductById(productId: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("DELETE FROM cached_products WHERE productId = :productId")
    suspend fun deleteProductById(productId: String)

    @Query("DELETE FROM cached_products")
    suspend fun clearAllProducts()
}
