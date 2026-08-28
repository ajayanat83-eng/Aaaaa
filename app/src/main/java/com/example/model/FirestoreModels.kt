package com.example.model

import com.example.service.FirestoreConfig
import kotlinx.serialization.Serializable

/**
 * Cloud Firestore Collection & Document Schema Constants
 * The Janakpur Waffle & Cafe (TJW Cafe) Architecture
 */
object FirestoreSchema {
    // Top-Level Collections mapped to FirestoreConfig
    const val COLLECTION_CATEGORIES = FirestoreConfig.CATEGORIES
    const val COLLECTION_PRODUCTS = FirestoreConfig.PRODUCTS
    const val COLLECTION_ORDERS = FirestoreConfig.ORDERS
    const val COLLECTION_KOTS = FirestoreConfig.KOTS
    const val COLLECTION_TABLES = FirestoreConfig.TABLES
    const val COLLECTION_USERS = FirestoreConfig.USERS
    const val COLLECTION_SETTINGS = FirestoreConfig.SETTINGS
    const val COLLECTION_COUPONS = FirestoreConfig.COUPONS
    const val COLLECTION_AUDIT_LOGS = FirestoreConfig.AUDIT_LOGS

    // Document Keys & Fields for Queries
    const val FIELD_CATEGORY_ID = FirestoreConfig.Fields.CATEGORY_ID
    const val FIELD_IS_AVAILABLE = FirestoreConfig.Fields.IS_AVAILABLE
    const val FIELD_SORT_ORDER = FirestoreConfig.Fields.SORT_ORDER
    const val FIELD_CREATED_AT = FirestoreConfig.Fields.CREATED_AT
    const val FIELD_UPDATED_AT = FirestoreConfig.Fields.UPDATED_AT
    const val FIELD_PHONE_NUMBER = FirestoreConfig.Fields.PHONE_NUMBER
    const val FIELD_ORDER_STATUS = FirestoreConfig.Fields.ORDER_STATUS
    const val FIELD_TABLE_STATUS = FirestoreConfig.Fields.TABLE_STATUS
}

/**
 * Firestore-compatible Category Entity
 * Represents menu categories such as Waffles, Momos, Burgers, Shakes, Beverages.
 */
@Serializable
data class FirestoreCategory(
    var id: String = "",
    var name: String = "",
    var iconEmoji: String = "🧇",
    var description: String = "",
    var image: String = "",
    var sortOrder: Int = 0,
    var isAvailable: Boolean = true,
    var isActive: Boolean = true,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            iconEmoji = iconEmoji,
            description = description,
            image = image,
            sortOrder = sortOrder,
            isAvailable = isAvailable
        )
    }

    companion object {
        fun fromDomain(domain: Category): FirestoreCategory {
            return FirestoreCategory(
                id = domain.id,
                name = domain.name,
                iconEmoji = domain.iconEmoji,
                description = domain.description,
                image = domain.image,
                sortOrder = domain.sortOrder,
                isAvailable = domain.isAvailable,
                isActive = domain.isAvailable
            )
        }
    }
}

/**
 * Firestore-compatible Variant Entity (e.g. Regular / Loaded / Single / Double)
 */
@Serializable
data class FirestoreVariant(
    var id: String = "",
    var name: String = "",
    var price: Double = 0.0
) {
    fun toDomain(): ProductVariant = ProductVariant(id = id, name = name, price = price)

    companion object {
        fun fromDomain(v: ProductVariant): FirestoreVariant =
            FirestoreVariant(id = v.id, name = v.name, price = v.price)
    }
}

/**
 * Firestore-compatible Addon Entity (e.g. Extra Nutella, Ice Cream Scoop)
 */
@Serializable
data class FirestoreAddon(
    var id: String = "",
    var name: String = "",
    var price: Double = 0.0
) {
    fun toDomain(): ProductAddon = ProductAddon(id = id, name = name, price = price)

    companion object {
        fun fromDomain(a: ProductAddon): FirestoreAddon =
            FirestoreAddon(id = a.id, name = a.name, price = a.price)
    }
}

/**
 * Firestore-compatible Product Entity
 * Complete item representation including 100% Pure Veg details, pricing in NPR,
 * preparation time, badges, and customization options.
 */
@Serializable
data class FirestoreProduct(
    var productId: String = "",
    var name: String = "",
    var description: String = "",
    var categoryId: String = "",
    var price: Double = 0.0,
    var image: String = "",
    var imageEmoji: String = "🧇",
    var isAvailable: Boolean = true,
    var isFeatured: Boolean = false,
    var isBestSeller: Boolean = false,
    var isPureVeg: Boolean = true, // 100% Pure Veg & Eggless guarantee
    var badge: String? = null, // "Must Try", "Chef Special", "Popular", etc.
    var variants: List<FirestoreVariant> = emptyList(),
    var addons: List<FirestoreAddon> = emptyList(),
    var preparationTime: Int = 10, // In minutes
    var tax: Double = 0.0,
    var sortOrder: Int = 0,
    var createdAt: Long = System.currentTimeMillis(),
    var updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Product {
        return Product(
            productId = productId,
            name = name,
            description = description,
            categoryId = categoryId,
            price = price,
            image = image,
            imageEmoji = imageEmoji,
            isAvailable = isAvailable,
            isFeatured = isFeatured,
            isBestSeller = isBestSeller,
            badge = badge,
            variants = variants.map { it.toDomain() },
            addons = addons.map { it.toDomain() },
            preparationTime = preparationTime,
            tax = tax,
            sortOrder = sortOrder,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(p: Product): FirestoreProduct {
            return FirestoreProduct(
                productId = p.productId,
                name = p.name,
                description = p.description,
                categoryId = p.categoryId,
                price = p.price,
                image = p.image,
                imageEmoji = p.imageEmoji,
                isAvailable = p.isAvailable,
                isFeatured = p.isFeatured,
                isBestSeller = p.isBestSeller,
                isPureVeg = true,
                badge = p.badge,
                variants = p.variants.map { FirestoreVariant.fromDomain(it) },
                addons = p.addons.map { FirestoreAddon.fromDomain(it) },
                preparationTime = p.preparationTime,
                tax = p.tax,
                sortOrder = p.sortOrder,
                createdAt = p.createdAt,
                updatedAt = p.updatedAt
            )
        }
    }
}

/**
 * Firestore Customer User Profile Entity
 * Synced upon Firebase Phone Authentication with Nepal (+977) numbers.
 */
@Serializable
data class FirestoreUserProfile(
    var uid: String = "",
    var phoneNumber: String = "",
    var displayName: String = "",
    var email: String = "",
    var loyaltyPoints: Int = 0,
    var totalOrders: Int = 0,
    var totalSpendNpr: Double = 0.0,
    var preferredAddress: String = "",
    var isVegConfirmed: Boolean = true,
    var createdAt: Long = System.currentTimeMillis(),
    var lastLoginAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): CustomerProfile {
        return CustomerProfile(
            customerId = uid,
            name = displayName.ifBlank { "Customer (${phoneNumber.takeLast(4)})" },
            phone = phoneNumber,
            email = email,
            loyaltyPoints = loyaltyPoints
        )
    }

    companion object {
        fun fromDomain(uid: String, profile: CustomerProfile): FirestoreUserProfile {
            return FirestoreUserProfile(
                uid = uid,
                phoneNumber = profile.phone,
                displayName = profile.name,
                email = profile.email,
                loyaltyPoints = profile.loyaltyPoints
            )
        }
    }
}
