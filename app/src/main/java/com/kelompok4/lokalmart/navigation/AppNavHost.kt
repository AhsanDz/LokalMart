package com.kelompok4.lokalmart.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kelompok4.lokalmart.feature.auth.ui.LoginScreen
import com.kelompok4.lokalmart.feature.auth.ui.RegisterScreen
import com.kelompok4.lokalmart.feature.auth.ui.SplashScreen
import com.kelompok4.lokalmart.feature.auth.ui.ProfileScreen
import com.kelompok4.lokalmart.feature.auth.ui.EditProfileScreen
import com.kelompok4.lokalmart.feature.cart.ui.CartScreen
import com.kelompok4.lokalmart.feature.checkout.ui.OrderTrackingScreen
import com.kelompok4.lokalmart.feature.checkout.ui.CheckoutScreen
import com.kelompok4.lokalmart.feature.checkout.ui.PaymentScreen
import com.kelompok4.lokalmart.feature.checkout.ui.OrderDetailScreen
import com.kelompok4.lokalmart.feature.search.ui.SearchScreen
import com.kelompok4.lokalmart.feature.store.ui.StoreScreen
import com.kelompok4.lokalmart.feature.store.ui.MyStoreScreen
import com.kelompok4.lokalmart.feature.store.ui.MyStoreProfileScreen
import com.kelompok4.lokalmart.feature.store.ui.StoreProfileScreen
import com.kelompok4.lokalmart.feature.store.ui.EditStoreInfoScreen
import com.kelompok4.lokalmart.feature.store.ui.InventoryScreen
import com.kelompok4.lokalmart.feature.store.ui.AllStoresScreen
import com.kelompok4.lokalmart.feature.catalog.ui.HomeScreen
import com.kelompok4.lokalmart.feature.catalog.ui.ProductDetailScreen
import com.kelompok4.lokalmart.feature.catalog.ui.AddProductScreen
import com.kelompok4.lokalmart.feature.catalog.ui.EditProductScreen
import com.kelompok4.lokalmart.feature.catalog.ui.WishlistScreen
import com.kelompok4.lokalmart.feature.catalog.ui.AllCategoriesScreen
import com.kelompok4.lokalmart.feature.catalog.ui.CategoryProductsScreen
import com.kelompok4.lokalmart.feature.catalog.ui.AllProductsScreen
import com.kelompok4.lokalmart.feature.notification.ui.NotificationScreen
import com.kelompok4.lokalmart.feature.chat.ui.ChatScreen
import com.kelompok4.lokalmart.feature.chat.ui.ChatDetailScreen
import com.kelompok4.lokalmart.feature.review.ui.ReviewFormScreen
import com.kelompok4.lokalmart.feature.review.ui.ReviewListScreen
import com.kelompok4.lokalmart.feature.review.ui.SellerDashboardScreen
import com.kelompok4.lokalmart.feature.admin.ui.AdminPanelScreen
import com.kelompok4.lokalmart.feature.admin.ui.StoreVerificationScreen
import com.kelompok4.lokalmart.feature.admin.ui.ProductModerationScreen
import com.kelompok4.lokalmart.feature.admin.ui.UserManagementScreen
import com.kelompok4.lokalmart.feature.auth.ui.SavedAddressesScreen
import com.kelompok4.lokalmart.feature.checkout.ui.PaymentMethodsScreen
import com.kelompok4.lokalmart.feature.store.ui.SellerOrdersScreen
import com.kelompok4.lokalmart.feature.chat.ui.SellerChatScreen
import com.kelompok4.lokalmart.feature.chat.ui.SellerChatDetailScreen

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
    val navigateToTab = { route: String ->
        navController.navigate(route) {
            popUpTo(Screen.Home.route) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

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
            MyStoreScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInventory = { navController.navigate(Screen.Inventory.route) },
                onNavigateToEditStore = { storeId -> navController.navigate(Screen.EditStoreInfo.create(storeId)) },
                onNavigateToPublicProfile = { storeId -> navController.navigate(Screen.StoreProfile.create(storeId)) },
                onNavigateToMyStoreProfile = { navController.navigate(Screen.MyStoreProfile.route) },
                onNavigateToReports = { navController.navigate(Screen.SellerDashboard.route) },
                onNavigateToSellerOrders = { navController.navigate(Screen.SellerOrders.route) },
                onNavigateToSellerChat = { navController.navigate(Screen.SellerChat.route) }
            )
        }

        composable(Screen.MyStoreProfile.route) {
            MyStoreProfileScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.MyStore.route) {
                        popUpTo(Screen.MyStore.route) { inclusive = false }
                    }
                },
                onNavigateToInventory = {
                    navController.navigate(Screen.Inventory.route) {
                        popUpTo(Screen.MyStore.route)
                    }
                },
                onNavigateToEditStore = { storeId ->
                    navController.navigate(Screen.EditStoreInfo.create(storeId))
                },
                onNavigateToPublicProfile = { storeId ->
                    navController.navigate(Screen.StoreProfile.create(storeId))
                },
                onNavigateToReports = {
                    navController.navigate(Screen.SellerDashboard.route)
                },
                onNavigateToSellerOrders = { navController.navigate(Screen.SellerOrders.route) }
            )
        }

        composable(Screen.StoreProfile.route) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            StoreProfileScreen(
                storeId = storeId,
                onNavigateBack = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.create(productId))
                },
                onNavigateToEditStore = { id ->
                    navController.navigate(Screen.EditStoreInfo.create(id))
                },
                onNavigateToInventory = {
                    navController.navigate(Screen.Inventory.route)
                },
                onChatClick = { sellerId, sellerName ->
                    navController.navigate(Screen.ChatDetail.create(sellerId, sellerName))
                }
            )
        }

        composable(Screen.EditStoreInfo.route) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            EditStoreInfoScreen(
                storeId = storeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Inventory.route) {
            InventoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddProduct = { navController.navigate(Screen.AddProduct.route) },
                onNavigateToEditProduct = { productId -> navController.navigate(Screen.EditProduct.create(productId)) },
                onNavigateToMyStoreProfile = { navController.navigate(Screen.MyStoreProfile.route) },
                onNavigateToReports = { navController.navigate(Screen.SellerDashboard.route) },
                onNavigateToSellerOrders = { navController.navigate(Screen.SellerOrders.route) }
            )
        }

        // ===== Home (Buyer catalog home screen) =====
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Screen.ProductDetail.create(productId))
                },
                onNavigateToSearch = { navigateToTab(Screen.Search.route) },
                onNavigateToCart = { navigateToTab(Screen.Cart.route) },
                onNavigateToOrders = { navigateToTab("orders") },
                onNavigateToProfile = { navigateToTab(Screen.Profile.route) },
                onNavigateToStoreDetail = { storeId ->
                    navController.navigate(Screen.StoreProfile.create(storeId))
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.Notifications.route)
                },
                onNavigateToChat = {
                    navController.navigate(Screen.Chat.route)
                },
                onNavigateToAllCategories = {
                    navController.navigate(Screen.AllCategories.route)
                },
                onNavigateToCategoryProducts = { categoryId, categoryName ->
                    navController.navigate(Screen.CategoryProducts.create(categoryId, categoryName))
                },
                onNavigateToAllProducts = {
                    navController.navigate(Screen.AllProducts.route)
                },
                onNavigateToAllStores = {
                    navController.navigate(Screen.AllStores.route)
                }
            )
        }

        // ===== Catalog & Products =====
        composable(Screen.ProductDetail.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStoreDetail = { storeId ->
                    navController.navigate(Screen.StoreProfile.create(storeId))
                },
                onNavigateToCheckout = { storeId ->
                    navController.navigate(Screen.Checkout.create(storeId))
                },
                onNavigateToAllReviews = { prodId ->
                    navController.navigate(Screen.ProductReviews.create(prodId))
                },
                onChatClick = { sellerId, sellerName ->
                    navController.navigate(Screen.ChatDetail.create(sellerId, sellerName))
                }
            )
        }

        composable(Screen.AddProduct.route) {
            AddProductScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EditProduct.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            EditProductScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ===== Profile & Edit Profile (Ahsan) =====
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToStore = { navController.navigate(Screen.Store.route) },
                onNavigateToMyStore = { navController.navigate(Screen.MyStore.route) },
                onNavigateToCart = { navigateToTab(Screen.Cart.route) },
                onNavigateToOrders = { navigateToTab("orders") },
                onNavigateToHome = { navigateToTab(Screen.Home.route) },
                onNavigateToSearch = { navigateToTab(Screen.Search.route) },
                onNavigateToWishlist = { navController.navigate(Screen.Wishlist.route) },
                onNavigateToSavedAddresses = { navController.navigate(Screen.SavedAddresses.route) },
                onNavigateToPaymentMethods = { navController.navigate(Screen.PaymentMethods.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToAdminPanel = {
                    navController.navigate(Screen.AdminPanel.route)
                }
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ── Search & Cart (Khoiriah) ──────────────────────────────────────
        composable(Screen.Search.route) {
            SearchScreen(
                onProductClick = { productId: String ->
                    navController.navigate(Screen.ProductDetail.create(productId))
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navigateToTab(Screen.Home.route) },
                onNavigateToCart = { navigateToTab(Screen.Cart.route) },
                onNavigateToOrders = { navigateToTab("orders") },
                onNavigateToProfile = { navigateToTab(Screen.Profile.route) }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                onCheckoutClick = { storeId ->
                    navController.navigate(Screen.Checkout.create(storeId))
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navigateToTab(Screen.Home.route) },
                onNavigateToSearch = { navigateToTab(Screen.Search.route) },
                onNavigateToOrders = { navigateToTab("orders") },
                onNavigateToProfile = { navigateToTab(Screen.Profile.route) }
            )
        }
        // ── End Search & Cart (Khoiriah) ──────────────────────────────────

        composable(Screen.Checkout.route) { backStackEntry ->
            val storeId = backStackEntry.arguments?.getString("storeId") ?: ""
            CheckoutScreen(
                navController = navController,
                storeId = storeId
            )
        }

        composable(Screen.Payment.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val method = backStackEntry.arguments?.getString("method") ?: ""
            val amount = backStackEntry.arguments?.getString("amount")?.toDoubleOrNull() ?: 0.0
            PaymentScreen(
                navController = navController,
                orderId = orderId,
                method = method,
                amount = amount
            )
        }

        composable(Screen.OrderDetail.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderDetailScreen(
                orderId = orderId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReviewForm = { ordId, prodId ->
                    navController.navigate(Screen.ReviewForm.create(ordId, prodId))
                }
            )
        }

        composable("orders") {
            OrderTrackingScreen(
                navController = navController,
                onNavigateToHome = { navigateToTab(Screen.Home.route) },
                onNavigateToSearch = { navigateToTab(Screen.Search.route) },
                onNavigateToCart = { navigateToTab(Screen.Cart.route) },
                onNavigateToProfile = { navigateToTab(Screen.Profile.route) }
            )
        }

        // ===== Review Form & List =====
        composable(Screen.ReviewForm.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ReviewFormScreen(
                navController = navController,
                orderId = orderId,
                productId = productId
            )
        }

        composable(Screen.ProductReviews.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ReviewListScreen(
                navController = navController,
                productId = productId
            )
        }

        // ===== Seller Analytics/Dashboard =====
        composable(Screen.SellerDashboard.route) {
            SellerDashboardScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.MyStore.route) {
                        popUpTo(Screen.MyStore.route) { inclusive = true }
                    }
                },
                onNavigateToInventory = {
                    navController.navigate(Screen.Inventory.route) {
                        popUpTo(Screen.MyStore.route)
                    }
                },
                onNavigateToMyStoreProfile = {
                    navController.navigate(Screen.MyStoreProfile.route) {
                        popUpTo(Screen.MyStore.route)
                    }
                },
                onNavigateToSellerOrders = {
                    navController.navigate(Screen.SellerOrders.route)
                }
            )
        }

        // ===== Admin Console =====
        composable(Screen.AdminPanel.route) {
            AdminPanelScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStoreVerification = { navController.navigate(Screen.AdminStoreVerification.route) },
                onNavigateToProductModeration = { navController.navigate(Screen.AdminProductList.route) },
                onNavigateToUserManagement = { navController.navigate(Screen.AdminUserList.route) }
            )
        }

        composable(Screen.AdminStoreVerification.route) {
            StoreVerificationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminProductList.route) {
            ProductModerationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminUserList.route) {
            UserManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ===== New Features =====
        composable(Screen.Wishlist.route) {
            WishlistScreen(
                onNavigateBack = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.create(productId))
                }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() },
                onChatClick = { sellerId, sellerName ->
                    navController.navigate(Screen.ChatDetail.create(sellerId, sellerName))
                }
            )
        }

        composable(Screen.ChatDetail.route) { backStackEntry ->
            val sellerId = backStackEntry.arguments?.getString("sellerId") ?: ""
            val sellerName = backStackEntry.arguments?.getString("sellerName") ?: ""
            ChatDetailScreen(
                sellerId = sellerId,
                sellerName = sellerName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AllCategories.route) {
            AllCategoriesScreen(
                onNavigateBack = { navController.popBackStack() },
                onCategoryClick = { categoryId, categoryName ->
                    navController.navigate(Screen.CategoryProducts.create(categoryId, categoryName))
                }
            )
        }

        composable(Screen.CategoryProducts.route) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId")?.toIntOrNull() ?: 1
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryProductsScreen(
                categoryId = categoryId,
                categoryName = categoryName,
                onNavigateBack = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.create(productId))
                }
            )
        }

        composable(Screen.AllProducts.route) {
            AllProductsScreen(
                onNavigateBack = { navController.popBackStack() },
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.create(productId))
                }
            )
        }

        composable(Screen.AllStores.route) {
            AllStoresScreen(
                onNavigateBack = { navController.popBackStack() },
                onStoreClick = { storeId ->
                    navController.navigate(Screen.StoreProfile.create(storeId))
                }
            )
        }

        // ===== Saved Addresses & Payment Methods =====
        composable(
            route = Screen.SavedAddresses.route,
            arguments = listOf(
                androidx.navigation.navArgument("isSelectionMode") {
                    type = androidx.navigation.NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val isSelectionMode = backStackEntry.arguments?.getBoolean("isSelectionMode") ?: false
            SavedAddressesScreen(
                onNavigateBack = { navController.popBackStack() },
                isSelectionMode = isSelectionMode
            )
        }

        composable(
            route = Screen.PaymentMethods.route,
            arguments = listOf(
                androidx.navigation.navArgument("isSelectionMode") {
                    type = androidx.navigation.NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val isSelectionMode = backStackEntry.arguments?.getBoolean("isSelectionMode") ?: false
            PaymentMethodsScreen(
                onNavigateBack = { navController.popBackStack() },
                isSelectionMode = isSelectionMode
            )
        }

        // ===== Seller Orders & Chat =====
        composable(Screen.SellerOrders.route) {
            SellerOrdersScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDashboard = {
                    navController.navigate(Screen.MyStore.route) {
                        popUpTo(Screen.MyStore.route) { inclusive = true }
                    }
                },
                onNavigateToInventory = {
                    navController.navigate(Screen.Inventory.route) {
                        popUpTo(Screen.MyStore.route)
                    }
                },
                onNavigateToReports = {
                    navController.navigate(Screen.SellerDashboard.route) {
                        popUpTo(Screen.MyStore.route)
                    }
                },
                onNavigateToMyStoreProfile = {
                    navController.navigate(Screen.MyStoreProfile.route) {
                        popUpTo(Screen.MyStore.route)
                    }
                }
            )
        }

        composable(Screen.SellerChat.route) {
            SellerChatScreen(
                onNavigateBack = { navController.popBackStack() },
                onChatClick = { buyerId, buyerName ->
                    navController.navigate(Screen.SellerChatDetail.create(buyerId, buyerName))
                }
            )
        }

        composable(Screen.SellerChatDetail.route) { backStackEntry ->
            val buyerId = backStackEntry.arguments?.getString("sellerId") ?: ""
            val buyerName = backStackEntry.arguments?.getString("sellerName") ?: ""
            SellerChatDetailScreen(
                sellerId = buyerId,
                sellerName = buyerName,
                onNavigateBack = { navController.popBackStack() }
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