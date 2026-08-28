package com.example.data

import com.example.model.*
import com.example.service.PrinterService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CafeRepository private constructor() {

    private val scope = CoroutineScope(Dispatchers.Default)

    // Reactive State Flows
    private val _products = MutableStateFlow<List<Product>>(DefaultMenuData.getInitialProducts())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(DefaultMenuData.categories)
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _tables = MutableStateFlow<List<Table>>(DefaultMenuData.defaultTables)
    val tables: StateFlow<List<Table>> = _tables.asStateFlow()

    private val _kots = MutableStateFlow<List<KOT>>(emptyList())
    val kots: StateFlow<List<KOT>> = _kots.asStateFlow()

    private val _coupons = MutableStateFlow<List<Coupon>>(DefaultMenuData.defaultCoupons)
    val coupons: StateFlow<List<Coupon>> = _coupons.asStateFlow()

    private val _customerProfile = MutableStateFlow(CustomerProfile())
    val customerProfile: StateFlow<CustomerProfile> = _customerProfile.asStateFlow()

    private val _loyaltyTransactions = MutableStateFlow<List<LoyaltyTransaction>>(emptyList())
    val loyaltyTransactions: StateFlow<List<LoyaltyTransaction>> = _loyaltyTransactions.asStateFlow()

    private val _staffList = MutableStateFlow<List<StaffUser>>(DefaultMenuData.defaultStaff)
    val staffList: StateFlow<List<StaffUser>> = _staffList.asStateFlow()

    private val _activeStaffRole = MutableStateFlow(StaffRole.OWNER)
    val activeStaffRole: StateFlow<StaffRole> = _activeStaffRole.asStateFlow()

    private val _printerSettings = MutableStateFlow(PrinterSettings())
    val printerSettings: StateFlow<PrinterSettings> = _printerSettings.asStateFlow()

    private val _deliveryProviders = MutableStateFlow<List<DeliveryProviderConfig>>(DefaultMenuData.defaultDeliveryProviders)
    val deliveryProviders: StateFlow<List<DeliveryProviderConfig>> = _deliveryProviders.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    private val _cafeSettings = MutableStateFlow(CafeSettings())
    val cafeSettings: StateFlow<CafeSettings> = _cafeSettings.asStateFlow()

    private val _printLogs = MutableStateFlow<List<String>>(emptyList())
    val printLogs: StateFlow<List<String>> = _printLogs.asStateFlow()

    private var orderCounter = 1
    private var kotCounter = 1

    init {
        seedInitialOrders()
    }

    private fun seedInitialOrders() {
        // Seed a sample confirmed dine-in and takeaway order for realistic initial display
        val initialOrder1 = Order(
            orderId = "ord_seed_1",
            humanOrderNumber = "TJW-0001",
            customerName = "Rohan Sharma",
            customerPhone = "+977-9812345678",
            orderType = OrderType.DINE_IN,
            tableId = "tbl_02",
            tableNumber = "TJW-TABLE-02",
            items = listOf(
                OrderItem("prod_wf_triple_choco", "Triple Chocolate Waffle", null, listOf("Extra Nutella"), 160.0, 1, 160.0),
                OrderItem("prod_dr_cold_coffee", "Cold Coffee", "Strong", emptyList(), 90.0, 1, 90.0)
            ),
            subtotal = 250.0,
            discount = 0.0,
            grandTotal = 250.0,
            paymentMethod = PaymentMethod.CASH,
            paymentStatus = PaymentStatus.PENDING,
            orderStatus = OrderStatus.PREPARING,
            createdAt = System.currentTimeMillis() - 15 * 60 * 1000
        )

        val initialOrder2 = Order(
            orderId = "ord_seed_2",
            humanOrderNumber = "TJW-0002",
            customerName = "Anita Thapa",
            customerPhone = "+977-9845678901",
            orderType = OrderType.TAKEAWAY,
            items = listOf(
                OrderItem("prod_momo_royal_pizza", "Royal Pizza Momos (6 Pcs)", null, emptyList(), 140.0, 2, 280.0),
                OrderItem("prod_dr_blue_lagoon", "Blue Lagoon Mojito", null, emptyList(), 100.0, 2, 200.0)
            ),
            subtotal = 480.0,
            discount = 48.0,
            couponCode = "TJW10",
            grandTotal = 432.0,
            paymentMethod = PaymentMethod.ESEWA,
            paymentStatus = PaymentStatus.SUCCESS,
            orderStatus = OrderStatus.READY,
            createdAt = System.currentTimeMillis() - 25 * 60 * 1000
        )

        _orders.value = listOf(initialOrder1, initialOrder2)
        orderCounter = 3

        val initialKot1 = KOT(
            kotNumber = "KOT-001",
            orderId = initialOrder1.orderId,
            humanOrderNumber = initialOrder1.humanOrderNumber,
            orderType = initialOrder1.orderType,
            tableNumber = initialOrder1.tableNumber,
            items = initialOrder1.items,
            time = initialOrder1.createdAt,
            status = KitchenStatus.PREPARING
        )
        val initialKot2 = KOT(
            kotNumber = "KOT-002",
            orderId = initialOrder2.orderId,
            humanOrderNumber = initialOrder2.humanOrderNumber,
            orderType = initialOrder2.orderType,
            tableNumber = initialOrder2.tableNumber,
            items = initialOrder2.items,
            time = initialOrder2.createdAt,
            status = KitchenStatus.READY
        )
        _kots.value = listOf(initialKot1, initialKot2)
        kotCounter = 3

        // Mark Table 02 as PREPARING
        _tables.value = _tables.value.map {
            if (it.tableId == "tbl_02") {
                it.copy(
                    status = TableStatus.PREPARING,
                    activeOrderId = initialOrder1.orderId,
                    activeOrderNumber = initialOrder1.humanOrderNumber,
                    activeOrderTotal = initialOrder1.grandTotal,
                    occupiedSince = initialOrder1.createdAt
                )
            } else it
        }

        logAudit("System", "Initial Menu, Tables, and Seed Orders initialized.")
    }

    // ==================== CART OPERATIONS ====================

    fun addToCart(product: Product, variant: ProductVariant? = null, addons: List<ProductAddon> = emptyList(), notes: String = "") {
        val current = _cartItems.value.toMutableList()
        val existingIndex = current.indexOfFirst {
            it.product.productId == product.productId &&
                    it.selectedVariant?.id == variant?.id &&
                    it.selectedAddons.map { a -> a.id }.sorted() == addons.map { a -> a.id }.sorted()
        }

        if (existingIndex >= 0) {
            val item = current[existingIndex]
            current[existingIndex] = item.copy(quantity = item.quantity + 1)
        } else {
            current.add(
                CartItem(
                    product = product,
                    selectedVariant = variant,
                    selectedAddons = addons,
                    quantity = 1,
                    itemNotes = notes
                )
            )
        }
        _cartItems.value = current
    }

    fun updateCartQuantity(cartItemId: String, delta: Int) {
        val current = _cartItems.value.toMutableList()
        val index = current.indexOfFirst { it.cartItemId == cartItemId }
        if (index >= 0) {
            val item = current[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else {
                current[index] = item.copy(quantity = newQty)
            }
            _cartItems.value = current
        }
    }

    fun removeCartItem(cartItemId: String) {
        _cartItems.value = _cartItems.value.filterNot { it.cartItemId == cartItemId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // ==================== ORDER CREATION & LIFECYCLE ====================

    fun createOrder(
        orderType: OrderType,
        customerName: String,
        customerPhone: String,
        tableNumber: String? = null,
        deliveryAddress: String? = null,
        couponCode: String? = null,
        discountAmount: Double = 0.0,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        paymentStatus: PaymentStatus = PaymentStatus.PENDING,
        paymentRecords: List<PaymentRecord> = emptyList(),
        specialInstructions: String = "",
        redeemedPoints: Int = 0,
        customItems: List<CartItem>? = null
    ): Order {
        val itemsSource = customItems ?: _cartItems.value
        val orderItems = itemsSource.map { cartItem ->
            OrderItem(
                productId = cartItem.product.productId,
                productName = cartItem.product.name,
                variantName = cartItem.selectedVariant?.name,
                addonNames = cartItem.selectedAddons.map { it.name },
                unitPrice = cartItem.unitPrice,
                quantity = cartItem.quantity,
                totalPrice = cartItem.totalPrice,
                notes = cartItem.itemNotes
            )
        }

        val subtotal = orderItems.sumOf { it.totalPrice }
        val deliveryFee = if (orderType == OrderType.DELIVERY) _cafeSettings.value.deliveryFee else 0.0
        val tax = 0.0
        val grandTotal = (subtotal - discountAmount + deliveryFee + tax).coerceAtLeast(0.0)

        val humanNum = "TJW-${String.format("%04d", orderCounter++)}"
        val tableId = if (tableNumber != null) _tables.value.find { it.tableNumber == tableNumber }?.tableId else null

        val order = Order(
            orderId = UUID.randomUUID().toString(),
            humanOrderNumber = humanNum,
            customerName = customerName.ifBlank { "Guest Customer" },
            customerPhone = customerPhone,
            orderType = orderType,
            tableId = tableId,
            tableNumber = tableNumber,
            deliveryAddress = deliveryAddress,
            items = orderItems,
            subtotal = subtotal,
            discount = discountAmount,
            couponCode = couponCode,
            tax = tax,
            deliveryFee = deliveryFee,
            grandTotal = grandTotal,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            orderStatus = OrderStatus.CONFIRMED,
            payments = paymentRecords.ifEmpty {
                listOf(
                    PaymentRecord(
                        orderId = humanNum,
                        method = paymentMethod,
                        amount = grandTotal,
                        status = paymentStatus
                    )
                )
            },
            specialInstructions = specialInstructions
        )

        // Add to orders list
        _orders.value = listOf(order) + _orders.value

        // Automatically Generate KOT for Kitchen
        val kotNum = "KOT-${String.format("%03d", kotCounter++)}"
        val kot = KOT(
            kotNumber = kotNum,
            orderId = order.orderId,
            humanOrderNumber = order.humanOrderNumber,
            orderType = order.orderType,
            tableNumber = order.tableNumber,
            items = order.items,
            notes = order.specialInstructions,
            status = KitchenStatus.NEW
        )
        _kots.value = listOf(kot) + _kots.value

        // Update Table status if Dine-In
        if (orderType == OrderType.DINE_IN && tableId != null) {
            updateTableStatus(tableId, TableStatus.ORDERED, order.orderId, order.humanOrderNumber, order.grandTotal)
        }

        // Award/Deduct Loyalty Points
        if (redeemedPoints > 0) {
            deductLoyaltyPoints(redeemedPoints, "Redeemed on Order $humanNum")
        }
        if (paymentStatus == PaymentStatus.SUCCESS) {
            awardLoyaltyPointsForOrder(grandTotal, humanNum)
        }

        // Auto-print if enabled
        if (_printerSettings.value.autoPrint) {
            if (_printerSettings.value.kotPrint) {
                printKot(kot)
            }
            if (_printerSettings.value.billPrint) {
                printBill(order)
            }
        }

        if (customItems == null) {
            clearCart()
        }

        logAudit("Order", "Created new order ${order.humanOrderNumber} (${order.orderType.label}) for NPR ${order.grandTotal.toInt()}")

        return order
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        val currentOrders = _orders.value.toMutableList()
        val idx = currentOrders.indexOfFirst { it.orderId == orderId }
        if (idx >= 0) {
            val oldOrder = currentOrders[idx]
            currentOrders[idx] = oldOrder.copy(orderStatus = newStatus, updatedAt = System.currentTimeMillis())
            _orders.value = currentOrders

            // Update associated Table
            if (oldOrder.tableId != null) {
                val tableState = when (newStatus) {
                    OrderStatus.PREPARING -> TableStatus.PREPARING
                    OrderStatus.READY -> TableStatus.READY
                    OrderStatus.SERVED -> TableStatus.OCCUPIED
                    OrderStatus.COMPLETED -> TableStatus.AVAILABLE
                    OrderStatus.CANCELLED -> TableStatus.AVAILABLE
                    else -> null
                }
                if (tableState != null) {
                    if (tableState == TableStatus.AVAILABLE) {
                        clearTable(oldOrder.tableId)
                    } else {
                        updateTableStatus(oldOrder.tableId, tableState)
                    }
                }
            }

            // If marked completed and paid, award loyalty
            if (newStatus == OrderStatus.COMPLETED && oldOrder.paymentStatus == PaymentStatus.SUCCESS) {
                awardLoyaltyPointsForOrder(oldOrder.grandTotal, oldOrder.humanOrderNumber)
            }

            logAudit("Order", "Updated Order ${oldOrder.humanOrderNumber} status: ${oldOrder.orderStatus.label} -> ${newStatus.label}")
        }
    }

    fun updatePaymentStatus(orderId: String, paymentStatus: PaymentStatus, method: PaymentMethod? = null) {
        val currentOrders = _orders.value.toMutableList()
        val idx = currentOrders.indexOfFirst { it.orderId == orderId }
        if (idx >= 0) {
            val old = currentOrders[idx]
            currentOrders[idx] = old.copy(
                paymentStatus = paymentStatus,
                paymentMethod = method ?: old.paymentMethod,
                updatedAt = System.currentTimeMillis()
            )
            _orders.value = currentOrders

            if (paymentStatus == PaymentStatus.SUCCESS && old.tableId != null) {
                updateTableStatus(old.tableId, TableStatus.PAID)
            }
            logAudit("Payment", "Order ${old.humanOrderNumber} payment updated to ${paymentStatus.label}")
        }
    }

    // ==================== KOT & KITCHEN OPERATIONS ====================

    fun updateKotStatus(kotNumber: String, status: KitchenStatus) {
        val current = _kots.value.toMutableList()
        val idx = current.indexOfFirst { it.kotNumber == kotNumber }
        if (idx >= 0) {
            val old = current[idx]
            current[idx] = old.copy(status = status)
            _kots.value = current

            // Sync corresponding order status
            val matchingOrder = _orders.value.find { it.orderId == old.orderId }
            if (matchingOrder != null) {
                val newOrderStatus = when (status) {
                    KitchenStatus.PREPARING -> OrderStatus.PREPARING
                    KitchenStatus.READY -> OrderStatus.READY
                    KitchenStatus.SERVED -> OrderStatus.SERVED
                    KitchenStatus.CANCELLED -> OrderStatus.CANCELLED
                    else -> null
                }
                if (newOrderStatus != null) {
                    updateOrderStatus(matchingOrder.orderId, newOrderStatus)
                }
            }
            logAudit("Kitchen", "KOT $kotNumber status set to ${status.label}")
        }
    }

    // ==================== TABLE MANAGEMENT ====================

    fun updateTableStatus(
        tableId: String,
        status: TableStatus,
        orderId: String? = null,
        orderNumber: String? = null,
        orderTotal: Double = 0.0
    ) {
        _tables.value = _tables.value.map {
            if (it.tableId == tableId) {
                it.copy(
                    status = status,
                    activeOrderId = orderId ?: it.activeOrderId,
                    activeOrderNumber = orderNumber ?: it.activeOrderNumber,
                    activeOrderTotal = if (orderTotal > 0) orderTotal else it.activeOrderTotal,
                    occupiedSince = if (it.occupiedSince == null && status != TableStatus.AVAILABLE) System.currentTimeMillis() else it.occupiedSince
                )
            } else it
        }
    }

    fun clearTable(tableId: String) {
        _tables.value = _tables.value.map {
            if (it.tableId == tableId) {
                it.copy(
                    status = TableStatus.AVAILABLE,
                    activeOrderId = null,
                    activeOrderNumber = null,
                    activeOrderTotal = 0.0,
                    occupiedSince = null
                )
            } else it
        }
        logAudit("Table", "Table $tableId cleared and set to AVAILABLE")
    }

    fun transferTable(fromTableId: String, toTableId: String) {
        val fromTable = _tables.value.find { it.tableId == fromTableId } ?: return
        val toTable = _tables.value.find { it.tableId == toTableId } ?: return

        _tables.value = _tables.value.map {
            when (it.tableId) {
                fromTableId -> it.copy(status = TableStatus.AVAILABLE, activeOrderId = null, activeOrderNumber = null, activeOrderTotal = 0.0, occupiedSince = null)
                toTableId -> it.copy(
                    status = fromTable.status,
                    activeOrderId = fromTable.activeOrderId,
                    activeOrderNumber = fromTable.activeOrderNumber,
                    activeOrderTotal = fromTable.activeOrderTotal,
                    occupiedSince = fromTable.occupiedSince ?: System.currentTimeMillis()
                )
                else -> it
            }
        }
        logAudit("Table", "Transferred order from ${fromTable.tableNumber} to ${toTable.tableNumber}")
    }

    fun mergeTables(primaryTableId: String, secondaryTableId: String) {
        val pTable = _tables.value.find { it.tableId == primaryTableId } ?: return
        val sTable = _tables.value.find { it.tableId == secondaryTableId } ?: return

        val combinedTotal = pTable.activeOrderTotal + sTable.activeOrderTotal
        _tables.value = _tables.value.map {
            when (it.tableId) {
                secondaryTableId -> it.copy(status = TableStatus.OCCUPIED, activeOrderNumber = "Merged with ${pTable.tableNumber}")
                primaryTableId -> it.copy(activeOrderTotal = combinedTotal)
                else -> it
            }
        }
        logAudit("Table", "Merged ${sTable.tableNumber} into ${pTable.tableNumber}")
    }

    fun addTable(table: Table) {
        val current = _tables.value.toMutableList()
        current.add(table)
        _tables.value = current
        logAudit("Table", "Registered new table ${table.tableNumber} (Capacity ${table.capacity}) with QR Code")
    }

    fun addTable(tableNumber: String, capacity: Int) {
        val newTable = Table(
            tableId = "tbl_${UUID.randomUUID().toString().take(6)}",
            tableNumber = tableNumber,
            capacity = capacity,
            status = TableStatus.AVAILABLE
        )
        _tables.value = _tables.value + newTable
        logAudit("Table", "Added new table $tableNumber")
    }

    fun deleteTable(tableId: String) {
        _tables.value = _tables.value.filterNot { it.tableId == tableId }
        logAudit("Table", "Deleted table $tableId")
    }

    fun setCustomerProfile(profile: CustomerProfile) {
        _customerProfile.value = profile
    }

    fun resetCustomerProfile() {
        _customerProfile.value = CustomerProfile()
    }

    // ==================== PRODUCT & MENU CRUD ====================

    fun addProduct(product: Product) {
        _products.value = _products.value + product
        scope.launch {
            FirestoreMenuRepository.instance.saveProduct(product)
        }
        logAudit("Menu", "Added product: ${product.name} (NPR ${product.price})")
    }

    fun updateProduct(product: Product) {
        val current = _products.value.toMutableList()
        val idx = current.indexOfFirst { it.productId == product.productId }
        if (idx >= 0) {
            val old = current[idx]
            val updated = product.copy(updatedAt = System.currentTimeMillis())
            current[idx] = updated
            _products.value = current
            scope.launch {
                FirestoreMenuRepository.instance.saveProduct(updated)
            }
            logAudit("Menu", "Updated product ${product.name} (Price: NPR ${old.price} -> NPR ${product.price})")
        }
    }

    fun toggleProductAvailability(productId: String) {
        _products.value = _products.value.map {
            if (it.productId == productId) {
                val updated = it.copy(isAvailable = !it.isAvailable)
                scope.launch {
                    FirestoreMenuRepository.instance.saveProduct(updated)
                }
                logAudit("Menu", "Toggled ${it.name} availability: ${updated.isAvailable}")
                updated
            } else it
        }
    }

    fun deleteProduct(productId: String) {
        val prod = _products.value.find { it.productId == productId }
        _products.value = _products.value.filterNot { it.productId == productId }
        scope.launch {
            FirestoreMenuRepository.instance.deleteProduct(productId)
        }
        logAudit("Menu", "Deleted product ${prod?.name ?: productId}")
    }

    // ==================== LOYALTY SYSTEM ====================

    private fun awardLoyaltyPointsForOrder(amount: Double, orderNumber: String) {
        val pointsPerNpr = _cafeSettings.value.pointsPerAmount // e.g. 100
        val earned = (amount / pointsPerNpr).toInt()
        if (earned > 0) {
            _customerProfile.value = _customerProfile.value.copy(
                loyaltyPoints = _customerProfile.value.loyaltyPoints + earned
            )
            val tx = LoyaltyTransaction(
                customerId = _customerProfile.value.customerId,
                orderId = orderNumber,
                pointsChange = earned,
                description = "Earned $earned TJW Points on order $orderNumber (NPR ${amount.toInt()})"
            )
            _loyaltyTransactions.value = listOf(tx) + _loyaltyTransactions.value
        }
    }

    private fun deductLoyaltyPoints(points: Int, reason: String) {
        _customerProfile.value = _customerProfile.value.copy(
            loyaltyPoints = (_customerProfile.value.loyaltyPoints - points).coerceAtLeast(0)
        )
        val tx = LoyaltyTransaction(
            customerId = _customerProfile.value.customerId,
            orderId = "REDEEM",
            pointsChange = -points,
            description = reason
        )
        _loyaltyTransactions.value = listOf(tx) + _loyaltyTransactions.value
    }

    // ==================== PRINTING SIMULATION ====================

    fun printBill(order: Order): String {
        val receipt = PrinterService.generateBillReceipt(order, _printerSettings.value)
        _printLogs.value = listOf("🧾 [BILL PRINT] ${order.humanOrderNumber} @ ${System.currentTimeMillis()}") + _printLogs.value
        return receipt
    }

    fun printKot(kot: KOT): String {
        val kotText = PrinterService.generateKotReceipt(kot, _printerSettings.value)
        _printLogs.value = listOf("🍳 [KOT PRINT] ${kot.kotNumber} @ ${System.currentTimeMillis()}") + _printLogs.value
        return kotText
    }

    fun updatePrinterSettings(settings: PrinterSettings) {
        _printerSettings.value = settings
        logAudit("Settings", "Updated printer settings: ${settings.printerName} (${settings.paperWidth.label})")
    }

    // ==================== SETTINGS, STAFF, AUDIT ====================

    fun setStaffRole(role: StaffRole) {
        _activeStaffRole.value = role
        logAudit("Security", "Active role switched to ${role.label}")
    }

    fun addStaffUser(staffUser: StaffUser) {
        _staffList.value = _staffList.value + staffUser
        logAudit("Staff", "Added staff member: ${staffUser.name} as ${staffUser.role.label}")
    }

    fun deleteStaffUser(staffId: String) {
        _staffList.value = _staffList.value.filterNot { it.staffId == staffId }
        logAudit("Staff", "Removed staff user $staffId")
    }

    fun updateCafeSettings(settings: CafeSettings) {
        _cafeSettings.value = settings
        logAudit("Settings", "Updated cafe configuration")
    }

    fun updateDeliveryProvider(config: DeliveryProviderConfig) {
        _deliveryProviders.value = _deliveryProviders.value.map {
            if (it.providerId == config.providerId) config else it
        }
        logAudit("Delivery", "Updated delivery provider configuration: ${config.name}")
    }

    fun logAudit(action: String, details: String) {
        val entry = AuditLog(
            userId = _activeStaffRole.value.name,
            userName = _activeStaffRole.value.label,
            action = action,
            newValue = details
        )
        _auditLogs.value = listOf(entry) + _auditLogs.value.take(99)
    }

    companion object {
        val instance: CafeRepository by lazy { CafeRepository() }
    }
}
