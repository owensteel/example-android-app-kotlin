package com.owensteel.starlingroundup.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.ui.components.AppButton
import com.owensteel.starlingroundup.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val hasInitialisedAccountDetails by viewModel.hasInitialisedAccountDetailsState.collectAsState()
    val amount by viewModel.roundUpAmountState.collectAsState()
    val accountName by viewModel.accountNameState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasInitialisedAccountDetails) {
                MainFeature(
                    viewModel = viewModel,
                    amount = amount,
                    accountName = accountName
                )
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun MainFeature(viewModel: MainViewModel, amount: String, accountName: String) {
    Text(
        text = stringResource(id = R.string.main_feature_greeting, accountName),
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = amount,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(24.dp))

    AppButton(
        onClick = { viewModel.performTransfer() },
        text = stringResource(id = R.string.main_feature_button_round_up_and_save)
    )
}