package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Product
import com.example.model.ProductAddon
import com.example.model.ProductVariant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "cached_products")
@Serializable
data class ProductEntity(
    @PrimaryKey
    val productId: String,
    val name: String,
    val description: String = "",
    val categoryId: String,
    val price: Double,
    val image: String = "",
    val imageEmoji: String = "🧇",
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false,
    val isBestSeller: Boolean = false,
    val isPureVeg: Boolean = true,
    val badge: String? = null,
    val variantsJson: String = "[]",
    val addonsJson: String = "[]",
    val preparationTime: Int = 10,
    val tax: Double = 0.0,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Product {
        val parsedVariants = try {
            if (variantsJson.isNotBlank()) {
                Json.decodeFromString<List<ProductVariant>>(variantsJson)
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val parsedAddons = try {
            if (addonsJson.isNotBlank()) {
                Json.decodeFromString<List<ProductAddon>>(addonsJson)
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }

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
            isPureVeg = isPureVeg,
            badge = badge,
            variants = parsedVariants,
            addons = parsedAddons,
            preparationTime = preparationTime,
            tax = tax,
            sortOrder = sortOrder,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(p: Product): ProductEntity {
            val jsonSerializer = Json { ignoreUnknownKeys = true }
            val vJson = try {
                jsonSerializer.encodeToString(p.variants)
            } catch (e: Exception) {
                "[]"
            }
            val aJson = try {
                jsonSerializer.encodeToString(p.addons)
            } catch (e: Exception) {
                "[]"
            }

            return ProductEntity(
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
                isPureVeg = p.isPureVeg,
                badge = p.badge,
                variantsJson = vJson,
                addonsJson = aJson,
                preparationTime = p.preparationTime,
                tax = p.tax,
                sortOrder = p.sortOrder,
                createdAt = p.createdAt,
                updatedAt = p.updatedAt
            )
        }
    }
}
