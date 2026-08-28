package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.model.TableStatus
import com.example.ui.theme.*
import com.example.util.PriceFormatter

@Composable
fun PureVegBadge(
    modifier: Modifier = Modifier,
    showText: Boolean = true
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF0D2818))
            .border(1.dp, VegGreen, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Green square with circle
        Box(
            modifier = Modifier
                .size(12.dp)
                .border(1.2.dp, VegGreen, RoundedCornerShape(2.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(VegGreen)
            )
        }
        if (showText) {
            Text(
                text = "100% PURE VEG & EGGLESS",
                color = VegGreen,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun NprPriceText(
    amount: Double,
    modifier: Modifier = Modifier,
    color: Color = GoldenAmber,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Text(
        text = PriceFormatter.formatNpr(amount),
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        modifier = modifier
    )
}

@Composable
fun ProductBadgeTag(
    badgeText: String,
    modifier: Modifier = Modifier
) {
    val bgColor = when (badgeText.lowercase()) {
        "best seller" -> WaffleOrange
        "must try" -> GoldenAmber
        "chef special" -> Color(0xFFE76F51)
        "loaded" -> Color(0xFFF4A261)
        "hot" -> ErrorRed
        "trending" -> Color(0xFF3A86FF)
        else -> WaffleOrange
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = badgeText.uppercase(),
            color = Color.Black,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
fun TableStatusChip(
    status: TableStatus,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (status) {
        TableStatus.AVAILABLE -> Pair(Color(0xFF0F3D37), VegGreen)
        TableStatus.OCCUPIED -> Pair(Color(0xFF3D2A05), GoldenAmber)
        TableStatus.ORDERED -> Pair(Color(0xFF3D1F05), WaffleOrange)
        TableStatus.KOT_SENT -> Pair(Color(0xFF1E2A4A), InfoBlue)
        TableStatus.PREPARING -> Pair(Color(0xFF38234D), Color(0xFFC77DFF))
        TableStatus.READY -> Pair(Color(0xFF0D3325), SuccessGreen)
        TableStatus.BILLING -> Pair(Color(0xFF4A3410), Color(0xFFFFD166))
        TableStatus.PAID -> Pair(Color(0xFF1C3A27), Color(0xFF52B788))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, fg.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun OrderStatusBadge(
    status: OrderStatus,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (status) {
        OrderStatus.PENDING -> Pair(Color(0xFF3D2A05), GoldenAmber)
        OrderStatus.CONFIRMED, OrderStatus.ACCEPTED -> Pair(Color(0xFF142B47), InfoBlue)
        OrderStatus.PREPARING -> Pair(Color(0xFF3B1E54), Color(0xFFD4A5FF))
        OrderStatus.READY -> Pair(Color(0xFF0E382B), SuccessGreen)
        OrderStatus.SERVED -> Pair(Color(0xFF163832), VegGreen)
        OrderStatus.OUT_FOR_DELIVERY -> Pair(Color(0xFF422800), WaffleOrange)
        OrderStatus.DELIVERED, OrderStatus.COMPLETED -> Pair(Color(0xFF0E382B), SuccessGreen)
        OrderStatus.CANCELLED, OrderStatus.REFUNDED -> Pair(Color(0xFF3B1115), ErrorRed)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PaymentStatusBadge(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (status) {
        PaymentStatus.SUCCESS -> Pair(Color(0xFF0E382B), SuccessGreen)
        PaymentStatus.PENDING -> Pair(Color(0xFF3D2A05), GoldenAmber)
        PaymentStatus.FAILED, PaymentStatus.CANCELLED -> Pair(Color(0xFF3B1115), ErrorRed)
        PaymentStatus.REFUNDED -> Pair(Color(0xFF282828), TextSecondary)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.label.uppercase(),
            color = fg,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
