package com.owensteel.starlingroundup.di

import com.owensteel.starlingroundup.data.local.SecureTokenStore
import com.owensteel.starlingroundup.network.StarlingAuthApiProvider
import com.owensteel.starlingroundup.network.DefaultStarlingAuthApiProvider
import com.owensteel.starlingroundup.token.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TokenManagerModule {

    @Provides
    @Singleton
    fun provideStarlingAuthApiProvider(): StarlingAuthApiProvider = DefaultStarlingAuthApiProvider()

    @Provides
    @Singleton
    fun provideTokenManager(
        secureTokenStore: SecureTokenStore,
        authApiProvider: StarlingAuthApiProvider
    ): TokenManager {
        return TokenManager(
            secureTokenStore,
            authApiProvider
        )
    }

}