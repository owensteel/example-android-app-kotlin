package com.owensteel.starlingroundup.fakes.security

import com.owensteel.starlingroundup.di.DeviceSecurityCheckModule
import com.owensteel.starlingroundup.util.DeviceSecurityCheck
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DeviceSecurityCheckModule::class] // replace your app's real provider
)
object FakeDeviceSecurityCheckModule {

    val sharedDeviceSecurityCheck = DeviceSecurityCheck()

    @Provides
    @Singleton
    fun provideFakeSecurityCheck(): DeviceSecurityCheck = sharedDeviceSecurityCheck
}
