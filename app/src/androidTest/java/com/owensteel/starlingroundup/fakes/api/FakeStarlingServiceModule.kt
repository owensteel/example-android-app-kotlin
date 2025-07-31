package com.owensteel.starlingroundup.fakes.api

import com.owensteel.starlingroundup.di.StarlingServiceModule
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeAccount
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeAccountHolder
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeSavingsGoal
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeSavingsGoal2
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeTransactionFeed
import com.owensteel.starlingroundup.model.AccountResponse
import com.owensteel.starlingroundup.model.CreateSavingsGoalResponse
import com.owensteel.starlingroundup.model.GetSavingsGoalsResponse
import com.owensteel.starlingroundup.model.TransferResponse
import com.owensteel.starlingroundup.network.StarlingApi
import com.owensteel.starlingroundup.network.StarlingApiProvider
import com.owensteel.starlingroundup.network.StarlingService
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.util.UUID
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [StarlingServiceModule::class]
)
object FakeStarlingServiceModule {

    @Provides
    @Singleton
    fun provideFakeStarlingApiProvider(): StarlingApiProvider {
        val mockStarlingApi = mock<StarlingApi>()

        // Stub relevant endpoints
        runBlocking {
            whenever(mockStarlingApi.getAccountDetails()).thenReturn(
                Response.success(
                    AccountResponse(
                        listOf(
                            FakeAccount
                        )
                    )
                )
            )
            whenever(mockStarlingApi.getAccountHolderIndividual()).thenReturn(
                Response.success(
                    FakeAccountHolder
                )
            )
            whenever(
                mockStarlingApi.getTransactionsForCurrentWeek(
                    any(), any(), any(), any()
                )
            ).thenReturn(
                Response.success(
                    FakeTransactionFeed
                )
            )
            whenever(
                mockStarlingApi.getSavingsGoals(any())
            ).thenReturn(
                Response.success(
                    GetSavingsGoalsResponse(listOf(FakeSavingsGoal, FakeSavingsGoal2))
                )
            )
            whenever(
                mockStarlingApi.createSavingsGoal(any(), any())
            ).thenReturn(
                Response.success(
                    CreateSavingsGoalResponse(
                        savingsGoalUid = UUID.randomUUID().toString(),
                        success = true
                    )
                )
            )
            whenever(
                mockStarlingApi.transferToSavingsGoal(
                    any(),
                    goalUid = any(),
                    transferUid = any(),
                    transfer = any()
                )
            ).thenReturn(
                Response.success(
                    TransferResponse(
                        success = true
                    )
                )
            )
        }

        return object : StarlingApiProvider {
            override fun getApi(): StarlingApi = mockStarlingApi
        }
    }

    @Provides
    @Singleton
    fun provideStarlingService(
        apiProvider: StarlingApiProvider
    ): StarlingService {
        return StarlingService(apiProvider)
    }

}