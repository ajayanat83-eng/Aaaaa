package com.example.ui.staff

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CafeRepository
import com.example.model.PaperWidth
import com.example.service.BluetoothThermalPrinterService
import com.example.service.PrinterService
import com.example.service.PrinterStatus
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PrinterSettingsScreen(
    repository: CafeRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val printerSettings by repository.printerSettings.collectAsState()
    val printLogs by repository.printLogs.collectAsState()
    val orders by repository.orders.collectAsState()
    val kots by repository.kots.collectAsState()

    val bluetoothPrinterService = remember { BluetoothThermalPrinterService.instance }
    val printerStatus by bluetoothPrinterService.printerStatus.collectAsState()
    val pairedDevices by bluetoothPrinterService.pairedDevices.collectAsState()

    var deviceName by remember { mutableStateOf(printerSettings.printerName) }
    var macAddress by remember { mutableStateOf(printerSettings.macAddress) }
    var paperWidth by remember { mutableStateOf(printerSettings.paperWidth) }
    var autoPrint by remember { mutableStateOf(printerSettings.autoPrint) }
    var kotPrint by remember { mutableStateOf(printerSettings.kotPrint) }
    var billPrint by remember { mutableStateOf(printerSettings.billPrint) }
    var testPrintOutput by remember { mutableStateOf("") }
    var isPrintingBt by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        bluetoothPrinterService.refreshPairedDevices(context)
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
                    Text("🖨️ Bluetooth Thermal Printer Manager", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("ESC/POS 58mm / 80mm Setup & Live Testing", color = GoldenAmber, fontSize = 11.sp)
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
            // Bluetooth Status Banner
            when (val st = printerStatus) {
                is PrinterStatus.Connecting -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF192A3D))
                            .border(1.dp, InfoBlue, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = InfoBlue)
                            Text("Connecting to Bluetooth Printer (${st.deviceName})...", color = InfoBlue, fontSize = 12.sp)
                        }
                    }
                }
                is PrinterStatus.Printing -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF382305))
                            .border(1.dp, GoldenAmber, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = GoldenAmber)
                            Text("Printing ESC/POS stream: ${st.message}", color = GoldenAmber, fontSize = 12.sp)
                        }
                    }
                }
                is PrinterStatus.Success -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F3D24))
                            .border(1.dp, SuccessGreen, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                            Text(st.message, color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                is PrinterStatus.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF3B1115))
                            .border(1.dp, ErrorRed, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp))
                            Text(st.errorMessage, color = ErrorRed, fontSize = 12.sp)
                        }
                    }
                }
                PrinterStatus.Idle -> {}
            }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Paired Bluetooth Thermal Printers", color = GoldenAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = {
                                bluetoothPrinterService.refreshPairedDevices(context)
                                Toast.makeText(context, "Scanning paired devices...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GoldenAmber)
                        }
                    }

                    if (pairedDevices.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            pairedDevices.forEach { dev ->
                                val isSelected = macAddress == dev.address || deviceName == dev.name
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFF381E04) else DarkSurfaceVariant)
                                        .border(1.dp, if (isSelected) GoldenAmber else CardBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            deviceName = dev.name
                                            macAddress = dev.address
                                            repository.updatePrinterSettings(
                                                printerSettings.copy(
                                                    printerName = dev.name,
                                                    macAddress = dev.address
                                                )
                                            )
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(dev.name, color = if (isSelected) GoldenAmber else TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(dev.address, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = GoldenAmber, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = deviceName,
                        onValueChange = {
                            deviceName = it
                            repository.updatePrinterSettings(printerSettings.copy(printerName = it))
                        },
                        label = { Text("Printer Name", color = TextSecondary) },
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

                    OutlinedTextField(
                        value = macAddress,
                        onValueChange = {
                            macAddress = it
                            repository.updatePrinterSettings(printerSettings.copy(macAddress = it))
                        },
                        label = { Text("Bluetooth MAC Address", color = TextSecondary) },
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

            // ==================== 3. LIVE TEST PRINT ACTIONS ====================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val sampleOrder = orders.firstOrNull()
                        if (sampleOrder != null) {
                            testPrintOutput = repository.printBill(sampleOrder)
                            coroutineScope.launch {
                                isPrintingBt = true
                                PrinterService.printBillDirectBluetooth(sampleOrder, printerSettings, context)
                                isPrintingBt = false
                            }
                        } else {
                            Toast.makeText(context, "No orders available for test bill", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WaffleOrange, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Print Test Bill", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        val sampleKot = kots.firstOrNull()
                        if (sampleKot != null) {
                            testPrintOutput = repository.printKot(sampleKot)
                            coroutineScope.launch {
                                isPrintingBt = true
                                PrinterService.printKotDirectBluetooth(sampleKot, printerSettings, context)
                                isPrintingBt = false
                            }
                        } else {
                            Toast.makeText(context, "No KOTs available for test print", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenAmber, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.SoupKitchen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Print Test KOT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
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
                    printLogs.take(6).forEach { log ->
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
