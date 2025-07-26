package com.owensteel.starlingroundup.model

data class SavingsGoal(
    val savingsGoalUid: String,
    val name: String,
    val target: Money,
    val totalSaved: Money,
    val savedPercentage: Int,
    val state: String
)