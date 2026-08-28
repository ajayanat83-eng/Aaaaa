package com.example.data

import com.example.model.*

object DefaultMenuData {

    val categories = listOf(
        Category(
            id = "cat_momos_sandwiches",
            name = "Momos & Sandwiches",
            iconEmoji = "🥟",
            description = "Crispy kurkure momos, tandoori paneer & grilled sandwiches",
            sortOrder = 1
        ),
        Category(
            id = "cat_pizza_fries_maggi",
            name = "Pizza, Fries & Maggi",
            iconEmoji = "🍕",
            description = "Freshly baked 8-inch pizzas, loaded peri peri fries & cheesy masala maggi",
            sortOrder = 2
        ),
        Category(
            id = "cat_waffles_desserts",
            name = "TJW Waffles & Desserts",
            iconEmoji = "🧇",
            description = "Signature chocolate waffles, pancakes & waffle cakes (100% Eggless)",
            sortOrder = 3
        ),
        Category(
            id = "cat_shakes_coffee_coolers",
            name = "Shakes, Coffee & Coolers",
            iconEmoji = "🥤",
            description = "Creamy thick shakes, refreshing coolers, iced tea & brewed cold coffee",
            sortOrder = 4
        ),
        Category(
            id = "cat_combos_addons",
            name = "Super Saver Combos & Add-ons",
            iconEmoji = "⭐",
            description = "Value combos, snack pairings & extra toppings",
            sortOrder = 5
        )
    )

    val defaultAddons = listOf(
        ProductAddon(name = "Vanilla Ice Cream Scoop", price = 30.0),
        ProductAddon(name = "Extra Nutella", price = 30.0),
        ProductAddon(name = "Extra Oreo", price = 30.0),
        ProductAddon(name = "Extra KitKat", price = 30.0)
    )

