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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kelompok4.lokalmart.feature.auth.ui.LoginScreen
import com.kelompok4.lokalmart.feature.auth.ui.RegisterScreen
import com.kelompok4.lokalmart.feature.auth.ui.SplashScreen
import com.kelompok4.lokalmart.feature.checkout.ui.OrderTrackingScreen
import com.kelompok4.lokalmart.feature.search.ui.SearchScreen
import com.kelompok4.lokalmart.feature.store.ui.StoreScreen

/**
 * NavHost utama aplikasi.
 *
 * Setiap anggota tambahkan composable() untuk screen-nya masing-masing.
 * Sementara placeholder dulu — diganti dengan Composable beneran nanti.
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
        composable(Screen.Splash.route) { PlaceholderScreen("Splash") }
        composable(Screen.Login.route) { PlaceholderScreen("Login") }
        composable(Screen.Register.route) { PlaceholderScreen("Register") }
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

        //FITUR STOREEEE
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

        // ===== Home (placeholder, akan diganti dengan katalog fitur Putri) =====
        composable(Screen.Home.route) { PlaceholderScreen("Home") }
        // TODO: anggota lain tambahkan composable() screen-nya di sini.

        // ── Search & Cart (Khoiriah) ──────────────────────────────────────
        composable(Screen.Search.route) {
            SearchScreen(
                onProductClick = { productId: String ->
                    navController.navigate(Screen.ProductDetail.create(productId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Cart menyusul setelah CartScreen selesai dibuat
        // composable(Screen.Cart.route) {
        //     CartScreen(
        //         onCheckoutClick = { navController.navigate(Screen.Checkout.route) },
        //         onNavigateBack = { navController.popBackStack() }
        //     )
        // }
        // ── End Search & Cart (Khoiriah) ──────────────────────────────────

        composable("orders") {
            OrderTrackingScreen(
                navController = navController,
                buyerId = ""
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$name screen — belum diimplementasi",
            style = MaterialTheme.typography.titleLarge
        )
    }
}