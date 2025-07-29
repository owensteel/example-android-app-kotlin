package com.owensteel.starlingroundup.model

import java.util.Currency

data class AccountDetails(
    val accountUid: String,
    val categoryUid: String,
    val currency: Currency,
    val accountHolderName: String
)