package com.kelompok4.lokalmart.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kelompok4.lokalmart.feature.auth.ui.LoginScreen
import com.kelompok4.lokalmart.feature.auth.ui.RegisterScreen
import com.kelompok4.lokalmart.feature.auth.ui.SplashScreen
import com.kelompok4.lokalmart.feature.checkout.ui.CheckoutScreen
import com.kelompok4.lokalmart.feature.checkout.ui.OrderTrackingScreen
import com.kelompok4.lokalmart.feature.checkout.ui.PaymentScreen
import com.kelompok4.lokalmart.feature.search.ui.SearchScreen
import com.kelompok4.lokalmart.feature.store.ui.StoreScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ===== Auth (Ahsan) =====
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
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ===== Home =====
        composable(Screen.Home.route) { PlaceholderScreen("Home") }

        // ===== Store (Mevya) =====
        composable(Screen.Store.route) {
            StoreScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateNext = {
                    navController.navigate(Screen.MyStore.route) {
                        popUpTo(Screen.Store.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.MyStore.route) {
            PlaceholderScreen("Dashboard Toko Saya (MyStore)")
        }

        // ===== Catalog (Putri) =====
        composable(Screen.ProductDetail.route) { PlaceholderScreen("Product Detail") }

        // ===== Search & Cart (Khoiriah) =====
        composable(Screen.Search.route) {
            SearchScreen(
                onProductClick = { productId: String ->
                    navController.navigate(Screen.ProductDetail.create(productId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Cart.route) { PlaceholderScreen("Cart") }

        // ===== Checkout & Order (Muna) =====
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
        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            PaymentScreen(
                navController = navController,
                orderId = orderId,
                method = "qris",
                amount = 0.0
            )
        }

        // ===== Review & Dashboard (Febrian) =====
        composable(Screen.SellerDashboard.route) { PlaceholderScreen("Seller Dashboard") }
        composable(
            route = Screen.ReviewForm.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType },
                navArgument("productId") { type = NavType.StringType }
            )
        ) { PlaceholderScreen("Review Form") }

        // ===== Admin (Ahsan) =====
        composable(Screen.AdminPanel.route) { PlaceholderScreen("Admin Panel") }
        composable(Screen.AdminStoreVerification.route) { PlaceholderScreen("Admin Store Verification") }
        composable(Screen.AdminProductList.route) { PlaceholderScreen("Admin Product List") }
        composable(Screen.AdminUserList.route) { PlaceholderScreen("Admin User List") }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$name screen — belum diimplementasi",
            style = MaterialTheme.typography.titleLarge
        )
    }
}