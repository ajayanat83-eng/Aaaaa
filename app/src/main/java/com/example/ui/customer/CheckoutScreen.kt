package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CafeRepository
import com.example.model.*
import com.example.service.PaymentManager
import com.example.service.PaymentResult
import com.example.ui.components.NprPriceText
import com.example.ui.components.PureVegBadge
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun CheckoutScreen(
    repository: CafeRepository,
    orderType: OrderType,
    tableNumber: String?,
    deliveryAddress: String?,
    couponCode: String?,
    discountAmount: Double,
    redeemedPoints: Int,
    specialNotes: String,
    onNavigateBack: () -> Unit,
    onOrderPlaced: (Order) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val cartItems by repository.cartItems.collectAsState()
    val customerProfile by repository.customerProfile.collectAsState()
    val cafeSettings by repository.cafeSettings.collectAsState()

    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var isSplitPayment by remember { mutableStateOf(false) }

    // Split amounts
    var splitMethod1 by remember { mutableStateOf(PaymentMethod.CASH) }
    var splitAmount1Text by remember { mutableStateOf("") }
    var splitMethod2 by remember { mutableStateOf(PaymentMethod.ESEWA) }
    var splitAmount2Text by remember { mutableStateOf("") }

    // Bank transfer details
    var bankReferenceCode by remember { mutableStateOf("") }
    var walletReferenceCode by remember { mutableStateOf("") }

    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val subtotal = cartItems.sumOf { it.totalPrice }
    val deliveryFee = if (orderType == OrderType.DELIVERY) cafeSettings.deliveryFee else 0.0
    val grandTotal = (subtotal - discountAmount + deliveryFee).coerceAtLeast(0.0)

    LaunchedEffect(grandTotal) {
        val half = (grandTotal / 2).toInt()
        splitAmount1Text = half.toString()
        splitAmount2Text = (grandTotal - half).toInt().toString()
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Column {
                    Text("Payment & Confirmation", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("The Janakpur Waffle & Cafe", color = GoldenAmber, fontSize = 11.sp)
                }
            }
        },
        bottomBar = {
            Surface(
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (errorMessage.isNotBlank()) {
                        Text(errorMessage, color = ErrorRed, fontSize = 12.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Payable:", color = TextSecondary, fontSize = 12.sp)
                            NprPriceText(amount = grandTotal, fontSize = 20.sp, color = GoldenAmber)
                        }
                        Button(
                            onClick = {
                                isProcessing = true
                                errorMessage = ""
                                coroutineScope.launch {
                                    try {
                                        val paymentRecords = mutableListOf<PaymentRecord>()
                                        val paymentStatus: PaymentStatus

                                        if (isSplitPayment) {
                                            val amt1 = splitAmount1Text.toDoubleOrNull() ?: 0.0
                                            val amt2 = splitAmount2Text.toDoubleOrNull() ?: 0.0
                                            if (amt1 + amt2 != grandTotal) {
                                                errorMessage = "Split amounts (NPR ${amt1.toInt()} + NPR ${amt2.toInt()}) must equal NPR ${grandTotal.toInt()}"
                                                isProcessing = false
                                                return@launch
                                            }
                                            paymentRecords.add(
                                                PaymentRecord(
                                                    orderId = "TEMP",
                                                    method = splitMethod1,
                                                    amount = amt1,
                                                    transactionId = "SPLIT-1-${UUID.randomUUID().toString().take(6)}",
                                                    status = PaymentStatus.SUCCESS
                                                )
                                            )
                                            paymentRecords.add(
                                                PaymentRecord(
                                                    orderId = "TEMP",
                                                    method = splitMethod2,
                                                    amount = amt2,
                                                    transactionId = "SPLIT-2-${UUID.randomUUID().toString().take(6)}",
                                                    status = PaymentStatus.SUCCESS
                                                )
                                            )
                                            paymentStatus = PaymentStatus.SUCCESS
                                        } else {
                                            val ref = if (selectedPaymentMethod == PaymentMethod.BANK) bankReferenceCode else walletReferenceCode
                                            val result = PaymentManager.executePayment(selectedPaymentMethod, "NEW", grandTotal, ref)
                                            when (result) {
                                                is PaymentResult.Success -> {
                                                    paymentRecords.add(
                                                        PaymentRecord(
                                                            orderId = "TEMP",
                                                            method = selectedPaymentMethod,
                                                            amount = grandTotal,
                                                            transactionId = result.transactionId,
                                                            status = PaymentStatus.SUCCESS
                                                        )
                                                    )
                                                    paymentStatus = PaymentStatus.SUCCESS
                                                }
                                                is PaymentResult.PendingVerification -> {
                                                    paymentRecords.add(result.paymentRecord)
                                                    paymentStatus = PaymentStatus.PENDING
                                                }
                                                is PaymentResult.Failed -> {
                                                    errorMessage = result.error
                                                    isProcessing = false
                                                    return@launch
                                                }
                                            }
                                        }

                                        val createdOrder = repository.createOrder(
                                            orderType = orderType,
                                            customerName = customerProfile.name,
                                            customerPhone = customerProfile.phone,
                                            tableNumber = tableNumber,
                                            deliveryAddress = deliveryAddress,
                                            couponCode = couponCode,
                                            discountAmount = discountAmount,
                                            paymentMethod = if (isSplitPayment) splitMethod1 else selectedPaymentMethod,
                                            paymentStatus = paymentStatus,
                                            paymentRecords = paymentRecords,
                                            specialInstructions = specialNotes,
                                            redeemedPoints = redeemedPoints
                                        )

                                        isProcessing = false
                                        onOrderPlaced(createdOrder)
                                    } catch (e: Exception) {
                                        errorMessage = "Order failed: ${e.message}"
                                        isProcessing = false
                                    }
                                }
                            },
                            enabled = !isProcessing,
                            modifier = Modifier
                                .height(48.dp)
                                .widthIn(min = 180.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WaffleOrange,
                                contentColor = Color.Black
                            )
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                            } else {
                                Text("Confirm & Place Order", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==================== 1. ORDER SUMMARY CARD ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Order Mode: ${orderType.label}", color = GoldenAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        PureVegBadge(showText = true)
                    }
                    if (tableNumber != null) {
                        Text("🪑 Table: $tableNumber", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (deliveryAddress != null) {
                        Text("📍 Delivery to: $deliveryAddress", color = TextPrimary, fontSize = 12.sp)
                    }
                    Text("👤 Customer: ${customerProfile.name} (${customerProfile.phone})", color = TextSecondary, fontSize = 12.sp)
                    Divider(color = CardBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                    Text("Items: ${cartItems.joinToString(", ") { "${it.product.name} x${it.quantity}" }}", color = TextSecondary, fontSize = 12.sp)
                }
            }

            // ==================== 2. SPLIT PAYMENT TOGGLE ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Split Payment Across 2 Modes", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("e.g. NPR 200 via Cash + NPR 300 via eSewa", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isSplitPayment,
                        onCheckedChange = { isSplitPayment = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldenAmber,
                            checkedTrackColor = Color(0xFF3D2A05)
                        )
                    )
                }
            }

            // ==================== 3. PAYMENT METHODS ====================
            if (!isSplitPayment) {
                Text("Select Nepal Payment Method:", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                val availableMethods = if (orderType == OrderType.DELIVERY) {
                    listOf(PaymentMethod.CASH, PaymentMethod.ESEWA, PaymentMethod.KHALTI, PaymentMethod.BANK, PaymentMethod.COD)
                } else {
                    listOf(PaymentMethod.CASH, PaymentMethod.ESEWA, PaymentMethod.KHALTI, PaymentMethod.BANK)
                }

                availableMethods.forEach { method ->
                    val isSelected = selectedPaymentMethod == method
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF3D2205) else DarkSurface)
                            .border(1.dp, if (isSelected) WaffleOrange else CardBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedPaymentMethod = method }
                            .padding(14.dp)
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
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedPaymentMethod = method },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = WaffleOrange,
                                        unselectedColor = TextMuted
                                    )
                                )
                                Text(
                                    text = when (method) {
                                        PaymentMethod.CASH -> "💵 Cash on Counter"
                                        PaymentMethod.ESEWA -> "🟢 eSewa Digital Wallet"
                                        PaymentMethod.KHALTI -> "🟣 Khalti Wallet"
                                        PaymentMethod.BANK -> "🏦 Bank Transfer / Fonepay QR"
                                        PaymentMethod.COD -> "🛵 Cash on Delivery (COD)"
                                    },
                                    color = if (isSelected) GoldenAmber else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // If Bank selected, show Bank QR & account info
                if (selectedPaymentMethod == PaymentMethod.BANK) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F1E1B))
                            .border(1.dp, VegGreen, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🏦 TJW Official Bank Account Details", color = GoldenAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Bank: ${cafeSettings.bankName}", color = TextPrimary, fontSize = 12.sp)
                            Text("A/C Name: ${cafeSettings.accountName}", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("A/C Number: ${cafeSettings.accountNumber}", color = GoldenAmber, fontSize = 13.sp, fontWeight = FontWeight.Black)
                            Text("Note: ${cafeSettings.bankQrNote}", color = TextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = bankReferenceCode,
                                onValueChange = { bankReferenceCode = it },
                                label = { Text("Bank / Fonepay Transaction Reference ID", color = TextSecondary, fontSize = 11.sp) },
                                placeholder = { Text("e.g. FP-981240182", color = TextMuted, fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = VegGreen,
                                    unfocusedBorderColor = CardBorder,
                                    focusedContainerColor = DarkSurfaceVariant,
                                    unfocusedContainerColor = DarkSurfaceVariant
                                )
                            )
                        }
                    }
                }

                // If eSewa or Khalti selected
                if (selectedPaymentMethod == PaymentMethod.ESEWA || selectedPaymentMethod == PaymentMethod.KHALTI) {
                    OutlinedTextField(
                        value = walletReferenceCode,
                        onValueChange = { walletReferenceCode = it },
                        label = { Text("${selectedPaymentMethod.label} Transaction ID / Mobile", color = TextSecondary, fontSize = 11.sp) },
                        placeholder = { Text("e.g. 9800000000 / TXN882910", color = TextMuted, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = GoldenAmber,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        )
                    )
                }
            } else {
                // SPLIT PAYMENT INPUTS
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .border(1.dp, GoldenAmber, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Split Payment Configuration", color = GoldenAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    // Part 1
                    Text("Portion 1:", color = TextSecondary, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(splitMethod1.label, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = splitAmount1Text,
                            onValueChange = { splitAmount1Text = it },
                            label = { Text("NPR", color = TextSecondary, fontSize = 10.sp) },
                            modifier = Modifier.width(110.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                    }

                    // Part 2
                    Text("Portion 2:", color = TextSecondary, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(splitMethod2.label, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        OutlinedTextField(
                            value = splitAmount2Text,
                            onValueChange = { splitAmount2Text = it },
                            label = { Text("NPR", color = TextSecondary, fontSize = 10.sp) },
                            modifier = Modifier.width(110.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                        )
                    }
                }
            }
        }
    }
}
