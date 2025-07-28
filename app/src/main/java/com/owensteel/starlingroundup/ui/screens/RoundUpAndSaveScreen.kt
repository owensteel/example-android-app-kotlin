package com.owensteel.starlingroundup.ui.screens

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.ui.features.RoundUpAndSaveFeature
import com.owensteel.starlingroundup.ui.features.TransactionsFeedFeature
import com.owensteel.starlingroundup.ui.modals.TransferToSavingsGoalModal
import com.owensteel.starlingroundup.viewmodel.RoundUpAndSaveViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundUpAndSaveScreen(
    viewModel: RoundUpAndSaveViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val hasInitialisedDataState by viewModel.hasInitialisedDataState.collectAsState()
    val roundUpAmount by viewModel.roundUpAmountState.collectAsState()
    val accountHolderName by viewModel.accountHolderNameState.collectAsState()
    val feedState by viewModel.feedState.collectAsState()
    val savingsGoalsModalUiState by viewModel.savingsGoalsModalUiState.collectAsState()

    // Transfer to Savings Goal modal
    val showTransferToSavingsSheet = remember { mutableStateOf(false) }

    // Refresh "onResume"
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshRoundUpTotalAndTransactionsFeed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Pull to refresh states
    val isRefreshing = remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        isRefreshing.value = true
        coroutineScope.launch {
            viewModel.refreshRoundUpTotalAndTransactionsFeed()
            isRefreshing.value = false
        }
    }
    val scaleFraction = {
        if (isRefreshing.value) 1f
        else LinearOutSlowInEasing.transform(pullToRefreshState.distanceFraction).coerceIn(0f, 1f)
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main body
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxSize()
                .pullToRefresh(
                    isRefreshing = isRefreshing.value,
                    state = pullToRefreshState,
                    onRefresh = onRefresh
                ),
            // Vertically arranging from top prevents elements
            // jumping about when one lower down changes size
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Don't show Transfer modal if the Round Up total is
            // zero (could make illegal transfer possible)
            if (showTransferToSavingsSheet.value && roundUpAmount.minorUnits > 0L) {
                TransferToSavingsGoalModal(
                    showTransferToSavingsSheet,
                    savingsGoalsModalUiState,
                    roundUpAmount.toString(),
                    viewModel
                )
            }

            if (hasInitialisedDataState.value) {
                RoundUpAndSaveFeature(
                    viewModel = viewModel,
                    roundUpAmount = roundUpAmount,
                    accountHolderName = accountHolderName,
                    showTransferToSavingsSheet = showTransferToSavingsSheet,
                    // Used for getting the last
                    // round-up timestamp when ready
                    feedState = feedState
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                )
                TransactionsFeedFeature(
                    feedState = feedState
                )
            } else {
                // Placeholder container
                Column(
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (hasInitialisedDataState.hasError) {
                        // Error message
                        Text(
                            stringResource(
                                R.string.error_could_not_fetch_data,
                                hasInitialisedDataState.errorCode ?: ""
                            )
                        )
                    } else {
                        // Loading spinner
                        CircularProgressIndicator()
                    }
                }
            }
        }

        // Pull to refresh indicator
        // Must be at end of Composable for it to overlay
        // all elements
        Box(
            Modifier
                .graphicsLayer {
                    scaleX = scaleFraction()
                    scaleY = scaleFraction()
                }
        ) {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing.value,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}