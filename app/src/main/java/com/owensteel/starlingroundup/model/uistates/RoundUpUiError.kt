package com.owensteel.starlingroundup.model.uistates

sealed class RoundUpUiError {
    data object Initialisation : RoundUpUiError()
    data object Transfer : RoundUpUiError()
    data object TransactionsFeed : RoundUpUiError()
    data class Network(val code: Int) : RoundUpUiError()
}