    fun getInitialProducts(): List<Product> = listOf(
        // ==================== CATEGORY 1: MOMOS & SANDWICHES ====================
        Product(
            productId = "prod_momo_tandoori",
            name = "Tandoori Paneer Momos (6 Pcs)",
            description = "Succulent paneer dumplings marinated in rich tandoori spices and char-grilled with mint chutney.",
            categoryId = "cat_momos_sandwiches",
            price = 120.0,
            imageEmoji = "🥟",
            isAvailable = true,
            isFeatured = true,
            isBestSeller = false,
            badge = "Must Try",
            preparationTime = 12,
            sortOrder = 1
        ),
        Product(
            productId = "prod_momo_kurkure",
            name = "Cheese Kurkure Momos (6 Pcs)",
            description = "Extra crispy coated momos stuffed with melted cheese & garden veggies.",
            categoryId = "cat_momos_sandwiches",
            price = 130.0,
            imageEmoji = "🥟",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 12,
            sortOrder = 2
        ),
        Product(
            productId = "prod_momo_royal_pizza",
            name = "Royal Pizza Momos (6 Pcs)",
            description = "Baked with rich pizza sauce, mozzarella cheese, corn, and Italian herbs.",
            categoryId = "cat_momos_sandwiches",
            price = 140.0,
            imageEmoji = "🥟",
            isAvailable = true,
            isFeatured = true,
            isBestSeller = false,
            badge = "Chef Special",
            preparationTime = 15,
            sortOrder = 3
        ),
        Product(
            productId = "prod_sw_chilli_garlic",
            name = "Hot Chilli Garlic Sandwich (4 Pcs)",
            description = "Toasted bread with fiery homemade chilli garlic spread, veggies and melted butter.",
            categoryId = "cat_momos_sandwiches",
            price = 90.0,
            imageEmoji = "🥪",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 8,
            sortOrder = 4
        ),
        Product(
            productId = "prod_sw_mix_veg",
            name = "Mix Veg Cheese Sandwich (4 Pcs)",
            description = "Classic grilled sandwich filled with bell peppers, cucumber, sweet corn and creamy cheese.",
            categoryId = "cat_momos_sandwiches",
            price = 100.0,
            imageEmoji = "🥪",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 8,
            sortOrder = 5
        ),
        Product(
            productId = "prod_sw_paneer_tikka",
            name = "Paneer Tikka Sandwich (4 Pcs)",
            description = "Smoky tandoori spiced paneer cubes layered with cheese sauce in golden toasted bread.",
            categoryId = "cat_momos_sandwiches",
            price = 120.0,
            imageEmoji = "🥪",
            isAvailable = true,
            isFeatured = true,
            isBestSeller = true,
            badge = "Best Seller",
            preparationTime = 10,
            sortOrder = 6
        ),

        // ==================== CATEGORY 2: PIZZA, FRIES & MAGGI ====================
        Product(
            productId = "prod_pz_otc",
            name = "OTC Pizza (8 Inch / 6 Slices)",
            description = "Classic Onion, Tomato & Capsicum loaded on a crisp hand-stretched crust with 100% mozzarella.",
            categoryId = "cat_pizza_fries_maggi",
            price = 180.0,
            imageEmoji = "🍕",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 15,
            sortOrder = 7
        ),
        Product(
            productId = "prod_pz_onion_corn",
            name = "Cheesy Onion & Corn Pizza (8 Inch / 6 Slices)",
            description = "Sweet American golden corn paired with caramelized diced onions and dual cheese blend.",
            categoryId = "cat_pizza_fries_maggi",
            price = 190.0,
            imageEmoji = "🍕",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 15,
            sortOrder = 8
        ),
        Product(
            productId = "prod_pz_paneer_special",
            name = "Paneer Special Pizza (8 Inch / 6 Slices)",
            description = "Loaded with spiced paneer chunks, bell peppers, olives, oregano drizzle and extra cheese.",
            categoryId = "cat_pizza_fries_maggi",
            price = 230.0,
            imageEmoji = "🍕",
            isAvailable = true,
            isFeatured = true,
            isBestSeller = false,
            badge = "Loaded",
            preparationTime = 18,
            sortOrder = 9
        ),
        Product(
            productId = "prod_fr_peri_peri",
            name = "Peri Peri Loaded Fries",
            description = "Crispy golden french fries tossed in spicy African peri peri seasoning with cheese dip.",
            categoryId = "cat_pizza_fries_maggi",
            price = 100.0,
            imageEmoji = "🍟",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 8,
            sortOrder = 10
        ),
        Product(
            productId = "prod_fr_cheesy_pizza",
            name = "Cheesy Pizza Fries",
            description = "Crispy fries smothered in marinara sauce, melted mozzarella cheese and herb sprinkles.",
            categoryId = "cat_pizza_fries_maggi",
            price = 130.0,
            imageEmoji = "🍟",
            isAvailable = true,
            isFeatured = true,
            isBestSeller = false,
            badge = "Popular",
            preparationTime = 10,
            sortOrder = 11
        ),
        Product(
            productId = "prod_paneer_bites",
            name = "Crispy Paneer Bites",
            description = "Crunchy breadcrumb coated cottage cheese cubes served with signature spicy dip.",
            categoryId = "cat_pizza_fries_maggi",
            price = 120.0,
            imageEmoji = "🧀",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 10,
            sortOrder = 12
        ),
        Product(
            productId = "prod_mg_tandoori",
            name = "Tandoori Cheese Maggi",
            description = "Hot noodles infused with smoked tandoori spices and topped with shredded cheese.",
            categoryId = "cat_pizza_fries_maggi",
            price = 90.0,
            imageEmoji = "🍜",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 7,
            sortOrder = 13
        ),
        Product(
            productId = "prod_mg_cheesy_masala",
            name = "Cheesy Masala Maggi",
            description = "Chef's secret vegetable masala noodle recipe topped with rich melted cheddar sauce.",
            categoryId = "cat_pizza_fries_maggi",
            price = 100.0,
            imageEmoji = "🍜",
            isAvailable = true,
            isFeatured = true,
            isBestSeller = false,
            badge = "Hot",
            preparationTime = 7,
            sortOrder = 14
        ),

        // ==================== CATEGORY 3: TJW WAFFLES & DESSERTS ====================
        Product(
            productId = "prod_wf_triple_choco",
            name = "Triple Chocolate Waffle",
            description = "Signature crispy waffle loaded with Dark Chocolate, Milk Chocolate, and White Chocolate drizzle.",
            categoryId = "cat_waffles_desserts",
            price = 130.0,
            imageEmoji = "🧇",
            isAvailable = true,
            isFeatured = true,
            isBestSeller = true,
            badge = "Best Seller",
            addons = defaultAddons,
            preparationTime = 10,
            sortOrder = 15
        ),
        Product(
            productId = "prod_wf_black_white",
            name = "Black & White Fantasy Waffle",
            description = "Crispy dark cocoa waffle base generously filled with sweet Belgian white cream.",
            categoryId = "cat_waffles_desserts",
            price = 130.0,
            imageEmoji = "🧇",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            addons = defaultAddons,
            preparationTime = 10,
            sortOrder = 16
        ),
        Product(
            productId = "prod_wf_cadbury",
            name = "Cadbury Overload Waffle",
            description = "Rich melted Dairy Milk Cadbury chocolate spread across golden baked waffle grid.",
            categoryId = "cat_waffles_desserts",
            price = 130.0,
            imageEmoji = "🧇",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            addons = defaultAddons,
            preparationTime = 10,
            sortOrder = 17
        ),
        Product(
            productId = "prod_wf_nutella_magic",
            name = "Nutella Magic Waffle",
            description = "Authentic hazelnut Nutella spread overloaded with roasted nuts and chocolate drops.",
            categoryId = "cat_waffles_desserts",
            price = 160.0,
            imageEmoji = "🧇",
            isAvailable = true,
            isFeatured = true,
            isBestSeller = false,
            badge = "Popular",
            addons = defaultAddons,
            preparationTime = 10,
            sortOrder = 18
        ),
        Product(
            productId = "prod_wf_red_velvet",
            name = "Red Velvet Waffle",
            description = "Vibrant crimson waffle infused with vanilla notes and frosted with white chocolate sauce.",
            categoryId = "cat_waffles_desserts",
            price = 120.0,
            imageEmoji = "🧇",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            addons = defaultAddons,
            preparationTime = 10,
            sortOrder = 19
        ),
        Product(
            productId = "prod_wf_biscoff",
            name = "Biscoff Special Waffle",
            description = "Lotus Biscoff caramel cookie butter spread with crunchy caramelized biscuit crumbles.",
            categoryId = "cat_waffles_desserts",
            price = 140.0,
            imageEmoji = "🧇",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            addons = defaultAddons,
            preparationTime = 10,
            sortOrder = 20
        ),
        Product(
            productId = "prod_pc_choco_overload",
            name = "Chocolate Overload Pancake (8 Pcs)",
            description = "Eight fluffy mini pancakes stacked and drenched in premium warm chocolate syrup.",
            categoryId = "cat_waffles_desserts",
            price = 110.0,
            imageEmoji = "🥞",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            addons = defaultAddons,
            preparationTime = 10,
            sortOrder = 21
        ),
        Product(
            productId = "prod_pc_nutella",
            name = "Nutella Pancake (8 Pcs)",
            description = "Eight golden fluffy pancakes drizzled with lavish Nutella and chocolate chips.",
            categoryId = "cat_waffles_desserts",
            price = 130.0,
            imageEmoji = "🥞",
            isAvailable = true,
            isFeatured = true,
            isBestSeller = false,
            badge = "Must Try",
            addons = defaultAddons,
            preparationTime = 10,
            sortOrder = 22
        ),
        Product(
            productId = "prod_wc_triple_choco",
            name = "Triple Chocolate Waffle Cake",
            description = "Layered celebratory waffle cake smothered in chocolate ganache and chocolate curls.",
            categoryId = "cat_waffles_desserts",
            price = 260.0, // Base price for Single Layer
            imageEmoji = "🎂",
            isAvailable = true,
            isFeatured = true,
            isBestSeller = false,
            variants = listOf(
                ProductVariant(name = "Single Layer", price = 260.0),
                ProductVariant(name = "Double Layer", price = 360.0)
            ),
            addons = defaultAddons,
            preparationTime = 15,
            sortOrder = 23
        ),
        Product(
            productId = "prod_box_choco_lover",
            name = "Chocolate Lover's Box (4 Pcs Mini)",
            description = "Assorted 4 mini waffles: Triple Chocolate, Nutella, Dark Fantasy & Cadbury.",
            categoryId = "cat_waffles_desserts",
            price = 260.0,
            imageEmoji = "🎁",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            addons = defaultAddons,
            preparationTime = 12,
            sortOrder = 24
        ),
        Product(
            productId = "prod_box_velvet_choco",
            name = "Velvet Choco Box (4 Pcs Mini)",
            description = "Assorted 4 mini waffles: 2 Red Velvet White Choco + 2 Triple Dark Chocolate.",
            categoryId = "cat_waffles_desserts",
            price = 290.0,
            imageEmoji = "🎁",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            addons = defaultAddons,
            preparationTime = 12,
            sortOrder = 25
        ),

        // ==================== CATEGORY 4: SHAKES, COFFEE & COOLERS ====================
        Product(
            productId = "prod_dr_cold_coffee",
            name = "Cold Coffee",
            description = "Freshly brewed slow espresso blended with chilled creamy milk and chocolate drizzle.",
            categoryId = "cat_shakes_coffee_coolers",
            price = 80.0,
            imageEmoji = "☕",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            variants = listOf(
                ProductVariant(name = "Classic", price = 80.0),
                ProductVariant(name = "Strong", price = 90.0)
            ),
            preparationTime = 5,
            sortOrder = 26
        ),
        Product(
            productId = "prod_dr_oreo_nutella",
            name = "Oreo Nutella Shake",
            description = "Crushed Oreo biscuits blended with authentic Nutella and vanilla ice cream thick shake.",
            categoryId = "cat_shakes_coffee_coolers",
            price = 110.0,
            imageEmoji = "🥤",
            isAvailable = true,
            isFeatured = true,
            isBestSeller = true,
            badge = "Top Seller",
            preparationTime = 6,
            sortOrder = 27
        ),
        Product(
            productId = "prod_dr_kitkat_mudslide",
            name = "KitKat Mudslide Shake",
            description = "Crispy KitKat wafer bars blended into decadent chocolate fudge shake.",
            categoryId = "cat_shakes_coffee_coolers",
            price = 110.0,
            imageEmoji = "🥤",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 6,
            sortOrder = 28
        ),
        Product(
            productId = "prod_dr_biscoff_cream",
            name = "Biscoff Cream Shake",
            description = "Rich spiced caramel Lotus Biscoff milkshake with whipped cream topping.",
            categoryId = "cat_shakes_coffee_coolers",
            price = 120.0,
            imageEmoji = "🥤",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 6,
            sortOrder = 29
        ),
        Product(
            productId = "prod_dr_peach_tea",
            name = "Peach Ice Tea",
            description = "Refreshing chilled black tea infused with natural juicy peach extract and mint leaves.",
            categoryId = "cat_shakes_coffee_coolers",
            price = 90.0,
            imageEmoji = "🍹",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 4,
            sortOrder = 30
        ),
        Product(
            productId = "prod_dr_blue_lagoon",
            name = "Blue Lagoon Mojito",
            description = "Sparkling blue curaçao cooler with fresh lime, mint sprigs, and chilled soda.",
            categoryId = "cat_shakes_coffee_coolers",
            price = 100.0,
            imageEmoji = "🍸",
            isAvailable = true,
            isFeatured = true,
            isBestSeller = false,
            badge = "Trending",
            preparationTime = 4,
            sortOrder = 31
        ),
        Product(
            productId = "prod_dr_passion_fruit",
            name = "Passion Fruit Mint Cooler",
            description = "Tropical tangy passion fruit paired with crushed mint and sparkling refreshment.",
            categoryId = "cat_shakes_coffee_coolers",
            price = 110.0,
            imageEmoji = "🍹",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 4,
            sortOrder = 32
        ),

        // ==================== CATEGORY 5: SUPER SAVER COMBOS & ADD-ONS ====================
        Product(
            productId = "prod_combo_snack",
            name = "Snack Combo",
            description = "Mix Veg Cheese Sandwich + Classic Cold Coffee. Save NPR 20!",
            categoryId = "cat_combos_addons",
            price = 160.0,
            imageEmoji = "⭐",
            isAvailable = true,
            isFeatured = true,
            isBestSeller = false,
            badge = "Combo Save",
            preparationTime = 10,
            sortOrder = 33
        ),
        Product(
            productId = "prod_combo_maggi_cooler",
            name = "Maggi & Cooler Combo",
            description = "Cheesy Masala Maggi + Blue Lagoon Mojito. Perfect tea-time pairing!",
            categoryId = "cat_combos_addons",
            price = 180.0,
            imageEmoji = "⭐",
            isAvailable = true,
            isFeatured = true,
            isBestSeller = false,
            badge = "Value Deal",
            preparationTime = 8,
            sortOrder = 34
        ),
        Product(
            productId = "prod_combo_royal",
            name = "Royal Combo",
            description = "Tandoori Paneer Momos (6 Pcs) + Triple Chocolate Waffle. The ultimate TJW feast!",
            categoryId = "cat_combos_addons",
            price = 230.0,
            imageEmoji = "👑",
            isAvailable = true,
            isFeatured = true,
            isBestSeller = true,
            badge = "Best Seller",
            preparationTime = 14,
            sortOrder = 35
        ),
        Product(
            productId = "prod_addon_ice_cream",
            name = "Vanilla Ice Cream Scoop",
            description = "Creamy chilled vanilla scoop, ideal companion for waffles & pancakes.",
            categoryId = "cat_combos_addons",
            price = 30.0,
            imageEmoji = "🍨",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 2,
            sortOrder = 36
        ),
        Product(
            productId = "prod_addon_nutella",
            name = "Extra Nutella Drizzle",
            description = "Extra 30ml pure hazelnut Nutella sauce topping.",
            categoryId = "cat_combos_addons",
            price = 30.0,
            imageEmoji = "🍫",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 1,
            sortOrder = 37
        ),
        Product(
            productId = "prod_addon_oreo",
            name = "Extra Crushed Oreo",
            description = "Crunchy chocolate Oreo biscuit topping.",
            categoryId = "cat_combos_addons",
            price = 30.0,
            imageEmoji = "🍪",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 1,
            sortOrder = 38
        ),
        Product(
            productId = "prod_addon_kitkat",
            name = "Extra KitKat Chunks",
            description = "Crisp chocolate wafer KitKat chunks.",
            categoryId = "cat_combos_addons",
            price = 30.0,
            imageEmoji = "🍫",
            isAvailable = true,
            isFeatured = false,
            isBestSeller = false,
            preparationTime = 1,
            sortOrder = 39
        )
    )

