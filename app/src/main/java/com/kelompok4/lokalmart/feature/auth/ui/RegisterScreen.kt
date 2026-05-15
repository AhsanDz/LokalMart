package com.kelompok4.lokalmart.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kelompok4.lokalmart.core.common.components.LokalMartPrimaryButton
import com.kelompok4.lokalmart.core.common.components.LokalMartTextField
import com.kelompok4.lokalmart.core.common.theme.Green600
import com.kelompok4.lokalmart.feature.auth.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.registerState.collectAsState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onRegisterSuccess()
            viewModel.resetRegisterState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // ===== Top bar dengan back button + title =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Green600
                )
            }
            Spacer(Modifier.width(50.dp))
            Text(
                text = "Daftar Akun",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.16).sp,
                color = Color(0xFF0F172A)
            )
        }

        // ===== Step indicator (3 segments, 1 aktif) =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StepDot(active = true)
            StepDot(active = false)
            StepDot(active = false)
        }

        Text(
            text = "Langkah 1 dari 3 · Data akun",
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // ===== Form =====
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FieldLabel("Nama lengkap")
            LokalMartTextField(
                value = state.fullName,
                onValueChange = viewModel::onRegisterFullNameChange,
                placeholder = "Nama sesuai KTP",
                enabled = !state.isLoading
            )

            Spacer(Modifier.height(4.dp))
            FieldLabel("Email")
            LokalMartTextField(
                value = state.email,
                onValueChange = viewModel::onRegisterEmailChange,
                placeholder = "nama@email.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !state.isLoading
            )

            Spacer(Modifier.height(4.dp))
            FieldLabel("Nomor HP")
            LokalMartTextField(
                value = state.phone,
                onValueChange = viewModel::onRegisterPhoneChange,
                placeholder = "08xxx",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                enabled = !state.isLoading
            )

            Spacer(Modifier.height(4.dp))
            FieldLabel("Password")
            LokalMartTextField(
                value = state.password,
                onValueChange = viewModel::onRegisterPasswordChange,
                placeholder = "Min. 8 karakter",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                enabled = !state.isLoading
            )

            Spacer(Modifier.height(8.dp))

            // ===== Checkbox syarat & ketentuan =====
            Row(verticalAlignment = Alignment.Top) {
                Checkbox(
                    checked = state.agreedToTerms,
                    onCheckedChange = viewModel::onAgreeTermsChange,
                    colors = CheckboxDefaults.colors(checkedColor = Green600),
                    enabled = !state.isLoading
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF64748B), fontSize = 11.sp)) {
                            append("Saya menyetujui ")
                        }
                        withStyle(
                            SpanStyle(
                                color = Green600,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Syarat & Ketentuan")
                        }
                        withStyle(SpanStyle(color = Color(0xFF64748B), fontSize = 11.sp)) {
                            append(" serta ")
                        }
                        withStyle(
                            SpanStyle(
                                color = Green600,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Kebijakan Privasi")
                        }
                        withStyle(SpanStyle(color = Color(0xFF64748B), fontSize = 11.sp)) {
                            append(" LokalMart.")
                        }
                    },
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            if (state.error != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            LokalMartPrimaryButton(
                text = "Daftar",
                onClick = viewModel::register,
                enabled = !state.isLoading,
                isLoading = state.isLoading
            )

            Spacer(Modifier.height(16.dp))
        }
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

@Composable
private fun RowScope.StepDot(active: Boolean) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(4.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) Green600 else Color(0xFFE2E8F0))
    )
}
