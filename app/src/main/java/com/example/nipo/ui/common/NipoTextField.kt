package com.example.nipo.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.nipo.ui.theme.LocalNipoModeColors

@Composable
fun NipoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    containerColor: Color? = null,
) {
    val colors = LocalNipoModeColors.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = colors.mutedText.copy(alpha = 0.55f)) },
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.border,
            focusedContainerColor = containerColor ?: Color.Transparent,
            unfocusedContainerColor = containerColor ?: Color.Transparent,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}
