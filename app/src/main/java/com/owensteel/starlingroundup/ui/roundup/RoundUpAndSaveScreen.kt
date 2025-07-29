package com.owensteel.starlingroundup.ui.roundup

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
import com.owensteel.starlingroundup.model.uistates.RoundUpUiError
import com.owensteel.starlingroundup.ui.transactionsfeed.TransactionsFeedFeature
import com.owensteel.starlingroundup.viewmodel.RoundUpAndSaveViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundUpAndSaveScreen(
    viewModel: RoundUpAndSaveViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsState()

    // Refresh "onResume"
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshTransactionsAndRoundUp()
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
            viewModel.refreshTransactionsAndRoundUp()
            isRefreshing.value = false
        }
    }
    val pullToRefreshIndicatorScaleFraction = {
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
            if (uiState.showModal && uiState.roundUpTotal.minorUnits > 0L) {
                TransferToSavingsGoalModal(
                    uiState,
                    uiState.roundUpTotal.toString(),
                    viewModel
                )
            }

            if (uiState.hasInitialised) {
                RoundUpAndSaveFeature(
                    viewModel = viewModel,
                    roundUpAmount = uiState.roundUpTotal,
                    accountHolderName = uiState.accountHolderName,
                    uiState = uiState
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                )
                TransactionsFeedFeature(uiState)
            } else {
                // Placeholder container
                Column(
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.error is RoundUpUiError.Initialisation) {
                        // Error message
                        Text(
                            stringResource(
                                R.string.error_could_not_fetch_data,
                                // TODO get network error code here
                                ""
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
                    scaleX = pullToRefreshIndicatorScaleFraction()
                    scaleY = pullToRefreshIndicatorScaleFraction()
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