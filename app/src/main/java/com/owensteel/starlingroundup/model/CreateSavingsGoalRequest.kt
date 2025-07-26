package com.owensteel.starlingroundup.model

data class CreateSavingsGoalRequest (
    val name: String,
    val currency: String,
    val target: Money,
    val base64EncodedPhoto: String?
)