    val defaultTables = (1..12).map { i ->
        val numStr = i.toString().padStart(2, '0')
        Table(
            tableId = "tbl_$numStr",
            tableNumber = "TJW-TABLE-$numStr",
            branchId = "branch_janakpur_main",
            capacity = if (i <= 6) 4 else if (i <= 10) 6 else 2,
            status = TableStatus.AVAILABLE
        )
    }

    val defaultCoupons = listOf(
        Coupon(
            code = "TJW10",
            title = "10% Welcome Discount",
            description = "Get 10% off on your order above NPR 200",
            discountType = DiscountType.PERCENTAGE,
            discountValue = 10.0,
            minOrderAmount = 200.0,
            maxDiscount = 100.0
        ),
        Coupon(
            code = "WAFFLE50",
            title = "NPR 50 Off Waffle Lovers",
            description = "Flat NPR 50 off on orders above NPR 350",
            discountType = DiscountType.FIXED,
            discountValue = 50.0,
            minOrderAmount = 350.0
        ),
        Coupon(
            code = "PUREVEG",
            title = "15% Pure Veg Celebration",
            description = "Enjoy 15% off on all super combos & waffles above NPR 400",
            discountType = DiscountType.PERCENTAGE,
            discountValue = 15.0,
            minOrderAmount = 400.0,
            maxDiscount = 150.0
        )
    )

