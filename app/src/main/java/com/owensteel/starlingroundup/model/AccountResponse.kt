package com.owensteel.starlingroundup.model

data class AccountResponse(
    val accounts: List<Account>
)

data class Account(
    val accountUid: String,
    val defaultCategory: String,
    val currency: String,
    val name: String
)