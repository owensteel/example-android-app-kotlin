package com.owensteel.starlingroundup.fakes.api

import com.owensteel.starlingroundup.di.StarlingServiceModule
import com.owensteel.starlingroundup.model.AccountResponse
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