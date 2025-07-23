package com.owensteel.starlingroundup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/*

    ViewModel for doing the calculation work

 */

class MainViewModel : ViewModel() {

    // Use MutableLiveData to allow value modification
    // It is also non-null
    private val _roundUpAmount = MutableLiveData<String>()

    // The public reference to the latest round up amount value
    // Use LiveData because it notifies its observers (e.g. Activity
    // or a Fragment) whenever its data changes — this will be best
    // for a value that can change between requests
    // We want this to be a String so we can display it directly
    val roundUpAmount: LiveData<String> = _roundUpAmount

    // Make sure the result is always ready to be seen by the user, so
    // run it when initialised
    init {
        loadRoundUpAmount()
    }

    private fun loadRoundUpAmount() {
        viewModelScope.launch {
            // TODO: Call API, calc round up
            _roundUpAmount.value = "£0.00" // TODO: Possibly make a reference?
        }
    }

    fun performTransfer() {
        viewModelScope.launch {
            // TODO: Trigger savings goal transfer
        }
    }

}