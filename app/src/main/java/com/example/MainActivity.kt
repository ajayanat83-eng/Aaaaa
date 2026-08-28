package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.CafeRepository
import com.example.model.OrderType
import com.example.model.Table
import com.example.ui.customer.*
import com.example.ui.staff.*
import com.example.ui.theme.*

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Customer", Icons.Default.Storefront)
    object Menu : Screen("menu?categoryId={categoryId}", "Menu", Icons.Default.RestaurantMenu)
    object Cart : Screen("cart", "Cart", Icons.Default.ShoppingCart)
    object Checkout : Screen("checkout/{orderType}?tableNumber={tableNumber}&deliveryAddress={deliveryAddress}&couponCode={couponCode}&discount={discount}&redeemedPoints={redeemedPoints}&notes={notes}", "Checkout", Icons.Default.Payment)
    object Tracking : Screen("tracking?orderId={orderId}", "Orders", Icons.Default.ReceiptLong)
    object Profile : Screen("profile", "Rewards", Icons.Default.Person)
    object Pos : Screen("pos", "POS Counter", Icons.Default.PointOfSale)
    object Tables : Screen("tables", "Floor Tables", Icons.Default.TableBar)
    object Kitchen : Screen("kitchen", "Kitchen KDS", Icons.Default.SoupKitchen)
    object Printers : Screen("printers", "Printers", Icons.Default.Print)
    object TableQr : Screen("table_qr", "Table QR", Icons.Default.QrCode)
    object Admin : Screen("admin", "Admin Control", Icons.Default.AdminPanelSettings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        TjwCafeApplication.initFirebase(applicationContext)
        val repository = CafeRepository.instance
        com.example.data.FirestoreMenuRepository.instance.initLocalDatabase(applicationContext)

        setContent {
            MyApplicationTheme {
                MainAppContent(repository)
            }
        }
    }
}

