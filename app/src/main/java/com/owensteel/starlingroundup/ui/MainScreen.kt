package com.owensteel.starlingroundup.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.owensteel.starlingroundup.ui.components.AppButton
import com.owensteel.starlingroundup.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val hasInitialisedAccountDetails by viewModel.hasInitialisedAccountDetailsState.collectAsState()
    val amount by viewModel.roundUpAmountState.collectAsState()
    val accountName by viewModel.accountNameState.collectAsState()

    // TODO: String references

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
                Text(
                    text = "Hi there, ${accountName}! Ready to save?",
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
                    text = "Round Up & Save"
                )
            } else {
                CircularProgressIndicator()
            }
        }
    }
}