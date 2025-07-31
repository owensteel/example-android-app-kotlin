package com.owensteel.starlingroundup.fakes.api

import com.owensteel.starlingroundup.model.Account
import com.owensteel.starlingroundup.model.AccountHolderIndividualResponse
import com.owensteel.starlingroundup.model.CreateSavingsGoalResponse
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.SavingsGoal
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.model.TransactionFeedResponse
import com.owensteel.starlingroundup.model.TransferResponse
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_DIRECTION_OUT

/*

    Provides the responses we want to test

 */

val FakeAccount = Account(
    accountUid = "c9df9210-ad10-4c66-b695-be2fc3719ed4",
    defaultCategory = "d784caae-5210-42bc-aca4-3e580f126e7d",
    currency = "GBP",
    name = "PERSONAL"
)

val FakeAccountHolder = AccountHolderIndividualResponse(
    title = "Mr",
    firstName = "Joe",
    lastName = "Smith",
    dateOfBirth = "01/01/2000",
    email = "joesmith@example.com",
    phone = "+44 12345678901"
)

val FakeSavingsGoal = SavingsGoal(
    savingsGoalUid = "369243aa-54c6-4524-9137-445b68a17920",
    name = "TestGoal",
    target = Money(
        FakeAccount.currency,
        100L
    ),
    totalSaved = Money(
        FakeAccount.currency,
        50L
    ),
    savedPercentage = 50,
    state = "active"
)

val FakeCreateSavingsGoalResponseSuccess = CreateSavingsGoalResponse(
    savingsGoalUid = "0c39ceda-f672-4638-b360-13735431b950",
    success = true
)

val FakeCreateSavingsGoalResponseFailure = CreateSavingsGoalResponse(
    savingsGoalUid = "0c39ceda-f672-4638-b360-13735431b950",
    success = false
)

val FakeTransferResponseSuccess = TransferResponse(
    success = true
)

val FakeTransferResponseFailure = TransferResponse(
    success = false
)

val FakeTransaction = Transaction(
    feedItemUid = "e46981a6-111e-4214-98bb-e13757e8fd3b",
    amount = Money(
        FakeAccount.currency,
        158L
    ),
    direction = TRANSACTION_DIRECTION_OUT,
    spendingCategory = FakeAccount.defaultCategory,
    transactionTime = "2025-01-01T09:00:00Z",
    source = "MASTER_CARD"
)

val FakeTransactionFeed = TransactionFeedResponse(
    feedItems = listOf(FakeTransaction)
)