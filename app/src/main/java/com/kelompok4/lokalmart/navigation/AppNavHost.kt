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
        composable(Screen.Splash.route) { com.kelompok4.lokalmart.SmokeTestScreen() }  // TODO: ganti kembali setelah smoke test selesai
        composable(Screen.Login.route) { PlaceholderScreen("Login") }
        composable(Screen.Register.route) { PlaceholderScreen("Register") }
        composable(Screen.Home.route) { PlaceholderScreen("Home") }
        // TODO: anggota lain tambahkan composable() screen-nya di sini.
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
