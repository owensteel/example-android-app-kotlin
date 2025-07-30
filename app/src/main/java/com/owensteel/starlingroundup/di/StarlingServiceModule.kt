package com.owensteel.starlingroundup.di

import com.owensteel.starlingroundup.network.StarlingService
import com.owensteel.starlingroundup.token.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StarlingServiceModule {

    @Provides
    @Singleton
    fun provideStarlingService(
        tokenManager: TokenManager
    ): StarlingService {
        return StarlingService(tokenManager)
    }

}