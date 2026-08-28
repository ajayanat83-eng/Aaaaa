package com.example.ui.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CafeRepository
import com.example.model.PaperWidth
import com.example.model.PrinterSettings
import com.example.ui.theme.*

@Composable
fun PrinterSettingsScreen(
    repository: CafeRepository,
    onNavigateBack: () -> Unit
) {
    val printerSettings by repository.printerSettings.collectAsState()
    val printLogs by repository.printLogs.collectAsState()
    val orders by repository.orders.collectAsState()

    var deviceName by remember { mutableStateOf(printerSettings.printerName) }
    var paperWidth by remember { mutableStateOf(printerSettings.paperWidth) }
    var autoPrint by remember { mutableStateOf(printerSettings.autoPrint) }
    var kotPrint by remember { mutableStateOf(printerSettings.kotPrint) }
    var billPrint by remember { mutableStateOf(printerSettings.billPrint) }
    var testPrintOutput by remember { mutableStateOf("") }

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
                    Text("🖨️ Bluetooth Thermal Printer Manager", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("ESC/POS 58mm / 80mm Setup", color = GoldenAmber, fontSize = 11.sp)
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
            // ==================== 1. DEVICE CONFIGURATION ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Bluetooth Printer Pairing", color = GoldenAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = deviceName,
                        onValueChange = {
                            deviceName = it
                            repository.updatePrinterSettings(printerSettings.copy(printerName = it))
                        },
                        label = { Text("Printer Bluetooth Name / MAC", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = WaffleOrange,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant
                        )
                    )

                    Text("Paper Roll Size:", color = TextSecondary, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PaperWidth.values().forEach { width ->
                            val isSel = paperWidth == width
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) Color(0xFF381E04) else DarkSurfaceVariant)
                                    .border(1.dp, if (isSel) GoldenAmber else CardBorder, RoundedCornerShape(10.dp))
                                    .clickable {
                                        paperWidth = width
                                        repository.updatePrinterSettings(printerSettings.copy(paperWidth = width))
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = width.label,
                                    color = if (isSel) GoldenAmber else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // ==================== 2. AUTO PRINT AUTOMATION ====================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Automated Printing Triggers", color = GoldenAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Auto-Print on Order Placement", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Trigger thermal output immediately", color = TextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = autoPrint,
                            onCheckedChange = {
                                autoPrint = it
                                repository.updatePrinterSettings(printerSettings.copy(autoPrint = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldenAmber)
                        )
                    }

                    Divider(color = CardBorder, thickness = 0.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Print Kitchen KOT on Order", color = TextPrimary, fontSize = 13.sp)
                        Switch(
                            checked = kotPrint,
                            onCheckedChange = {
                                kotPrint = it
                                repository.updatePrinterSettings(printerSettings.copy(kotPrint = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldenAmber)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Print Customer Bill Receipt", color = TextPrimary, fontSize = 13.sp)
                        Switch(
                            checked = billPrint,
                            onCheckedChange = {
                                billPrint = it
                                repository.updatePrinterSettings(printerSettings.copy(billPrint = it))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldenAmber)
                        )
                    }
                }
            }

            // ==================== 3. TEST PRINT ACTION ====================
            Button(
                onClick = {
                    val sampleOrder = orders.firstOrNull()
                    if (sampleOrder != null) {
                        testPrintOutput = repository.printBill(sampleOrder)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Print Test Bill (ESC/POS)", fontWeight = FontWeight.Bold)
            }

            // ==================== 4. TEST OUTPUT PREVIEW ====================
            if (testPrintOutput.isNotBlank()) {
                Text("Simulated Thermal Terminal Output:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .padding(12.dp)
                ) {
                    Text(
                        text = testPrintOutput,
                        color = Color(0xFF00FF66),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }

            // ==================== 5. PRINT LOGS ====================
            if (printLogs.isNotEmpty()) {
                Text("Recent Print History (${printLogs.size})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    printLogs.take(5).forEach { log ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .padding(8.dp)
                        ) {
                            Text(log, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
