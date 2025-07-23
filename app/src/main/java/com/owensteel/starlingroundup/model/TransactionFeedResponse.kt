package com.owensteel.starlingroundup.model

data class TransactionFeedResponse(
    val feedItems: List<Transaction>
)

data class Transaction(
    val feedItemUid: String,
    val amount: CurrencyAmount,
    val direction: String,
    val spendingCategory: String?,
    val transactionTime: String
)

data class CurrencyAmount(
    val currency: String,
    val minorUnits: Long
)