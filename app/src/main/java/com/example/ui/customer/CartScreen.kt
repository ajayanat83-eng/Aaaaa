package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import kotlinx.coroutines.launch
import com.example.data.CafeRepository
import com.example.model.*
import com.example.ui.components.NprPriceText
import com.example.ui.components.PureVegBadge
import com.example.ui.theme.*
import com.example.util.PriceFormatter

@Composable
fun CartScreen(
    repository: CafeRepository,
    preselectedTable: Table? = null,
    onNavigateBack: () -> Unit,
    onProceedToCheckout: (
        orderType: OrderType,
        tableNumber: String?,
        deliveryAddress: String?,
        couponCode: String?,
        discountAmount: Double,
        redeemedPoints: Int,
        specialNotes: String
    ) -> Unit
) {
    val cartItems by repository.cartItems.collectAsState()
    val firestoreRepo = remember { com.example.data.FirestoreMenuRepository.instance }
    val firestoreCoupons by firestoreRepo.couponsFlow.collectAsState()
    val customerProfile by repository.customerProfile.collectAsState()
    val tables by repository.tables.collectAsState()
    val cafeSettings by repository.cafeSettings.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var orderType by remember { mutableStateOf(if (preselectedTable != null) OrderType.DINE_IN else OrderType.DINE_IN) }
    var selectedTableNumber by remember { mutableStateOf(preselectedTable?.tableNumber ?: "TJW-TABLE-01") }
    var deliveryAddress by remember { mutableStateOf(customerProfile.addresses.firstOrNull() ?: "Janakpurdham, Nepal") }
    var specialInstructions by remember { mutableStateOf("") }
    var couponInput by remember { mutableStateOf("") }
    var appliedCoupon by remember { mutableStateOf<Coupon?>(null) }
    var couponMessage by remember { mutableStateOf("") }
    var isCouponValidating by remember { mutableStateOf(false) }
    var isRedeemingLoyalty by remember { mutableStateOf(false) }

    val subtotal = cartItems.sumOf { it.totalPrice }

    // Coupon discount calculation
    val couponDiscount = remember(appliedCoupon, subtotal) {
        if (appliedCoupon == null || subtotal < appliedCoupon!!.minOrderAmount) 0.0
        else {
            when (appliedCoupon!!.discountType) {
                DiscountType.PERCENTAGE -> (subtotal * (appliedCoupon!!.discountValue / 100.0)).coerceAtMost(appliedCoupon!!.maxDiscount)
                DiscountType.FIXED -> appliedCoupon!!.discountValue.coerceAtMost(subtotal)
            }
        }
    }

    // Loyalty points calculation
    val maxPointsToRedeem = remember(customerProfile.loyaltyPoints, subtotal) {
        val maxPointsByPercentage = (subtotal * (cafeSettings.maxRedeemPercentage / 100.0)).toInt()
        minOf(customerProfile.loyaltyPoints, maxPointsByPercentage)
    }
    val loyaltyDiscount = if (isRedeemingLoyalty && customerProfile.loyaltyPoints >= cafeSettings.minRedeemPoints) {
        maxPointsToRedeem.toDouble()
    } else 0.0

    val totalDiscount = couponDiscount + loyaltyDiscount
    val deliveryFee = if (orderType == OrderType.DELIVERY) cafeSettings.deliveryFee else 0.0
    val grandTotal = (subtotal - totalDiscount + deliveryFee).coerceAtLeast(0.0)

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
                        Text("My Cart", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("${cartItems.sumOf { it.quantity }} items selected", color = TextSecondary, fontSize = 11.sp)
                    }
                }
                if (cartItems.isNotEmpty()) {
                    TextButton(onClick = { repository.clearCart() }) {
                        Text("Clear All", color = ErrorRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Amount:", color = TextSecondary, fontSize = 12.sp)
                                NprPriceText(amount = grandTotal, fontSize = 20.sp, color = GoldenAmber)
                            }
                            Button(
                                onClick = {
                                    onProceedToCheckout(
                                        orderType,
                                        if (orderType == OrderType.DINE_IN) selectedTableNumber else null,
                                        if (orderType == OrderType.DELIVERY) deliveryAddress else null,
                                        appliedCoupon?.code,
                                        totalDiscount,
                                        if (isRedeemingLoyalty) maxPointsToRedeem else 0,
                                        specialInstructions
                                    )
                                },
                                modifier = Modifier
                                    .height(48.dp)
                                    .widthIn(min = 160.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WaffleOrange,
                                    contentColor = Color.Black
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("Checkout", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🛒", fontSize = 56.sp)
                    Text("Your cart is empty", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Add delicious waffles, momos & shakes from our menu", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange, contentColor = Color.Black)
                    ) {
                        Text("Browse Menu", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==================== 1. ORDER TYPE SELECTOR ====================
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Order Type:", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OrderType.values().forEach { type ->
                                val isSelected = orderType == type
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Color(0xFF3D2205) else DarkSurface)
                                        .border(
                                            1.dp,
                                            if (isSelected) WaffleOrange else CardBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { orderType = type }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = when (type) {
                                                OrderType.DINE_IN -> "🪑"
                                                OrderType.TAKEAWAY -> "🛍️"
                                                OrderType.DELIVERY -> "🛵"
                                            },
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = type.label,
                                            color = if (isSelected) GoldenAmber else TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ==================== 2. DINE-IN TABLE / DELIVERY ADDRESS ====================
                if (orderType == OrderType.DINE_IN) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Select Table Number:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                tables.take(6).forEach { table ->
                                    val isSelected = selectedTableNumber == table.tableNumber
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) WaffleOrange else DarkSurfaceVariant)
                                            .clickable { selectedTableNumber = table.tableNumber }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = table.tableNumber.replace("TJW-TABLE-", "T"),
                                            color = if (isSelected) Color.Black else TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else if (orderType == OrderType.DELIVERY) {
                    item {
                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = { deliveryAddress = it },
                            label = { Text("Delivery Address in Janakpur", color = TextSecondary, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GoldenAmber) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = WaffleOrange,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            )
                        )
                    }
                }

                // ==================== 3. CART ITEMS LIST ====================
                item {
                    Text("Items in Cart (${cartItems.size})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                items(cartItems) { item ->
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.product.imageEmoji, fontSize = 28.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.name,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (item.selectedVariant != null) {
                                    Text(
                                        text = "Size: ${item.selectedVariant.name}",
                                        color = GoldenAmber,
                                        fontSize = 11.sp
                                    )
                                }
                                if (item.selectedAddons.isNotEmpty()) {
                                    Text(
                                        text = "+ ${item.selectedAddons.joinToString(", ") { it.name }}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                if (item.itemNotes.isNotBlank()) {
                                    Text(
                                        text = "Note: ${item.itemNotes}",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                                NprPriceText(amount = item.totalPrice, fontSize = 14.sp)
                            }

                            // Quantity Adjuster
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = { repository.updateCartQuantity(item.cartItemId, -1) },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(
                                        if (item.quantity == 1) Icons.Default.Delete else Icons.Default.Remove,
                                        contentDescription = "Decrease",
                                        tint = if (item.quantity == 1) ErrorRed else GoldenAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "${item.quantity}",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { repository.updateCartQuantity(item.cartItemId, 1) },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = GoldenAmber, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // ==================== 4. SPECIAL INSTRUCTIONS & NOTES ====================
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurface)
                            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = GoldenAmber, modifier = Modifier.size(18.dp))
                            Text("Special Instructions / Kitchen Notes", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = specialInstructions,
                            onValueChange = { specialInstructions = it },
                            placeholder = { Text("e.g. Less spicy momos, extra crispy waffle, separate chutney...", color = TextMuted, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = WaffleOrange,
                                unfocusedBorderColor = CardBorder,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant
                            )
                        )
                    }
                }

                // ==================== 5. COUPONS & LOYALTY ====================
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurface)
                            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏷️ Apply Coupon Code", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Validated with Cloud Firestore", color = GoldenAmber, fontSize = 10.sp)
                        }

                        if (appliedCoupon != null) {
                            // Coupon Applied Card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0F3D24))
                                    .border(1.dp, SuccessGreen, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                                    Column {
                                        Text(
                                            text = "${appliedCoupon!!.code} (${if (appliedCoupon!!.discountType == DiscountType.PERCENTAGE) "${appliedCoupon!!.discountValue.toInt()}% Off" else "NPR ${appliedCoupon!!.discountValue.toInt()} Off"})",
                                            color = SuccessGreen,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Discount: NPR ${couponDiscount.toInt()} saved on order total",
                                            color = Color(0xFFA5D6A7),
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        appliedCoupon = null
                                        couponMessage = ""
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = ErrorRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = couponInput,
                                    onValueChange = { couponInput = it.uppercase() },
                                    placeholder = { Text("e.g. TJW10, PUREVEG, WAFFLE50", color = TextMuted, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
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
                                Button(
                                    onClick = {
                                        if (couponInput.isBlank()) {
                                            couponMessage = "Please enter a coupon code"
                                            return@Button
                                        }
                                        coroutineScope.launch {
                                            isCouponValidating = true
                                            couponMessage = ""
                                            val result = firestoreRepo.validateCoupon(couponInput, subtotal)
                                            isCouponValidating = false
                                            when (result) {
                                                is com.example.data.CouponValidationResult.Success -> {
                                                    appliedCoupon = result.coupon
                                                    couponMessage = result.message
                                                }
                                                is com.example.data.CouponValidationResult.Error -> {
                                                    appliedCoupon = null
                                                    couponMessage = result.message
                                                }
                                            }
                                        }
                                    },
                                    enabled = !isCouponValidating,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber, contentColor = Color.Black)
                                ) {
                                    if (isCouponValidating) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                                    } else {
                                        Text("Apply", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Quick suggestion chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                firestoreCoupons.take(3).forEach { coup ->
                                    val discountLabel = if (coup.discountType == DiscountType.PERCENTAGE) "${coup.discountValue.toInt()}% Off" else "NPR ${coup.discountValue.toInt()}"
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(DarkSurfaceVariant)
                                            .border(0.5.dp, CardBorder, RoundedCornerShape(6.dp))
                                            .clickable {
                                                couponInput = coup.code
                                                coroutineScope.launch {
                                                    isCouponValidating = true
                                                    couponMessage = ""
                                                    val result = firestoreRepo.validateCoupon(coup.code, subtotal)
                                                    isCouponValidating = false
                                                    when (result) {
                                                        is com.example.data.CouponValidationResult.Success -> {
                                                            appliedCoupon = result.coupon
                                                            couponMessage = result.message
                                                        }
                                                        is com.example.data.CouponValidationResult.Error -> {
                                                            appliedCoupon = null
                                                            couponMessage = result.message
                                                        }
                                                    }
                                                }
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("${coup.code} ($discountLabel)", color = GoldenAmber, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        if (couponMessage.isNotBlank()) {
                            Text(
                                text = couponMessage,
                                color = if (appliedCoupon != null) SuccessGreen else ErrorRed,
                                fontSize = 11.sp
                            )
                        }

                        Divider(color = CardBorder, thickness = 0.5.dp)

                        // Loyalty Points Redemption
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("⭐ TJW Loyalty Points", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("You have ${customerProfile.loyaltyPoints} points (Worth NPR ${customerProfile.loyaltyPoints})", color = GoldenAmber, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isRedeemingLoyalty,
                                onCheckedChange = { isRedeemingLoyalty = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = GoldenAmber,
                                    checkedTrackColor = Color(0xFF3D2A05)
                                ),
                                enabled = customerProfile.loyaltyPoints >= cafeSettings.minRedeemPoints
                            )
                        }
                        if (isRedeemingLoyalty) {
                            Text("Redeeming $maxPointsToRedeem points (-NPR $maxPointsToRedeem discount)", color = SuccessGreen, fontSize = 11.sp)
                        }
                    }
                }

                // ==================== 5. BILL SUMMARY ====================
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurface)
                            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Bill Details", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Item Total (Subtotal)", color = TextSecondary, fontSize = 13.sp)
                            Text(PriceFormatter.formatNpr(subtotal), color = TextPrimary, fontSize = 13.sp)
                        }
                        if (couponDiscount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Coupon Discount (${appliedCoupon?.code})", color = SuccessGreen, fontSize = 13.sp)
                                Text(PriceFormatter.formatDiscount(couponDiscount), color = SuccessGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (loyaltyDiscount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Loyalty Points Redeemed", color = SuccessGreen, fontSize = 13.sp)
                                Text(PriceFormatter.formatDiscount(loyaltyDiscount), color = SuccessGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (deliveryFee > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Delivery Fee", color = TextSecondary, fontSize = 13.sp)
                                Text(PriceFormatter.formatNpr(deliveryFee), color = TextPrimary, fontSize = 13.sp)
                            }
                        }
                        Divider(color = CardBorder, thickness = 1.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Grand Total", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            NprPriceText(amount = grandTotal, fontSize = 18.sp, color = GoldenAmber)
                        }
                    }
                }
            }
        }
    }
}
