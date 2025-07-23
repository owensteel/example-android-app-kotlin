package com.owensteel.starlingroundup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/*

    ViewModel for doing the calculation work

 */

class MainViewModel : ViewModel() {

    // TODO: Possibly make default value a reference?

    // Use StateFlow to maintain an observable mutable state for our
    // value that may change between appearances
    private val _roundUpAmount = MutableStateFlow("£0.00")
    val roundUpAmount: StateFlow<String> = _roundUpAmount.asStateFlow()

    // Make sure the result is always ready to be seen by the user, so
    // run it when initialised
    init {
        loadRoundUpAmount()
    }

    private fun loadRoundUpAmount() {
        viewModelScope.launch {
            // TODO: Call API, calc round up
            _roundUpAmount.value = "£0.00"
        }
    }

    fun performTransfer() {
        viewModelScope.launch {
            // TODO: Trigger savings goal transfer
        }
    }

}