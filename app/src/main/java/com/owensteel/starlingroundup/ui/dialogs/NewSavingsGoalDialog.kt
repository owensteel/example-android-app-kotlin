package com.owensteel.starlingroundup.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.ui.components.AppButton
import com.owensteel.starlingroundup.ui.components.AppSecondaryButton
import com.owensteel.starlingroundup.ui.components.CurrencyTextField
import java.util.Currency

/*

    Dialog that allows user to create a Savings Goal

 */

@Composable
fun NewSavingsGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit,
    accountCurrency: Currency
) {
    val goalNameValue = remember { mutableStateOf("") }
    val goalTargetValue = remember { mutableStateOf("0.00") }

    val dialogErrorMessageStringResourceId = remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            AppButton(
                onClick = {
                    val goalTargetNumberDouble: Double? = goalTargetValue.value.toDoubleOrNull()

                    if (goalTargetNumberDouble == null) {
                        // Invalid goal target
                        dialogErrorMessageStringResourceId.value =
                            R.string.create_savings_goal_dialog_error_invalid_target
                    } else {
                        if (goalNameValue.value.isNotBlank()) {
                            val goalTargetNumberLong: Long = (goalTargetNumberDouble * 100).toLong()
                            onConfirm(goalNameValue.value, goalTargetNumberLong)
                        } else {
                            // Invalid goal name
                            dialogErrorMessageStringResourceId.value =
                                R.string.create_savings_goal_dialog_error_invalid_name
                        }
                    }
                },
                text = stringResource(R.string.create_savings_goal_dialog_goal_finish_button)
            )
        },
        dismissButton = {
            AppSecondaryButton(
                onClick = onDismiss,
                text = stringResource(R.string.dialog_button_cancel),
            )
        },
        title = {
            Text(stringResource(R.string.create_savings_goal_dialog_title))
        },
        text = {
            Column {
                OutlinedTextField(
                    value = goalNameValue.value,
                    onValueChange = { goalNameValue.value = it },
                    label = { Text(stringResource(R.string.create_savings_goal_dialog_goal_name_input_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                CurrencyTextField(
                    valueState = goalTargetValue,
                    label = stringResource(R.string.create_savings_goal_dialog_goal_target_input_label),
                    currency = accountCurrency
                )
                // Inline error message for invalid input
                if (dialogErrorMessageStringResourceId.value != null) {
                    Text(
                        text = stringResource(dialogErrorMessageStringResourceId.value!!),
                        color = Color.Red
                    )
                }
            }
        }
    )
}