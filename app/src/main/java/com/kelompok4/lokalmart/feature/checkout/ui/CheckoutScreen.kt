package com.kelompok4.lokalmart.feature.checkout.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kelompok4.lokalmart.data.model.OrderItem
import com.kelompok4.lokalmart.feature.checkout.viewmodel.CheckoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    storeId: String,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val green       = Color(0xFF16A34A)
    val bgPage      = Color (0xFFFFFFFF)
    val textPrimary = Color(0xFF0F172A)
    val textMuted   = Color(0xFF64748B)
    val divider     = Color(0xFFCBD5E1)
    val greenBg     = Color(0xFFDCFCE7)

    LaunchedEffect(storeId) {
        viewModel.loadCheckoutDetails(storeId)
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            val orderId = uiState.orderId ?: ""
            val method = uiState.selectedPaymentMethod
            val amount = uiState.totalPrice
            navController.navigate("payment/$orderId/$method/$amount") {
                popUpTo("checkout/$storeId") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgPage)
    ) {
        Scaffold(
            containerColor = bgPage,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Checkout",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = textPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = bgPage,
                        titleContentColor = textPrimary
                    )
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgPage)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.placeOrder(storeId)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = green
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isLoading && uiState.items.isNotEmpty()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Bayar Rp ${"%,.0f".format(uiState.totalPrice).replace(",", ".")}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .background(bgPage)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── Alamat Pengiriman ──────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = green,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Alamat pengiriman",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = textPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = divider)
                        Spacer(modifier = Modifier.height(10.dp))
                        val addr = uiState.selectedAddress
                        val nameStr = addr?.name ?: "Sari Wulandari"
                        val phoneStr = addr?.phone ?: "0812-3456-7890"
                        val fullAddrStr = addr?.fullAddress ?: "Jl. Bunga Kana 12B, Lowokwaru, Kota Malang, Jawa Timur 65141"
                        val labelStr = addr?.label ?: "Rumah"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "$nameStr · $phoneStr",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = textPrimary
                            )
                            Text(
                                "Ubah",
                                color = green,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable {
                                    navController.navigate("saved_addresses?isSelectionMode=true")
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            fullAddrStr,
                            fontSize = 13.sp,
                            color = textMuted,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                labelStr,
                                fontSize = 11.sp,
                                color = textMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // ── Ringkasan Pesanan ──────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Ringkasan pesanan",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            uiState.items.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${item.productName} ×${item.quantity}",
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f),
                                        color = textPrimary
                                    )
                                    Text(
                                        "Rp ${"%,.0f".format(item.productPrice * item.quantity).replace(",", ".")}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textPrimary
                                    )
                                }
                                if (index < uiState.items.lastIndex) {
                                    HorizontalDivider(color = divider)
                                }
                            }
                        }
                    }
                }

                // ── Metode Pembayaran ──────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏦", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Metode pembayaran",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = textPrimary
                            )
                        }
                        Text(
                            "Ubah",
                            color = green,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                navController.navigate("payment_methods?isSelectionMode=true")
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    val selectedDto = uiState.selectedPaymentMethodDto
                    val label = when (uiState.selectedPaymentMethod) {
                        "qris" -> "QRIS"
                        "gopay" -> "GP"
                        else -> "BANK"
                    }
                    val name = selectedDto?.name ?: when (uiState.selectedPaymentMethod) {
                        "qris" -> "QRIS"
                        "gopay" -> "GoPay"
                        else -> "Transfer Bank"
                    }
                    val details = selectedDto?.details ?: when (uiState.selectedPaymentMethod) {
                        "qris" -> "QRIS — Semua e-wallet"
                        "gopay" -> "0812-****-7890"
                        else -> "Transfer Bank BCA"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = divider, shape = RoundedCornerShape(10.dp))
                            .background(color = Color.White, shape = RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (uiState.selectedPaymentMethod) {
                                "qris"  -> green
                                "gopay" -> Color(0xFF00AED6)
                                else    -> Color(0xFF003087)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    label,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                name,
                                fontSize = 14.sp,
                                color = textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                details,
                                fontSize = 12.sp,
                                color = textMuted
                            )
                        }
                    }
                }

                // ── Ringkasan Pembayaran ───────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", fontSize = 13.sp, color = textMuted)
                            Text("Rp ${"%,.0f".format(uiState.totalPrice).replace(",", ".")}", fontSize = 13.sp, color = textPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ongkos kirim", fontSize = 13.sp, color = textMuted)
                            Text("Rp 8.000", fontSize = 13.sp, color = textPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Diskon GRATIS ONGKIR", fontSize = 13.sp, color = green)
                            Text("-Rp 8.000", fontSize = 13.sp, color = green, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = divider)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total bayar", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                            Text(
                                "Rp ${"%,.0f".format(uiState.totalPrice).replace(",", ".")}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = green
                            )
                        }
                    }
                }

                uiState.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}