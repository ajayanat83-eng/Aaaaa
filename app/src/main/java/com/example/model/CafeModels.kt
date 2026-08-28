package com.example.model

import kotlinx.serialization.Serializable
import java.util.UUID

// ==================== ENUMS ====================

enum class OrderType(val label: String) {
    DINE_IN("Dine-In"),
    TAKEAWAY("Takeaway"),
    DELIVERY("Delivery")
}

enum class OrderStatus(val label: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    ACCEPTED("Accepted"),
    PREPARING("Preparing"),
    READY("Ready"),
    SERVED("Served"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded")
}

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    ESEWA("eSewa"),
    KHALTI("Khalti"),
    BANK("Bank Transfer / QR"),
    COD("Cash on Delivery")
}

enum class PaymentStatus(val label: String) {
    PENDING("Pending"),
    SUCCESS("Success"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded")
}

enum class BankVerificationStatus(val label: String) {
    PENDING("Pending Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

enum class TableStatus(val label: String) {
    AVAILABLE("Available"),
    OCCUPIED("Occupied"),
    ORDERED("Ordered"),
    KOT_SENT("KOT Sent"),
    PREPARING("Preparing"),
    READY("Ready"),
    BILLING("Billing"),
    PAID("Paid")
}

enum class KitchenStatus(val label: String) {
    NEW("New KOT"),
    ACCEPTED("Accepted"),
    PREPARING("Preparing"),
    READY("Ready"),
    SERVED("Served"),
    CANCELLED("Cancelled")
}

enum class DiscountType {
    PERCENTAGE,
    FIXED
}

enum class StaffRole(val label: String) {
    OWNER("Owner (Full Access)"),
    ADMIN("Admin"),
    MANAGER("Manager"),
    CASHIER("Cashier (POS & Billing)"),
    KITCHEN("Kitchen (KDS & Prep)"),
    WAITER("Waiter (Tables & Orders)")
}

enum class PaperWidth(val label: String, val charsPerLine: Int) {
    WIDTH_58MM("58 mm (32 chars)", 32),
    WIDTH_80MM("80 mm (48 chars)", 48)
}

enum class DeliveryStatus(val label: String) {
    NEW("New Order"),
    ACCEPTED("Accepted"),
    ASSIGNED("Driver Assigned"),
    PICKED_UP("Picked Up"),
    ON_THE_WAY("On The Way"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled")
}

// ==================== CORE MODELS ====================

@Serializable
data class ProductVariant(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", // e.g. "Classic", "Strong", "Single Layer", "Double Layer"
    val price: Double = 0.0
)

@Serializable
data class ProductAddon(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", // e.g. "Vanilla Ice Cream Scoop", "Extra Nutella"
    val price: Double = 0.0
)

@Serializable
data class Category(
    val id: String = "",
    val name: String = "",
    val iconEmoji: String = "🧇",
    val description: String = "",
    val image: String = "", // Image URL / Asset path for category
    val sortOrder: Int = 0,
    val isAvailable: Boolean = true
)

@Serializable
data class Product(
    val productId: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val categoryId: String = "",
    val price: Double = 0.0, // Base price in NPR
    val image: String = "", // Image URL / Asset path for product image
    val imageEmoji: String = "🧇", // Stylized visual emoji placeholder/asset
    val isAvailable: Boolean = true,
    val isFeatured: Boolean = false,
    val isBestSeller: Boolean = false,
    val isPureVeg: Boolean = true,
    val badge: String? = null, // "Must Try", "Chef Special", "Loaded", "Popular", "Hot", "Top Seller", "Trending"
    val variants: List<ProductVariant> = emptyList(),
    val addons: List<ProductAddon> = emptyList(),
    val preparationTime: Int = 10, // In minutes
    val tax: Double = 0.0, // Tax % if applicable
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class CartItem(
    val cartItemId: String = UUID.randomUUID().toString(),
    val product: Product,
    val selectedVariant: ProductVariant? = null,
    val selectedAddons: List<ProductAddon> = emptyList(),
    val quantity: Int = 1,
    val itemNotes: String = ""
) {
    val unitPrice: Double
        get() {
            val variantPrice = selectedVariant?.price ?: product.price
            val addonTotal = selectedAddons.sumOf { it.price }
            return variantPrice + addonTotal
        }

    val totalPrice: Double
        get() = unitPrice * quantity
}

data class OrderItem(
    val productId: String,
    val productName: String,
    val variantName: String? = null,
    val addonNames: List<String> = emptyList(),
    val unitPrice: Double,
    val quantity: Int,
    val totalPrice: Double,
    val notes: String = ""
)

data class PaymentRecord(
    val paymentId: String = UUID.randomUUID().toString(),
    val orderId: String,
    val method: PaymentMethod,
    val amount: Double,
    val transactionId: String = "",
    val status: PaymentStatus = PaymentStatus.SUCCESS,
    val reference: String = "",
    val screenshotNote: String = "",
    val bankVerificationStatus: BankVerificationStatus = BankVerificationStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val verifiedAt: Long? = null
)

data class Order(
    val orderId: String = UUID.randomUUID().toString(),
    val humanOrderNumber: String, // e.g. "TJW-0001"
    val customerId: String = "guest",
    val customerName: String = "Customer",
    val customerPhone: String = "",
    val branchId: String = "branch_janakpur_main",
    val orderType: OrderType = OrderType.DINE_IN,
    val tableId: String? = null,
    val tableNumber: String? = null, // e.g. "TJW-TABLE-01"
    val deliveryAddress: String? = null,
    val items: List<OrderItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val couponCode: String? = null,
    val tax: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val orderStatus: OrderStatus = OrderStatus.PENDING,
    val payments: List<PaymentRecord> = emptyList(),
    val specialInstructions: String = "",
    val isOfflineQueued: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Table(
    val tableId: String,
    val tableNumber: String, // e.g. "TJW-TABLE-01"
    val branchId: String = "branch_janakpur_main",
    val capacity: Int = 4,
    val status: TableStatus = TableStatus.AVAILABLE,
    val activeOrderId: String? = null,
    val activeOrderNumber: String? = null,
    val activeOrderTotal: Double = 0.0,
    val occupiedSince: Long? = null
)

data class KOT(
    val kotNumber: String, // e.g. "KOT-001"
    val orderId: String,
    val humanOrderNumber: String, // "TJW-0001"
    val orderType: OrderType,
    val tableNumber: String? = null,
    val items: List<OrderItem>,
    val notes: String = "",
    val time: Long = System.currentTimeMillis(),
    val status: KitchenStatus = KitchenStatus.NEW,
    val prepTimeMinutes: Int = 12
)

data class Coupon(
    val code: String,
    val title: String,
    val description: String,
    val discountType: DiscountType,
    val discountValue: Double, // Percentage (e.g. 15 for 15%) or Fixed NPR
    val minOrderAmount: Double = 0.0,
    val maxDiscount: Double = Double.MAX_VALUE,
    val validFrom: Long = 0L,
    val validTo: Long = Long.MAX_VALUE,
    val usageLimit: Int = 1000,
    val usedCount: Int = 0,
    val isActive: Boolean = true
)

data class CustomerProfile(
    val customerId: String = "cust_001",
    val name: String = "Sanjay Kumar",
    val phone: String = "+977-9800000000",
    val email: String = "sanjay@tjwcafe.com",
    val addresses: List<String> = listOf("Station Road, Janakpurdham, Nepal", "Ramanand Chowk, Janakpur"),
    val loyaltyPoints: Int = 45, // 1 TJW pt per NPR 100
    val savedPreferences: String = "Extra chocolate, crispy waffles"
)

data class LoyaltyTransaction(
    val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val orderId: String,
    val pointsChange: Int, // +ve for earned, -ve for redeemed
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class StaffUser(
    val staffId: String = UUID.randomUUID().toString(),
    val name: String,
    val role: StaffRole,
    val phone: String,
    val pin: String = "1234",
    val branchId: String = "branch_janakpur_main",
    val isActive: Boolean = true
)

data class PrinterSettings(
    val printerId: String = "PRN-BT-01",
    val printerName: String = "TJW Bluetooth Thermal Printer 58mm",
    val macAddress: String = "66:55:44:33:22:11",
    val paperWidth: PaperWidth = PaperWidth.WIDTH_58MM,
    val autoPrint: Boolean = true,
    val kotPrint: Boolean = true,
    val billPrint: Boolean = true,
    val copies: Int = 1,
    val isConnected: Boolean = true
)

data class DeliveryProviderConfig(
    val providerId: String, // delivery_provider_1, delivery_provider_2, delivery_provider_3
    val name: String,
    val apiKey: String = "",
    val apiSecret: String = "",
    val storeId: String = "TJW_JANAKPUR_01",
    val baseUrl: String = "https://api.deliveryprovider.np/v1",
    val isEnabled: Boolean = false
)

data class AuditLog(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "Admin",
    val userName: String = "Manager",
    val action: String,
    val orderId: String? = null,
    val oldValue: String? = null,
    val newValue: String? = null,
    val branchId: String = "branch_janakpur_main",
    val timestamp: Long = System.currentTimeMillis()
)

data class Branch(
    val branchId: String,
    val name: String,
    val address: String,
    val phone: String,
    val isMain: Boolean = false
)

data class CafeSettings(
    val cafeName: String = "The Janakpur Waffle & Cafe",
    val shortName: String = "TJW Cafe",
    val brandStatement: String = "100% Pure Veg & Eggless",
    val logoEmoji: String = "🧇",
    val phone: String = "+977-9706612914",
    val whatsapp: String = "+9779706612914",
    val address: String = "Station Road, Janakpurdham, Nepal",
    val openingHours: String = "08:00 AM",
    val closingHours: String = "10:00 PM",
    val holiday: String = "Open All 7 Days",
    val currency: String = "NPR",
    val taxRate: Double = 0.0, // 0% or standard service tax
    val deliveryFee: Double = 50.0,
    val minOrderAmount: Double = 100.0,
    val pointsPerAmount: Int = 100, // 1 pt per 100 NPR
    val minRedeemPoints: Int = 20,
    val maxRedeemPercentage: Int = 50, // max 50% discount using points
    val bankName: String = "Nabil Bank / Nepal SBI Bank",
    val accountName: String = "The Janakpur Waffle & Cafe",
    val accountNumber: String = "0192837465019283",
    val bankQrNote: String = "Scan with any Fonepay / ConnectIPS / Mobile Banking App",
    val activeBranchId: String = "branch_janakpur_main",
    val branches: List<Branch> = listOf(
        Branch("branch_janakpur_main", "TJW Main Branch - Station Road", "Station Road, Janakpurdham", "+977-9706612914", true),
        Branch("branch_janakpur_chowk", "TJW Express - Ramanand Chowk", "Ramanand Chowk, Janakpurdham", "+977-9706612915", false)
    )
)
