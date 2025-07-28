package com.owensteel.starlingroundup.ui.roundup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.ui.components.AppButton
import com.owensteel.starlingroundup.ui.theme.AccessibleGrey
import com.owensteel.starlingroundup.util.DateTimeUtils
import com.owensteel.starlingroundup.viewmodel.TransactionsFeedUiState
import com.owensteel.starlingroundup.viewmodel.RoundUpAndSaveViewModel

/*

    The Round Up & Save feature

    Displays the round-up sum, the button to access
    the transfer modal, and the time last used

 */

@Composable
fun RoundUpAndSaveFeature(
    viewModel: RoundUpAndSaveViewModel,
    roundUpAmount: Money,
    accountHolderName: String,
    showTransferToSavingsSheet: MutableState<Boolean>,
    feedState: TransactionsFeedUiState
) {
    Column(
        modifier = Modifier
            .padding(0.dp)
            .height(300.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.main_feature_greeting, accountHolderName),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(id = R.string.main_feature_intro, accountHolderName),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = roundUpAmount.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            onClick = {
                // Show modal first
                showTransferToSavingsSheet.value = true
                // Request fetching of savings goal list
                viewModel.getAccountSavingsGoals()
            },
            text = stringResource(id = R.string.main_feature_button_round_up_and_save),
            // Only enable if there is actually a Round Up total
            enabled = roundUpAmount.minorUnits > 0L
        )

        // Only get timestamp when we know it has been
        // initialised and won't be the fallback
        if (!feedState.isLoading) {
            Text(
                text = stringResource(
                    R.string.main_feature_last_round_up,
                    DateTimeUtils.timeSince(
                        feedState.latestRoundUpCutoffTimestamp
                    )
                ),
                color = AccessibleGrey,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic
            )
        }
    }
}
