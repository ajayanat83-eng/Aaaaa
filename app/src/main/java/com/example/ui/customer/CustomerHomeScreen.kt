package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CafeRepository
import com.example.data.FirestoreMenuRepository
import com.example.model.*
import com.example.service.WhatsAppService
import com.example.ui.components.CategorySkeletonRow
import com.example.ui.components.MenuSkeletonLoadingGrid
import com.example.ui.components.NprPriceText
import com.example.ui.components.ProductBadgeTag
import com.example.ui.components.ProductCardSkeleton
import com.example.ui.components.PureVegBadge
import com.example.ui.theme.*
import com.example.util.PriceFormatter

@Composable
fun CustomerHomeScreen(
    repository: CafeRepository,
    onNavigateToMenu: (categoryId: String?) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onStartDineIn: (Table) -> Unit
) {
    val context = LocalContext.current
    val products by repository.products.collectAsState()
    val categories by repository.categories.collectAsState()
    val cartItems by repository.cartItems.collectAsState()
    val tables by repository.tables.collectAsState()
    val customerProfile by repository.customerProfile.collectAsState()
    val isFirestoreLoading by FirestoreMenuRepository.instance.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedProductForCustomization by remember { mutableStateOf<Product?>(null) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    val filteredProducts = remember(searchQuery, products) {
        if (searchQuery.isBlank()) products
        else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true) ||
                    it.badge?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    val superSaverCombos = remember(products) {
        products.filter { it.categoryId == "cat_combos_addons" && it.name.contains("Combo", ignoreCase = true) }
    }

    val bestSellers = remember(products) {
        products.filter { it.isBestSeller || it.badge?.equals("Best Seller", ignoreCase = true) == true }
    }

    val popularItems = remember(products) {
        products.filter { it.isFeatured || it.badge?.equals("Popular", ignoreCase = true) == true || it.badge?.equals("Must Try", ignoreCase = true) == true }
    }

    if (showFeedbackDialog) {
        CustomerFeedbackDialog(
            repository = repository,
            onDismiss = { showFeedbackDialog = false }
        )
    }

    if (showQrDialog) {
        QrScanDialog(
            tables = tables,
            onDismiss = { showQrDialog = false },
            onTableSelected = { table ->
                onStartDineIn(table)
            }
        )
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
        floatingActionButton = {
            if (cartItems.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToCart,
                    containerColor = WaffleOrange,
                    contentColor = Color.Black,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = Color.Black, contentColor = GoldenAmber) {
                                    Text(cartItems.sumOf { it.quantity }.toString(), fontWeight = FontWeight.Bold)
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                        Text(
                            text = "View Cart (${PriceFormatter.formatNpr(cartItems.sumOf { it.totalPrice })})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = if (cartItems.isNotEmpty()) 80.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ==================== 1. TOP HEADER & BRANDING ====================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Logo Box
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF2A1504))
                                    .border(1.5.dp, WaffleOrange, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🧇", fontSize = 22.sp)
                            }
                            Column {
                                Text(
                                    text = "The Janakpur Waffle & Cafe",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "TJW Cafe • Janakpurdham, Nepal",
                                    color = GoldenAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Feedback, Orders & Profile Shortcuts
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { showFeedbackDialog = true },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF382305))
                                    .border(1.dp, GoldenAmber.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = "Rate Experience", tint = GoldenAmber, modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                onClick = onNavigateToOrders,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = "Orders", tint = TextPrimary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(
                                onClick = onNavigateToProfile,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = "Profile", tint = GoldenAmber, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // Pure Veg & Eggless Statement Badge
                    PureVegBadge(modifier = Modifier.fillMaxWidth())
                }
            }

            // ==================== 2. SEARCH BAR ====================
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search waffles, momos, pizzas, shakes, coolers...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldenAmber) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = WaffleOrange,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    singleLine = true
                )
            }

            // ==================== 3. HERO PROMOTIONAL BANNER ====================
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF381E04), Color(0xFF1E1408), Color(0xFF101C1A))
                            )
                        )
                        .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✨ FRESH & EGGLESS",
                                color = GoldenAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(VegGreen)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("NEPAL'S BEST", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        Text(
                            text = "Crispy Waffles, Momos\n& Shakes in Janakpur",
                            color = TextPrimary,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 24.sp
                        )
                        Text(
                            text = "Order Dine-In via Table QR, Takeaway or Doorstep Delivery!",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { onNavigateToMenu(null) },
                                colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text("Explore Menu", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    val dummyOrder = Order(
                                        humanOrderNumber = "ENQUIRY",
                                        customerName = customerProfile.name,
                                        customerPhone = customerProfile.phone
                                    )
                                    WhatsAppService.launchWhatsApp(context, dummyOrder)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreen),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = "WhatsApp", modifier = Modifier.size(14.dp))
                                    Text("WhatsApp Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // ==================== 4. QUICK ORDER ENTRIES (Dine-In QR, Takeaway, Delivery) ====================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "⚡ Quick Order Modes",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Dine-In QR
                        QuickActionCard(
                            emoji = "🪑",
                            title = "Dine-In QR",
                            subtitle = "Scan Table",
                            accentColor = WaffleOrange,
                            modifier = Modifier.weight(1f),
                            onClick = { showQrDialog = true }
                        )
                        // Takeaway
                        QuickActionCard(
                            emoji = "🛍️",
                            title = "Takeaway",
                            subtitle = "Self Pickup",
                            accentColor = GoldenAmber,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToMenu(null) }
                        )
                        // Delivery
                        QuickActionCard(
                            emoji = "🛵",
                            title = "Delivery",
                            subtitle = "Doorstep",
                            accentColor = VegGreen,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToMenu(null) }
                        )
                    }
                }
            }

            // ==================== 5. CATEGORY CARDS ====================
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🍴 Menu Categories",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "See All (${products.size})",
                            color = GoldenAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onNavigateToMenu(null) }
                        )
                    }

                    if (categories.isEmpty() || isFirestoreLoading) {
                        CategorySkeletonRow()
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(categories) { category ->
                                CategoryCard(
                                    category = category,
                                    onClick = { onNavigateToMenu(category.id) }
                                )
                            }
                        }
                    }
                }
            }

            // ==================== 6. SUPER SAVER COMBOS ====================
            if (superSaverCombos.isNotEmpty() && searchQuery.isBlank()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⭐ Super Saver Combos",
                                color = GoldenAmber,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Save More",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(superSaverCombos) { combo ->
                                ComboProductCard(
                                    product = combo,
                                    onAddToCart = { selectedProductForCustomization = combo }
                                )
                            }
                        }
                    }
                }
            }

            // ==================== 7. BEST SELLERS ====================
            if (bestSellers.isNotEmpty() && searchQuery.isBlank()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "🔥 Best Sellers",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(bestSellers) { product ->
                                ProductGridCard(
                                    product = product,
                                    onAddToCart = { selectedProductForCustomization = product }
                                )
                            }
                        }
                    }
                }
            }

            // ==================== 8. POPULAR ITEMS & SEARCH RESULTS ====================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Search Results (${filteredProducts.size})" else "🌟 Popular Items",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (products.isEmpty() || isFirestoreLoading) {
                        repeat(3) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ProductCardSkeleton(modifier = Modifier.weight(1f))
                                ProductCardSkeleton(modifier = Modifier.weight(1f))
                            }
                        }
                    } else {
                        filteredProducts.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { product ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        ProductGridCard(
                                            product = product,
                                            modifier = Modifier.fillMaxWidth(),
                                            onAddToCart = { selectedProductForCustomization = product }
                                        )
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    emoji: String,
    title: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CategoryCard(
    category: Category,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(category.iconEmoji, fontSize = 26.sp)
            Text(
                text = category.name,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ComboProductCard(
    product: Product,
    onAddToCart: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF26180B))
            .border(1.dp, WaffleOrange.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(product.imageEmoji, fontSize = 28.sp)
                if (product.badge != null) {
                    ProductBadgeTag(badgeText = product.badge)
                }
            }
            Text(
                text = product.name,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = product.description,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NprPriceText(amount = product.price, fontSize = 15.sp)
                Button(
                    onClick = onAddToCart,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("+ ADD", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun ProductGridCard(
    product: Product,
    modifier: Modifier = Modifier,
    onAddToCart: () -> Unit
) {
    Box(
        modifier = modifier
            .widthIn(min = 160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Visual header with Veg dot & badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(product.imageEmoji, fontSize = 22.sp)
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PureVegBadge(showText = false)
                    if (product.badge != null) {
                        ProductBadgeTag(badgeText = product.badge)
                    }
                }
            }

            Text(
                text = product.name,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = product.description,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (product.variants.isNotEmpty()) {
                        Text("Starts from", color = TextMuted, fontSize = 9.sp)
                    }
                    NprPriceText(amount = product.price, fontSize = 14.sp)
                }
                Button(
                    onClick = onAddToCart,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (product.isAvailable) WaffleOrange else DarkSurfaceElevated,
                        contentColor = if (product.isAvailable) Color.Black else TextMuted
                    ),
                    enabled = product.isAvailable,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = if (product.isAvailable) "+ ADD" else "SOLD OUT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
