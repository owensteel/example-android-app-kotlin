package com.owensteel.starlingroundup.di

import com.owensteel.starlingroundup.network.DefaultStarlingApiProvider
import com.owensteel.starlingroundup.network.StarlingApiProvider
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
    fun provideStarlingApiProvider(
        tokenManager: TokenManager
    ): StarlingApiProvider = DefaultStarlingApiProvider(tokenManager)

    @Provides
    @Singleton
    fun provideStarlingService(
        apiProvider: StarlingApiProvider
    ): StarlingService {
        return StarlingService(apiProvider)
    }

}