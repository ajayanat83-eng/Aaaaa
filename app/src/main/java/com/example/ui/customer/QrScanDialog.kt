package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.Table
import com.example.ui.components.PureVegBadge
import com.example.ui.theme.*

@Composable
fun QrScanDialog(
    tables: List<Table>,
    onDismiss: () -> Unit,
    onTableSelected: (Table) -> Unit
) {
    var rawQrInput by remember { mutableStateOf("") }
    var scanStatusMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📷 Scan Dine-In Table QR",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Simulated Scanner Viewfinder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0A0A0A))
                        .border(2.dp, WaffleOrange, RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "QR Scanner",
                            tint = GoldenAmber,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Point camera at Table QR stand to auto pre-fill table ID",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Or manual paste / code input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = rawQrInput,
                        onValueChange = { rawQrInput = it },
                        placeholder = { Text("e.g. tjwcafe://order?tableNumber=TJW-TABLE-04", color = TextMuted, fontSize = 11.sp) },
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
                            val parsedTable = parseTableFromQr(rawQrInput, tables)
                            if (parsedTable != null) {
                                onTableSelected(parsedTable)
                                onDismiss()
                            } else {
                                scanStatusMessage = "Could not identify table from scanned payload."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Parse", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (scanStatusMessage.isNotBlank()) {
                    Text(scanStatusMessage, color = ErrorRed, fontSize = 11.sp)
                }

                Text(
                    text = "Or tap your table number below to order directly:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                // Grid of tables
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tables) { table ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    onTableSelected(table)
                                    onDismiss()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = table.tableNumber.replace("TJW-TABLE-", "T-"),
                                    color = GoldenAmber,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${table.capacity} Seats",
                                    color = TextMuted,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                PureVegBadge()
            }
        }
    }
}

private fun parseTableFromQr(payload: String, tables: List<Table>): Table? {
    val trimmed = payload.trim()
    // 1. Direct Table Number match (e.g. "TJW-TABLE-03" or "03" or "3")
    tables.find { it.tableNumber.equals(trimmed, ignoreCase = true) || it.tableId.equals(trimmed, ignoreCase = true) }?.let {
        return it
    }

    // 2. Query param in URL/URI (e.g. tjwcafe://order?tableNumber=TJW-TABLE-02 or tableId=tbl_02 or table=2)
    val regexTableNum = Regex("tableNumber=([a-zA-Z0-9_-]+)", RegexOption.IGNORE_CASE)
    regexTableNum.find(trimmed)?.groupValues?.get(1)?.let { tableNum ->
        tables.find { it.tableNumber.equals(tableNum, ignoreCase = true) || it.tableId.equals(tableNum, ignoreCase = true) }?.let {
            return it
        }
    }

    val regexTableId = Regex("tableId=([a-zA-Z0-9_-]+)", RegexOption.IGNORE_CASE)
    regexTableId.find(trimmed)?.groupValues?.get(1)?.let { id ->
        tables.find { it.tableId.equals(id, ignoreCase = true) }?.let {
            return it
        }
    }

    val regexTableShort = Regex("table=([0-9]+)", RegexOption.IGNORE_CASE)
    regexTableShort.find(trimmed)?.groupValues?.get(1)?.let { num ->
        val formatted = "TJW-TABLE-${num.padStart(2, '0')}"
        tables.find { it.tableNumber.equals(formatted, ignoreCase = true) }?.let {
            return it
        }
    }

    return null
}

