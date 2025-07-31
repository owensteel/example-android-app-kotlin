package com.owensteel.starlingroundup.fakes.security

import android.content.Context
import com.owensteel.starlingroundup.di.DeviceSecurityCheckModule
import com.owensteel.starlingroundup.util.IDeviceSecurityCheck
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DeviceSecurityCheckModule::class]
)
object FakeDeviceSecurityCheckModule {
    val spy = SpyDeviceSecurityCheck()

    @Provides
    fun provideSpyDeviceSecurityCheck(): IDeviceSecurityCheck = spy
}

class SpyDeviceSecurityCheck : IDeviceSecurityCheck {
    var wasCalled = false
    override fun isCompromised(context: Context): Boolean {
        wasCalled = true
        return false
    }
}