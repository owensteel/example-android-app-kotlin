package com.owensteel.starlingroundup.fakes.viewmodel

import androidx.lifecycle.ViewModel
import com.owensteel.starlingroundup.model.uistates.RoundUpUiState
import com.owensteel.starlingroundup.viewmodel.RoundUpAndSaveViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeRoundUpAndSaveViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RoundUpUiState())
    val uiState: StateFlow<RoundUpUiState> = _uiState.asStateFlow()

    // Manually set state for testing
    fun setUiState(newState: RoundUpUiState) {
        _uiState.value = newState
    }

    fun showModal(show: Boolean) {
        _uiState.update { it.copy(showModal = show) }
    }
}
