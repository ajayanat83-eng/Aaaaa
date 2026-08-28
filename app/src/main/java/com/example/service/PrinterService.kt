package com.example.service

import android.content.Context
import com.example.model.KOT
import com.example.model.Order
import com.example.model.PaperWidth
import com.example.model.PrinterSettings
import com.example.util.PriceFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrinterService {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

    /**
     * Generates plain text printable preview string for UI / sharing.
     */
    fun generateBillReceipt(order: Order, settings: PrinterSettings): String {
        val width = settings.paperWidth.charsPerLine
        val line = "=".repeat(width)
        val dashLine = "-".repeat(width)
        val sb = StringBuilder()

        sb.append(centerText("THE JANAKPUR WAFFLE & CAFE", width)).append("\n")
        sb.append(centerText("100% PURE VEG & EGGLESS", width)).append("\n")
        sb.append(centerText("Station Road, Janakpurdham, Nepal", width)).append("\n")
        sb.append(centerText("Ph: +977-9706612914", width)).append("\n")
        sb.append(line).append("\n")

        sb.append(formatRow("Order No: ${order.humanOrderNumber}", "Type: ${order.orderType.label}", width)).append("\n")
        if (order.tableNumber != null) {
            sb.append(formatRow("Table: ${order.tableNumber}", "Cust: ${order.customerName}", width)).append("\n")
        } else {
            sb.append(formatRow("Cust: ${order.customerName}", "Ph: ${order.customerPhone.ifBlank { "N/A" }}", width)).append("\n")
        }
        sb.append("Date: ${dateFormatter.format(Date(order.createdAt))}\n")
        sb.append(dashLine).append("\n")

        // Items Header
        if (width >= 40) {
            sb.append(String.format("%-22s %4s %8s %8s\n", "Item", "Qty", "Price", "Total"))
        } else {
            sb.append(String.format("%-16s %3s %5s %6s\n", "Item", "Qty", "Rate", "Amt"))
        }
        sb.append(dashLine).append("\n")

        // Item Rows
        order.items.forEach { item ->
            val itemName = if (item.variantName != null) "${item.productName} (${item.variantName})" else item.productName
            val truncated = if (itemName.length > (if (width >= 40) 22 else 16)) {
                itemName.take(if (width >= 40) 20 else 14) + ".."
            } else itemName

            if (width >= 40) {
                sb.append(String.format("%-22s %4d %8.1f %8.1f\n", truncated, item.quantity, item.unitPrice, item.totalPrice))
            } else {
                sb.append(String.format("%-16s %3d %5.0f %6.0f\n", truncated, item.quantity, item.unitPrice, item.totalPrice))
            }
            if (item.addonNames.isNotEmpty()) {
                sb.append("  + ${item.addonNames.joinToString(", ")}\n")
            }
        }
        sb.append(dashLine).append("\n")

        // Totals
        sb.append(formatRow("Subtotal:", PriceFormatter.formatNpr(order.subtotal), width)).append("\n")
        if (order.discount > 0) {
            sb.append(formatRow("Discount (${order.couponCode ?: "Promo"}):", PriceFormatter.formatDiscount(order.discount), width)).append("\n")
        }
        if (order.tax > 0) {
            sb.append(formatRow("Tax:", PriceFormatter.formatNpr(order.tax), width)).append("\n")
        }
        if (order.deliveryFee > 0) {
            sb.append(formatRow("Delivery Fee:", PriceFormatter.formatNpr(order.deliveryFee), width)).append("\n")
        }
        sb.append(line).append("\n")
        sb.append(formatRow("GRAND TOTAL:", PriceFormatter.formatNpr(order.grandTotal), width)).append("\n")
        sb.append(line).append("\n")

        sb.append(formatRow("Payment Mode:", order.paymentMethod.label, width)).append("\n")
        sb.append(formatRow("Payment Status:", order.paymentStatus.label, width)).append("\n")
        sb.append(dashLine).append("\n")
        sb.append(centerText("Thank You! Visit Again! 🧇", width)).append("\n")
        sb.append(centerText("For Home Delivery WhatsApp: +977-9706612914", width)).append("\n")
        sb.append("\n\n") // Feed lines

        return sb.toString()
    }

    /**
     * Generates plain text printable preview string for Kitchen KOT.
     */
    fun generateKotReceipt(kot: KOT, settings: PrinterSettings): String {
        val width = settings.paperWidth.charsPerLine
        val line = "=".repeat(width)
        val dashLine = "-".repeat(width)
        val sb = StringBuilder()

        sb.append(centerText("*** KITCHEN ORDER TICKET (KOT) ***", width)).append("\n")
        sb.append(centerText("THE JANAKPUR WAFFLE & CAFE", width)).append("\n")
        sb.append(line).append("\n")

        sb.append(formatRow("KOT: ${kot.kotNumber}", "Order: ${kot.humanOrderNumber}", width)).append("\n")
        sb.append(formatRow("Type: ${kot.orderType.label}", "Table: ${kot.tableNumber ?: "N/A"}", width)).append("\n")
        sb.append("Time: ${dateFormatter.format(Date(kot.time))}\n")
        sb.append(dashLine).append("\n")

        sb.append(String.format("%-4s %-20s %4s\n", "#", "Item Name", "Qty"))
        sb.append(dashLine).append("\n")

        kot.items.forEachIndexed { idx, item ->
            val itemName = if (item.variantName != null) "${item.productName} [${item.variantName}]" else item.productName
            sb.append(String.format("%-4d %-20s %4d\n", idx + 1, itemName.take(20), item.quantity))
            if (item.addonNames.isNotEmpty()) {
                sb.append("     + Extra: ${item.addonNames.joinToString(", ")}\n")
            }
            if (item.notes.isNotBlank()) {
                sb.append("     * NOTE: ${item.notes}\n")
            }
        }
        sb.append(dashLine).append("\n")
        if (kot.notes.isNotBlank()) {
            sb.append("SPECIAL INSTRUCTION: ${kot.notes}\n")
            sb.append(dashLine).append("\n")
        }
        sb.append(centerText("--- END OF KOT ---", width)).append("\n")
        sb.append("\n\n")

        return sb.toString()
    }

    /**
     * Direct Bluetooth Thermal Printing using ESC/POS protocol
     */
    suspend fun printBillDirectBluetooth(order: Order, settings: PrinterSettings, context: Context): Result<String> {
        return BluetoothThermalPrinterService.instance.printCustomerReceipt(order, settings, context)
    }

    /**
     * Direct Bluetooth Kitchen KOT Printing using ESC/POS protocol
     */
    suspend fun printKotDirectBluetooth(kot: KOT, settings: PrinterSettings, context: Context): Result<String> {
        return BluetoothThermalPrinterService.instance.printKitchenKot(kot, settings, context)
    }

    private fun centerText(text: String, width: Int): String {
        if (text.length >= width) return text.take(width)
        val padding = (width - text.length) / 2
        return " ".repeat(padding) + text
    }

    private fun formatRow(left: String, right: String, width: Int): String {
        val totalSpace = width - left.length - right.length
        if (totalSpace <= 0) return "$left $right"
        return left + " ".repeat(totalSpace) + right
    }
}

