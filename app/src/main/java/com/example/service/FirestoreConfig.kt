package com.example.service

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Firestore Database Configuration & Collection Paths Service.
 * Centralizes collection paths and reference generators to maintain
 * a clean, consistent, and type-safe database architecture.
 */
object FirestoreConfig {

    // ==================== TOP-LEVEL COLLECTION PATHS ====================
    const val PRODUCTS = "products"
    const val CATEGORIES = "categories"
    const val USERS = "users"
    const val ORDERS = "orders"
    const val KOTS = "kots"
    const val TABLES = "tables"
    const val SETTINGS = "settings"
    const val COUPONS = "coupons"
    const val AUDIT_LOGS = "audit_logs"
    const val FEEDBACK = "feedback"

    // ==================== SUB-COLLECTION PATHS ====================
    const val SUB_COLLECTION_PAYMENTS = "payments"
    const val SUB_COLLECTION_ITEMS = "items"
    const val SUB_COLLECTION_ADDRESSES = "addresses"
    const val SUB_COLLECTION_LOYALTY = "loyalty_history"

    // ==================== FIELD NAMES & QUERY KEYS ====================
    object Fields {
        const val PRODUCT_ID = "productId"
        const val CATEGORY_ID = "categoryId"
        const val PRICE = "price"
        const val IS_AVAILABLE = "isAvailable"
        const val IS_FEATURED = "isFeatured"
        const val IS_BEST_SELLER = "isBestSeller"
        const val SORT_ORDER = "sortOrder"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
        const val USER_ID = "uid"
        const val PHONE_NUMBER = "phoneNumber"
        const val ORDER_STATUS = "orderStatus"
        const val PAYMENT_STATUS = "paymentStatus"
        const val TABLE_STATUS = "status"
    }

    /**
     * Obtains the shared FirebaseFirestore instance safely.
     */
    val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }

    // ==================== COLLECTION REFERENCES ====================

    fun getProductsCollection(db: FirebaseFirestore? = firestore): CollectionReference? =
        db?.collection(PRODUCTS)

    fun getCategoriesCollection(db: FirebaseFirestore? = firestore): CollectionReference? =
        db?.collection(CATEGORIES)

    fun getUsersCollection(db: FirebaseFirestore? = firestore): CollectionReference? =
        db?.collection(USERS)

    fun getOrdersCollection(db: FirebaseFirestore? = firestore): CollectionReference? =
        db?.collection(ORDERS)

    fun getKotsCollection(db: FirebaseFirestore? = firestore): CollectionReference? =
        db?.collection(KOTS)

    fun getTablesCollection(db: FirebaseFirestore? = firestore): CollectionReference? =
        db?.collection(TABLES)

    fun getFeedbackCollection(db: FirebaseFirestore? = firestore): CollectionReference? =
        db?.collection(FEEDBACK)

    // ==================== DOCUMENT REFERENCES ====================

    fun getProductDocument(productId: String, db: FirebaseFirestore? = firestore): DocumentReference? =
        getProductsCollection(db)?.document(productId)

    fun getCategoryDocument(categoryId: String, db: FirebaseFirestore? = firestore): DocumentReference? =
        getCategoriesCollection(db)?.document(categoryId)

    fun getUserDocument(userId: String, db: FirebaseFirestore? = firestore): DocumentReference? =
        getUsersCollection(db)?.document(userId)

    fun getOrderDocument(orderId: String, db: FirebaseFirestore? = firestore): DocumentReference? =
        getOrdersCollection(db)?.document(orderId)

    // ==================== SUB-COLLECTION REFERENCES ====================

    fun getOrderPaymentsCollection(orderId: String, db: FirebaseFirestore? = firestore): CollectionReference? =
        getOrderDocument(orderId, db)?.collection(SUB_COLLECTION_PAYMENTS)

    fun getUserLoyaltyCollection(userId: String, db: FirebaseFirestore? = firestore): CollectionReference? =
        getUserDocument(userId, db)?.collection(SUB_COLLECTION_LOYALTY)
}