    val defaultStaff = listOf(
        StaffUser(staffId = "stf_1", name = "Sanjay (Owner)", role = StaffRole.OWNER, phone = "+977-9706612914", pin = "9999"),
        StaffUser(staffId = "stf_2", name = "Aakash (Cashier)", role = StaffRole.CASHIER, phone = "+977-9811111111", pin = "1111"),
        StaffUser(staffId = "stf_3", name = "Bikash (Kitchen Chef)", role = StaffRole.KITCHEN, phone = "+977-9822222222", pin = "2222"),
        StaffUser(staffId = "stf_4", name = "Rohan (Waiter)", role = StaffRole.WAITER, phone = "+977-9833333333", pin = "3333"),
        StaffUser(staffId = "stf_5", name = "Priya (Manager)", role = StaffRole.MANAGER, phone = "+977-9844444444", pin = "4444")
    )

    val defaultDeliveryProviders = listOf(
        DeliveryProviderConfig(
            providerId = "delivery_provider_1",
            name = "Foodmandu / Local Express",
            apiKey = "INSERT_REAL_API_CREDENTIAL_HERE",
            apiSecret = "INSERT_REAL_API_CREDENTIAL_HERE",
            storeId = "TJW_JANAKPUR_01",
            baseUrl = "https://api.foodmandu.np/v1/store/tjw",
            isEnabled = true
        ),
        DeliveryProviderConfig(
            providerId = "delivery_provider_2",
            name = "Bhoj Deals Integration",
            apiKey = "INSERT_REAL_API_CREDENTIAL_HERE",
            apiSecret = "INSERT_REAL_API_CREDENTIAL_HERE",
            storeId = "TJW_BHOJ_02",
            baseUrl = "https://api.bhojdeals.com/partner/v1",
            isEnabled = false
        ),
        DeliveryProviderConfig(
            providerId = "delivery_provider_3",
            name = "Pathao / TJW Direct In-House Riders",
            apiKey = "INSERT_REAL_API_CREDENTIAL_HERE",
            apiSecret = "INSERT_REAL_API_CREDENTIAL_HERE",
            storeId = "TJW_DIRECT_03",
            baseUrl = "https://api.pathao.com/v2/merchant",
            isEnabled = true
        )
    )
}
