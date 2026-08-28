package com.example.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.window.Dialog
import com.example.data.CafeRepository
import com.example.model.Table
import com.example.model.TableStatus
import com.example.ui.components.NprPriceText
import com.example.ui.components.TableStatusChip
import com.example.ui.theme.*

@Composable
fun TableManagementScreen(
    repository: CafeRepository,
    onNavigateBack: () -> Unit
) {
    val tables by repository.tables.collectAsState()
    var selectedTableForAction by remember { mutableStateOf<Table?>(null) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showAddTableDialog by remember { mutableStateOf(false) }

    if (selectedTableForAction != null) {
        val tbl = selectedTableForAction!!
        Dialog(onDismissRequest = { selectedTableForAction = null }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(tbl.tableNumber, color = GoldenAmber, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Capacity: ${tbl.capacity} Seats", color = TextSecondary, fontSize = 12.sp)
                        }
                        TableStatusChip(status = tbl.status)
                    }

                    if (tbl.activeOrderNumber != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Active Order: ${tbl.activeOrderNumber}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Current Bill:", color = TextSecondary, fontSize = 12.sp)
                                    NprPriceText(amount = tbl.activeOrderTotal, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Divider(color = CardBorder, thickness = 0.5.dp)

                    // Quick Table Actions
                    Text("Change Status / Table Actions:", color = TextSecondary, fontSize = 12.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                repository.clearTable(tbl.tableId)
                                selectedTableForAction = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = VegGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mark Free", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                repository.updateTableStatus(tbl.tableId, TableStatus.OCCUPIED)
                                selectedTableForAction = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Occupied", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                showTransferDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = InfoBlue, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Transfer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showTransferDialog && selectedTableForAction != null) {
        Dialog(onDismissRequest = { showTransferDialog = false }) {
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
                    Text("Transfer ${selectedTableForAction!!.tableNumber} to:", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    val otherTables = tables.filter { it.tableId != selectedTableForAction!!.tableId }
                    otherTables.forEach { target ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .clickable {
                                    repository.transferTable(selectedTableForAction!!.tableId, target.tableId)
                                    showTransferDialog = false
                                    selectedTableForAction = null
                                }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(target.tableNumber, color = TextPrimary, fontSize = 13.sp)
                            TableStatusChip(status = target.status)
                        }
                    }
                }
            }
        }
    }

    if (showAddTableDialog) {
        var newTableNum by remember { mutableStateOf("TJW-TABLE-${(tables.size + 1).toString().padStart(2, '0')}") }
        var capacityText by remember { mutableStateOf("4") }

        Dialog(onDismissRequest = { showAddTableDialog = false }) {
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
                    Text("➕ Add New Dining Table", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = newTableNum,
                        onValueChange = { newTableNum = it },
                        label = { Text("Table Identifier", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = capacityText,
                        onValueChange = { capacityText = it },
                        label = { Text("Capacity (Seats)", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val cap = capacityText.toIntOrNull() ?: 4
                            repository.addTable(newTableNum, cap)
                            showAddTableDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange, contentColor = Color.Black)
                    ) {
                        Text("Save Table", fontWeight = FontWeight.Bold)
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
                        Text("🪑 Floor & Table Management", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("${tables.count { it.status == TableStatus.AVAILABLE }} of ${tables.size} Tables Available", color = VegGreen, fontSize = 11.sp)
                    }
                }
                IconButton(onClick = { showAddTableDialog = true }) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Table", tint = GoldenAmber)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(VegGreen, RoundedCornerShape(2.dp)))
                    Text("Free", color = TextSecondary, fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(GoldenAmber, RoundedCornerShape(2.dp)))
                    Text("Occupied", color = TextSecondary, fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(WaffleOrange, RoundedCornerShape(2.dp)))
                    Text("Ordered/KOT", color = TextSecondary, fontSize = 10.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFFC77DFF), RoundedCornerShape(2.dp)))
                    Text("Preparing", color = TextSecondary, fontSize = 10.sp)
                }
            }

            // Tables Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tables) { table ->
                    val isAvailable = table.status == TableStatus.AVAILABLE
                    val borderColor = when (table.status) {
                        TableStatus.AVAILABLE -> VegGreen
                        TableStatus.OCCUPIED -> GoldenAmber
                        TableStatus.ORDERED, TableStatus.KOT_SENT -> WaffleOrange
                        TableStatus.PREPARING -> Color(0xFFC77DFF)
                        TableStatus.READY -> SuccessGreen
                        TableStatus.BILLING, TableStatus.PAID -> Color(0xFF52B788)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurface)
                            .border(1.5.dp, borderColor.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                            .clickable { selectedTableForAction = table }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🪑", fontSize = 22.sp)
                            Text(
                                text = table.tableNumber.replace("TJW-TABLE-", "Table "),
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            TableStatusChip(status = table.status)
                            if (table.activeOrderTotal > 0) {
                                NprPriceText(amount = table.activeOrderTotal, fontSize = 11.sp)
                            } else {
                                Text("${table.capacity} Seats", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
