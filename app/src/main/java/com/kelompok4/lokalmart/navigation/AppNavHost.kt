package com.kelompok4.lokalmart.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kelompok4.lokalmart.feature.admin.ui.AdminPanelScreen
import com.kelompok4.lokalmart.feature.auth.ui.*
import com.kelompok4.lokalmart.feature.cart.ui.CartScreen
import com.kelompok4.lokalmart.feature.catalog.ui.HomeScreen
import com.kelompok4.lokalmart.feature.catalog.ui.ProductDetailScreen
import com.kelompok4.lokalmart.feature.checkout.ui.CheckoutScreen
import com.kelompok4.lokalmart.feature.checkout.ui.OrderDetailScreen
import com.kelompok4.lokalmart.feature.checkout.ui.OrderTrackingScreen
import com.kelompok4.lokalmart.feature.checkout.ui.PaymentScreen
import com.kelompok4.lokalmart.feature.review.ui.ReviewFormScreen
import com.kelompok4.lokalmart.feature.review.ui.SellerDashboardScreen
import com.kelompok4.lokalmart.feature.search.ui.SearchScreen
import com.kelompok4.lokalmart.feature.store.ui.MyStoreScreen
import com.kelompok4.lokalmart.feature.store.ui.StoreRegisterScreen

/**
 * NavHost utama aplikasi.
 * Semua screen sudah di-wire ke composable masing-masing.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ══════════════════════════════════════════════════════════════
        // AUTH (Ahsan)
        // ══════════════════════════════════════════════════════════════
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToStore = { navController.navigate(Screen.StoreRegister.route) },
                onNavigateToAdmin = { navController.navigate(Screen.AdminPanel.route) },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                onNavigateToPesanan = { navController.navigate(Screen.OrderHistory.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════════
        // CATALOG / HOME (Putri)
        // ══════════════════════════════════════════════════════════════
        composable(Screen.Home.route) {
            HomeScreen(
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.create(productId))
                },
                onSearchClick = { navController.navigate(Screen.Search.route) },
                onCartClick = { navController.navigate(Screen.Cart.route) },
                onPesananClick = { navController.navigate(Screen.OrderHistory.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        }




        composable(Screen.Cart.route) {
            CartScreen(
                onCheckoutClick = { navController.navigate(Screen.Checkout.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════════
        // CHECKOUT & ORDERS (Muna)
        // ══════════════════════════════════════════════════════════════
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                navController = navController,
                buyerId = "",
                storeId = "",
                items = emptyList(),
                totalPrice = 0.0
            )
        }

        composable(Screen.OrderHistory.route) {
            OrderTrackingScreen(
                navController = navController,
                buyerId = ""
            )
        }

        // Keep the "orders" route for backward compatibility
        composable("orders") {
            OrderTrackingScreen(
                navController = navController,
                buyerId = ""
            )
        }

        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            OrderDetailScreen(
                orderId = orderId,
                onNavigateBack = { navController.popBackStack() },
                onReviewClick = { oId, pId ->
                    navController.navigate(Screen.ReviewForm.create(oId, pId))
                }
            )
        }

        // ══════════════════════════════════════════════════════════════
        // STORE (Mevya)
        // ══════════════════════════════════════════════════════════════
        composable(Screen.StoreRegister.route) {
            StoreRegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate(Screen.MyStore.route) {
                        popUpTo(Screen.StoreRegister.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.MyStore.route) {
            MyStoreScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddProduct = { navController.navigate(Screen.AddProduct.route) },
                onEditProduct = { productId ->
                    navController.navigate(Screen.EditProduct.create(productId))
                }
            )
        }

        // ══════════════════════════════════════════════════════════════
        // REVIEW & DASHBOARD (Febrian)
        // ══════════════════════════════════════════════════════════════
        composable(
            route = Screen.ReviewForm.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType },
                navArgument("productId") { type = NavType.StringType }
            )
        ) {
            ReviewFormScreen(
                onNavigateBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.SellerDashboard.route) {
            SellerDashboardScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ══════════════════════════════════════════════════════════════
        // ADMIN (Ahsan)
        // ══════════════════════════════════════════════════════════════
        composable(Screen.AdminPanel.route) {
            AdminPanelScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}