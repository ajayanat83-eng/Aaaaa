package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CafeRepository
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.service.PrinterService
import com.example.service.WhatsAppService
import com.example.ui.components.NprPriceText
import com.example.ui.components.OrderStatusBadge
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.components.PureVegBadge
import com.example.ui.customer.CustomerFeedbackDialog
import com.example.ui.theme.*
import com.example.util.PriceFormatter
import kotlinx.coroutines.launch

@Composable
fun OrderTrackingScreen(
    repository: CafeRepository,
    targetOrderId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val orders by repository.orders.collectAsState()
    val printerSettings by repository.printerSettings.collectAsState()
    var selectedOrder by remember(targetOrderId, orders) {
        mutableStateOf(orders.find { it.orderId == targetOrderId } ?: orders.firstOrNull())
    }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }

    if (showFeedbackDialog) {
        CustomerFeedbackDialog(
            repository = repository,
            onDismiss = { showFeedbackDialog = false }
        )
    }

    if (showReceiptDialog && selectedOrder != null) {
        val receiptText = remember(selectedOrder, printerSettings) { PrinterService.generateBillReceipt(selectedOrder!!, printerSettings) }
        Dialog(onDismissRequest = { showReceiptDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF181818),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🧾 Bill Receipt (ESC/POS)", color = GoldenAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showReceiptDialog = false }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = receiptText,
                            color = Color(0xFF00FF66),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            repository.printBill(selectedOrder!!)
                            coroutineScope.launch {
                                PrinterService.printBillDirectBluetooth(selectedOrder!!, printerSettings, context)
                            }
                            showReceiptDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Print Bluetooth ESC/POS Receipt", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        Text("Order Status & Receipt", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("Live Kitchen & Billing Sync", color = GoldenAmber, fontSize = 11.sp)
                    }
                }
                IconButton(onClick = onNavigateToHome) {
                    Icon(Icons.Default.Home, contentDescription = "Home", tint = GoldenAmber)
                }
            }
        }
    ) { innerPadding ->
        if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No orders placed yet.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==================== 1. ACTIVE SELECTED ORDER CARD ====================
                if (selectedOrder != null) {
                    item {
                        val ord = selectedOrder!!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(DarkSurface)
                                .border(1.5.dp, WaffleOrange, RoundedCornerShape(18.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(ord.humanOrderNumber, color = GoldenAmber, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                        Text("Type: ${ord.orderType.label}${if (ord.tableNumber != null) " • ${ord.tableNumber}" else ""}", color = TextSecondary, fontSize = 12.sp)
                                    }
                                    OrderStatusBadge(status = ord.orderStatus)
                                }

                                PureVegBadge()

                                // Visual Status Tracker Timeline
                                OrderTimelineTracker(currentStatus = ord.orderStatus)

                                Divider(color = CardBorder, thickness = 0.8.dp)

                                // Order items summary
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Items Ordered:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    ord.items.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${item.productName}${if (item.variantName != null) " (${item.variantName})" else ""} x${item.quantity}",
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                            Text(PriceFormatter.formatNpr(item.totalPrice), color = TextPrimary, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Divider(color = CardBorder, thickness = 0.8.dp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Grand Total:", color = TextSecondary, fontSize = 12.sp)
                                        NprPriceText(amount = ord.grandTotal, fontSize = 18.sp)
                                    }
                                    PaymentStatusBadge(status = ord.paymentStatus)
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Feedback trigger button
                                OutlinedButton(
                                    onClick = { showFeedbackDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldenAmber),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldenAmber.copy(alpha = 0.7f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldenAmber)
                                        Text("Rate Dining Experience & Food", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Actions: WhatsApp and Print Receipt
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { WhatsAppService.launchWhatsApp(context, ord) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.Black)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Text("WhatsApp Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { showReceiptDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldenAmber),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldenAmber)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Text("View Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==================== 2. PAST ORDERS LIST ====================
                item {
                    Text("All Recent Orders (${orders.size})", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                items(orders) { order ->
                    val isSelected = selectedOrder?.orderId == order.orderId
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF24160A) else DarkSurface)
                            .border(1.dp, if (isSelected) WaffleOrange else CardBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedOrder = order }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(order.humanOrderNumber, color = GoldenAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("${order.orderType.label} • ${order.items.size} items", color = TextSecondary, fontSize = 11.sp)
                                NprPriceText(amount = order.grandTotal, fontSize = 13.sp)
                            }
                            OrderStatusBadge(status = order.orderStatus)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderTimelineTracker(currentStatus: OrderStatus) {
    val steps = listOf(
        Pair("Placed", OrderStatus.PENDING),
        Pair("Kitchen", OrderStatus.PREPARING),
        Pair("Ready", OrderStatus.READY),
        Pair("Completed", OrderStatus.COMPLETED)
    )

    val currentStepIndex = when (currentStatus) {
        OrderStatus.PENDING -> 0
        OrderStatus.CONFIRMED, OrderStatus.ACCEPTED -> 0
        OrderStatus.PREPARING -> 1
        OrderStatus.READY -> 2
        OrderStatus.SERVED, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED, OrderStatus.COMPLETED -> 3
        OrderStatus.CANCELLED, OrderStatus.REFUNDED -> -1
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, pair ->
            val isDone = currentStepIndex >= index
            val isCurrent = currentStepIndex == index

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isDone) (if (isCurrent) WaffleOrange else VegGreen) else DarkSurfaceVariant)
                        .border(1.dp, if (isDone) GoldenAmber else CardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    } else {
                        Text("${index + 1}", color = TextMuted, fontSize = 10.sp)
                    }
                }
                Text(
                    text = pair.first,
                    color = if (isDone) TextPrimary else TextMuted,
                    fontSize = 10.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
