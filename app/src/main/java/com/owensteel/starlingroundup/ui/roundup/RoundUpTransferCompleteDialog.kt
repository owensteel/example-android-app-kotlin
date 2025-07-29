package com.owensteel.starlingroundup.ui.roundup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.model.uistates.RoundUpUiState
import com.owensteel.starlingroundup.ui.components.AppSecondaryButton

/*

    Dialog that tells user that the transfer of the
    round-up sum to their selected Savings Goal has
    completed

 */

@Composable
fun RoundUpTransferCompleteDialog(
    uiState: RoundUpUiState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            AppSecondaryButton(
                onClick = onDismiss,
                text = stringResource(R.string.dialog_button_ok),
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = Icons.Default.Done.name,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = buildAnnotatedString {
                        val parts = stringResource(R.string.roundup_transfer_complete_dialog).split(
                            "%1\$s",
                            "%2\$s"
                        )
                        // UX: put the Round-Up Total money string and the
                        // targeted Savings Goal name in bold
                        append(parts[0]) // first part of template
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(uiState.roundUpTransferCompleteDialogRoundUpTotal.toString())
                        }
                        append(parts[1]) // middle part of template
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(uiState.roundUpTransferCompleteDialogChosenSavingsGoalName)
                        }
                        // Append the last part of the template, if there is one in this locale
                        if (parts.size > 2) append(parts[2])
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    )
}