package com.kelompok4.lokalmart.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kelompok4.lokalmart.core.common.components.LokalMartPrimaryButton
import com.kelompok4.lokalmart.core.common.components.LokalMartTextField
import com.kelompok4.lokalmart.core.common.theme.Green100
import com.kelompok4.lokalmart.core.common.theme.Green50
import com.kelompok4.lokalmart.core.common.theme.Green600
import com.kelompok4.lokalmart.feature.auth.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.loginState.collectAsState()

    // Watch login success → navigate ke Home
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onLoginSuccess()
            viewModel.resetLoginState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 30.dp)
    ) {
        // ===== Header dengan logo (gradient hijau muda) =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Green50, Green100)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Storefront,
                contentDescription = null,
                tint = Green600,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // ===== Heading =====
        Text(
            text = "Selamat datang 👋",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.44).sp,
            color = Color(0xFF0F172A)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Masuk untuk belanja produk lokal & dukung UMKM di sekitarmu.",
            fontSize = 12.sp,
            color = Color(0xFF64748B)
        )

        Spacer(Modifier.height(20.dp))

        // ===== Email field =====
        FieldLabel("Email")
        Spacer(Modifier.height(4.dp))
        LokalMartTextField(
            value = state.email,
            onValueChange = viewModel::onLoginEmailChange,
            placeholder = "nama@email.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !state.isLoading,
            isError = state.error != null
        )

        Spacer(Modifier.height(12.dp))

        // ===== Password field =====
        FieldLabel("Password")
        Spacer(Modifier.height(4.dp))
        LokalMartTextField(
            value = state.password,
            onValueChange = viewModel::onLoginPasswordChange,
            placeholder = "••••••••",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            enabled = !state.isLoading,
            isError = state.error != null
        )

        Spacer(Modifier.height(8.dp))

        // ===== Lupa password (placeholder, belum diimplementasi) =====
        Text(
            text = "Lupa password?",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Green600,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )

        // Error message
        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // ===== Tombol Masuk =====
        LokalMartPrimaryButton(
            text = "Masuk",
            onClick = viewModel::login,
            enabled = !state.isLoading,
            isLoading = state.isLoading
        )

        Spacer(Modifier.height(20.dp))

        // ===== Divider "atau" =====
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
            Text(
                "atau",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
        }

        Spacer(Modifier.height(20.dp))

        // ===== Link ke Register =====
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Color(0xFF64748B), fontSize = 12.sp)) {
                    append("Belum punya akun? ")
                }
                withStyle(
                    SpanStyle(
                        color = Green600,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("Daftar gratis")
                }
            },
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !state.isLoading, onClick = onNavigateToRegister)
                .padding(8.dp)
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF334155)
    )
}
