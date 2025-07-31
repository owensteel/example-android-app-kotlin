package com.owensteel.starlingroundup.di

import com.owensteel.starlingroundup.util.DeviceSecurityCheck
import com.owensteel.starlingroundup.util.IDeviceSecurityCheck
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DeviceSecurityCheckModule {
    @Binds
    fun bindDeviceSecurityCheck(impl: DeviceSecurityCheck): IDeviceSecurityCheck
}