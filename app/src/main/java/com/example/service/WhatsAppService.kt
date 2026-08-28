package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.Order
import java.net.URLEncoder

object WhatsAppService {

    const val CAFE_WHATSAPP_NUMBER = "+9779706612914" // +977-9706612914

    fun generateWhatsAppOrderMessage(order: Order): String {
        val sb = StringBuilder()
        sb.append("🧇 *THE JANAKPUR WAFFLE & CAFE (TJW Cafe)*\n")
        sb.append("🌱 *100% Pure Veg & Eggless*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📋 *Order Number:* ${order.humanOrderNumber}\n")
        sb.append("🛵 *Order Type:* ${order.orderType.label}\n")
        if (order.tableNumber != null) {
            sb.append("🪑 *Table:* ${order.tableNumber}\n")
        }
        sb.append("👤 *Customer:* ${order.customerName} (${order.customerPhone.ifBlank { "N/A" }})\n")
        if (!order.deliveryAddress.isNullOrBlank()) {
            sb.append("📍 *Delivery Address:* ${order.deliveryAddress}\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("🛍️ *ITEMS:*\n")

        order.items.forEachIndexed { i, item ->
            val variant = if (item.variantName != null) " (${item.variantName})" else ""
            sb.append("${i + 1}. *${item.productName}$variant* x ${item.quantity} = NPR ${item.totalPrice.toInt()}\n")
            if (item.addonNames.isNotEmpty()) {
                sb.append("   └ _+ ${item.addonNames.joinToString(", ")}_\n")
            }
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💵 *Subtotal:* NPR ${order.subtotal.toInt()}\n")
        if (order.discount > 0) {
            sb.append("🏷️ *Discount (${order.couponCode ?: "Offer"}):* -NPR ${order.discount.toInt()}\n")
        }
        if (order.deliveryFee > 0) {
            sb.append("🛵 *Delivery Fee:* NPR ${order.deliveryFee.toInt()}\n")
        }
        sb.append("💰 *GRAND TOTAL:* *NPR ${order.grandTotal.toInt()}*\n")
        sb.append("💳 *Payment:* ${order.paymentMethod.label} (${order.paymentStatus.label})\n")
        if (order.specialInstructions.isNotBlank()) {
            sb.append("📝 *Note:* ${order.specialInstructions}\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("✨ _Thank you for ordering with TJW Cafe!_")
        return sb.toString()
    }

    fun launchWhatsApp(context: Context, order: Order) {
        try {
            val message = generateWhatsAppOrderMessage(order)
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val cleanPhone = CAFE_WHATSAPP_NUMBER.replace("+", "").replace("-", "").trim()
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
