package com.kelompok4.lokalmart.core.common.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.kelompok4.lokalmart.core.common.theme.Green600

/**
 * Reusable text field dengan styling LokalMart:
 * - Rounded 12dp
 * - Border #e2e8f0, focus jadi hijau
 * - Placeholder abu-abu #757575
 */
@Composable
fun LokalMartTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFF757575),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        modifier = modifier,
        enabled = enabled,
        singleLine = singleLine,
        shape = RoundedCornerShape(12.dp),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        isError = isError,
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Green600,
            unfocusedBorderColor = Color(0xFFE2E8F0),
            disabledBorderColor = Color(0xFFE2E8F0),
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}
