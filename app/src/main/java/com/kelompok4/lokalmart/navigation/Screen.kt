package com.kelompok4.lokalmart.navigation

/**
 * Semua route navigasi aplikasi.
 * Tambahkan route baru di sini saat menambah screen baru.
 *
 * Konvensi penamaan: <fitur>_<screen>, contoh: auth_login, catalog_detail.
 */
sealed class Screen(val route: String) {

    // Onboarding & Auth (Ahsan)
    data object Splash : Screen("splash")
    data object Login : Screen("auth_login")
    data object Register : Screen("auth_register")

    // Buyer
    data object Home : Screen("home")
    data object Profile : Screen("profile")
    data object EditProfile : Screen("profile_edit")
    data object SavedAddresses : Screen("saved_addresses?isSelectionMode={isSelectionMode}") {
        fun create(isSelectionMode: Boolean = false) = "saved_addresses?isSelectionMode=$isSelectionMode"
    }
    data object PaymentMethods : Screen("payment_methods?isSelectionMode={isSelectionMode}") {
        fun create(isSelectionMode: Boolean = false) = "payment_methods?isSelectionMode=$isSelectionMode"
    }
    data object Wishlist : Screen("wishlist")
    data object Notifications : Screen("notifications")
    data object Chat : Screen("chat")
    data object ChatDetail : Screen("chat_detail/{sellerId}/{sellerName}") {
        fun create(sellerId: String, sellerName: String) = "chat_detail/$sellerId/$sellerName"
    }

    // Catalog (Putri)
    data object ProductDetail : Screen("product_detail/{productId}") {
        fun create(productId: String) = "product_detail/$productId"
    }
    data object AllCategories : Screen("all_categories")
    data object CategoryProducts : Screen("category_products/{categoryId}/{categoryName}") {
        fun create(categoryId: Int, categoryName: String) = "category_products/$categoryId/$categoryName"
    }
    data object AllProducts : Screen("all_products")
    data object AllStores : Screen("all_stores")


    // Store (Mevya)
    data object Store : Screen("Store")
    data object MyStore : Screen("my_store")
    data object MyStoreProfile : Screen("my_store_profile")
    data object StoreProfile : Screen("store_profile/{storeId}") {
        fun create(storeId: String) = "store_profile/$storeId"
    }
    data object EditStoreInfo : Screen("store_edit/{storeId}") {
        fun create(storeId: String) = "store_edit/$storeId"
    }
    data object Inventory : Screen("store_inventory")
    data object AddProduct : Screen("product_add")
    data object EditProduct : Screen("product_edit/{productId}") {
        fun create(productId: String) = "product_edit/$productId"
    }
    data object SellerOrders : Screen("seller_orders")
    data object SellerChat : Screen("seller_chat")
    data object SellerChatDetail : Screen("seller_chat_detail/{sellerId}/{sellerName}") {
        fun create(sellerId: String, sellerName: String) = "seller_chat_detail/$sellerId/$sellerName"
    }

    // Search & Cart (Khoiriah)
    data object Search : Screen("search")
    data object Cart : Screen("cart")

    // Checkout & Order (Muna)
    data object Checkout : Screen("checkout/{storeId}") {
        fun create(storeId: String) = "checkout/$storeId"
    }
    data object Payment : Screen("payment/{orderId}/{method}/{amount}") {
        fun create(orderId: String, method: String, amount: Double) = "payment/$orderId/$method/$amount"
    }
    data object OrderHistory : Screen("order_history")
    data object OrderDetail : Screen("order_detail/{orderId}") {
        fun create(orderId: String) = "order_detail/$orderId"
    }

    // Review & Dashboard (Febrian)
    data object ReviewForm : Screen("review_form/{orderId}/{productId}") {
        fun create(orderId: String, productId: String) = "review_form/$orderId/$productId"
    }
    data object ProductReviews : Screen("product_reviews/{productId}") {
        fun create(productId: String) = "product_reviews/$productId"
    }
    data object SellerDashboard : Screen("seller_dashboard")

    // Admin (Ahsan)
    data object AdminPanel : Screen("admin_panel")
    data object AdminStoreVerification : Screen("admin_store_verification")
    data object AdminProductList : Screen("admin_product_list")
    data object AdminUserList : Screen("admin_user_list")
}