@Composable
fun MainAppContent(repository: CafeRepository) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Bottom Navigation Destinations for easy access
    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Pos,
        Screen.Kitchen,
        Screen.Tables,
        Screen.Admin
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute?.startsWith(screen.route.substringBefore("?").substringBefore("/")) == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route.substringBefore("?").substringBefore("/")) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = GoldenAmber,
                            indicatorColor = WaffleOrange,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                // ==================== 1. CUSTOMER HOME ====================
                composable(Screen.Home.route) {
                    CustomerHomeScreen(
                        repository = repository,
                        onNavigateToMenu = { categoryId ->
                            val route = if (categoryId != null) "menu?categoryId=$categoryId" else "menu?categoryId="
                            navController.navigate(route)
                        },
                        onNavigateToCart = {
                            navController.navigate(Screen.Cart.route)
                        },
                        onNavigateToProfile = {
                            navController.navigate(Screen.Profile.route)
                        },
                        onNavigateToOrders = {
                            navController.navigate("tracking?orderId=")
                        },
                        onStartDineIn = { table ->
                            navController.navigate(Screen.Cart.route)
                        }
                    )
                }

                // ==================== 2. MENU SCREEN ====================
                composable(
                    route = Screen.Menu.route,
                    arguments = listOf(navArgument("categoryId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { backStackEntry ->
                    val categoryId = backStackEntry.arguments?.getString("categoryId")
                    MenuScreen(
                        repository = repository,
                        initialCategoryId = if (categoryId.isNullOrBlank()) null else categoryId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToCart = { navController.navigate(Screen.Cart.route) }
                    )
                }

                // ==================== 3. CART SCREEN ====================
                composable(Screen.Cart.route) {
                    CartScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() },
                        onProceedToCheckout = { type, table, address, coupon, discount, points, notes ->
                            val encTable = table ?: "NONE"
                            val encAddress = address ?: "NONE"
                            val encCoupon = coupon ?: "NONE"
                            val encNotes = if (notes.isNotBlank()) notes else "NONE"
                            navController.navigate("checkout/${type.name}?tableNumber=$encTable&deliveryAddress=$encAddress&couponCode=$encCoupon&discount=$discount&redeemedPoints=$points&notes=$encNotes")
                        }
                    )
                }

                // ==================== 4. CHECKOUT SCREEN ====================
                composable(
                    route = Screen.Checkout.route,
                    arguments = listOf(
                        navArgument("orderType") { type = NavType.StringType },
                        navArgument("tableNumber") { type = NavType.StringType; defaultValue = "NONE" },
                        navArgument("deliveryAddress") { type = NavType.StringType; defaultValue = "NONE" },
                        navArgument("couponCode") { type = NavType.StringType; defaultValue = "NONE" },
                        navArgument("discount") { type = NavType.StringType; defaultValue = "0.0" },
                        navArgument("redeemedPoints") { type = NavType.IntType; defaultValue = 0 },
                        navArgument("notes") { type = NavType.StringType; defaultValue = "NONE" }
                    )
                ) { backStack ->
                    val orderTypeName = backStack.arguments?.getString("orderType") ?: "DINE_IN"
                    val tableArg = backStack.arguments?.getString("tableNumber")?.takeIf { it != "NONE" }
                    val addressArg = backStack.arguments?.getString("deliveryAddress")?.takeIf { it != "NONE" }
                    val couponArg = backStack.arguments?.getString("couponCode")?.takeIf { it != "NONE" }
                    val discountArg = backStack.arguments?.getString("discount")?.toDoubleOrNull() ?: 0.0
                    val pointsArg = backStack.arguments?.getInt("redeemedPoints") ?: 0
                    val notesArg = backStack.arguments?.getString("notes")?.takeIf { it != "NONE" } ?: ""

                    val orderTypeEnum = try {
                        OrderType.valueOf(orderTypeName)
                    } catch (e: Exception) {
                        OrderType.DINE_IN
                    }

                    CheckoutScreen(
                        repository = repository,
                        orderType = orderTypeEnum,
                        tableNumber = tableArg,
                        deliveryAddress = addressArg,
                        couponCode = couponArg,
                        discountAmount = discountArg,
                        redeemedPoints = pointsArg,
                        specialNotes = notesArg,
                        onNavigateBack = { navController.popBackStack() },
                        onOrderPlaced = { createdOrder ->
                            navController.navigate("tracking?orderId=${createdOrder.orderId}") {
                                popUpTo(Screen.Home.route)
                            }
                        }
                    )
                }

                // ==================== 5. ORDER TRACKING & RECEIPT ====================
                composable(
                    route = Screen.Tracking.route,
                    arguments = listOf(navArgument("orderId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    })
                ) { backStack ->
                    val orderId = backStack.arguments?.getString("orderId")
                    OrderTrackingScreen(
                        repository = repository,
                        targetOrderId = if (orderId.isNullOrBlank()) null else orderId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHome = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                        }
                    )
                }

                // ==================== 6. CUSTOMER PROFILE & LOYALTY ====================
                composable(Screen.Profile.route) {
                    CustomerProfileScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToOrders = { navController.navigate("tracking?orderId=") }
                    )
                }

                // ==================== 7. POS BILLING COUNTER ====================
                composable(Screen.Pos.route) {
                    PosBillingScreen(
                        repository = repository,
                        onNavigateBack = { navController.navigate(Screen.Home.route) }
                    )
                }

                // ==================== 8. TABLE MANAGEMENT ====================
                composable(Screen.Tables.route) {
                    TableManagementScreen(
                        repository = repository,
                        onNavigateBack = { navController.navigate(Screen.Home.route) },
                        onNavigateToTableQr = { navController.navigate(Screen.TableQr.route) }
                    )
                }

                // ==================== 9. KITCHEN KDS ====================
                composable(Screen.Kitchen.route) {
                    KitchenKdsScreen(
                        repository = repository,
                        onNavigateBack = { navController.navigate(Screen.Home.route) }
                    )
                }

                // ==================== 10. PRINTERS ====================
                composable(Screen.Printers.route) {
                    PrinterSettingsScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // ==================== 11. TABLE QR GENERATOR ====================
                composable(Screen.TableQr.route) {
                    TableQrManagerScreen(
                        repository = repository,
                        onNavigateBack = { navController.popBackStack() },
                        onSimulateScanToOrder = { table ->
                            // Open cart/checkout workflow with table pre-filled
                            navController.navigate(Screen.Cart.route)
                        }
                    )
                }

                // ==================== 12. ADMIN CONTROL ====================
                composable(Screen.Admin.route) {
                    AdminDashboardScreen(
                        repository = repository,
                        onNavigateBack = { navController.navigate(Screen.Home.route) },
                        onNavigateToPos = { navController.navigate(Screen.Pos.route) },
                        onNavigateToTables = { navController.navigate(Screen.Tables.route) },
                        onNavigateToKitchen = { navController.navigate(Screen.Kitchen.route) },
                        onNavigateToPrinters = { navController.navigate(Screen.Printers.route) },
                        onNavigateToTableQr = { navController.navigate(Screen.TableQr.route) }
                    )
                }
            }
        }
    }
}
