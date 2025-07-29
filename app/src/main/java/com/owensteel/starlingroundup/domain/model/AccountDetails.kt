package com.owensteel.starlingroundup.domain.model

import java.util.Currency

data class AccountDetails(
    val accountUid: String,
    val categoryUid: String,
    val currency: Currency,
    val accountHolderName: String
)