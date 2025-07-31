package com.owensteel.starlingroundup.di

import com.owensteel.starlingroundup.util.DeviceSecurityCheck
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DeviceSecurityCheckModule {
    @Provides
    fun provideDeviceSecurityCheck(): DeviceSecurityCheck = DeviceSecurityCheck()
}