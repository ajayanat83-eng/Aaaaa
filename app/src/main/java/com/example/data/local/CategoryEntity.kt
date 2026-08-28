package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.Category
import kotlinx.serialization.Serializable

@Entity(tableName = "cached_categories")
@Serializable
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val iconEmoji: String = "🧇",
    val description: String = "",
    val image: String = "",
    val sortOrder: Int = 0,
    val isAvailable: Boolean = true
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
        fun fromDomain(domain: Category): CategoryEntity {
            return CategoryEntity(
                id = domain.id,
                name = domain.name,
                iconEmoji = domain.iconEmoji,
                description = domain.description,
                image = domain.image,
                sortOrder = domain.sortOrder,
                isAvailable = domain.isAvailable
            )
        }
    }
}
