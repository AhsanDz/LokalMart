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

    // Catalog (Putri)
    data object ProductDetail : Screen("product_detail/{productId}") {
        fun create(productId: String) = "product_detail/$productId"
    }

    // Store (Mevya)
    data object StoreRegister : Screen("store_register")
    data object MyStore : Screen("my_store")
    data object StoreProfile : Screen("store_profile/{storeId}") {
        fun create(storeId: String) = "store_profile/$storeId"
    }
    data object AddProduct : Screen("product_add")
    data object EditProduct : Screen("product_edit/{productId}") {
        fun create(productId: String) = "product_edit/$productId"
    }

    // Search & Cart (Khoiriah)
    data object Search : Screen("search")
    data object Cart : Screen("cart")

    // Checkout & Order (Muna)
    data object Checkout : Screen("checkout")
    data object OrderHistory : Screen("order_history")
    data object OrderDetail : Screen("order_detail/{orderId}") {
        fun create(orderId: String) = "order_detail/$orderId"
    }

    // Review & Dashboard (Febrian)
    data object ReviewForm : Screen("review_form/{orderId}/{productId}") {
        fun create(orderId: String, productId: String) = "review_form/$orderId/$productId"
    }
    data object SellerDashboard : Screen("seller_dashboard")

    // Admin (Ahsan)
    data object AdminPanel : Screen("admin_panel")
    data object AdminStoreVerification : Screen("admin_store_verification")
    data object AdminProductList : Screen("admin_product_list")
    data object AdminUserList : Screen("admin_user_list")
}
