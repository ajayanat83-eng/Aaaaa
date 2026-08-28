package com.example.ui.staff

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CafeRepository
import com.example.model.*
import com.example.ui.components.NprPriceText
import com.example.ui.components.PureVegBadge
import com.example.ui.components.TableStatusChip
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun AdminDashboardScreen(
    repository: CafeRepository,
    onNavigateBack: () -> Unit,
    onNavigateToPos: () -> Unit,
    onNavigateToTables: () -> Unit,
    onNavigateToKitchen: () -> Unit,
    onNavigateToPrinters: () -> Unit
) {
    val orders by repository.orders.collectAsState()
    val products by repository.products.collectAsState()
    val tables by repository.tables.collectAsState()
    val staffList by repository.staffList.collectAsState()
    val activeRole by repository.activeStaffRole.collectAsState()
    val deliveryProviders by repository.deliveryProviders.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()

    var selectedAdminTab by remember { mutableStateOf(0) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    // Analytics calculations
    val totalRevenue = orders.filter { it.paymentStatus == PaymentStatus.SUCCESS }.sumOf { it.grandTotal }
    val totalOrdersCount = orders.size
    val averageOrderValue = if (totalOrdersCount > 0) totalRevenue / totalOrdersCount else 0.0
    val activeOccupiedTables = tables.count { it.status != TableStatus.AVAILABLE }

    // Pending bank verification orders
    val pendingBankOrders = orders.filter { it.paymentMethod == PaymentMethod.BANK && it.paymentStatus == PaymentStatus.PENDING }

    if (showAddProductDialog) {
        var newName by remember { mutableStateOf("") }
        var newPriceText by remember { mutableStateOf("150") }
        var newDesc by remember { mutableStateOf("") }
        var newEmoji by remember { mutableStateOf("🧇") }
        var newCategory by remember { mutableStateOf("cat_waffles_desserts") }

        Dialog(onDismissRequest = { showAddProductDialog = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("➕ Add New Menu Product", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Product Name", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPriceText,
                        onValueChange = { newPriceText = it },
                        label = { Text("Price (NPR)", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Description", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newEmoji,
                        onValueChange = { newEmoji = it },
                        label = { Text("Icon Emoji (e.g. 🧇, 🥟, 🍕)", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val price = newPriceText.toDoubleOrNull() ?: 100.0
                            val newProd = Product(
                                productId = "prod_${UUID.randomUUID().toString().take(6)}",
                                name = newName,
                                description = newDesc,
                                categoryId = newCategory,
                                price = price,
                                imageEmoji = newEmoji.ifBlank { "🧇" }
                            )
                            repository.addProduct(newProd)
                            showAddProductDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange, contentColor = Color.Black)
                    ) {
                        Text("Save to Live Menu", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Column {
                            Text("👑 TJW Cafe Control Center", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Role: ${activeRole.label} (Full Access)", color = GoldenAmber, fontSize = 11.sp)
                        }
                    }
                    PureVegBadge(showText = false)
                }

                // Module Navigation Shortcuts
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Button(
                            onClick = onNavigateToPos,
                            colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("⚡ POS Counter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    item {
                        Button(
                            onClick = onNavigateToTables,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("🪑 Floor Tables", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    item {
                        Button(
                            onClick = onNavigateToKitchen,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC77DFF), contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("🍳 Kitchen KDS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    item {
                        Button(
                            onClick = onNavigateToPrinters,
                            colors = ButtonDefaults.buttonColors(containerColor = InfoBlue, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("🖨️ Printer Setup", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Sub-tabs
                val tabs = listOf("📊 Live KPIs", "🍔 Menu CRUD", "👥 Staff & Roles", "🛵 Delivery Providers", "📜 Audit Logs")
                ScrollableTabRow(
                    selectedTabIndex = selectedAdminTab,
                    containerColor = Color.Transparent,
                    contentColor = GoldenAmber,
                    edgePadding = 0.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedAdminTab == index,
                            onClick = { selectedAdminTab = index },
                            text = { Text(tab, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        when (selectedAdminTab) {
            0 -> {
                // ==================== KPI & BANK APPROVALS ====================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // KPI Row 1
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KpiMetricCard(
                                title = "Today's Revenue",
                                value = "NPR ${totalRevenue.toInt()}",
                                subtitle = "Paid & Settled",
                                iconEmoji = "💰",
                                accentColor = GoldenAmber,
                                modifier = Modifier.weight(1f)
                            )
                            KpiMetricCard(
                                title = "Total Orders",
                                value = "$totalOrdersCount",
                                subtitle = "All Modes",
                                iconEmoji = "📋",
                                accentColor = WaffleOrange,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // KPI Row 2
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KpiMetricCard(
                                title = "Avg Order Value",
                                value = "NPR ${averageOrderValue.toInt()}",
                                subtitle = "Per Order",
                                iconEmoji = "📈",
                                accentColor = VegGreen,
                                modifier = Modifier.weight(1f)
                            )
                            KpiMetricCard(
                                title = "Occupied Tables",
                                value = "$activeOccupiedTables / ${tables.size}",
                                subtitle = "Floor Utilization",
                                iconEmoji = "🪑",
                                accentColor = Color(0xFFC77DFF),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // PENDING BANK VERIFICATION QUEUE
                    if (pendingBankOrders.isNotEmpty()) {
                        item {
                            Text("🏦 Pending Bank QR Verifications (${pendingBankOrders.size})", color = GoldenAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        items(pendingBankOrders) { ord ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, GoldenAmber, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("${ord.humanOrderNumber} • NPR ${ord.grandTotal.toInt()}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("Ref: ${ord.payments.firstOrNull()?.reference?.ifBlank { "Unspecified" }}", color = GoldenAmber, fontSize = 11.sp)
                                        Text("Customer: ${ord.customerName} (${ord.customerPhone})", color = TextSecondary, fontSize = 11.sp)
                                    }
                                    Button(
                                        onClick = {
                                            repository.updatePaymentStatus(ord.orderId, PaymentStatus.SUCCESS)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // ==================== MENU CRUD ====================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("All Menu Items (${products.size})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { showAddProductDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ Add Item", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(products) { prod ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
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
                                    Text(prod.imageEmoji, fontSize = 24.sp)
                                    Column {
                                        Text(prod.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        NprPriceText(amount = prod.price, fontSize = 12.sp)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Switch(
                                        checked = prod.isAvailable,
                                        onCheckedChange = { repository.toggleProductAvailability(prod.productId) },
                                        colors = SwitchDefaults.colors(checkedThumbColor = VegGreen)
                                    )
                                    IconButton(
                                        onClick = { repository.deleteProduct(prod.productId) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // ==================== STAFF & ROLES ====================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("Active Simulator Role Switcher:", color = GoldenAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StaffRole.values().forEach { role ->
                                val isSel = activeRole == role
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) WaffleOrange else DarkSurface)
                                        .clickable { repository.setStaffRole(role) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = role.label,
                                        color = if (isSel) Color.Black else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Registered Staff Members (${staffList.size})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    items(staffList) { staff ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(staff.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("${staff.role.label} • ${staff.phone}", color = TextSecondary, fontSize = 11.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DarkSurfaceVariant)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("PIN: ${staff.pin}", color = GoldenAmber, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // ==================== DELIVERY PROVIDERS ====================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text("Multi-Provider Delivery Gateways", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    items(deliveryProviders) { provider ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(provider.name, color = GoldenAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Switch(
                                        checked = provider.isEnabled,
                                        onCheckedChange = { repository.updateDeliveryProvider(provider.copy(isEnabled = it)) },
                                        colors = SwitchDefaults.colors(checkedThumbColor = GoldenAmber)
                                    )
                                }
                                Text("Store ID: ${provider.storeId}", color = TextSecondary, fontSize = 11.sp)
                                Text("API URL: ${provider.baseUrl}", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            4 -> {
                // ==================== AUDIT LOGS ====================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(auditLogs) { log ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("[${log.action}] ${log.userName}", color = GoldenAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(log.newValue ?: "", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    iconEmoji: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(iconEmoji, fontSize = 16.sp)
            }
            Text(value, color = accentColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = TextMuted, fontSize = 10.sp)
        }
    }
}
