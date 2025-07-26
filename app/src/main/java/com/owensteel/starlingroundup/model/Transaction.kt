package com.owensteel.starlingroundup.model

data class Transaction(
    val feedItemUid: String,
    val amount: Money,
    val direction: String,
    val spendingCategory: String?,
    val transactionTime: String
)