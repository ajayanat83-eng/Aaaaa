package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Utility for standardizing price and currency formatting across the entire app.
 * Provides consistent 'NPR 0.00' formatted strings for all menus, carts, bills, and receipts.
 */
object PriceFormatter {

    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    private val standardDecimalFormat = DecimalFormat("###,##0.00", symbols)
    private val roundedFormat = DecimalFormat("###,##0", symbols)

    /**
     * Formats any numeric amount to the standard 'NPR 0.00' format.
     * Example: 130.0 -> "NPR 130.00", 1250.5 -> "NPR 1,250.50"
     */
    fun formatNpr(amount: Double): String {
        return "NPR ${standardDecimalFormat.format(amount)}"
    }

    /**
     * Formats integer/long amounts to standard 'NPR 0.00' format.
     */
    fun formatNpr(amount: Int): String = formatNpr(amount.toDouble())
    fun formatNpr(amount: Long): String = formatNpr(amount.toDouble())

    /**
     * Formats amount with explicit sign (for discounts, surcharges).
     * Example: 50.0 -> "+NPR 50.00", -48.0 -> "-NPR 48.00"
     */
    fun formatNprWithSign(amount: Double): String {
        return if (amount >= 0) {
            "+NPR ${standardDecimalFormat.format(amount)}"
        } else {
            "-NPR ${standardDecimalFormat.format(-amount)}"
        }
    }

    /**
     * Formats amount to rounded whole number format without decimals.
     * Example: 130.0 -> "NPR 130", 1250.0 -> "NPR 1,250"
     */
    fun formatNprRounded(amount: Double): String {
        return "NPR ${roundedFormat.format(amount)}"
    }

    /**
     * Formats discount display.
     * Example: 48.0 -> "-NPR 48.00"
     */
    fun formatDiscount(amount: Double): String {
        if (amount <= 0) return "NPR 0.00"
        return "-NPR ${standardDecimalFormat.format(amount)}"
    }

    /**
     * Safely parses user input string to double NPR amount.
     */
    fun parseNpr(input: String): Double {
        val clean = input.replace("NPR", "", ignoreCase = true)
            .replace(",", "")
            .trim()
        return clean.toDoubleOrNull() ?: 0.0
    }
}
