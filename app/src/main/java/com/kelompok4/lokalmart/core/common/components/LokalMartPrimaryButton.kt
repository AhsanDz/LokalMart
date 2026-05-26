package com.kelompok4.lokalmart.core.common.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompok4.lokalmart.core.common.theme.Green600

/**
 * Tombol utama LokalMart (warna hijau, rounded 14dp).
 * Saat loading, isi tombol diganti CircularProgressIndicator.
 */
@Composable
fun LokalMartPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Green600,
            contentColor = Color.White,
            disabledContainerColor = Green600.copy(alpha = 0.5f),
            disabledContentColor = Color.White
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
