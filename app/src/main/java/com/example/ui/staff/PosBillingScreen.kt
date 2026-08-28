package com.example.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CafeRepository
import com.example.model.*
import com.example.service.PrinterService
import com.example.ui.components.NprPriceText
import com.example.ui.components.PureVegBadge
import com.example.ui.components.TableStatusChip
import com.example.ui.customer.ProductCustomizeDialog
import com.example.ui.theme.*
import com.example.util.PriceFormatter
import kotlinx.coroutines.launch

@Composable
fun PosBillingScreen(
    repository: CafeRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val products by repository.products.collectAsState()
    val categories by repository.categories.collectAsState()
    val tables by repository.tables.collectAsState()
    val cafeSettings by repository.cafeSettings.collectAsState()
    val printerSettings by repository.printerSettings.collectAsState()

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTable by remember { mutableStateOf<Table?>(tables.firstOrNull()) }
    var orderType by remember { mutableStateOf(OrderType.DINE_IN) }

    // POS Active Cart
    var posCartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var selectedProductForCustomization by remember { mutableStateOf<Product?>(null) }
    var customerNameInput by remember { mutableStateOf("Walk-in Guest") }
    var customerPhoneInput by remember { mutableStateOf("") }
    var showSettlementDialog by remember { mutableStateOf(false) }
    var successNotification by remember { mutableStateOf("") }

    val filteredProducts = remember(selectedCategoryId, searchQuery, products) {
        products.filter { product ->
            val matchesCat = selectedCategoryId == null || product.categoryId == selectedCategoryId
            val matchesSearch = searchQuery.isBlank() || product.name.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesSearch
        }
    }

    val subtotal = posCartItems.sumOf { it.totalPrice }

    if (selectedProductForCustomization != null) {
        ProductCustomizeDialog(
            product = selectedProductForCustomization!!,
            onDismiss = { selectedProductForCustomization = null },
            onAddToCart = { variant, addons, notes ->
                val current = posCartItems.toMutableList()
                val existing = current.indexOfFirst {
                    it.product.productId == selectedProductForCustomization!!.productId &&
                            it.selectedVariant?.id == variant?.id &&
                            it.selectedAddons.map { a -> a.id }.sorted() == addons.map { a -> a.id }.sorted()
                }
                if (existing >= 0) {
                    val itm = current[existing]
                    current[existing] = itm.copy(quantity = itm.quantity + 1)
                } else {
                    current.add(
                        CartItem(
                            product = selectedProductForCustomization!!,
                            selectedVariant = variant,
                            selectedAddons = addons,
                            quantity = 1,
                            itemNotes = notes
                        )
                    )
                }
                posCartItems = current
            }
        )
    }

    if (showSettlementDialog) {
        PosSettlementModal(
            subtotal = subtotal,
            onDismiss = { showSettlementDialog = false },
            onSettle = { method, isPaid ->
                val order = repository.createOrder(
                    orderType = orderType,
                    customerName = customerNameInput,
                    customerPhone = customerPhoneInput,
                    tableNumber = if (orderType == OrderType.DINE_IN) selectedTable?.tableNumber else null,
                    paymentMethod = method,
                    paymentStatus = if (isPaid) PaymentStatus.SUCCESS else PaymentStatus.PENDING,
                    customItems = posCartItems
                )
                posCartItems = emptyList()
                showSettlementDialog = false
                successNotification = "Order ${order.humanOrderNumber} created & KOT dispatched!"

                // Bluetooth Thermal Printing
                coroutineScope.launch {
                    if (printerSettings.billPrint) {
                        PrinterService.printBillDirectBluetooth(order, printerSettings, context)
                    }
                }
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
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            Text("⚡ POS Rapid Billing Counter", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("TJW Cafe POS • Multi-Table Sync", color = GoldenAmber, fontSize = 11.sp)
                        }
                    }
                    PureVegBadge(showText = false)
                }

                if (successNotification.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0E382B))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(successNotification, color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { successNotification = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = SuccessGreen)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ==================== 1. MODE & TABLE PICKER ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OrderType.values().forEach { type ->
                    val isSel = orderType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) WaffleOrange else DarkSurface)
                            .clickable { orderType = type }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.label,
                            color = if (isSel) Color.Black else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (orderType == OrderType.DINE_IN) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(tables) { table ->
                        val isSel = selectedTable?.tableId == table.tableId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF381E04) else DarkSurface)
                                .border(1.dp, if (isSel) GoldenAmber else CardBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedTable = table }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = table.tableNumber.replace("TJW-TABLE-", "T-"),
                                    color = if (isSel) GoldenAmber else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TableStatusChip(status = table.status)
                            }
                        }
                    }
                }
            }

            // ==================== 2. CATEGORY CHIPS ====================
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null },
                        label = { Text("All", fontSize = 11.sp) }
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = { selectedCategoryId = cat.id },
                        label = { Text("${cat.iconEmoji} ${cat.name}", fontSize = 11.sp) }
                    )
                }
            }

            // ==================== 3. SPLIT WORKSPACE: MENU GRID & CURRENT POS BILL ====================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // LEFT: MENU GRID
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts) { prod ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    if (prod.variants.isNotEmpty() || prod.addons.isNotEmpty()) {
                                        selectedProductForCustomization = prod
                                    } else {
                                        val current = posCartItems.toMutableList()
                                        val idx = current.indexOfFirst { it.product.productId == prod.productId }
                                        if (idx >= 0) {
                                            current[idx] = current[idx].copy(quantity = current[idx].quantity + 1)
                                        } else {
                                            current.add(CartItem(product = prod, quantity = 1))
                                        }
                                        posCartItems = current
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(prod.imageEmoji, fontSize = 20.sp)
                                    NprPriceText(amount = prod.price, fontSize = 12.sp)
                                }
                                Text(
                                    text = prod.name,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // RIGHT: ACTIVE RUNNING POS BILL
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Running Bill (${posCartItems.sumOf { it.quantity }} items)",
                                color = GoldenAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Divider(color = CardBorder, thickness = 0.5.dp)

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(posCartItems) { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${item.product.name} x${item.quantity}",
                                                color = TextPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text("NPR ${item.totalPrice.toInt()}", color = TextSecondary, fontSize = 10.sp)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            IconButton(
                                                onClick = {
                                                    val current = posCartItems.toMutableList()
                                                    val idx = current.indexOf(item)
                                                    if (idx >= 0) {
                                                        if (item.quantity == 1) current.removeAt(idx)
                                                        else current[idx] = item.copy(quantity = item.quantity - 1)
                                                        posCartItems = current
                                                    }
                                                },
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Icon(Icons.Default.Remove, contentDescription = null, tint = GoldenAmber, modifier = Modifier.size(12.dp))
                                            }
                                            IconButton(
                                                onClick = {
                                                    val current = posCartItems.toMutableList()
                                                    val idx = current.indexOf(item)
                                                    if (idx >= 0) {
                                                        current[idx] = item.copy(quantity = item.quantity + 1)
                                                        posCartItems = current
                                                    }
                                                },
                                                modifier = Modifier.size(22.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = null, tint = GoldenAmber, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Bottom Settlement trigger
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Divider(color = CardBorder, thickness = 0.5.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total:", color = TextSecondary, fontSize = 11.sp)
                                NprPriceText(amount = subtotal, fontSize = 15.sp)
                            }
                            Button(
                                onClick = { showSettlementDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = posCartItems.isNotEmpty(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange, contentColor = Color.Black)
                            ) {
                                Text("Settle / Dispatch KOT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PosSettlementModal(
    subtotal: Double,
    onDismiss: () -> Unit,
    onSettle: (PaymentMethod, Boolean) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var markAsPaid by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💳 Settle Bill & Dispatch KOT", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = TextSecondary)
                    }
                }

                Text("Payable Amount: NPR ${subtotal.toInt()}", color = GoldenAmber, fontSize = 16.sp, fontWeight = FontWeight.Black)

                Text("Select Settlement Mode:", color = TextSecondary, fontSize = 12.sp)

                listOf(
                    PaymentMethod.CASH,
                    PaymentMethod.ESEWA,
                    PaymentMethod.KHALTI,
                    PaymentMethod.BANK
                ).forEach { method ->
                    val isSel = selectedMethod == method
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF381E04) else DarkSurfaceVariant)
                            .clickable { selectedMethod = method }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = isSel,
                            onClick = { selectedMethod = method },
                            colors = RadioButtonDefaults.colors(selectedColor = WaffleOrange)
                        )
                        Text(method.label, color = if (isSel) GoldenAmber else TextPrimary, fontSize = 13.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Payment Received Instantly", color = TextSecondary, fontSize = 12.sp)
                    Switch(
                        checked = markAsPaid,
                        onCheckedChange = { markAsPaid = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = GoldenAmber, checkedTrackColor = Color(0xFF381E04))
                    )
                }

                Button(
                    onClick = { onSettle(selectedMethod, markAsPaid) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange, contentColor = Color.Black)
                ) {
                    Text("Complete Settlement & Print", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
