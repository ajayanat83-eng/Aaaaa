package com.example.data

import android.util.Log
import com.example.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreMenuRepository private constructor() {

    private val tag = "TJW_FirestoreMenu"
    private val scope = CoroutineScope(Dispatchers.IO)

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(tag, "FirebaseFirestore instance error: ${e.message}")
            null
        }
    }

    private val _categoriesFlow = MutableStateFlow<List<Category>>(DefaultMenuData.categories)
    val categoriesFlow: StateFlow<List<Category>> = _categoriesFlow.asStateFlow()

    private val _productsFlow = MutableStateFlow<List<Product>>(DefaultMenuData.getInitialProducts())
    val productsFlow: StateFlow<List<Product>> = _productsFlow.asStateFlow()

    private val _syncStatus = MutableStateFlow("Initialized")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private var categoriesListener: ListenerRegistration? = null
    private var productsListener: ListenerRegistration? = null

    init {
        startRealtimeMenuSync()
    }

    /**
     * Starts Real-Time Snapshot Listeners for Categories & Products collections
     */
    fun startRealtimeMenuSync() {
        val db = firestore
        if (db == null) {
            _syncStatus.value = "Local In-Memory Mode (Firebase Pending)"
            return
        }

        try {
            // Listen to Categories
            categoriesListener?.remove()
            categoriesListener = db.collection(FirestoreSchema.COLLECTION_CATEGORIES)
                .orderBy(FirestoreSchema.FIELD_SORT_ORDER)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w(tag, "Categories listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshots != null && !snapshots.isEmpty) {
                        val items = snapshots.documents.mapNotNull { doc ->
                            doc.toObject(FirestoreCategory::class.java)?.toDomain()
                        }
                        if (items.isNotEmpty()) {
                            _categoriesFlow.value = items
                        }
                    } else if (snapshots != null && snapshots.isEmpty) {
                        // Optionally seed if Firestore collection is completely blank
                        scope.launch { seedDefaultMenuIfEmpty() }
                    }
                }

            // Listen to Products
            productsListener?.remove()
            productsListener = db.collection(FirestoreSchema.COLLECTION_PRODUCTS)
                .orderBy(FirestoreSchema.FIELD_SORT_ORDER)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w(tag, "Products listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshots != null && !snapshots.isEmpty) {
                        val items = snapshots.documents.mapNotNull { doc ->
                            doc.toObject(FirestoreProduct::class.java)?.toDomain()
                        }
                        if (items.isNotEmpty()) {
                            _productsFlow.value = items
                        }
                    }
                }

            _syncStatus.value = "Connected (Cloud Firestore)"
        } catch (e: Exception) {
            Log.e(tag, "Failed to start Firestore snapshot listeners: ${e.message}")
            _syncStatus.value = "Offline Mode"
        }
    }

    /**
     * Uploads initial default Janakpur Waffle & Cafe menu data into Firestore
     */
    suspend fun seedDefaultMenuIfEmpty() {
        val db = firestore ?: return
        try {
            val categoriesSnapshot = db.collection(FirestoreSchema.COLLECTION_CATEGORIES).get().await()
            if (categoriesSnapshot.isEmpty) {
                Log.d(tag, "Seeding default categories to Firestore...")
                val batch = db.batch()

                DefaultMenuData.categories.forEach { cat ->
                    val doc = db.collection(FirestoreSchema.COLLECTION_CATEGORIES).document(cat.id)
                    batch.set(doc, FirestoreCategory.fromDomain(cat))
                }

                DefaultMenuData.getInitialProducts().forEach { prod ->
                    val doc = db.collection(FirestoreSchema.COLLECTION_PRODUCTS).document(prod.productId)
                    batch.set(doc, FirestoreProduct.fromDomain(prod))
                }

                batch.commit().await()
                Log.d(tag, "Default menu seeded successfully to Firestore.")
            }
        } catch (e: Exception) {
            Log.w(tag, "Error seeding menu to Firestore: ${e.message}")
        }
    }

    /**
     * Creates or updates a Category in Firestore
     */
    suspend fun saveCategory(category: Category) {
        val db = firestore
        if (db != null) {
            try {
                val firestoreCategory = FirestoreCategory.fromDomain(category)
                db.collection(FirestoreSchema.COLLECTION_CATEGORIES)
                    .document(category.id)
                    .set(firestoreCategory, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.e(tag, "Error saving category to Firestore: ${e.message}")
            }
        }
        // Also update local state flow immediately
        val current = _categoriesFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == category.id }
        if (index >= 0) {
            current[index] = category
        } else {
            current.add(category)
        }
        _categoriesFlow.value = current
    }

    /**
     * Deletes a Category from Firestore
     */
    suspend fun deleteCategory(categoryId: String) {
        val db = firestore
        if (db != null) {
            try {
                db.collection(FirestoreSchema.COLLECTION_CATEGORIES)
                    .document(categoryId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                Log.e(tag, "Error deleting category from Firestore: ${e.message}")
            }
        }
        _categoriesFlow.value = _categoriesFlow.value.filterNot { it.id == categoryId }
    }

    /**
     * Creates or updates a Product in Firestore
     */
    suspend fun saveProduct(product: Product) {
        val db = firestore
        if (db != null) {
            try {
                val firestoreProduct = FirestoreProduct.fromDomain(product)
                db.collection(FirestoreSchema.COLLECTION_PRODUCTS)
                    .document(product.productId)
                    .set(firestoreProduct, SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.e(tag, "Error saving product to Firestore: ${e.message}")
            }
        }
        val current = _productsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.productId == product.productId }
        if (index >= 0) {
            current[index] = product
        } else {
            current.add(product)
        }
        _productsFlow.value = current
    }

    /**
     * Deletes a Product from Firestore
     */
    suspend fun deleteProduct(productId: String) {
        val db = firestore
        if (db != null) {
            try {
                db.collection(FirestoreSchema.COLLECTION_PRODUCTS)
                    .document(productId)
                    .delete()
                    .await()
            } catch (e: Exception) {
                Log.e(tag, "Error deleting product from Firestore: ${e.message}")
            }
        }
        _productsFlow.value = _productsFlow.value.filterNot { it.productId == productId }
    }

    companion object {
        val instance: FirestoreMenuRepository by lazy { FirestoreMenuRepository() }
    }
}
