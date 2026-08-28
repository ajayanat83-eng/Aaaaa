package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CafeRepository
import com.example.model.Category
import com.example.model.Product
import com.example.ui.components.NprPriceText
import com.example.ui.components.ProductBadgeTag
import com.example.ui.components.PureVegBadge
import com.example.ui.theme.*

@Composable
fun MenuScreen(
    repository: CafeRepository,
    initialCategoryId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit
) {
    val products by repository.products.collectAsState()
    val categories by repository.categories.collectAsState()
    val cartItems by repository.cartItems.collectAsState()

    var selectedCategoryId by remember { mutableStateOf<String?>(initialCategoryId) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedProductForCustomization by remember { mutableStateOf<Product?>(null) }

    val filteredProducts = remember(selectedCategoryId, searchQuery, products) {
        products.filter { product ->
            val matchesCategory = selectedCategoryId == null || product.categoryId == selectedCategoryId
            val matchesSearch = searchQuery.isBlank() ||
                    product.name.contains(searchQuery, ignoreCase = true) ||
                    product.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    if (selectedProductForCustomization != null) {
        ProductCustomizeDialog(
            product = selectedProductForCustomization!!,
            onDismiss = { selectedProductForCustomization = null },
            onAddToCart = { variant, addons, notes ->
                repository.addToCart(selectedProductForCustomization!!, variant, addons, notes)
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Column {
                            Text("TJW Cafe Menu", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text("100% Pure Veg & Eggless", color = VegGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (cartItems.isNotEmpty()) {
                        IconButton(onClick = onNavigateToCart) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = WaffleOrange, contentColor = Color.Black) {
                                        Text(cartItems.sumOf { it.quantity }.toString())
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = GoldenAmber)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter items by name or ingredients...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldenAmber) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = WaffleOrange,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips Filter
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        val isAllSelected = selectedCategoryId == null
                        FilterChip(
                            selected = isAllSelected,
                            onClick = { selectedCategoryId = null },
                            label = { Text("All Items (${products.size})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WaffleOrange,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                    items(categories) { category ->
                        val isSelected = selectedCategoryId == category.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryId = category.id },
                            label = { Text("${category.iconEmoji} ${category.name}", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WaffleOrange,
                                selectedLabelColor = Color.Black,
                                containerColor = DarkSurfaceVariant,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (cartItems.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToCart,
                    containerColor = WaffleOrange,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Checkout (NPR ${cartItems.sumOf { it.totalPrice }.toInt()})", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredProducts) { product ->
                MenuProductListItem(
                    product = product,
                    onCustomize = { selectedProductForCustomization = product }
                )
            }
            if (filteredProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No items found", color = TextSecondary, fontSize = 14.sp)
                            Text("Try searching for something else", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuProductListItem(
    product: Product,
    onCustomize: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onCustomize)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(product.imageEmoji, fontSize = 26.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PureVegBadge(showText = false)
                    Text(
                        text = product.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (product.badge != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    ProductBadgeTag(badgeText = product.badge)
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NprPriceText(amount = product.price, fontSize = 14.sp)
                    if (product.variants.isNotEmpty()) {
                        Text("• Variants available", color = TextMuted, fontSize = 10.sp)
                    }
                }
            }

            Button(
                onClick = onCustomize,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (product.isAvailable) WaffleOrange else DarkSurfaceElevated,
                    contentColor = if (product.isAvailable) Color.Black else TextMuted
                ),
                enabled = product.isAvailable,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = if (product.isAvailable) "+ ADD" else "SOLD OUT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
