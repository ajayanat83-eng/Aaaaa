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
import com.example.data.CafeRepository
import com.example.model.KOT
import com.example.model.KitchenStatus
import com.example.ui.components.PureVegBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun KitchenKdsScreen(
    repository: CafeRepository,
    onNavigateBack: () -> Unit
) {
    val kots by repository.kots.collectAsState()
    var selectedFilter by remember { mutableStateOf<KitchenStatus?>(null) }
    var printedKotNotification by remember { mutableStateOf("") }

    val filteredKots = remember(selectedFilter, kots) {
        if (selectedFilter == null) kots
        else kots.filter { it.status == selectedFilter }
    }

    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                            Text("🍳 Kitchen Display (KDS)", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text("Live Real-Time Kitchen Tickets", color = GoldenAmber, fontSize = 11.sp)
                        }
                    }
                    PureVegBadge(showText = false)
                }

                if (printedKotNotification.isNotBlank()) {
                    Text(printedKotNotification, color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Filter Tabs
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = { Text("All (${kots.size})", fontSize = 11.sp) }
                        )
                    }
                    items(KitchenStatus.values()) { status ->
                        FilterChip(
                            selected = selectedFilter == status,
                            onClick = { selectedFilter = status },
                            label = {
                                Text("${status.label} (${kots.count { it.status == status }})", fontSize = 11.sp)
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (filteredKots.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("No kitchen tickets under this filter", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredKots) { kot ->
                    val statusColor = when (kot.status) {
                        KitchenStatus.NEW -> WaffleOrange
                        KitchenStatus.ACCEPTED -> InfoBlue
                        KitchenStatus.PREPARING -> Color(0xFFC77DFF)
                        KitchenStatus.READY -> SuccessGreen
                        KitchenStatus.SERVED -> VegGreen
                        KitchenStatus.CANCELLED -> ErrorRed
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkSurface)
                            .border(1.5.dp, statusColor, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(kot.kotNumber, color = GoldenAmber, fontSize = 15.sp, fontWeight = FontWeight.Black)
                                    Text("• ${kot.humanOrderNumber}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statusColor.copy(alpha = 0.2f))
                                        .border(1.dp, statusColor, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(kot.status.label.uppercase(), color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Type: ${kot.orderType.label}${if (kot.tableNumber != null) " • Table: ${kot.tableNumber}" else ""}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Time: ${timeFormatter.format(Date(kot.time))}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }

                            Divider(color = CardBorder, thickness = 0.5.dp)

                            // Items List
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                kot.items.forEachIndexed { idx, item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "${idx + 1}. ${item.productName}${if (item.variantName != null) " [${item.variantName}]" else ""}",
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (item.addonNames.isNotEmpty()) {
                                                Text("   └ Extra: ${item.addonNames.joinToString(", ")}", color = GoldenAmber, fontSize = 11.sp)
                                            }
                                            if (item.notes.isNotBlank()) {
                                                Text("   * Note: ${item.notes}", color = Color(0xFFFF9E00), fontSize = 11.sp)
                                            }
                                        }
                                        Text("x${item.quantity}", color = GoldenAmber, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            if (kot.notes.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF281C06))
                                        .padding(8.dp)
                                ) {
                                    Text("📝 Special: ${kot.notes}", color = GoldenAmber, fontSize = 11.sp)
                                }
                            }

                            Divider(color = CardBorder, thickness = 0.5.dp)

                            // KDS Chef Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (kot.status == KitchenStatus.NEW) {
                                    Button(
                                        onClick = { repository.updateKotStatus(kot.kotNumber, KitchenStatus.PREPARING) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC77DFF), contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Start Preparing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (kot.status == KitchenStatus.PREPARING) {
                                    Button(
                                        onClick = { repository.updateKotStatus(kot.kotNumber, KitchenStatus.READY) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Mark Ready", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (kot.status == KitchenStatus.READY) {
                                    Button(
                                        onClick = { repository.updateKotStatus(kot.kotNumber, KitchenStatus.SERVED) },
                                        colors = ButtonDefaults.buttonColors(containerColor = VegGreen, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Mark Served", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        repository.printKot(kot)
                                        printedKotNotification = "Reprinted ${kot.kotNumber} ESC/POS to Kitchen!"
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldenAmber),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldenAmber),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reprint KOT", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
