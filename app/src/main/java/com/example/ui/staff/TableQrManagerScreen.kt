package com.example.ui.staff

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CafeRepository
import com.example.model.Table
import com.example.model.TableStatus
import com.example.ui.components.TableQrCodeView
import com.example.ui.components.TableStatusChip
import com.example.ui.theme.*

/**
 * Administrative Screen for Generating and Managing Unique QR Codes for each Cafe Table.
 * Allows cafe administrators to generate printable QR table stands, preview payloads,
 * share QR URLs, and simulate customer scanning to auto-prefill tables in the ordering flow.
 */
@Composable
fun TableQrManagerScreen(
    repository: CafeRepository,
    onNavigateBack: () -> Unit,
    onSimulateScanToOrder: (Table) -> Unit
) {
    val tables by repository.tables.collectAsState()
    val context = LocalContext.current
    var selectedTable by remember { mutableStateOf<Table?>(tables.firstOrNull()) }
    var showQrStandModal by remember { mutableStateOf(false) }
    var showAddTableDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredTables = tables.filter {
        it.tableNumber.contains(searchQuery, ignoreCase = true) ||
                it.tableId.contains(searchQuery, ignoreCase = true)
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
                        Text("Table QR Code Generator", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Generate & manage dine-in table QR stands", color = TextSecondary, fontSize = 11.sp)
                    }
                }
                IconButton(onClick = { showAddTableDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Table", tint = GoldenAmber)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search and Summary Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search tables...", color = TextMuted, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = WaffleOrange,
                        unfocusedBorderColor = CardBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Text("Select a table to view, generate, print, or test its QR Stand:", color = TextSecondary, fontSize = 12.sp)

            // Table Grid / Selection Cards
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredTables) { table ->
                    val isSelected = selectedTable?.tableId == table.tableId
                    val qrPayload = "tjwcafe://order?tableId=${table.tableId}&tableNumber=${table.tableNumber}"

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF331D08) else DarkSurface)
                            .border(
                                1.5.dp,
                                if (isSelected) WaffleOrange else CardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedTable = table
                                showQrStandModal = true
                            }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(table.tableNumber, color = GoldenAmber, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${table.capacity} 🪑", color = TextSecondary, fontSize = 11.sp)
                        }

                        // Compact Table QR View
                        TableQrCodeView(
                            data = qrPayload,
                            size = 90.dp,
                            backgroundColor = Color.White
                        )

                        Text(
                            text = "Tap for Stand & Scan",
                            color = WaffleOrange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Quick Batch Actions Bar
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${tables.size} Active Tables Registered", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("QR format: tjwcafe://order?table=...", color = TextSecondary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            Toast.makeText(context, "Ready to print ${tables.size} table QR tent cards!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // ==================== QR STAND MODAL & TEST SIMULATOR ====================
    if (showQrStandModal && selectedTable != null) {
        val table = selectedTable!!
        val qrPayload = "tjwcafe://order?tableId=${table.tableId}&tableNumber=${table.tableNumber}"
        val webUrl = "https://tjwcafe.com/table/${table.tableNumber}"

        Dialog(onDismissRequest = { showQrStandModal = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldenAmber)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Modal Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Table QR Stand & Deep-Link", color = GoldenAmber, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("The Janakpur Waffle & Cafe", color = TextSecondary, fontSize = 11.sp)
                        }
                        IconButton(onClick = { showQrStandModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    // Printable Table Stand Card Mockup
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFFDF5))
                            .border(2.dp, WaffleOrange, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🧇 THE JANAKPUR WAFFLE & CAFE", color = Color(0xFF6D3800), fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Text("100% PURE VEG & EGGLESS", color = Color(0xFF1B5E20), fontSize = 10.sp, fontWeight = FontWeight.Bold)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF6D3800))
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text(table.tableNumber, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                        }

                        // High Resolution QR Code
                        TableQrCodeView(
                            data = qrPayload,
                            size = 150.dp,
                            backgroundColor = Color.White,
                            qrColor = Color.Black
                        )

                        Text("📱 Scan with Camera to View Menu & Order", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Free Wi-Fi: TJW_Cafe_Guest | Pass: waffle2026", color = Color.DarkGray, fontSize = 10.sp)
                    }

                    // Action Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Simulate customer scan into ordering workflow
                        Button(
                            onClick = {
                                showQrStandModal = false
                                onSimulateScanToOrder(table)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate Scan & Order for ${table.tableNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        // 2. Share / Copy Link
                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Order at The Janakpur Waffle (${table.tableNumber}): $webUrl\nApp Payload: $qrPayload")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Table QR Link"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share QR Deep-Link", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // ==================== ADD TABLE DIALOG ====================
    if (showAddTableDialog) {
        var newTableNum by remember { mutableStateOf("TJW-TABLE-${(tables.size + 1).toString().padStart(2, '0')}") }
        var newCapacity by remember { mutableStateOf("4") }

        AlertDialog(
            onDismissRequest = { showAddTableDialog = false },
            title = { Text("Add New Cafe Table", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTableNum,
                        onValueChange = { newTableNum = it.uppercase() },
                        label = { Text("Table Number") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = WaffleOrange,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                    OutlinedTextField(
                        value = newCapacity,
                        onValueChange = { newCapacity = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Seating Capacity") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = WaffleOrange,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cap = newCapacity.toIntOrNull() ?: 4
                        val newTbl = Table(
                            tableId = "tbl_${newTableNum.lowercase().replace("-", "_")}",
                            tableNumber = newTableNum,
                            capacity = cap
                        )
                        repository.addTable(newTbl)
                        showAddTableDialog = false
                        Toast.makeText(context, "Added $newTableNum with QR code!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber, contentColor = Color.Black)
                ) {
                    Text("Create Table & QR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTableDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }
}
