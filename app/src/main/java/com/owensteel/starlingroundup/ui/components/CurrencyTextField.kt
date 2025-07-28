package com.owensteel.starlingroundup.ui.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import java.util.Currency
import java.util.Locale

@Composable
fun CurrencyTextField(
    valueState: MutableState<String>,
    label: String,
    currency: Currency
) {
    OutlinedTextField(
        value = valueState.value,
        onValueChange = { input ->
            val cleaned = input.replace(Regex("[^\\d.]"), "")

            // Only allow one decimal point
            if (cleaned.count { it == '.' } > 1) return@OutlinedTextField

            val number = cleaned.toDoubleOrNull()
            if (number != null) {
                // Normalises, e.g. "20.0" to "20.00"
                val formatted = String.format(Locale.UK, "%.2f", number)
                valueState.value = formatted
            } else if (cleaned.isEmpty()) {
                // Allow clearing
                valueState.value = ""
            }

            // Ignores invalid, e.g. "1.0.0"
        },
        label = { Text(label) },
        singleLine = true,
        leadingIcon = { Text(currency.getSymbol(Locale.getDefault())) }
    )
}