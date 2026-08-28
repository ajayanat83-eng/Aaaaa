package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.local.CafeDatabase
import com.example.data.local.CategoryEntity
import com.example.data.local.ProductEntity
import com.example.model.*
import com.example.service.FirestoreConfig
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

    private var localDatabase: CafeDatabase? = null

    private fun getFirestoreSafe(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(tag, "FirebaseFirestore instance unavailable, falling back to local Room database: ${e.message}")
            null
        }
    }

    private val firestore: FirebaseFirestore?
        get() = getFirestoreSafe()

    private val _categoriesFlow = MutableStateFlow<List<Category>>(DefaultMenuData.categories)
    val categoriesFlow: StateFlow<List<Category>> = _categoriesFlow.asStateFlow()

    private val _productsFlow = MutableStateFlow<List<Product>>(DefaultMenuData.getInitialProducts())
    val productsFlow: StateFlow<List<Product>> = _productsFlow.asStateFlow()

    private val _syncStatus = MutableStateFlow("Initialized")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _feedbackList = MutableStateFlow<List<CustomerFeedback>>(emptyList())
    val feedbackList: StateFlow<List<CustomerFeedback>> = _feedbackList.asStateFlow()

    private val _couponsFlow = MutableStateFlow<List<Coupon>>(DefaultMenuData.defaultCoupons)
    val couponsFlow: StateFlow<List<Coupon>> = _couponsFlow.asStateFlow()

    private var categoriesListener: ListenerRegistration? = null
    private var productsListener: ListenerRegistration? = null
    private var feedbackListener: ListenerRegistration? = null
    private var couponsListener: ListenerRegistration? = null

    init {
        startRealtimeMenuSync()
    }

    /**
     * Initializes the local Room Database cache for offline-first support.
     */
    fun initLocalDatabase(context: Context) {
        if (localDatabase == null) {
            localDatabase = CafeDatabase.getDatabase(context)
            scope.launch {
                loadFromLocalRoomCache()
            }
        }
    }

    /**
     * Loads categories and products from Room cache into memory.
     */
    private suspend fun loadFromLocalRoomCache() {
        val db = localDatabase ?: return
        try {
            val cachedCategories = db.categoryDao().getAllCategoriesList()
            if (cachedCategories.isNotEmpty()) {
                _categoriesFlow.value = cachedCategories.map { it.toDomain() }
                Log.d(tag, "Loaded ${cachedCategories.size} categories from local Room DB cache.")
            } else {
                // Pre-populate Room cache with default menu
                db.categoryDao().insertCategories(DefaultMenuData.categories.map { CategoryEntity.fromDomain(it) })
            }

            val cachedProducts = db.productDao().getAllProductsList()
            if (cachedProducts.isNotEmpty()) {
                _productsFlow.value = cachedProducts.map { it.toDomain() }
                Log.d(tag, "Loaded ${cachedProducts.size} products from local Room DB cache.")
            } else {
                // Pre-populate Room cache with default products
                db.productDao().insertProducts(DefaultMenuData.getInitialProducts().map { ProductEntity.fromDomain(it) })
            }
        } catch (e: Exception) {
            Log.e(tag, "Error loading from local Room database cache: ${e.message}")
        }
    }

    /**
     * Starts Real-Time Snapshot Listeners for Categories & Products collections
     */
    fun startRealtimeMenuSync() {
        val db = firestore
        if (db == null) {
            _syncStatus.value = "Local Cache Mode (Offline Ready)"
            return
        }

        try {
            _isLoading.value = true

            // Listen to Categories
            categoriesListener?.remove()
            categoriesListener = db.collection(FirestoreSchema.COLLECTION_CATEGORIES)
                .orderBy(FirestoreSchema.FIELD_SORT_ORDER)
                .addSnapshotListener { snapshots, error ->
                    _isLoading.value = false
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
                            // Save to Room cache asynchronously
                            scope.launch {
                                localDatabase?.categoryDao()?.insertCategories(items.map { CategoryEntity.fromDomain(it) })
                            }
                        }
                    } else if (snapshots != null && snapshots.isEmpty) {
                        scope.launch { seedDefaultMenuIfEmpty() }
                    }
                }

            // Listen to Products
            productsListener?.remove()
            productsListener = db.collection(FirestoreSchema.COLLECTION_PRODUCTS)
                .orderBy(FirestoreSchema.FIELD_SORT_ORDER)
                .addSnapshotListener { snapshots, error ->
                    _isLoading.value = false
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
                            // Save to Room cache asynchronously
                            scope.launch {
                                localDatabase?.productDao()?.insertProducts(items.map { ProductEntity.fromDomain(it) })
                            }
                        }
                    }
                }

            // Listen to Feedback Collection
            feedbackListener?.remove()
            feedbackListener = db.collection(FirestoreConfig.FEEDBACK)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w(tag, "Feedback listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        val items = snapshots.documents.mapNotNull { doc ->
                            doc.toObject(FirestoreFeedback::class.java)?.toDomain()
                        }
                        _feedbackList.value = items
                    }
                }

            // Listen to Coupons Collection in Firestore
            couponsListener?.remove()
            couponsListener = db.collection(FirestoreConfig.COUPONS)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.w(tag, "Coupons listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshots != null && !snapshots.isEmpty) {
                        val items = snapshots.documents.mapNotNull { doc ->
                            doc.toObject(FirestoreCoupon::class.java)?.toDomain()
                        }
                        if (items.isNotEmpty()) {
                            _couponsFlow.value = items
                        }
                    }
                }

            _syncStatus.value = "Connected (Cloud Firestore + Room Cache)"
        } catch (e: Exception) {
            Log.e(tag, "Failed to start Firestore snapshot listeners: ${e.message}")
            _syncStatus.value = "Offline Room Cache Mode"
            _isLoading.value = false
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
                Log.d(tag, "Seeding default categories & products to Firestore...")
                val batch = db.batch()

                DefaultMenuData.categories.forEach { cat ->
                    val doc = db.collection(FirestoreSchema.COLLECTION_CATEGORIES).document(cat.id)
                    batch.set(doc, FirestoreCategory.fromDomain(cat))
                }

                DefaultMenuData.getInitialProducts().forEach { prod ->
                    val doc = db.collection(FirestoreSchema.COLLECTION_PRODUCTS).document(prod.productId)
                    batch.set(doc, FirestoreProduct.fromDomain(prod))
                }

                DefaultMenuData.defaultCoupons.forEach { coup ->
                    val doc = db.collection(FirestoreConfig.COUPONS).document(coup.code)
                    batch.set(doc, FirestoreCoupon.fromDomain(coup))
                }

                batch.commit().await()
                Log.d(tag, "Default menu & coupons seeded successfully to Firestore.")
            }
        } catch (e: Exception) {
            Log.w(tag, "Error seeding menu to Firestore: ${e.message}")
        }
    }

    /**
     * Creates or updates a Category in Firestore & Room Cache
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

        // Update local Room database cache
        try {
            localDatabase?.categoryDao()?.insertCategory(CategoryEntity.fromDomain(category))
        } catch (e: Exception) {
            Log.e(tag, "Error saving category to Room cache: ${e.message}")
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
     * Deletes a Category from Firestore & Room Cache
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

        try {
            localDatabase?.categoryDao()?.deleteCategoryById(categoryId)
        } catch (e: Exception) {
            Log.e(tag, "Error deleting category from Room cache: ${e.message}")
        }

        _categoriesFlow.value = _categoriesFlow.value.filterNot { it.id == categoryId }
    }

    /**
     * Creates or updates a Product in Firestore & Room Cache
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

        // Update local Room database cache
        try {
            localDatabase?.productDao()?.insertProduct(ProductEntity.fromDomain(product))
        } catch (e: Exception) {
            Log.e(tag, "Error saving product to Room cache: ${e.message}")
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
     * Deletes a Product from Firestore & Room Cache
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

        try {
            localDatabase?.productDao()?.deleteProductById(productId)
        } catch (e: Exception) {
            Log.e(tag, "Error deleting product from Room cache: ${e.message}")
        }

        _productsFlow.value = _productsFlow.value.filterNot { it.productId == productId }
    }

    /**
     * Saves Customer Dining & Service Feedback to Firestore 'feedback' collection
     */
    suspend fun submitFeedback(feedback: CustomerFeedback): Boolean {
        val db = firestore
        var savedRemote = false
        if (db != null) {
            try {
                val firestoreFeedback = FirestoreFeedback.fromDomain(feedback)
                db.collection(FirestoreConfig.FEEDBACK)
                    .document(feedback.feedbackId)
                    .set(firestoreFeedback)
                    .await()
                savedRemote = true
                Log.d(tag, "Feedback saved to Firestore 'feedback' collection: ${feedback.feedbackId}")
            } catch (e: Exception) {
                Log.e(tag, "Error submitting feedback to Firestore: ${e.message}")
            }
        }

        // Append to local state list immediately
        _feedbackList.value = listOf(feedback) + _feedbackList.value
        return savedRemote || true
    }

    /**
     * Validates a coupon code directly against the 'coupons' collection in Firestore.
     * Computes percentage-based discount or fixed discount with max-discount limit and min-order amount.
     */
    suspend fun validateCoupon(rawCode: String, subtotal: Double): CouponValidationResult {
        val code = rawCode.trim().uppercase()
        if (code.isBlank()) {
            return CouponValidationResult.Error("Please enter a valid coupon code")
        }

        // 1. First try direct query to Firestore 'coupons' collection
        val db = firestore
        var fetchedCoupon: Coupon? = null

        if (db != null) {
            try {
                val doc = db.collection(FirestoreConfig.COUPONS).document(code).get().await()
                if (doc.exists()) {
                    fetchedCoupon = doc.toObject(FirestoreCoupon::class.java)?.toDomain()
                } else {
                    // Search by code field if document ID didn't match directly
                    val querySnapshot = db.collection(FirestoreConfig.COUPONS)
                        .whereEqualTo("code", code)
                        .limit(1)
                        .get()
                        .await()
                    if (!querySnapshot.isEmpty) {
                        fetchedCoupon = querySnapshot.documents.first().toObject(FirestoreCoupon::class.java)?.toDomain()
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "Firestore coupon query error: ${e.message}, checking cached coupons.")
            }
        }

        // Fallback to local cached / default coupons flow
        if (fetchedCoupon == null) {
            fetchedCoupon = _couponsFlow.value.find { it.code.equals(code, ignoreCase = true) }
        }

        if (fetchedCoupon == null) {
            return CouponValidationResult.Error("Coupon code '$code' is invalid or does not exist")
        }

        if (!fetchedCoupon.isActive) {
            return CouponValidationResult.Error("Coupon '$code' is currently inactive or expired")
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime < fetchedCoupon.validFrom || currentTime > fetchedCoupon.validTo) {
            return CouponValidationResult.Error("Coupon '$code' has expired or is not yet valid")
        }

        if (subtotal < fetchedCoupon.minOrderAmount) {
            return CouponValidationResult.Error(
                "Min order amount for '$code' is NPR ${fetchedCoupon.minOrderAmount.toInt()} (Current: NPR ${subtotal.toInt()})"
            )
        }

        val discountAmount = when (fetchedCoupon.discountType) {
            DiscountType.PERCENTAGE -> {
                val calculated = (subtotal * fetchedCoupon.discountValue) / 100.0
                calculated.coerceAtMost(fetchedCoupon.maxDiscount)
            }
            DiscountType.FIXED -> {
                fetchedCoupon.discountValue.coerceAtMost(subtotal)
            }
        }

        val discountDesc = if (fetchedCoupon.discountType == DiscountType.PERCENTAGE) {
            "${fetchedCoupon.discountValue.toInt()}% off"
        } else {
            "NPR ${fetchedCoupon.discountValue.toInt()} off"
        }

        return CouponValidationResult.Success(
            coupon = fetchedCoupon,
            discountAmount = discountAmount,
            message = "Applied '$code'! Saved NPR ${discountAmount.toInt()} ($discountDesc)"
        )
    }

    /**
     * Saves or updates a coupon in Firestore 'coupons' collection
     */
    suspend fun saveCoupon(coupon: Coupon) {
        val db = firestore
        if (db != null) {
            try {
                db.collection(FirestoreConfig.COUPONS)
                    .document(coupon.code)
                    .set(FirestoreCoupon.fromDomain(coupon), SetOptions.merge())
                    .await()
            } catch (e: Exception) {
                Log.e(tag, "Error saving coupon to Firestore: ${e.message}")
            }
        }

        val current = _couponsFlow.value.toMutableList()
        val idx = current.indexOfFirst { it.code.equals(coupon.code, ignoreCase = true) }
        if (idx >= 0) {
            current[idx] = coupon
        } else {
            current.add(coupon)
        }
        _couponsFlow.value = current
    }

    companion object {
        val instance: FirestoreMenuRepository by lazy { FirestoreMenuRepository() }
    }
}

sealed class CouponValidationResult {
    data class Success(val coupon: Coupon, val discountAmount: Double, val message: String) : CouponValidationResult()
    data class Error(val message: String) : CouponValidationResult()
}

