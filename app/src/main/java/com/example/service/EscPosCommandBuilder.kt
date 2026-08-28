package com.example.service

import com.example.model.KOT
import com.example.model.Order
import com.example.model.PaperWidth
import com.example.model.PrinterSettings
import com.example.util.PriceFormatter
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Low-level ESC/POS binary command builder for 58mm / 80mm thermal receipt printers.
 * Compliant with standard Epson/Star ESC/POS specifications.
 */
object EscPosCommandBuilder {

    private val CHARSET = Charset.forName("CP437")
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US)

    // Standard ESC/POS Control Codes
    private val ESC: Byte = 0x1B
    private val GS: Byte = 0x1D
    private val LF: Byte = 0x0A

    enum class Align {
        LEFT, CENTER, RIGHT
    }

    /**
     * Initializes the thermal printer hardware to default state.
     */
    fun initPrinter(): ByteArray = byteArrayOf(ESC, 0x40)

    /**
     * Sets text alignment (LEFT = 0, CENTER = 1, RIGHT = 2).
     */
    fun setAlign(align: Align): ByteArray {
        val mode: Byte = when (align) {
            Align.LEFT -> 0x00
            Align.CENTER -> 0x01
            Align.RIGHT -> 0x02
        }
        return byteArrayOf(ESC, 0x61, mode)
    }

    /**
     * Toggles bold text weight.
     */
    fun setBold(enabled: Boolean): ByteArray {
        return byteArrayOf(ESC, 0x45, if (enabled) 0x01 else 0x00)
    }

    /**
     * Sets font size scaling (1x to 4x).
     */
    fun setTextSize(widthScale: Int = 1, heightScale: Int = 1): ByteArray {
        val w = (widthScale - 1).coerceIn(0, 7)
        val h = (heightScale - 1).coerceIn(0, 7)
        val n = ((w shl 4) or h).toByte()
        return byteArrayOf(GS, 0x21, n)
    }

    /**
     * Sets underline mode.
     */
    fun setUnderline(enabled: Boolean): ByteArray {
        return byteArrayOf(ESC, 0x2D, if (enabled) 0x01 else 0x00)
    }

    /**
     * Feeds N lines of paper.
     */
    fun feedLines(lines: Int = 1): ByteArray {
        return byteArrayOf(ESC, 0x64, lines.coerceIn(1, 10).toByte())
    }

    /**
     * Full / Partial Paper Cut command.
     */
    fun cutPaper(): ByteArray {
        return byteArrayOf(GS, 0x56, 0x42, 0x00)
    }

    /**
     * Audio buzzer / beep tone on order ticket arrival.
     */
    fun buzzerBeep(): ByteArray {
        return byteArrayOf(ESC, 0x42, 0x02, 0x02)
    }

    /**
     * Converts a text string into bytes using standard printer encoding.
     */
    fun text(string: String): ByteArray {
        return string.toByteArray(CHARSET)
    }

    /**
     * Converts text with an immediate linefeed.
     */
    fun textLine(string: String): ByteArray {
        return (string + "\n").toByteArray(CHARSET)
    }

    /**
     * Generates complete binary ESC/POS byte stream for a Customer Bill Receipt.
     */
    fun buildCustomerReceiptBytes(order: Order, settings: PrinterSettings): ByteArray {
        val stream = ByteArrayOutputStream()
        val width = settings.paperWidth.charsPerLine
        val dividerLine = "=".repeat(width)
        val dashLine = "-".repeat(width)

        stream.write(initPrinter())

        // Header
        stream.write(setAlign(Align.CENTER))
        stream.write(setBold(true))
        stream.write(setTextSize(2, 2))
        stream.write(textLine("TJW CAFE"))
        stream.write(setTextSize(1, 1))
        stream.write(textLine("THE JANAKPUR WAFFLE & CAFE"))
        stream.write(setBold(false))
        stream.write(textLine("100% PURE VEG & EGGLESS"))
        stream.write(textLine("Station Road, Janakpurdham, Nepal"))
        stream.write(textLine("Ph: +977-9706612914"))
        stream.write(textLine(dividerLine))

        // Order Meta
        stream.write(setAlign(Align.LEFT))
        stream.write(setBold(true))
        stream.write(textLine(formatRow("Order No: ${order.humanOrderNumber}", "Type: ${order.orderType.label}", width)))
        stream.write(setBold(false))

        if (order.tableNumber != null) {
            stream.write(textLine(formatRow("Table: ${order.tableNumber}", "Cust: ${order.customerName}", width)))
        } else {
            stream.write(textLine(formatRow("Cust: ${order.customerName}", "Ph: ${order.customerPhone.ifBlank { "N/A" }}", width)))
        }
        stream.write(textLine("Date: ${dateFormatter.format(Date(order.createdAt))}"))
        stream.write(textLine(dashLine))

        // Items Table Header
        stream.write(setBold(true))
        if (width >= 40) {
            stream.write(textLine(String.format("%-22s %4s %8s %8s", "Item", "Qty", "Price", "Total")))
        } else {
            stream.write(textLine(String.format("%-16s %3s %5s %6s", "Item", "Qty", "Rate", "Amt")))
        }
        stream.write(setBold(false))
        stream.write(textLine(dashLine))

        // Item Rows
        order.items.forEach { item ->
            val itemName = if (item.variantName != null) "${item.productName} (${item.variantName})" else item.productName
            val maxLen = if (width >= 40) 22 else 16
            val truncated = if (itemName.length > maxLen) itemName.take(maxLen - 2) + ".." else itemName

            if (width >= 40) {
                stream.write(textLine(String.format("%-22s %4d %8.0f %8.0f", truncated, item.quantity, item.unitPrice, item.totalPrice)))
            } else {
                stream.write(textLine(String.format("%-16s %3d %5.0f %6.0f", truncated, item.quantity, item.unitPrice, item.totalPrice)))
            }
            if (item.addonNames.isNotEmpty()) {
                stream.write(textLine("  + ${item.addonNames.joinToString(", ")}"))
            }
        }
        stream.write(textLine(dashLine))

        // Totals Calculation
        stream.write(textLine(formatRow("Subtotal:", PriceFormatter.formatNpr(order.subtotal), width)))
        if (order.discount > 0) {
            stream.write(textLine(formatRow("Discount (${order.couponCode ?: "Promo"}):", PriceFormatter.formatDiscount(order.discount), width)))
        }
        if (order.tax > 0) {
            stream.write(textLine(formatRow("Tax:", PriceFormatter.formatNpr(order.tax), width)))
        }
        if (order.deliveryFee > 0) {
            stream.write(textLine(formatRow("Delivery Fee:", PriceFormatter.formatNpr(order.deliveryFee), width)))
        }

        stream.write(textLine(dividerLine))
        stream.write(setBold(true))
        stream.write(setTextSize(1, 2))
        stream.write(textLine(formatRow("TOTAL:", PriceFormatter.formatNpr(order.grandTotal), width)))
        stream.write(setTextSize(1, 1))
        stream.write(textLine(dividerLine))

        // Payment Details
        stream.write(setBold(false))
        stream.write(textLine(formatRow("Payment Mode:", order.paymentMethod.label, width)))
        stream.write(textLine(formatRow("Payment Status:", order.paymentStatus.label, width)))
        stream.write(textLine(dashLine))

        // Footer
        stream.write(setAlign(Align.CENTER))
        stream.write(textLine("Thank You! Visit Again! 🧇"))
        stream.write(textLine("Home Delivery: +977-9706612914"))
        stream.write(feedLines(3))
        stream.write(cutPaper())

        return stream.toByteArray()
    }

    /**
     * Generates complete binary ESC/POS byte stream for a Kitchen Order Ticket (KOT).
     */
    fun buildKitchenKotBytes(kot: KOT, settings: PrinterSettings): ByteArray {
        val stream = ByteArrayOutputStream()
        val width = settings.paperWidth.charsPerLine
        val dividerLine = "=".repeat(width)
        val dashLine = "-".repeat(width)

        stream.write(initPrinter())
        stream.write(buzzerBeep())

        // KOT Header
        stream.write(setAlign(Align.CENTER))
        stream.write(setBold(true))
        stream.write(setTextSize(2, 2))
        stream.write(textLine("KITCHEN TICKET"))
        stream.write(setTextSize(1, 1))
        stream.write(textLine("*** THE JANAKPUR WAFFLE & CAFE ***"))
        stream.write(textLine(dividerLine))

        // KOT Info
        stream.write(setAlign(Align.LEFT))
        stream.write(setBold(true))
        stream.write(textLine(formatRow("KOT: ${kot.kotNumber}", "Order: ${kot.humanOrderNumber}", width)))
        stream.write(textLine(formatRow("Type: ${kot.orderType.label}", "Table: ${kot.tableNumber ?: "TAKEAWAY"}", width)))
        stream.write(setBold(false))
        stream.write(textLine("Time: ${dateFormatter.format(Date(kot.time))}"))
        stream.write(textLine(dashLine))

        // Items List
        stream.write(setBold(true))
        stream.write(textLine(String.format("%-4s %-20s %4s", "#", "Item Name", "Qty")))
        stream.write(setBold(false))
        stream.write(textLine(dashLine))

        kot.items.forEachIndexed { idx, item ->
            val itemName = if (item.variantName != null) "${item.productName} [${item.variantName}]" else item.productName
            stream.write(setBold(true))
            stream.write(textLine(String.format("%-4d %-20s %4d", idx + 1, itemName.take(20), item.quantity)))
            stream.write(setBold(false))

            if (item.addonNames.isNotEmpty()) {
                stream.write(textLine("     + Extra: ${item.addonNames.joinToString(", ")}"))
            }
            if (item.notes.isNotBlank()) {
                stream.write(setBold(true))
                stream.write(textLine("     * NOTE: ${item.notes}"))
                stream.write(setBold(false))
            }
        }
        stream.write(textLine(dashLine))

        if (kot.notes.isNotBlank()) {
            stream.write(setBold(true))
            stream.write(textLine("KITCHEN INSTRUCTION:"))
            stream.write(textLine(kot.notes))
            stream.write(setBold(false))
            stream.write(textLine(dashLine))
        }

        stream.write(setAlign(Align.CENTER))
        stream.write(textLine("--- END OF KOT ---"))
        stream.write(feedLines(3))
        stream.write(cutPaper())

        return stream.toByteArray()
    }

    private fun formatRow(left: String, right: String, width: Int): String {
        val totalSpace = width - left.length - right.length
        if (totalSpace <= 0) return "$left $right"
        return left + " ".repeat(totalSpace) + right
    }
}
